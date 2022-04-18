package com.zjw.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-15 14:36
 * @Modifier:
 */
@Data
public class SkuEsModel { //common中
    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private BigDecimal skuPrice;
    private String skuImg;
    private Long saleCount;
    private boolean hasStock;
    private Long hotScore;
    private Long brandId;
    private Long catalogId;
    private String brandName;
    private String brandImg;
    private String catalogName;
    private List<Attr> attrs;

    @Data
    public static class Attr{
        private Long attrId;
        private String attrName;
        private String attrValue;
    }
}

