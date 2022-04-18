package com.zjw.guliamll.seckill.feign;

import com.zjw.common.utils.R;
import com.zjw.guliamll.seckill.vo.SeckillSessionEntityVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-30 23:40
 * @Modifier:
 */
@FeignClient("guli-mall-coupon-service")
public interface CouponFeignService
{

    @GetMapping("/coupon/seckillsession/getSessionsInfoByTime")
    R getSessionsInfoByTime(@RequestParam("duration") int duration);
}
