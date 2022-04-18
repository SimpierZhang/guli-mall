package com.zjw.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Author: Zjw
 * @Description: 商品详情页的销售属性
 * @Create 2021-08-22 13:55
 * @Modifier:
 */
@Data
public class ItemSaleAttrVo
{
    private Long attrId;
    private String attrName;

    /** AttrValueWithSkuIdVo两个属性 attrValue、skuIds */
    private List<AttrValueWithSkuIdVo> attrValues;
}

@Data
@ToString
class AttrValueWithSkuIdVo{
    private String skuIds;
    private String attrValue;
}
