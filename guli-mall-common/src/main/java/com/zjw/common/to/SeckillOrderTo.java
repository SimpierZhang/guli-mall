package com.zjw.common.to;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-09-01 20:39
 * @Modifier:
 */
@Data
public class SeckillOrderTo
{
    private String orderSn;

    private int skuQuantity;

    private BigDecimal realAmount;

    private Long skuId;

    private SkuInfoEntityTo skuInfoVo;

    private String memberName;

    private Long memberId;


}
