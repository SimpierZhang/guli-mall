package com.zjw.gulimall.product.dao;

import com.zjw.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 商品属性
 * 
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 16:35:30
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<Long> listQueryIdList();
}
