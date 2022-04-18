package com.zjw.gulimall.order.dao;

import com.zjw.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 18:25:47
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
