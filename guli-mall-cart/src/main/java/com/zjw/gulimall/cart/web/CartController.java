package com.zjw.gulimall.cart.web;

import com.zjw.common.constant.BizCodeEnum;
import com.zjw.common.to.UserInfoTo;
import com.zjw.common.utils.R;
import com.zjw.gulimall.cart.interceptor.CartInterceptor;
import com.zjw.gulimall.cart.service.CartService;
import com.zjw.gulimall.cart.vo.Cart;
import com.zjw.gulimall.cart.vo.CartItem;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-23 22:35
 * @Modifier:
 */
@Controller
public class CartController
{
    @Resource
    private CartService cartService;

    @GetMapping(value = {"/index.html", "/", "/cart.html"})
    public String toCartIndex(Model model){
        cartService.mergeCartItem();
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    //查询当前登录人的购物车中选中的商品项，用于远程调用
    @ResponseBody
    @GetMapping(value = "/getLoginCartItems")
    public R getLoginCartItems(){
        List<CartItem> cartItemList = cartService.getLoginCartItems();
        if(cartItemList == null){
            return R.error(BizCodeEnum.CART_CHECK_EXCEPTION.getCode(), BizCodeEnum.CART_CHECK_EXCEPTION.getMsg());
        }else {
            return R.ok().put("data", cartItemList);
        }
    }

    @GetMapping("/countItem")
    public String countItem(Long skuId, int num){
        if(skuId == null || skuId <= 0 || num < 0){
            return "redirect:http://cart.gulimall.com";
        }
        if(num == 0) deleteItem(skuId);
        //更改redis中数量并且进行刷新
        cartService.countItem(skuId, num);
        return "redirect:http://cart.gulimall.com";
    }

    @GetMapping("/checkItem.html")
    public String checkItem(Long skuId, int check){
        if(skuId == null || skuId <= 0){
            return "redirect:http://cart.gulimall.com";
        }
        cartService.checkItem(skuId, check == 1);
        return "redirect:http://cart.gulimall.com";
    }

    /**
     * 加入购物车
     * @return
     */
    //http://cart.gulimall.com/addToCart?skuId=11&num=1
    @GetMapping("/addToCart")
    public String addToCart(Long skuId, int num){
        cartService.mergeCartItem();
        cartService.addToCart(skuId, num);
        //添加商品后重定向到成功页面，避免一直刷新成功页面重复添加商品
        return "redirect:http://cart.gulimall.com/toSuccess?skuId=" + skuId;
    }

    //展示商品添加到购物车成功页面
    @GetMapping("/toSuccess")
    public String toSuccess(@RequestParam(required = false) Long skuId, Model model){
        if(skuId == null) return "success";
        CartItem cartItem = cartService.toSuccess(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }

    @GetMapping("/deleteItem")
    public String deleteItem(Long skuId){
        cartService.deleteItem(skuId);
        return "redirect:http://cart.gulimall.com";
    }
}
