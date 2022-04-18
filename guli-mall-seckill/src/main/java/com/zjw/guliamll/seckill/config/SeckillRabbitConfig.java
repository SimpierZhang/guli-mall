package com.zjw.guliamll.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * @Author: Zjw
 * @Description: order服务的消息队列配置，用于自动关单和解锁库存
 * @Create 2021-08-28 15:30
 * @Modifier:
 */
@Configuration
public class SeckillRabbitConfig
{
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

}
