package com.zjw.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.zjw.common.constant.OrderConstant;
import com.zjw.common.constant.WareConstant;
import com.zjw.common.exception.StockLackException;
import com.zjw.common.to.MemberInfoTo;
import com.zjw.common.to.WareSkuVo;
import com.zjw.common.utils.R;
import com.zjw.gulimall.order.config.OrderRabbitConfig;
import com.zjw.gulimall.order.entity.OrderItemEntity;
import com.zjw.gulimall.order.entity.PaymentInfoEntity;
import com.zjw.gulimall.order.feign.CartFeignService;
import com.zjw.gulimall.order.feign.MemberFeignService;
import com.zjw.gulimall.order.feign.ProductFeignService;
import com.zjw.gulimall.order.feign.WareFeignService;
import com.zjw.gulimall.order.interceptor.OrderInterceptor;
import com.zjw.gulimall.order.service.OrderItemService;
import com.zjw.gulimall.order.service.PaymentInfoService;
import com.zjw.gulimall.order.to.OrderCreateTo;
import com.zjw.gulimall.order.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.order.dao.OrderDao;
import com.zjw.gulimall.order.entity.OrderEntity;
import com.zjw.gulimall.order.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;

@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService
{

    @Resource
    private MemberFeignService memberFeignService;
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private ExecutorService executor;
    @Resource
    private WareFeignService wareFeignService;
    @Resource
    private CartFeignService cartFeignService;
    @Resource
    private OrderItemService orderItemService;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private PaymentInfoService paymentInfoService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo getConfirmInfo() {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberInfoTo memberInfoTo = OrderInterceptor.loginThreadLocal.get();
        //1.远程获取收件人信息列表
        CompletableFuture.runAsync(() -> {
            List<MemberAddressVo> addressVoList = initAddressInfo(memberInfoTo);
            orderConfirmVo.setAddress(addressVoList);
        }, executor);
        //2.调用购物车模块获取订单商品列表
        List<OrderItemVo> itemVoList = initItemListInfo(memberInfoTo, orderConfirmVo);
        orderConfirmVo.setItems(itemVoList);

        //3.为每一个订单项生成一个唯一的token并存入redis，防止重复提交
        String token = UUID.randomUUID().toString().replace("-", "");
        orderConfirmVo.setOrderToken(token);
        redisTemplate.opsForValue().set(OrderConstant.ORDER_TOKEN_PREFIX + memberInfoTo.getId(), token, 10, TimeUnit.MINUTES);
        orderConfirmVo.setIntegration(memberInfoTo.getIntegration());
        return orderConfirmVo;
    }

    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) {
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        //0代表提交订单成功
        responseVo.setCode(0);
        if (orderSubmitVo == null) {
            responseVo.setCode(OrderConstant.OrderSubmitStatusEnum.ORDER_SUBMIT_FAIL_DEFAULT_ERROR.getSubmitStatusCode());
            return responseVo;
        }
        //获取用户信息
        MemberInfoTo memberInfoTo = OrderInterceptor.loginThreadLocal.get();
        //提交订单
        //1.原子验证令牌，用于避免重复提交
        String orderToken = orderSubmitVo.getOrderToken();
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        //采用lua脚本进行原子操作，0表示验证失败，1表示验证成功
        Long executeResult = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                Arrays.asList(OrderConstant.ORDER_TOKEN_PREFIX + memberInfoTo.getId()), orderToken);
        if (executeResult == null || Integer.parseInt(executeResult.toString()) != 1) {
            responseVo.setCode(OrderConstant.OrderSubmitStatusEnum.ORDER_SUBMIT_FAIL_EXPIRED.getSubmitStatusCode());
            return responseVo;
        }
        //2.令牌通过后，构建订单信息插入数据库
        //2.1 先进行验价>>查看前端传来的价格是否和后端计算的价格保持一致
        OrderCreateTo order = createOrder(orderSubmitVo, memberInfoTo);
        //订单创建失败，订单中购买商品项无都视为创建订单失败
        if (order.getOrder() == null || order.getOrderItems() == null || order.getOrderItems().size() <= 0) {
            log.error("创建订单失败，请稍后重试或联系技术人员");
            responseVo.setCode(OrderConstant.OrderSubmitStatusEnum.ORDER_SUBMIT_FAIL_DEFAULT_ERROR.getSubmitStatusCode());
        }
        else {
            if (Math.abs(orderSubmitVo.getPayPrice().subtract(order.getOrder().getPayAmount()).doubleValue()) <= 0.01d) {
                //2.2 保存订单相关数据到数据库中
                saveOrder(order);
                //2.3金额比对且订单数据保存成功，开始锁库存
                List<OrderItemEntity> orderItems = order.getOrderItems();
                WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
                wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
                List<OrderItemVo> orderItemList = orderItems.stream().map(orderItemEntity -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(orderItemEntity.getSkuId());
                    orderItemVo.setTitle(orderItemEntity.getSkuName());
                    orderItemVo.setCount(orderItemEntity.getSkuQuantity());
                    return orderItemVo;
                }).collect(Collectors.toList());
                wareSkuLockVo.setLocks(orderItemList);
                //2.2.1 一个一个商品进行锁库存
                //锁库存只需要四个参数：1.skuId 2.skuName 3.count 4.orderSn
                try {
                    boolean lockResult = wareFeignService.lockWareItems(wareSkuLockVo);
                    responseVo.setOrderEntity(order.getOrder());
                    //分布式事务常常出现的两种情况
                    //1.假事务，远程调用服务已经成功，但是由于网络问题，返回调用失败结果，从而导致订单回滚，但是库存已经扣除，状态不一致
                    //2.远程调用服务之后出现异常，虽然远程调用服务成功，但是之后的代码出现异常，这样的话远程库存服务感知不到异常，也不会回滚
                    //但是订单服务可以感知异常，从而导致订单回滚，但是库存已经扣除，状态不一致
                    //解决办法：可以通过消息队列构造延迟队列来解决

                }
                catch (Exception e) {
                    if (e instanceof StockLackException) {
                        log.error(e.getMessage());
                        throw new StockLackException(e.getMessage());
                    }
                    throw new RuntimeException(e.getMessage());
                }
            }
            else {
                responseVo.setCode(OrderConstant.OrderSubmitStatusEnum.ORDER_SUBMIT_FAIL_PRICE_CHANGED.getSubmitStatusCode());
            }
        }
        return responseVo;
    }

    @Override
    public void cancelOrder(OrderEntity orderEntity) {
        if (orderEntity == null || orderEntity.getId() == null) {
            log.error("要取消的订单不存在，请检查");
            return;
        }
        //应当重新查询数据库，然后对其状态进行判断，保证消息重复提交也不会重复更改数据库状态，即幂等性
        OrderEntity order = getById(orderEntity.getId());
        //只有是新建的订单过时未支付才要取消
        if (order.getStatus() == OrderConstant.OrderStatus.ORDER_STATUS_NEW.getStatusCode()) {
            log.info("订单超时未支付，准备解锁");
            order = new OrderEntity();
            order.setId(orderEntity.getId());
            order.setStatus(OrderConstant.OrderStatus.ORDER_STATUS_CANCELED.getStatusCode());
            updateById(order);
        }
    }

    @Override
    public OrderEntity getOrderInfoByOrderSn(String orderSn) {
        if (orderSn == null) return null;
        OrderEntity orderEntity = getOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return orderEntity;
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = null;
        //根据订单号查询订单信息
        OrderEntity orderEntity = getOrderInfoByOrderSn(orderSn);
        //查看订单状态，可能已经被自动取消了,只有新建订单才能进行付款
        Integer orderStatus = orderEntity.getStatus();
        if (orderStatus == OrderConstant.OrderStatus.ORDER_STATUS_NEW.getStatusCode()) {
            payVo = new PayVo();
            payVo.setOut_trade_no(orderSn);
            //使金额保留两位小数
            payVo.setTotal_amount(orderEntity.getPayAmount().setScale(2, BigDecimal.ROUND_UP).toString());
            List<OrderItemEntity> list = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn).orderByDesc("id"));
            //将查询到商品详情的第一项信息作为订单主体信息
            payVo.setSubject(list.get(0).getSkuName());
            payVo.setBody(list.get(0).getSkuName());
        }
        return payVo;
    }

    @Override
    public PageUtils listOrderPage(Map<String, Object> params) {
        MemberInfoTo memberInfoTo = OrderInterceptor.loginThreadLocal.get();
        List<OrderEntity> orderEntityList = list(new QueryWrapper<OrderEntity>().eq("member_id", memberInfoTo.getId()).and(qw -> {
            qw.eq("status", 0).or().eq("status", 5);
        }));
        List<String> orderSnList = orderEntityList.stream().map(OrderEntity::getOrderSn).collect(Collectors.toList());
        IPage<OrderItemEntity> page = orderItemService.listOrderItemPage(params, orderSnList);
        return new PageUtils(page);
    }

    //处理支付成功的订单
    @Override
    @Transactional
    public String handlePayedOrder(PayAsyncVo payAsyncVo) {
        //1.更改订单状态
        OrderEntity orderEntity = getOne(new QueryWrapper<OrderEntity>().eq("order_sn", payAsyncVo.getOut_trade_no()));
        if (orderEntity == null) return "fail";
        orderEntity.setStatus(OrderConstant.OrderStatus.ORDER_STATUS_PAYED.getStatusCode());
        updateById(orderEntity);
        //2.更改锁定的库存状态
        R r = wareFeignService.updateWareTasksDetailStatus(payAsyncVo.getOut_trade_no(), WareConstant.WareOrderTaskDetailStatusEnum.TASK_DETAIL_STATUS_SALE.getCode());
        if(r.getCode() != 0){
            throw  new RuntimeException("订单库存状态修改失败，请重新下单");
        }
        //3.插入支付信息到数据库中
        PaymentInfoEntity paymentInfo = new PaymentInfoEntity();
        paymentInfo.setAlipayTradeNo(payAsyncVo.getTrade_no());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            paymentInfo.setCreateTime(sdf.parse(payAsyncVo.getNotify_time()));
            paymentInfo.setTotalAmount(new BigDecimal(payAsyncVo.getBuyer_pay_amount()));
            paymentInfo.setPaymentStatus(payAsyncVo.getTrade_status());
            paymentInfo.setSubject(payAsyncVo.getSubject());
            paymentInfo.setCallbackTime(new Date());
            paymentInfo.setOrderSn(payAsyncVo.getOut_trade_no());
            paymentInfo.setId(Long.valueOf(payAsyncVo.getBuyer_id()));
            paymentInfoService.save(paymentInfo);
        }
        catch (ParseException e) {
            throw  new RuntimeException("支付信息保存失败，请重新下单");
        }
        return "success";
    }

    //保存订单数据和订单商品详情到数据库中
    private void saveOrder(OrderCreateTo order) {
        save(order.getOrder());
        //订单创建所有流程执行完毕之后，应当发送信息给MQ用于自动关单
        rabbitTemplate.convertAndSend(OrderRabbitConfig.ORDER_EVENT_EXCHANGE, OrderRabbitConfig.orderDelayRouteKey, order.getOrder());
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
        log.info("订单保存成功，发送信息到取消订单队列");
    }

    private OrderCreateTo createOrder(OrderSubmitVo orderSubmitVo, MemberInfoTo memberInfoTo) {
        OrderCreateTo order = new OrderCreateTo();
        try {
            //1. 构建订单详情>>oms_order,包括自动生产订单号
            CompletableFuture<OrderEntity> getOrderEntityFuture = CompletableFuture.supplyAsync(() -> {
                return buildOrderEntity(orderSubmitVo, memberInfoTo);
            });
            OrderEntity orderEntity = getOrderEntityFuture.get();
            order.setOrder(orderEntity);
            //2. 构建订单商品详情>>oms_order_item
            List<OrderItemEntity> orderItemEntityList = buildItemEntityList(orderEntity);
            order.setOrderItems(orderItemEntityList);
            // 3.根据订单项计算价格	传入订单 、订单项 计算价格、积分、成长值等相关信息
            computerPrice(orderEntity, orderItemEntityList);
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error("生成订单失败>>{}", e.getMessage());
        }
        return order;
    }

    private void computerPrice(OrderEntity orderEntity, List<OrderItemEntity> items) {

        // 叠加每一个订单项的金额
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal integration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");
        BigDecimal gift = new BigDecimal("0.0");
        BigDecimal growth = new BigDecimal("0.0");

        // 总价
        BigDecimal totalPrice = new BigDecimal("0.0");
        for (OrderItemEntity item : items) {  // 这段逻辑不是特别合理，最重要的是累积总价，别的可以跳过
            // 优惠券的金额
            coupon = coupon.add(item.getCouponAmount());
            // 积分优惠的金额
            integration = integration.add(item.getIntegrationAmount());
            // 打折的金额
            promotion = promotion.add(item.getPromotionAmount());
            BigDecimal realAmount = item.getRealAmount();
            totalPrice = totalPrice.add(realAmount);

            // 购物获取的积分、成长值
            gift.add(new BigDecimal(item.getGiftIntegration().toString()));
            growth.add(new BigDecimal(item.getGiftGrowth().toString()));
        }
        // 1.订单价格相关 总额、应付总额
        orderEntity.setTotalAmount(totalPrice);
        orderEntity.setPayAmount(totalPrice.add(orderEntity.getFreightAmount()));

        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(integration);
        orderEntity.setCouponAmount(coupon);

        // 设置积分、成长值
        orderEntity.setIntegration(gift.intValue());
        orderEntity.setGrowth(growth.intValue());

        // 设置订单的删除状态
        orderEntity.setDeleteStatus(OrderConstant.OrderStatus.ORDER_STATUS_NEW.getStatusCode());
    }


    private List<OrderItemEntity> buildItemEntityList(OrderEntity orderEntity) {
        //异步远程调用购物车确定购买的商品
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<List<OrderItemVo>> getOrderItemListFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            R cartResult = cartFeignService.getLoginCartItems();
            System.out.println(cartResult.getCode() + ">>" + cartResult.get("msg").toString());
            List<OrderItemVo> orderItemList = cartResult.getData(new TypeReference<List<OrderItemVo>>()
            {
            });
            return orderItemList;
        }, executor);
        //异步调用将购物车确定购买的商品转换成要存入数据库的商品详情项
        CompletableFuture<List<OrderItemEntity>> getOrderItemEntityListFuture = getOrderItemListFuture.thenApplyAsync(orderItemList -> {
            List<OrderItemEntity> orderItemEntityList = orderItemList.stream().filter(Objects::nonNull).map(orderItem -> {
                OrderItemEntity orderItemEntity = buildOrderItem(orderItem);
                orderItemEntity.setOrderId(orderEntity.getId());
                orderItemEntity.setOrderSn(orderEntity.getOrderSn());
                return orderItemEntity;
            }).collect(Collectors.toList());
            return orderItemEntityList;
        });
        List<OrderItemEntity> result = null;
        try {
            result = getOrderItemEntityListFuture.get();
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error("获取订单购物项信息失败>>{}", e.getMessage());
        }
        return result;
    }

    //设置单个订单商品
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //设置跟sku相关的信息
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuAttrsVals(String.join(";", item.getSkuAttr()));
        orderItemEntity.setSkuQuantity(item.getCount());
        //设置和spu相关的信息,远程调用根据skuId查出其所属的spu信息
        R spuResult = productFeignService.getSpuInfoBySkuId(item.getSkuId());
        try {
            SpuInfoEntity spuInfo = spuResult.getData(new TypeReference<SpuInfoEntity>()
            {
            });
            if (spuInfo != null) {
                orderItemEntity.setSpuId(spuInfo.getId());
                orderItemEntity.setSpuName(spuInfo.getSpuName());
                //还可以根据spuId去查品牌名字，太麻烦不做了
                orderItemEntity.setSpuBrand(spuInfo.getBrandId().toString());
                orderItemEntity.setCategoryId(spuInfo.getCatalogId());
                orderItemEntity.setSpuPic(spuInfo.getSpuDescription());
            }
        }
        catch (Exception e) {
            log.error("类型转换异常>>{}", e.getMessage());
            e.printStackTrace();
        }
        // 4.积分信息 买的数量越多积分越多 成长值越多
        orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());

        // 5.订单项的价格信息 优惠金额
        orderItemEntity.setPromotionAmount(new BigDecimal("0.0")); // 促销打折
        orderItemEntity.setCouponAmount(new BigDecimal("0.0")); // 优惠券
        orderItemEntity.setIntegrationAmount(new BigDecimal("0.0")); // 积分

        // 当前订单项的原价
        BigDecimal orign = orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()));
        // 减去各种优惠的价格
        BigDecimal subtract =
                orign.subtract(orderItemEntity.getCouponAmount()) // 优惠券逻辑没有写，应该去coupon服务查用户的sku优惠券
                        .subtract(orderItemEntity.getPromotionAmount()) // 官方促销
                        .subtract(orderItemEntity.getIntegrationAmount()); // 京豆/积分
        orderItemEntity.setRealAmount(subtract);
        return orderItemEntity;
    }

    private OrderEntity buildOrderEntity(OrderSubmitVo orderSubmitVo, MemberInfoTo memberInfoTo) {
        OrderEntity orderEntity = new OrderEntity();
        //借助IdWorker生产订单号
        orderEntity.setOrderSn(IdWorker.getTimeId());
        //0代表订单新建
        orderEntity.setConfirmStatus(0);
        orderEntity.setStatus(OrderConstant.OrderStatus.ORDER_STATUS_NEW.getStatusCode());
        orderEntity.setCreateTime(new Date());
        orderEntity.setAutoConfirmDay(15);//15天后自动收获
        orderEntity.setMemberId(memberInfoTo.getId());
        orderEntity.setMemberUsername(memberInfoTo.getUsername());
        //远程查询用户收获地址
        Long addrId = orderSubmitVo.getAddrId();
        R addressResult = memberFeignService.getAddressInfoById(addrId);
        MemberAddressVo memberAddressInfo = addressResult.getData(new TypeReference<MemberAddressVo>()
        {
        });
        //将地址信息加入订单信息中
        orderEntity.setFreightAmount(memberAddressInfo.getFare());
        orderEntity.setReceiverCity(memberAddressInfo.getCity());
        orderEntity.setReceiverDetailAddress(memberAddressInfo.getDetailAddress());
        orderEntity.setReceiverPhone(memberAddressInfo.getPhone());
        orderEntity.setReceiverName(memberAddressInfo.getName());
        orderEntity.setReceiverProvince(memberAddressInfo.getProvince());
        orderEntity.setReceiverPostCode(memberAddressInfo.getPostCode());
        orderEntity.setReceiverRegion(memberAddressInfo.getRegion());
        return orderEntity;
    }

    private List<OrderItemVo> initItemListInfo(MemberInfoTo memberInfoTo, OrderConfirmVo orderConfirmVo) {
        //这是主线程的请求上下文环境
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<List<OrderItemVo>> getItemListFuture = CompletableFuture.supplyAsync(() -> {
            //这是异步线程的请求上下文环境
            //为了使异步线程和主线程的请求上下文环境一致，从而解决feign远程调用丢失请求头
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //远程调用购物车服务查询购物车数据
            R r = cartFeignService.getLoginCartItems();
            if (r.getCode() == 0) {
                return r.getData(new TypeReference<List<OrderItemVo>>()
                {
                });
            }
            return null;
        }, executor);
        CompletableFuture<Void> getPriceFuture = getItemListFuture.thenAcceptAsync(orderItemVoList -> {
            //由于订单数据是从购物车中查出的，所以有可能购物车中的价格是很久之前的价格了，所以对于每件商品的价格应当重新查询一次
            if (orderItemVoList != null) {
                //远程调用商品服务查询价格
                List<Long> skuIdList = orderItemVoList.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
                R r = productFeignService.listPriceByIds(skuIdList);
                if (r.getCode() == 0) {
                    List<SkuPriceVo> skuPriceVoList = r.getData(new TypeReference<List<SkuPriceVo>>()
                    {
                    });
                    if (skuPriceVoList != null && skuPriceVoList.size() > 0) {
                        orderItemVoList.forEach(item -> {
                            skuPriceVoList.forEach(sv -> {
                                if (sv.getSkuId().equals(item.getSkuId())) {
                                    item.setPrice(sv.getPrice());
                                }
                            });
                        });
                    }
                }
            }
        }, executor);
        CompletableFuture<Void> getStockFuture = getItemListFuture.thenAcceptAsync(orderItemVoList -> {
            //远程查询仓储服务查看每一件订单商品是否有货
            List<Long> idList = orderItemVoList.stream().filter(Objects::nonNull).map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R r = wareFeignService.listStock(idList);
            if (r.getCode() == 0) {
                List<WareSkuVo> wareSkuVoList = r.getData(new TypeReference<List<WareSkuVo>>()
                {
                });
                Map<Long, Boolean> collect = wareSkuVoList.stream().filter(Objects::nonNull).collect(Collectors.toMap(WareSkuVo::getSkuId, WareSkuVo::isHasStock));
                orderConfirmVo.setStocks(collect);
            }
            else {
                log.error("远程查询仓储服务失败，请重试");
            }
        }, executor);
        List<OrderItemVo> orderItemVoList = null;
        try {
            orderItemVoList = getItemListFuture.get();
            getPriceFuture.get();
            getStockFuture.get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return orderItemVoList;
    }

    private List<MemberAddressVo> initAddressInfo(MemberInfoTo memberInfoTo) {
        R addressResult = memberFeignService.getAddressInfoByMemberId(memberInfoTo.getId());
        if (addressResult.getCode() == 0) {
            List<MemberAddressVo> memberAddressVoList = addressResult.getData(new TypeReference<List<MemberAddressVo>>()
            {
            });
            return memberAddressVoList;
        }
        return null;
    }

}