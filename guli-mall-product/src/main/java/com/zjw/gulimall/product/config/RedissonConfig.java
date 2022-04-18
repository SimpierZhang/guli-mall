package com.zjw.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-18 23:35
 * @Modifier:
 */
@Configuration
public class RedissonConfig
{

    // redission通过redissonClient对象使用 // 如果是多个redis集群，可以配置
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson() {
        Config config = new Config();
        // 创建单例模式的配置
        config.useSingleServer().setAddress("redis://192.168.233.216:6379");
        return Redisson.create(config);
    }

    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }
}
