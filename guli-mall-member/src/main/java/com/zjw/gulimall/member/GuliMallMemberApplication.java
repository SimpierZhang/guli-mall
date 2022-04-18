package com.zjw.gulimall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@MapperScan("com.zjw.gulimall.member/dao")
public class GuliMallMemberApplication {

	public static void main(String[] args) {
		SpringApplication.run(GuliMallMemberApplication.class, args);
	}

}
