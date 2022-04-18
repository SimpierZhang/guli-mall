package com.zjw.gulimall.product.service.serviceBK.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.gulimall.product.dao.CategoryDao;
import com.zjw.gulimall.product.entity.CategoryEntity;
import com.zjw.gulimall.product.service.CategoryService;
import com.zjw.gulimall.product.vo.Catalog2Vo;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2022-04-02 20:01
 * @Modifier:
 */
@Service("categoryServiceBK")
public class CategoryServiceBKImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService
{
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        return null;
    }

    @Override
    public List<CategoryEntity> queryCategoryForTree() {
        List<CategoryEntity> categoryEntityList = list(null);
        categoryEntityList.forEach(categoryEntity -> {
            categoryEntityList.forEach(category -> {
                if (category.getParentCid().equals(categoryEntity.getCatId())) {
                    List<CategoryEntity> children = categoryEntity.getChildren();
                    if (children == null) {
                        children = new ArrayList<>();
                        categoryEntity.setChildren(children);
                    }
                    children.add(category);
                }
            });
        });
        return categoryEntityList.stream().filter(c -> c.getParentCid().equals(0L)).collect(Collectors.toList());
    }

    @Override
    public List<Long> selectFullPath(Long catelogId) {
        return null;
    }

    @Override
    public List<CategoryEntity> getLevel1CategoryList() {
        return null;
    }

    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        return null;
    }
}
