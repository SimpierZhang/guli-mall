package com.zjw.gulimall.order.feign.impl;

import com.zjw.common.utils.R;
import com.zjw.gulimall.order.feign.CartFeignService;
import org.springframework.stereotype.Component;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-09-11 15:30
 * @Modifier:
 */
@Component
public class CartFeignFallBackImpl implements CartFeignService
{
    @Override
    public R getLoginCartItems() {
        return R.error(200, "error");
    }
}
