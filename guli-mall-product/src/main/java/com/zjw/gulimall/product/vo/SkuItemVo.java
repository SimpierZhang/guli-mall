package com.zjw.gulimall.product.vo;

import com.zjw.common.to.SeckillSkuRedisTo;
import com.zjw.gulimall.product.entity.SkuImagesEntity;
import com.zjw.gulimall.product.entity.SkuInfoEntity;
import com.zjw.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Author: Zjw
 * @Description: 商品详情信息
 * @Create 2021-08-22 12:04
 * @Modifier:
 */
@Data
public class SkuItemVo
{
    //1.相关的skuInfo>>pms_sku_info
    private SkuInfoEntity info;
    //2.相关的sku销售属性组合>>pms_sku_sale_attr_value
    private List<ItemSaleAttrVo> saleAttr;
    //3.商品详情描述图片>>pms_spu_info_desc
    private SpuInfoDescEntity desc;
    //4.sku属性描述>>pms_product_attr_value
    private List<SpuItemAttrGroup> groupAttrs;
    //5.sku的图片信息
    private List<SkuImagesEntity> images;

    private boolean hasStock = true;

    private SeckillSkuRedisTo seckillInfoVo;



    @Data
    @ToString
    public static class SpuItemAttrGroup
    {
        private String groupName;
        private List<SpuBaseAttrVo> attrs;
    }

    @Data
    @ToString
    public static class SpuBaseAttrVo
    {
        private String attrName;
        private String attrValue;
    }
}
