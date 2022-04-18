package com.zjw.common.to;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-15 16:13
 * @Modifier:
 */
@Data
public class WareSkuVo implements Serializable
{
    private Long skuId;
    private Long wareId;
    private boolean hasStock;
}
