package com.zjw.gulimall.product.service.impl;

import com.zjw.gulimall.product.entity.CategoryBrandRelationEntity;
import com.zjw.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.product.dao.BrandDao;
import com.zjw.gulimall.product.entity.BrandEntity;
import com.zjw.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Resource
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                new QueryWrapper<BrandEntity>()
        );

        return new PageUtils(page);
    }

    //级联更新，更新时不只更新品牌表，还要更新品牌和分类关系表
    @Override
    @Transactional
    public boolean cascadeUpdateById(BrandEntity brand) {
        boolean result = false;
        boolean brandUpdateResult = updateById(brand);
        List<CategoryBrandRelationEntity> brandRelList =
                categoryBrandRelationService.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brand.getBrandId()));
        brandRelList.forEach(b -> b.setBrandName(brand.getName()));
        boolean brandRelUpdateResult = categoryBrandRelationService.updateBatchById(brandRelList);
        return brandRelUpdateResult && brandUpdateResult;
    }

    @Override
    @Transactional
    public boolean cascadeRemoveByIds(List<Long> brandIdList) {
        //删除品牌表
        boolean removeBrandResult = this.removeByIds(brandIdList);
        QueryWrapper<CategoryBrandRelationEntity> queryWrapper = new QueryWrapper<>();
        //删除品牌种类关系表
        brandIdList.forEach(bi -> queryWrapper.or().eq("brand_id", bi));
        boolean removeBrandRelResult = categoryBrandRelationService.remove(queryWrapper);
        return removeBrandResult && removeBrandRelResult;
    }

}