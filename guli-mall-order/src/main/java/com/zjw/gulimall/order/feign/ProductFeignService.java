package com.zjw.gulimall.order.feign;

import com.zjw.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-25 21:18
 * @Modifier:
 */
@FeignClient("guli-mall-product-service")
public interface ProductFeignService
{
    @GetMapping("/product/skuinfo/listPriceByIds")
    public R listPriceByIds(@RequestParam("idList") List<Long> idList);

    @GetMapping("/product/spuinfo/getSpuInfoBySkuId")
    public R getSpuInfoBySkuId(@RequestParam("skuId") Long skuId);
}
