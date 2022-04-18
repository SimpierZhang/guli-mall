package com.zjw.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjw.common.utils.PageUtils;
import com.zjw.gulimall.product.entity.SkuInfoEntity;
import com.zjw.gulimall.product.vo.ProductInfoVo;
import com.zjw.gulimall.product.vo.SkuItemVo;
import com.zjw.gulimall.product.vo.SkuPriceVo;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 16:35:30
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    SkuItemVo getSkuItemBySkuId(Long skuId);

    List<SkuPriceVo> listPriceByIds(List<Long> idList);
}

