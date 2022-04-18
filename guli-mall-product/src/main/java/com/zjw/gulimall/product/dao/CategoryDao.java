package com.zjw.gulimall.product.dao;

import com.zjw.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 16:35:30
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
