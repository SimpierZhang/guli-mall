package com.zjw.gulimall.order.listener;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.zjw.common.constant.OrderConstant;
import com.zjw.common.to.MemberInfoTo;
import com.zjw.common.to.OrderEntityVo;
import com.zjw.common.to.SeckillOrderTo;
import com.zjw.gulimall.order.config.OrderRabbitConfig;
import com.zjw.gulimall.order.entity.OrderEntity;
import com.zjw.gulimall.order.entity.OrderItemEntity;
import com.zjw.gulimall.order.interceptor.OrderInterceptor;
import com.zjw.gulimall.order.service.OrderItemService;
import com.zjw.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-09-01 21:02
 * @Modifier:
 */
@Component
@Slf4j
@RabbitListener(queues = OrderRabbitConfig.SECKILL_ORDER_QUEUE)
public class SeckillOrderRabbitListener
{
    @Resource
    private OrderService orderService;
    @Resource
    private OrderItemService orderItemService;

    //创建快速订单
    @RabbitHandler
    public void saveSeckillOrder(Message message, Channel channel, SeckillOrderTo seckillOrderTo){
        try {
            log.error("创建秒杀订单中...");
            //取消订单之后也需要给库存服务发送消息让其解锁库存
            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setStatus(OrderConstant.OrderStatus.ORDER_STATUS_NEW.getStatusCode());
            orderEntity.setMemberUsername(seckillOrderTo.getMemberName());
            orderEntity.setMemberId(seckillOrderTo.getMemberId());
            orderEntity.setAutoConfirmDay(15);
            orderEntity.setOrderSn(seckillOrderTo.getOrderSn());
            orderEntity.setPayAmount(seckillOrderTo.getRealAmount());
            orderService.save(orderEntity);
            OrderItemEntity orderItemEntity = new OrderItemEntity();
            BeanUtils.copyProperties(orderItemEntity, seckillOrderTo.getSkuInfoVo());
            orderItemEntity.setOrderSn(seckillOrderTo.getOrderSn());
            orderItemEntity.setSkuQuantity(seckillOrderTo.getSkuQuantity());
            orderItemEntity.setSkuId(seckillOrderTo.getSkuId());
            orderItemService.save(orderItemEntity);
        }catch (Exception e){
            log.error("创建秒杀订单失败>>重试中...");
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
