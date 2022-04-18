package com.zjw.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjw.common.utils.PageUtils;
import com.zjw.gulimall.coupon.entity.SeckillSkuNoticeEntity;

import java.util.Map;

/**
 * 秒杀商品通知订阅
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 18:35:13
 */
public interface SeckillSkuNoticeService extends IService<SeckillSkuNoticeEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

