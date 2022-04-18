package com.zjw.gulimall.ware.feign;

import com.zjw.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-28 17:05
 * @Modifier:
 */
@FeignClient("guli-mall-order-service")
public interface OrderFeignService
{
    @GetMapping("/order/order/getOrderInfoByOrderSn")
    public R getOrderInfoByOrderSn(@RequestParam("orderSn") String orderSn);
}
