package com.zjw.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.zjw.common.to.OrderEntityVo;
import com.zjw.gulimall.order.config.OrderRabbitConfig;
import com.zjw.gulimall.order.entity.OrderEntity;
import com.zjw.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-28 15:56
 * @Modifier:
 */
@Component
@Slf4j
@RabbitListener(queues = OrderRabbitConfig.ORDER_RELEASE_QUEUE)
public class OrderRabbitListener
{
    @Resource
    private OrderService orderService;
    @Resource
    private RabbitTemplate rabbitTemplate;

    //监听取消订单队列，当监听到消息时便表示未在指定时间内支付订单，应当取消订单
    @RabbitHandler
    public void cancelOrder(Message message, OrderEntity orderEntity, Channel channel){
        try {
            log.error("取消订单中...");
            orderService.cancelOrder(orderEntity);
            //取消订单之后也需要给库存服务发送消息让其解锁库存
            OrderEntityVo orderEntityVo = new OrderEntityVo();
            BeanUtils.copyProperties(orderEntity, orderEntityVo);
            rabbitTemplate.convertAndSend(OrderRabbitConfig.ORDER_EVENT_EXCHANGE, "order.release.other", orderEntityVo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (Exception e){
            log.error("取消订单失败>>重试中...");
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
