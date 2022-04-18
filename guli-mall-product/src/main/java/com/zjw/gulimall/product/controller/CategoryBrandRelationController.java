package com.zjw.gulimall.product.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjw.gulimall.product.entity.BrandEntity;
import com.zjw.gulimall.product.entity.CategoryEntity;
import com.zjw.gulimall.product.service.BrandService;
import com.zjw.gulimall.product.service.CategoryService;
import com.zjw.gulimall.product.vo.BrandEntityVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zjw.gulimall.product.entity.CategoryBrandRelationEntity;
import com.zjw.gulimall.product.service.CategoryBrandRelationService;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;

import javax.annotation.Resource;


/**
 * 品牌分类关联
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 17:10:36
 */
@Slf4j
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Resource
    private BrandService brandService;
    @Resource
    private CategoryService categoryService;


    /**
     * /product/categorybrandrelation/brands/list
     * 根据categoryId获取分类关联的品牌
     * @param catId
     * @return
     */
    @GetMapping("/brands/list")
    public R listBrandsByCatId(@RequestParam(value = "catId", required = true) Long catId){
        List<CategoryBrandRelationEntity> relEntityList = categoryBrandRelationService.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        List<BrandEntityVo> brandEntityVoList = new ArrayList<>();
        if(relEntityList != null){
            brandEntityVoList = relEntityList.stream().map(r -> {
                BrandEntityVo brandEntityVo = new BrandEntityVo();
                BeanUtils.copyProperties(r, brandEntityVo);
                return brandEntityVo;
            }).collect(Collectors.toList());
        }
        return R.ok().put("data", brandEntityVoList);
    }


    @GetMapping("/catelog/list")
    public R list(@RequestParam Long brandId){
        List<CategoryBrandRelationEntity> brandRelEntity = categoryBrandRelationService.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
        return R.ok().put("data", brandRelEntity);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
        BrandEntity brand = brandService.getById(categoryBrandRelation.getBrandId());
        if(brand != null) categoryBrandRelation.setBrandName(brand.getName());
        CategoryEntity category = categoryService.getById(categoryBrandRelation.getCatelogId());
        if(category != null) categoryBrandRelation.setCatelogName(category.getName());
        categoryBrandRelationService.save(categoryBrandRelation);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
