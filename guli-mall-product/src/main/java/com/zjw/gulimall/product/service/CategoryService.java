package com.zjw.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;
import com.zjw.gulimall.product.entity.CategoryEntity;
import com.zjw.gulimall.product.vo.Catalog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 16:35:30
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> queryCategoryForTree();

    //根据三级分类id查询出对应的完整分类路径
    List<Long> selectFullPath(Long catelogId);

    //获取所有一级分类商品
    List<CategoryEntity> getLevel1CategoryList();

    Map<String, List<Catalog2Vo>> getCatalogJson();
}

