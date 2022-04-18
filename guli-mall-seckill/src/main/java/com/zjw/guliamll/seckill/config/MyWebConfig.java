package com.zjw.guliamll.seckill.config;

import com.zjw.guliamll.seckill.interceptor.SeckillInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-25 20:18
 * @Modifier:
 */
@Configuration
public class MyWebConfig implements WebMvcConfigurer
{
    @Resource
    private SeckillInterceptor seckillInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(seckillInterceptor).excludePathPatterns("/getSeckillInfoBySkuId/**", "/currentSeckillSkus");
    }
}
