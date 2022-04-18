package com.zjw.gulimall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableRabbit
@EnableDiscoveryClient
@EnableFeignClients("com.zjw.gulimall.ware.feign")
@SpringBootApplication
@MapperScan("com.zjw.gulimall.ware.dao")
public class GuliMallWareApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(GuliMallWareApplication.class, args);
    }

}
