package com.zjw.guliamll.seckill.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zjw.common.to.SeckillSkuRedisTo;
import com.zjw.common.to.SkuInfoEntityTo;
import com.zjw.common.utils.R;
import com.zjw.guliamll.seckill.feign.CouponFeignService;
import com.zjw.guliamll.seckill.feign.ProductFeignService;
import com.zjw.guliamll.seckill.service.SeckillService;
import com.zjw.guliamll.seckill.vo.SeckillSessionEntityVo;
import com.zjw.guliamll.seckill.vo.SeckillSkuRelationEntityVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicDouble;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: Zjw
 * @Description: 秒杀服务的定时任务
 * @Create 2021-08-30 23:28
 * @Modifier:
 */
@Slf4j
@EnableAsync
@EnableScheduling
@Component
public class SeckillSkuJob
{

    @Resource
    private CouponFeignService couponFeignService;
    @Resource
    private SeckillService seckillService;

    //每天晚上十二点将当天及未来两天要进行秒杀活动商品进行上架（加入redis中）
    @Scheduled(cron = "0 27 * * * ?")
    @Async
    public void uploadSkuToSecKill() {
        System.out.println("定时上架商品");
        //由于是定时任务，每天都会上架三天的商品，为了避免重复上架，采用分布式锁（之所以不直接判断redis中是否有相应key
        // 是因为分布式下可能有多台机器都执行该定时任务，所以要用分布式锁）做幂等性处理
        R r = couponFeignService.getSessionsInfoByTime(3);
        if(r.getCode() == 0){
            seckillService.uploadSeckillInfoToRedis(r);
        }
    }


}
