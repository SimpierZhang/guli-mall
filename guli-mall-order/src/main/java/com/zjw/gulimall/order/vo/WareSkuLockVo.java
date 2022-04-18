package com.zjw.gulimall.order.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-27 21:09
 * @Modifier:
 */
@Data
public class WareSkuLockVo
{
    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 要锁住的所有库存信息
     */
    private List<OrderItemVo> locks;
}
