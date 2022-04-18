package com.zjw.gulimall.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-22 15:08
 * @Modifier:
 */
@Configuration
public class MyThreadConfig
{
    //线程池
    @Bean
    public ExecutorService getExecutorService(ThreadPoolParamsConfig paramsConfig){
        //为了让参数可以从配置文件中读取，可以再写一个参数配置类
        ThreadPoolExecutor executor = new ThreadPoolExecutor(paramsConfig.getCorePoolSize(), paramsConfig.getMaxPoolSize(), paramsConfig.getKeepAliveTime(), TimeUnit.SECONDS,
                new LinkedBlockingDeque<Runnable>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }
}
