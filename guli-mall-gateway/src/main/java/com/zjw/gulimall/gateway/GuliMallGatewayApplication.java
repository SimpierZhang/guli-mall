package com.zjw.gulimall.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-02 23:01
 * @Modifier:
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GuliMallGatewayApplication
{

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(GuliMallGatewayApplication.class, args);
    }
}
