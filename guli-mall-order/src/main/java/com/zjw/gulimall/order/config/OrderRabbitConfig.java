package com.zjw.gulimall.order.config;

import lombok.Getter;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;

/**
 * @Author: Zjw
 * @Description: order服务的消息队列配置，用于自动关单和解锁库存
 * @Create 2021-08-28 15:30
 * @Modifier:
 */
@Configuration
public class OrderRabbitConfig
{
    private RabbitTemplate rabbitTemplate;
    //订单服务消息处理交换机（在该服务中，同时也是死信交换机）
    public final static String ORDER_EVENT_EXCHANGE = "order.event.exchange";
    //订单服务的延时队列
    public final static String ORDER_DELAY_QUEUE = "order.delay.queue";
    //订单服务的释放订单队列
    public final static String ORDER_RELEASE_QUEUE = "order.release.queue";
    //库存服务的释放库存队列
    public final static String WARE_RELEASE_QUEUE = "ware.release.queue";
    //秒杀服务的订单队列
    public final static String SECKILL_ORDER_QUEUE = "seckill.order.queue";
    //订单自动解锁时间
    public static int delayTime = 1000 * 60 * 1;
    //订单创建时发给消息处理交换机的路由键
    public static String orderDelayRouteKey = "order.create.order";
    //订单由延时队列发出的路由键，用于自动取消订单
    public static String orderReleaseRouteKey = "order.release.order";

    public static String orderReleaseStockRouteKey = "order.release.other.#";

    public static String seckillOrderRouteKey = "order.seckill.order";


    public void initRabbitTemplate(){
        //要执行回调函数之前，必须开启该功能
//        publisher-returns: true
        //ConfirmCallback>>用于监听从provider发送消息给exchange，当消息发送到exchange时便会执行该回调函数
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback()
        {
            /**
             * Confirmation callback.
             * @param correlationData correlation data for the callback. 回调参数
             * @param b true for ack, false for nack exchange是否正常接受到消息
             * @param s An optional cause, for nack, when available, otherwise null. 如果未能正常接受到信息，失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {
                if (correlationData != null)
                    System.out.println("消息：" + new String(correlationData.getReturnedMessage().getBody()));
                if(b){
                    System.out.println("exchange正常接收到信息" + correlationData);
                }else {
                    System.out.println("exchange未能正常接收到信息，原因：" + s);
                }
            }
        });

        //ReturnCallback用于监听从exchange发送消息给queue，当消息发送到exchange失败时便会执行该回调函数，成功时该函数不会执行
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback()
        {
            /**
             * Returned message callback.
             * @param message the returned message.
             * @param replyCode the reply code.
             * @param replyText the reply text.
             * @param exchange the exchange.
             * @param routingKey the routing key.
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("消息：" + new String(message.getBody()));
                System.out.println("replyCode:" + replyCode + ",replyText:" + replyText + ",exchange" + exchange + ",routingKey" + routingKey);
            }
        });
    }

    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange(ORDER_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderDelayQueue() {
        HashMap<String, Object> arguments = new HashMap<>();
        //死信交换机 >>>> 在该服务中，死信交换机和处理交换机是同一个，通过不同的路由将信息发给不同的队列，从而节省了一个交换机
        arguments.put("x-dead-letter-exchange", ORDER_EVENT_EXCHANGE);
        //死信路由键
        arguments.put("x-dead-letter-routing-key", orderReleaseRouteKey);
        arguments.put("x-message-ttl", delayTime); // 消息过期时间 1分钟
        return new Queue(ORDER_DELAY_QUEUE, true, false, false, arguments);
    }

    @Bean
    public Queue orderReleaseQueue() {
        return new Queue(ORDER_RELEASE_QUEUE, true, false, false);
    }

    @Bean
    public Queue seckillOrderQueue(){
        return new Queue(SECKILL_ORDER_QUEUE, true, false, true);
    }

    //绑定订单交换机和延时队列
    @Bean
    public Binding bindExchangeToDelayQueue() {

        return new Binding(ORDER_DELAY_QUEUE, Binding.DestinationType.QUEUE,
                ORDER_EVENT_EXCHANGE, orderDelayRouteKey, null);
    }

    //绑定订单交换机和释放订单队列
    @Bean
    public Binding bindExchangeToReleaseQueue() {
        return new Binding(ORDER_RELEASE_QUEUE, Binding.DestinationType.QUEUE,
                ORDER_EVENT_EXCHANGE, orderReleaseRouteKey, null);
    }

    //绑定订单交换机和释放库存队列
    @Bean
    public Binding bindExchangeToReleaseStockQueue() {
        return new Binding(WARE_RELEASE_QUEUE, Binding.DestinationType.QUEUE,
                ORDER_EVENT_EXCHANGE, orderReleaseStockRouteKey, null);
    }

    @Bean
    public Binding bindExchangeToSeckillOrderQueue(){
        return new Binding(SECKILL_ORDER_QUEUE, Binding.DestinationType.QUEUE, ORDER_EVENT_EXCHANGE, seckillOrderRouteKey, null);
    }




    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
