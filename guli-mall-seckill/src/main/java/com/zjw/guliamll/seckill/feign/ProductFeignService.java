package com.zjw.guliamll.seckill.feign;

import com.zjw.common.to.SkuInfoEntityTo;
import com.zjw.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-31 0:43
 * @Modifier:
 */
@FeignClient("guli-mall-product-service")
public interface ProductFeignService
{

    //根据idList返回对应的数据
    @PostMapping("/product/skuinfo/listByIds")
    public R listSkuInfoByIds(@RequestBody List<Long> idList);
}
