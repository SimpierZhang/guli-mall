package com.zjw.gulimall.product.dao;

import com.zjw.gulimall.product.entity.SkuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjw.gulimall.product.vo.SkuPriceVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku信息
 * 
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 16:35:30
 */
@Mapper
public interface SkuInfoDao extends BaseMapper<SkuInfoEntity> {

    List<SkuPriceVo> listPriceByIds(@Param("idList") List<Long> idList);
}
