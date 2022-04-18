package com.zjw.gulimall.ware.feign;

import com.zjw.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-12 18:29
 * @Modifier:
 */
@FeignClient("guli-mall-product-service")
public interface ProductFeignService
{
    @RequestMapping("/product/skuinfo/info/{skuId}")
    //@RequiresPermissions("product:skuinfo:info")
    public R getSkuInfo(@PathVariable("skuId") Long skuId);

    @PostMapping("/product/skuinfo/listByIds")
    public R getSkuInfoList(List<Long> skuIds);
}
