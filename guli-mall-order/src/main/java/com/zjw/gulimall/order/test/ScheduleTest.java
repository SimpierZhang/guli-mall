package com.zjw.gulimall.order.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Zjw
 * @Description: 定时任务测试类
 * @Create 2021-08-30 22:09
 * @Modifier:
 */
//@Component
@Slf4j
//@EnableScheduling
//@EnableAsync //表示支持多线程的定时任务
public class ScheduleTest
{
    //每秒打印一次
    @Scheduled(cron = "* * * * * ?")
    public void hello(){
        try {
            log.info("hello schedule!!!" + Thread.currentThread().getName());
            //@Scheduled是堵塞式的，上一个任务未执行完毕，下一个定时任务会一直等待
            TimeUnit.SECONDS.sleep(3);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //每秒打印一次
    @Async
    @Scheduled(cron = "* * * * * ?")
    public void hello3(){
        try {
            //要想并发执行异步定时任务，需要手动配置定时任务线程池
            log.info("hello schedule!!!>>非堵塞" + Thread.currentThread().getName());
            TimeUnit.SECONDS.sleep(3);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //每周五的下午五点打印一次
    @Scheduled(cron = "0 0 17 * * 5")
    public void hello2(){
        log.info("现在是星期五的下午");
    }
}
