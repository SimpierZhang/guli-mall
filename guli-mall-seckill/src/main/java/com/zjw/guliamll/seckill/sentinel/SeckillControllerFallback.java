package com.zjw.guliamll.seckill.sentinel;

import com.zjw.common.utils.R;
import org.springframework.stereotype.Component;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-09-11 15:48
 * @Modifier:
 */
public class SeckillControllerFallback
{
    public R currentSeckillSkusBlockHandler() {
        return R.error(200, "被降级了");
    }

    public R currentSeckillSkusFallback() {
        return R.error(201, "系统内部异常");
    }
}
