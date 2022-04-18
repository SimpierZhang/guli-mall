package com.zjw.gulimall.product.feign;

import com.zjw.common.to.SkuEsModel;
import com.zjw.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-15 17:58
 * @Modifier:
 */
@FeignClient("guli-mall-search-service")
public interface SearchFeignService
{
    @PostMapping("/search/save/product")
    public R uploadProductInfoToEs(@RequestBody List<SkuEsModel> modelList);
}
