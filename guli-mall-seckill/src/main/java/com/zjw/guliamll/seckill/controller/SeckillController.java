package com.zjw.guliamll.seckill.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.zjw.common.constant.BizCodeEnum;
import com.zjw.common.to.SeckillSkuRedisTo;
import com.zjw.common.utils.R;
import com.zjw.guliamll.seckill.sentinel.SeckillControllerFallback;
import com.zjw.guliamll.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-31 23:07
 * @Modifier:
 */
@Slf4j
@Controller
public class SeckillController
{
    @Resource
    private SeckillService seckillService;

    //获取现在正在进行的秒杀场次，并显示其商品
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    @SentinelResource(fallback = "currentSeckillSkusFallback",
    blockHandler = "currentSeckillSkusBlockHandler")
    public R currentSeckillSkus() {
        List<SeckillSkuRedisTo> skuRedisToList = seckillService.getCurrentSeckillSkus();
        return R.ok().put("data", skuRedisToList);
    }

    public R currentSeckillSkusBlockHandler() {
        return R.error(200, "被降级了");
    }

    public R currentSeckillSkusFallback() {
        return R.error(201, "系统内部异常");
    }

    @GetMapping("/getSeckillInfoBySkuId/{skuId}")
    @ResponseBody
    public R getSeckillInfoBySkuId(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo redisTo = seckillService.getSeckillInfoBySkuId(skuId);
        if (redisTo == null) {
            return R.error(BizCodeEnum.NOT_STOCK_EXCEPTION.getCode(), BizCodeEnum.NOT_STOCK_EXCEPTION.getMsg());
        }
        return R.ok().put("data", redisTo);
    }

    //秒杀活动下单
    @GetMapping("/kill")
    public String kill(@RequestParam("killId") String killId, @RequestParam("key") String key, @RequestParam("num") int num, Model model) {
        long startTime = System.currentTimeMillis();
        BizCodeEnum seckillAbility = seckillService.ensureSeckillAbility(killId, key, num);
        if(!seckillAbility.equals(BizCodeEnum.SECKILL_ABILITY_SUCCESS)) return R.error(seckillAbility.getCode(), seckillAbility.getMsg()).toString();
        //此时才能进行秒杀
        String orderSn = seckillService.secKill(killId, key, num);
        log.info("秒杀耗时：{}", System.currentTimeMillis() - startTime);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }


}
