package com.zjw.guliamll.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zjw.common.constant.BizCodeEnum;
import com.zjw.common.to.MemberInfoTo;
import com.zjw.common.to.SeckillOrderTo;
import com.zjw.common.to.SeckillSkuRedisTo;
import com.zjw.common.to.SkuInfoEntityTo;
import com.zjw.common.utils.R;
import com.zjw.guliamll.seckill.feign.ProductFeignService;
import com.zjw.guliamll.seckill.interceptor.SeckillInterceptor;
import com.zjw.guliamll.seckill.service.SeckillService;
import com.zjw.guliamll.seckill.vo.SeckillSessionEntityVo;
import com.zjw.guliamll.seckill.vo.SeckillSkuRelationEntityVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-31 20:53
 * @Modifier:
 */
@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService
{
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Resource
    private ProductFeignService productFeignService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private RabbitTemplate rabbitTemplate;
    private static final String REDIS_SECKILL_SKUS_KEY = "seckill:skus";
    private static final String REDIS_SECKILL_SESSIONS_PREFIX = "seckill:sessions:";
    private static final String REDIS_SECKILL_STOCK_PREFIX = "seckill:stock:";
    private static final String REDIS_REDISSION_SECKILL_LOCK = "seckill.lock";
    private static final String REDIS_SECKILL_BUYED_PREFIX = "seckill:buyed:";

    public final static String ORDER_EVENT_EXCHANGE = "order.event.exchange";
    private static String seckillOrderRouteKey = "order.seckill.order";

    @Override
    public void uploadSeckillInfoToRedis(R r) {
        RLock lock = redissonClient.getLock(REDIS_REDISSION_SECKILL_LOCK);
        try {
            lock.lock();
            //1.远程调用coupon服务查出场次以及场次对应的商品id
            if (r.getCode() != 0) return;
            List<SeckillSessionEntityVo> entityVoList = r.getData(new TypeReference<List<SeckillSessionEntityVo>>()
            {
            });
            //1.保存场次及相关的商品id信息到redis中
            if (entityVoList == null || entityVoList.size() <= 0) return;
            for (SeckillSessionEntityVo item : entityVoList) {
                String key = REDIS_SECKILL_SESSIONS_PREFIX + item.getStartTime().getTime() + "_" + item.getEndTime().getTime();
                if (!redisTemplate.hasKey(key)) {
                    List<String> skuIdList = item.getRelationSkus().stream().filter(Objects::nonNull).map(relation -> item.getId() + "_" + relation.getSkuId()).collect(Collectors.toList());
                    redisTemplate.opsForList().leftPushAll(key, skuIdList);
                    redisTemplate.expire(key, item.getEndTime().getTime() - item.getStartTime().getTime(), TimeUnit.MILLISECONDS);
                }
            }
            //2.保存相关的商品信息到redis中
            saveSecSkuToRedis(entityVoList);
        }
        catch (Exception e) {
            log.error("上架秒杀商品失败");
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //1.查出所有场次信息
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(REDIS_SECKILL_SKUS_KEY);
        List<SeckillSkuRedisTo> skuRedisToList = null;
        Set<String> keys = redisTemplate.keys(REDIS_SECKILL_SESSIONS_PREFIX + "*");
        if (keys == null || keys.size() <= 0) return null;
        long now = new Date().getTime();
        //用于存储正在开启秒杀活动的商品key
        List<String> sessionList = new ArrayList<>();
        for (String key : keys) {
            String sessionDurationStr = key.replace(REDIS_SECKILL_SESSIONS_PREFIX, "");
            String[] timeArr = sessionDurationStr.split("_");
            if (now >= Long.parseLong(timeArr[0]) && now <= Long.parseLong(timeArr[1])) {
                List<String> range = redisTemplate.opsForList().range(key, 0, 100);
                if (range != null && range.size() > 0)
                    sessionList.addAll(range);
            }
        }
        if (sessionList.size() > 0) {
            //查出所有商品信息
            skuRedisToList = sessionList.stream().filter(Objects::nonNull).map(skuKey -> {
                String redisToStr = hashOps.get(skuKey);
                return JSON.parseObject(redisToStr, new TypeReference<SeckillSkuRedisTo>()
                {
                });
            }).collect(Collectors.toList());
        }
        return skuRedisToList;
    }

    @Override
    public SeckillSkuRedisTo getSeckillInfoBySkuId(Long skuId) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(REDIS_SECKILL_SKUS_KEY);
        //拿到所有参与秒杀活动的商品，无论场次
        Set<String> keys = hashOps.keys();
        //查看当前商品是否位于秒杀活动中
        if (keys == null || keys.size() <= 0) return null;
        long now = new Date().getTime();
        //redis中取出的值为1_13这种
        //如果该商品正处于秒杀中，就返回秒杀信息
        //如果该商品处于未来的多个秒杀场次中，就返回最近的秒杀信息
        String minKey = null;
        String pattern = "\\d_" + skuId;
        long minStartTime = Long.MAX_VALUE;
        for (String key : keys) {
            if (key.matches(pattern)) {
                //表示该商品会参加秒杀活动，只是场次未知
                String skuInfoStr = hashOps.get(key);
                SeckillSkuRedisTo redisTo = JSON.parseObject(skuInfoStr, new TypeReference<SeckillSkuRedisTo>()
                {
                });
                if (redisTo != null) {
                    //获取当前商品秒杀的开始和结束时间
                    Long startTime = redisTo.getStartTime();
                    Long endTime = redisTo.getEndTime();
                    if (now >= startTime && now <= endTime) {
                        return redisTo;
                    }
                    else {
                        //找到最近的秒杀场次
                        if (startTime < minStartTime) {
                            minKey = key;
                            minStartTime = startTime;
                        }
                    }
                }
            }
        }
        if (minKey == null) return null;
        SeckillSkuRedisTo redisTo = JSON.parseObject(hashOps.get(minKey), new TypeReference<SeckillSkuRedisTo>()
        {
        });
        if (redisTo != null)
            //由于不是当前秒杀场次，还是属于预约阶段，因此不需要返回随机码
            redisTo.setRandomCode(null);
        return redisTo;
    }

    @Override
    public BizCodeEnum ensureSeckillAbility(String killId, String key, int num) {
        //1.判断用户是否登录
        MemberInfoTo memberInfoTo = SeckillInterceptor.memberInfo.get();
        if(memberInfoTo == null) return BizCodeEnum.USER_NOT_LOGIN_EXCEPTION;
        //2.判断秒杀商品是否位于正确时间段
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(REDIS_SECKILL_SKUS_KEY);
        String skuInfoStr = hashOps.get(killId);
        if(skuInfoStr == null) return BizCodeEnum.SECKILL_END_EXCEPTION;
        SeckillSkuRedisTo redisTo = JSON.parseObject(skuInfoStr, new TypeReference<SeckillSkuRedisTo>(){});
        long startTime = redisTo.getStartTime();
        long endTime = redisTo.getEndTime();
        long now = new Date().getTime();
        if(now < startTime) return BizCodeEnum.SECKILL_NOT_START_EXCEPTION;
        if(now > endTime) return BizCodeEnum.SECKILL_END_EXCEPTION;
        //3.判断秒杀商品是否有足够的库存
        //3.1 判断用户是否已经参与过秒杀活动，是否超过了购买限制
        String memberBuyedKey = REDIS_SECKILL_BUYED_PREFIX + memberInfoTo.getId() + ":" + killId;
        String buyedCount = redisTemplate.opsForValue().get(memberBuyedKey);
        int limitCount = redisTo.getSeckillLimit().intValue();
        //只有当用户参与了该活动并且超出了购买限制之后，才不允许参与该秒杀活动
        if(buyedCount != null && Integer.parseInt(buyedCount) >= limitCount) return BizCodeEnum.SECKILL_ALREADY_BUYED_LIMIT_EXCEPTION;
        if(buyedCount != null && Integer.parseInt(buyedCount) + num > limitCount) return BizCodeEnum.SECKILL_BUYED_LIMIT_EXCEPTION;
        //3.1 获取秒杀库存
        String stockKey = REDIS_SECKILL_STOCK_PREFIX + key;
        String seckillStock = redisTemplate.opsForValue().get(stockKey);
        if(seckillStock == null || num > Integer.parseInt(seckillStock)) return BizCodeEnum.SECKILL_LACK_STOCK_EXCEPTION;
        return BizCodeEnum.SECKILL_ABILITY_SUCCESS;
    }

    @Override
    public String secKill(String killId, String key, int num) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(REDIS_SECKILL_SKUS_KEY);
        MemberInfoTo memberInfoTo = SeckillInterceptor.memberInfo.get();
        //1.创建订单号
        String orderSn = UUID.randomUUID().toString().replace("-", "");
        //2.扣减库存
        RSemaphore semaphore = redissonClient.getSemaphore(REDIS_SECKILL_STOCK_PREFIX + key);
        semaphore.release(num);
        //3.设置用户已购买数量
        String memberBuyedKey = REDIS_SECKILL_BUYED_PREFIX + memberInfoTo.getId() + ":" + killId;
        String buyedCount = redisTemplate.opsForValue().get(memberBuyedKey);
        if(buyedCount == null){
            redisTemplate.opsForValue().set(memberBuyedKey, num + "");
        }else {
            redisTemplate.opsForValue().set(memberBuyedKey, Integer.parseInt(buyedCount) + num + "");
        }
        //2.发送信息给seckillOrderQueue，让其慢慢进行订单加载，并同时进行削峰处理
        SeckillSkuRedisTo redisTo = JSON.parseObject(hashOps.get(killId), new TypeReference<SeckillSkuRedisTo>()
        {
        });
        SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
        seckillOrderTo.setOrderSn(orderSn);
        seckillOrderTo.setRealAmount(redisTo.getSeckillPrice().multiply(redisTo.getSeckillCount()));
        seckillOrderTo.setSkuId(redisTo.getSkuId());
        seckillOrderTo.setSkuQuantity(num);
        seckillOrderTo.setSkuInfoVo(redisTo.getSkuInfoVo());
        seckillOrderTo.setMemberId(memberInfoTo.getId());
        seckillOrderTo.setMemberName(memberInfoTo.getUsername());
        rabbitTemplate.convertAndSend(ORDER_EVENT_EXCHANGE, seckillOrderRouteKey, seckillOrderTo);
        return orderSn;
    }

    private void saveSecSkuToRedis(List<SeckillSessionEntityVo> entityVoList) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(REDIS_SECKILL_SKUS_KEY);
        if (entityVoList == null || entityVoList.size() <= 0) return;
        for (SeckillSessionEntityVo item : entityVoList) {
            //为场次中的每一件商品设置过期时间
            hashOps.expire(item.getEndTime().getTime() - new Date().getTime(), TimeUnit.MILLISECONDS);
            List<Long> skuIdList = item.getRelationSkus().stream().filter(Objects::nonNull).map(SeckillSkuRelationEntityVo::getSkuId).collect(Collectors.toList());
            R r = productFeignService.listSkuInfoByIds(skuIdList);
            if (r.getCode() != 0) continue;
            List<SkuInfoEntityTo> skuInfoList = r.getData(new TypeReference<List<SkuInfoEntityTo>>()
            {
            });
            Map<String, String> redisToModel = new HashMap<>();
            for (SkuInfoEntityTo skuInfo : skuInfoList) {
                String skuRedisKey = item.getId() + "_" + skuInfo.getSkuId();
                if (hashOps.hasKey(skuRedisKey)) continue;
                //找出skuId对应的场次
                SeckillSkuRelationEntityVo relationEntityVo = null;
                for (SeckillSkuRelationEntityVo relation : item.getRelationSkus()) {
                    if (relation.getSkuId().equals(skuInfo.getSkuId())) {
                        relationEntityVo = relation;
                        break;
                    }
                }
                if (relationEntityVo != null)
                    redisToModel.put(skuRedisKey, JSON.toJSONString(initTo(item, skuInfo, relationEntityVo)));
            }
            hashOps.putAll(redisToModel);
        }
    }

    private SeckillSkuRedisTo initTo(SeckillSessionEntityVo sessionEntityVo, SkuInfoEntityTo skuInfo, SeckillSkuRelationEntityVo skuRelation) {
        SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
        redisTo.setSkuId(skuInfo.getSkuId());
        redisTo.setSkuInfoVo(skuInfo);
        redisTo.setStartTime(sessionEntityVo.getStartTime().getTime());
        redisTo.setEndTime(sessionEntityVo.getEndTime().getTime());
        redisTo.setPromotionSessionId(sessionEntityVo.getId());
        redisTo.setRandomCode(UUID.randomUUID().toString().replace("-", ""));
        redisTo.setSeckillCount(skuRelation.getSeckillCount());
        redisTo.setSeckillLimit(skuRelation.getSeckillLimit());
        redisTo.setSeckillPrice(skuRelation.getSeckillPrice());
        redisTo.setSeckillSort(skuRelation.getSeckillSort());
        String stockKey = REDIS_SECKILL_STOCK_PREFIX + redisTo.getRandomCode();
        if (!redisTemplate.hasKey(stockKey)) {
            // 5.使用库存作为分布式信号量  限流
            RSemaphore semaphore =
                    redissonClient.getSemaphore(stockKey);//"seckill:stock:";
            // 在信号量中设置秒杀数量
            semaphore.trySetPermits(redisTo.getSeckillCount().intValue());
            redisTemplate.expire(stockKey, redisTo.getEndTime() - redisTo.getStartTime(), TimeUnit.MILLISECONDS);
        }
        return redisTo;
    }
}
