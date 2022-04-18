package com.zjw.gulimall.cart.config;

import com.zjw.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-23 23:24
 * @Modifier:
 */
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer
{
    //将自定义拦截器加入mvc中
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
    }
}
