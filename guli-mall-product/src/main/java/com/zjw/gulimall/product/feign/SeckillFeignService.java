package com.zjw.gulimall.product.feign;

import com.zjw.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-31 23:52
 * @Modifier:
 */
@FeignClient("guli-mall-seckill-service")
public interface SeckillFeignService
{

    @GetMapping("/getSeckillInfoBySkuId/{skuId}")
    public R getSeckillInfoBySkuId(@PathVariable("skuId") Long skuId);
}
