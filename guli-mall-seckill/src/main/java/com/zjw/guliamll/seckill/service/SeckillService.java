package com.zjw.guliamll.seckill.service;

import com.zjw.common.constant.BizCodeEnum;
import com.zjw.common.to.SeckillSkuRedisTo;
import com.zjw.common.utils.R;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-31 20:52
 * @Modifier:
 */
@Service
public interface SeckillService
{
    public void uploadSeckillInfoToRedis(R r);

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSeckillInfoBySkuId(Long skuId);

    //判断商品是否可以被秒杀
    BizCodeEnum ensureSeckillAbility (String killId, String key, int num);

    //真正的进行秒杀下单
    String secKill(String killId, String key, int num);
}
