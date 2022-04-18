package com.zjw.gulimall.product.service.impl;

import com.zjw.gulimall.product.vo.ItemSaleAttrVo;
import com.zjw.gulimall.product.vo.SkuItemVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.product.dao.SkuSaleAttrValueDao;
import com.zjw.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.zjw.gulimall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<ItemSaleAttrVo> getItemSaleAttr(Long spuId) {
        return baseMapper.getItemSaleAttr(spuId);
    }

    @Override
    public List<String> getBySkuId(Long skuId) {
        return baseMapper.getBySkuId(skuId);
    }

}