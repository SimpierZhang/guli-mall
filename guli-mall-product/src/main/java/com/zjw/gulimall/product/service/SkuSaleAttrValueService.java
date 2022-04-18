package com.zjw.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjw.common.utils.PageUtils;
import com.zjw.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.zjw.gulimall.product.vo.ItemSaleAttrVo;
import com.zjw.gulimall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 16:35:30
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<ItemSaleAttrVo> getItemSaleAttr(Long spuId);

    List<String> getBySkuId(Long skuId);

}

