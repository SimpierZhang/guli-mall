package com.zjw.guliamll.seckill.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-22 15:11
 * @Modifier:
 */
@Component
@ConfigurationProperties(prefix = "gulimall.threadpool")
@Data
public class ThreadPoolParamsConfig
{
    private int corePoolSize = 20;
    private int maxPoolSize = 50;
    private long keepAliveTime = 10;
}
