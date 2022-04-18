package com.zjw.gulimall.product.feign;

import com.zjw.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-15 16:26
 * @Modifier:
 */
@FeignClient("guli-mall-ware-service")
public interface WareFeignService
{
    @PostMapping("/ware/waresku/list/stock")
    public R listSkuStock(@RequestBody List<Long> skuIdList);
}
