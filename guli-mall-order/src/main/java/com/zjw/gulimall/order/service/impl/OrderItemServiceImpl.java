package com.zjw.gulimall.order.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.order.dao.OrderItemDao;
import com.zjw.gulimall.order.entity.OrderItemEntity;
import com.zjw.gulimall.order.service.OrderItemService;


@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public IPage<OrderItemEntity> listOrderItemPage(Map<String, Object> params, List<String> orderSnList) {
        QueryWrapper<OrderItemEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("order_sn", orderSnList);
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                queryWrapper
        );
        return page;
    }

}