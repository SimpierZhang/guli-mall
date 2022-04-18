package com.zjw.gulimall.order.config;

import com.zjw.gulimall.order.interceptor.OrderInterceptor;
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
    private OrderInterceptor orderInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(orderInterceptor).excludePathPatterns("/order/order/getOrderInfoByOrderSn", "/payListener");
    }
}
