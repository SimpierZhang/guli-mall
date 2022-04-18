package com.zjw.gulimall.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

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

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);//我这里设置的线程数是2,可以根据需求调整
        return taskScheduler;
    }
}
