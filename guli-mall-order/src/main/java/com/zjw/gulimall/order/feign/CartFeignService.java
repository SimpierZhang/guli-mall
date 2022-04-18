package com.zjw.gulimall.order.feign;

import com.zjw.common.utils.R;
import com.zjw.gulimall.order.feign.impl.CartFeignFallBackImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-25 22:48
 * @Modifier:
 */
@FeignClient(value = "guli-mall-cart-service", fallback = CartFeignFallBackImpl.class)
public interface CartFeignService
{
    @ResponseBody
    @GetMapping(value = "/getLoginCartItems")
    public R getLoginCartItems();
}
