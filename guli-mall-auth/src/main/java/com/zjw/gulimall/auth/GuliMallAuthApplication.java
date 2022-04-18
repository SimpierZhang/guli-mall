package com.zjw.gulimall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

//创建了一个springSessionRepositoryFilter ，负责将原生HttpSession 替换为Spring Session的实现
@EnableRedisHttpSession
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class GuliMallAuthApplication
{

    public static void main(String[] args) {
        SpringApplication.run(GuliMallAuthApplication.class, args);
    }

}
