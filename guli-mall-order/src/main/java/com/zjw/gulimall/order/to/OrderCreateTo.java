package com.zjw.gulimall.order.to;

import com.zjw.gulimall.order.entity.OrderEntity;
import com.zjw.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: Zjw
 * @Description: 订单数据
 * @Create 2021-08-26 21:07
 * @Modifier:
 */
@Data
public class OrderCreateTo
{
    private OrderEntity order;

    private List<OrderItemEntity> orderItems;

    /** 订单计算的应付价格 **/
    private BigDecimal payPrice;

    /** 运费 **/
    private BigDecimal fare;
}
