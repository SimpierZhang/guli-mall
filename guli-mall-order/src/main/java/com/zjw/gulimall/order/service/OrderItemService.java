package com.zjw.gulimall.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjw.common.utils.PageUtils;
import com.zjw.gulimall.order.entity.OrderItemEntity;

import java.util.List;
import java.util.Map;

/**
 * 订单项信息
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 18:25:47
 */
public interface OrderItemService extends IService<OrderItemEntity> {

    PageUtils queryPage(Map<String, Object> params);

    IPage<OrderItemEntity> listOrderItemPage(Map<String, Object> params, List<String> orderSnList);
}

