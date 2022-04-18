package com.zjw.gulimall.ware.dao;

import com.zjw.gulimall.ware.entity.WmsWareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品库存
 * 
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 19:00:46
 */
@Mapper
public interface WmsWareSkuDao extends BaseMapper<WmsWareSkuEntity> {

    void unlockStock(@Param("lockNum") Integer lockNum, @Param("wareSkuId") Long wareSkuId);
}
