package com.zjw.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.zjw.gulimall.product.feign")
@MapperScan("com.zjw.gulimall.product.dao")
public class GuliMallProductApplication {

	public static void main(String[] args) {
		SpringApplication.run(GuliMallProductApplication.class, args);
	}

}
