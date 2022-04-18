package com.zjw.gulimall.member.config;

import com.zjw.gulimall.member.interceptor.MemberInterceptor;
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
    private MemberInterceptor memberInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //放行登录和注册等请求
        registry.addInterceptor(memberInterceptor).
                excludePathPatterns("/member/**");
    }
}
