package com.zjw.gulimall.order.vo;

import com.zjw.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-26 20:43
 * @Modifier:
 */
@Data
public class SubmitOrderResponseVo
{
    // 该实体为order表的映射
    private OrderEntity orderEntity;

    /** 错误状态码 **/
    private Integer code;
}
