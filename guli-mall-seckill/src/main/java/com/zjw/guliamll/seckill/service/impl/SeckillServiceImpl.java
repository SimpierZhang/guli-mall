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
            //1.????????????coupon?????????????????????????????????????????????id
            if (r.getCode() != 0) return;
            List<SeckillSessionEntityVo> entityVoList = r.getData(new TypeReference<List<SeckillSessionEntityVo>>()
            {
            });
            //1.??????????????????????????????id?????????redis???
            if (entityVoList == null || entityVoList.size() <= 0) return;
            for (SeckillSessionEntityVo item : entityVoList) {
                String key = REDIS_SECKILL_SESSIONS_PREFIX + item.getStartTime().getTime() + "_" + item.getEndTime().getTime();
                if (!redisTemplate.hasKey(key)) {
                    List<String> skuIdList = item.getRelationSkus().stream().filter(Objects::nonNull).map(relation -> item.getId() + "_" + relation.getSkuId()).collect(Collectors.toList());
                    redisTemplate.opsForList().leftPushAll(key, skuIdList);
                    redisTemplate.expire(key, item.getEndTime().getTime() - item.getStartTime().getTime(), TimeUnit.MILLISECONDS);
                }
            }
            //2.??????????????????????????????redis???
            saveSecSkuToRedis(entityVoList);
        }
        catch (Exception e) {
            log.error("????????????????????????");
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
    }

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //1.????????????????????????
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(REDIS_SECKILL_SKUS_KEY);
        List<SeckillSkuRedisTo> skuRedisToList = null;
        Set<String> keys = redisTemplate.keys(REDIS_SECKILL_SESSIONS_PREFIX + "*");
        if (keys == null || keys.size() <= 0) return null;
        long now = new Date().getTime();
        //?????????????????????????????????????????????key
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
            //????????????????????????
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
        //??????????????????????????????????????????????????????
        Set<String> keys = hashOps.keys();
        //?????????????????????????????????????????????
        if (keys == null || keys.size() <= 0) return null;
        long now = new Date().getTime();
        //redis??????????????????1_13??????
        //?????????????????????????????????????????????????????????
        //????????????????????????????????????????????????????????????????????????????????????
        String minKey = null;
        String pattern = "\\d_" + skuId;
        long minStartTime = Long.MAX_VALUE;
        for (String key : keys) {
            if (key.matches(pattern)) {
                //?????????????????????????????????????????????????????????
                String skuInfoStr = hashOps.get(key);
                SeckillSkuRedisTo redisTo = JSON.parseObject(skuInfoStr, new TypeReference<SeckillSkuRedisTo>()
                {
                });
                if (redisTo != null) {
                    //????????????????????????????????????????????????
                    Long startTime = redisTo.getStartTime();
                    Long endTime = redisTo.getEndTime();
                    if (now >= startTime && now <= endTime) {
                        return redisTo;
                    }
                    else {
                        //???????????????????????????
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
            //??????????????????????????????????????????????????????????????????????????????????????????
            redisTo.setRandomCode(null);
        return redisTo;
    }

    @Override
    public BizCodeEnum ensureSeckillAbility(String killId, String key, int num) {
        //1.????????????????????????
        MemberInfoTo memberInfoTo = SeckillInterceptor.memberInfo.get();
        if(memberInfoTo == null) return BizCodeEnum.USER_NOT_LOGIN_EXCEPTION;
        //2.?????????????????????????????????????????????
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(REDIS_SECKILL_SKUS_KEY);
        String skuInfoStr = hashOps.get(killId);
        if(skuInfoStr == null) return BizCodeEnum.SECKILL_END_EXCEPTION;
        SeckillSkuRedisTo redisTo = JSON.parseObject(skuInfoStr, new TypeReference<SeckillSkuRedisTo>(){});
        long startTime = redisTo.getStartTime();
        long endTime = redisTo.getEndTime();
        long now = new Date().getTime();
        if(now < startTime) return BizCodeEnum.SECKILL_NOT_START_EXCEPTION;
        if(now > endTime) return BizCodeEnum.SECKILL_END_EXCEPTION;
        //3.??????????????????????????????????????????
        //3.1 ???????????????????????????????????????????????????????????????????????????
        String memberBuyedKey = REDIS_SECKILL_BUYED_PREFIX + memberInfoTo.getId() + ":" + killId;
        String buyedCount = redisTemplate.opsForValue().get(memberBuyedKey);
        int limitCount = redisTo.getSeckillLimit().intValue();
        //??????????????????????????????????????????????????????????????????????????????????????????????????????
        if(buyedCount != null && Integer.parseInt(buyedCount) >= limitCount) return BizCodeEnum.SECKILL_ALREADY_BUYED_LIMIT_EXCEPTION;
        if(buyedCount != null && Integer.parseInt(buyedCount) + num > limitCount) return BizCodeEnum.SECKILL_BUYED_LIMIT_EXCEPTION;
        //3.1 ??????????????????
        String stockKey = REDIS_SECKILL_STOCK_PREFIX + key;
        String seckillStock = redisTemplate.opsForValue().get(stockKey);
        if(seckillStock == null || num > Integer.parseInt(seckillStock)) return BizCodeEnum.SECKILL_LACK_STOCK_EXCEPTION;
        return BizCodeEnum.SECKILL_ABILITY_SUCCESS;
    }

    @Override
    public String secKill(String killId, String key, int num) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(REDIS_SECKILL_SKUS_KEY);
        MemberInfoTo memberInfoTo = SeckillInterceptor.memberInfo.get();
        //1.???????????????
        String orderSn = UUID.randomUUID().toString().replace("-", "");
        //2.????????????
        RSemaphore semaphore = redissonClient.getSemaphore(REDIS_SECKILL_STOCK_PREFIX + key);
        semaphore.release(num);
        //3.???????????????????????????
        String memberBuyedKey = REDIS_SECKILL_BUYED_PREFIX + memberInfoTo.getId() + ":" + killId;
        String buyedCount = redisTemplate.opsForValue().get(memberBuyedKey);
        if(buyedCount == null){
            redisTemplate.opsForValue().set(memberBuyedKey, num + "");
        }else {
            redisTemplate.opsForValue().set(memberBuyedKey, Integer.parseInt(buyedCount) + num + "");
        }
        //2.???????????????seckillOrderQueue???????????????????????????????????????????????????????????????
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
            //????????????????????????????????????????????????
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
                //??????skuId???????????????
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
            // 5.????????????????????????????????????  ??????
            RSemaphore semaphore =
                    redissonClient.getSemaphore(stockKey);//"seckill:stock:";
            // ?????????????????????????????????
            semaphore.trySetPermits(redisTo.getSeckillCount().intValue());
            redisTemplate.expire(stockKey, redisTo.getEndTime() - redisTo.getStartTime(), TimeUnit.MILLISECONDS);
        }
        return redisTo;
    }
}
