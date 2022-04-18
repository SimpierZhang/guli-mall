package com.zjw.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjw.common.utils.PageUtils;
import com.zjw.gulimall.order.entity.MqMessageEntity;

import java.util.Map;

/**
 * 
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 18:25:47
 */
public interface MqMessageService extends IService<MqMessageEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

