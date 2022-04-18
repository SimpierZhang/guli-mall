package com.zjw.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjw.common.utils.PageUtils;
import com.zjw.gulimall.product.entity.SpuInfoEntity;
import com.zjw.gulimall.product.vo.ProductInfoVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 16:35:30
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 保存商品
     * @param productInfoVo
     * @return
     */
    boolean saveProduct(ProductInfoVo productInfoVo);

    /**
     * 上架商品
     */
    boolean upProduct(Long spuId);

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

