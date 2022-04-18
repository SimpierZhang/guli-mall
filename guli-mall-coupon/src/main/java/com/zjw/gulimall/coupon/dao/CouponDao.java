package com.zjw.gulimall.coupon.dao;

import com.zjw.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 18:35:13
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
