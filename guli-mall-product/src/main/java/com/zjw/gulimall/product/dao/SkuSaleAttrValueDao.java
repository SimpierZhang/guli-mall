package com.zjw.gulimall.product.dao;

import com.zjw.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjw.gulimall.product.vo.ItemSaleAttrVo;
import com.zjw.gulimall.product.vo.SkuItemVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 16:35:30
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<ItemSaleAttrVo> getItemSaleAttr(@Param("spuId") Long spuId);

    List<String> getBySkuId(@Param("skuId") Long skuId);
}
