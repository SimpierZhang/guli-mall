package com.zjw.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-25 21:09
 * @Modifier:
 */
@Data
public class SkuPriceVo
{
    private Long skuId;
    private BigDecimal price;
}
