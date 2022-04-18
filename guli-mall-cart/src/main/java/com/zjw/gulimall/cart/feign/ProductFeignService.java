package com.zjw.gulimall.cart.feign;

import com.zjw.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-24 20:13
 * @Modifier:
 */
@FeignClient("guli-mall-product-service")
public interface ProductFeignService
{

    @RequestMapping("/product/skuinfo/info/{skuId}")
    //@RequiresPermissions("product:skuinfo:info")
    public R getSkuInfoById(@PathVariable("skuId") Long skuId);

    @ResponseBody
    @GetMapping("/product/skusaleattrvalue/getSaleAttrBySkuId")
    public R getSaleAttrBySkuId(@RequestParam("skuId")Long skuId);
}
