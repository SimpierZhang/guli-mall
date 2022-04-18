package com.zjw.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjw.common.utils.PageUtils;
import com.zjw.gulimall.order.entity.OrderEntity;
import com.zjw.gulimall.order.vo.*;

import java.util.Map;

/**
 * 订单
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 18:25:47
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    //获取订单确认页所需信息
    OrderConfirmVo getConfirmInfo();

    SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo);

    void cancelOrder(OrderEntity orderEntity);

    OrderEntity getOrderInfoByOrderSn(String orderSn);

    PayVo getOrderPay(String orderSn);

    PageUtils listOrderPage(Map<String, Object> params);

    String handlePayedOrder(PayAsyncVo payAsyncVo);
}

