package com.zjw.gulimall.product.service.impl;

import com.zjw.gulimall.product.service.AttrService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.product.dao.ProductAttrValueDao;
import com.zjw.gulimall.product.entity.ProductAttrValueEntity;
import com.zjw.gulimall.product.service.ProductAttrValueService;

import javax.annotation.Resource;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {


    @Resource
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<ProductAttrValueEntity> listQueryAttr(Long spuId) {
        List<Long> queryIdList = attrService.listQueryIdList();
        return list(new QueryWrapper<ProductAttrValueEntity>().in("attr_id", queryIdList).eq("spu_id", spuId));
    }

}