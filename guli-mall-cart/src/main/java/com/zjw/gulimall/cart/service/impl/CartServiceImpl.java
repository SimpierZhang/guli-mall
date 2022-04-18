package com.zjw.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zjw.common.to.SkuInfoEntityTo;
import com.zjw.common.to.UserInfoTo;
import com.zjw.common.utils.R;
import com.zjw.gulimall.cart.feign.ProductFeignService;
import com.zjw.gulimall.cart.interceptor.CartInterceptor;
import com.zjw.gulimall.cart.service.CartService;
import com.zjw.gulimall.cart.vo.Cart;
import com.zjw.gulimall.cart.vo.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-23 22:35
 * @Modifier:
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService
{
    private final String CART_REDIS_PREFIX = "cart:user:";

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private ExecutorService executor;

    @Transactional
    @Override
    public void addToCart(Long skuId, int num) {
        BoundHashOperations<String, Object, Object> hashOps = getHashOps();
        //进一步优化
        //1.如果购物车已经有数据，应该只增加数量
        CartItem cartItem = getCartItemBySkuId(skuId);
        if (cartItem!= null) {
            cartItem.setCount(cartItem.getCount() + num);
        }
        else {
            //2.借助异步操作完成查询
            cartItem = new CartItem();
            //加入购物车后默认被选中
            cartItem.setCheck(true);
            cartItem.setSkuId(skuId);
            cartItem.setCount(num);
            //获取操作临时购物车或者登录购物车的redis
            //TODO 远程服务调用获取sku详细信息
            CartItem finalCartItem = cartItem;
            CompletableFuture<Void> getSkuInfoFuture = CompletableFuture.runAsync(() -> {
                R r = productFeignService.getSkuInfoById(skuId);
                SkuInfoEntityTo skuInfoEntityTo = r.get("skuInfo", new TypeReference<SkuInfoEntityTo>()
                {
                });
                if (skuInfoEntityTo != null) {
                    finalCartItem.setPrice(skuInfoEntityTo.getPrice());
                    finalCartItem.setImage(skuInfoEntityTo.getSkuDefaultImg());
                    finalCartItem.setTitle(skuInfoEntityTo.getSkuTitle());
                }
            }, executor);

            CompletableFuture<Void> getSaleAttrFuture = CompletableFuture.runAsync(() -> {
                //TODO 远程服务调用获取SaleAttr信息
                R saleAttr = productFeignService.getSaleAttrBySkuId(skuId);
                List<String> saleAttrList = saleAttr.getData(new TypeReference<List<String>>()
                {
                });
                if (saleAttrList != null && saleAttrList.size() > 0) {
                    finalCartItem.setSkuAttr(saleAttrList);
                }
            });
            try {
                CompletableFuture.allOf(getSaleAttrFuture, getSkuInfoFuture).get();
            }
            catch (Exception e) {
                log.error("远程连接异常>>{}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
        hashOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public CartItem toSuccess(Long skuId) {
        CartItem cartItem = getCartItemBySkuId(skuId);
        return cartItem;
    }

    //合并购物车项
    //如果登录了之后查看购物车和添加商品到购物车都会将临时购物车的数据加入登录购物车中
    @Override
    public void mergeCartItem(){
        //1.判断用户是否登录
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //临时用户无需合并操作
        if(userInfoTo.isTempUser()) return;
        //获取临时购物车数据
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(CART_REDIS_PREFIX + userInfoTo.getUserKey());
        List<Object> values = hashOps.values();
        if(values != null && values.size() > 0){
            List<CartItem> tempCartItemList = values.stream().map(v -> {
                CartItem cartItem = JSON.parseObject(v.toString(), CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());
            if(tempCartItemList.size() > 0){
                tempCartItemList.forEach(tc -> addToCart(tc.getSkuId(), tc.getCount()));
            }
            //合并之后将临时购物车数据删除
            redisTemplate.delete(CART_REDIS_PREFIX + userInfoTo.getUserKey());
        }
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> hashOps = getHashOps();
        Long delete = hashOps.delete(skuId.toString());
    }

    @Override
    public boolean deleteCart() {
        String userKey = null;
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.isTempUser()) {
            //用户未登录，数据全都保存到临时购物车
            userKey = CART_REDIS_PREFIX + userInfoTo.getUserKey();
        }
        else {
            //用于已经登录，数据全部保存到登录购物车
            userKey = CART_REDIS_PREFIX + userInfoTo.getUserId();
        }
        //清空购物车
        Boolean delete = redisTemplate.delete(userKey);
        return delete == null ? false : delete;
    }

    @Override
    public void countItem(Long skuId, int num) {
        BoundHashOperations<String, Object, Object> hashOps = getHashOps();
        CartItem cartItem = getCartItemBySkuId(skuId);
        if(cartItem != null){
            cartItem.setCount(num);
            hashOps.put(skuId.toString(), JSON.toJSONString(cartItem));
        }
    }

    @Override
    public void checkItem(Long skuId, boolean check) {
        BoundHashOperations<String, Object, Object> hashOps = getHashOps();
        CartItem cartItem = getCartItemBySkuId(skuId);
        if(cartItem != null){
            cartItem.setCheck(check);
            hashOps.put(skuId.toString(), JSON.toJSONString(cartItem));
        }
    }

    @Override
    public List<CartItem> getLoginCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(userInfoTo.isTempUser()) return null;
        BoundHashOperations<String, Object, Object> hashOps = getHashOps();
        List<Object> values = hashOps.values();
        List<CartItem> cartItemList = null;
        if(values != null){
            cartItemList = values.stream().filter(Objects::nonNull).map(v -> {
                CartItem item = JSON.parseObject(v.toString(), CartItem.class);
                return item;
            }).filter(CartItem::getCheck).collect(Collectors.toList());
        }
        return cartItemList;
    }

    private CartItem getCartItemBySkuId(Long skuId) {
        try {
            BoundHashOperations<String, Object, Object> hashOps = getHashOps();
            Object o = hashOps.get(skuId.toString());
            CartItem cartItem = JSON.parseObject(o.toString(), CartItem.class);
            return cartItem;
        }catch (Exception e){
            log.error("购物车>>类型转换异常>>{}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    //获取用户的购物车信息
    @Override
    public Cart getCart() {
        Cart cart = new Cart();
        BoundHashOperations<String, Object, Object> hashOps = getHashOps();
        Set<Object> keys = hashOps.keys();
        if (keys != null && keys.size() > 0) {
            List<CartItem> cartItemList = keys.stream().map(k -> {
                CartItem cartItem = getCartItemBySkuId(Long.parseLong(k.toString()));
                return cartItem;
            }).collect(Collectors.toList());
            if (cartItemList.size() > 0)
                cart.setItems(cartItemList);
        }
        return cart;
    }

    private BoundHashOperations<String, Object, Object> getHashOps() {
        String userKey;
        //判断是临时购物车还是登录购物车>>即判断用户是否登录>>判断session中是否有值
        //为了避免频繁的去session中获取用户信息，可以加上拦截器在请求进来时获取一次用户信息，然后保存到ThreadLocal中
        //这样一个线程（即一个请求）只需要去redis中获取一次用户信息即可
        //1.判断用户是否登录
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.isTempUser()) {
            //用户未登录，数据全都保存到临时购物车
            userKey = CART_REDIS_PREFIX + userInfoTo.getUserKey();
        }
        else {
            //用于已经登录，数据全部保存到登录购物车
            userKey = CART_REDIS_PREFIX + userInfoTo.getUserId();
        }
        return redisTemplate.boundHashOps(userKey);
    }
}
