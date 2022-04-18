package com.zjw.gulimall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.zjw.gulimall.coupon.dao")
@SpringBootApplication
public class GuliMallCouponApplication {

	public static void main(String[] args) {
		SpringApplication.run(GuliMallCouponApplication.class, args);
	}

}
