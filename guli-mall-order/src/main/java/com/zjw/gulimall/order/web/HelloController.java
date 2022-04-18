package com.zjw.gulimall.order.web;

import com.zjw.gulimall.order.entity.OrderEntity;
import com.zjw.gulimall.order.entity.OrderReturnApplyEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-28 12:24
 * @Modifier:
 */
@RestController
public class HelloController
{
    @Resource
    private RabbitTemplate rabbitTemplate;

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        return "hello ding";
    }
}
