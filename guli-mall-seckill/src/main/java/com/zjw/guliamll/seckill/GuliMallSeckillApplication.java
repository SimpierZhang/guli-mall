package com.zjw.guliamll.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GuliMallSeckillApplication
{

    public static void main(String[] args) {
        SpringApplication.run(GuliMallSeckillApplication.class, args);
    }

}
