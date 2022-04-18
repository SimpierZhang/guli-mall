package com.zjw.gulimall.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-23 21:38
 * @Modifier:
 */
@Configuration
public class GuliMallSessionConfig
{
    @Bean // redis的json序列化
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

    @Bean // cookie
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("GULISESSIONID"); // cookie的键
        serializer.setDomainName("gulimall.com"); // 扩大session作用域，也就是cookie的有效域
        return serializer;
    }
}
