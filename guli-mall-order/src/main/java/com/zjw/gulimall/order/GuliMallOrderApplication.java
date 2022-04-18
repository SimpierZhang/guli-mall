package com.zjw.gulimall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
@MapperScan("com.zjw.gulimall.order.dao")
public class GuliMallOrderApplication {

	public static void main(String[] args) {
		SpringApplication.run(GuliMallOrderApplication.class, args);
	}

}
