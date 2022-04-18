package com.zjw.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjw.common.utils.PageUtils;
import com.zjw.gulimall.coupon.entity.HomeSubjectSpuEntity;

import java.util.Map;

/**
 * 专题商品
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 18:35:13
 */
public interface HomeSubjectSpuService extends IService<HomeSubjectSpuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

