package com.zjw.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjw.common.utils.PageUtils;
import com.zjw.gulimall.coupon.entity.CouponSpuRelationEntity;

import java.util.Map;

/**
 * 优惠券与产品关联
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 18:35:13
 */
public interface CouponSpuRelationService extends IService<CouponSpuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

