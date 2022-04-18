package com.zjw.gulimall.member.feign;

import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-29 0:49
 * @Modifier:
 */
@FeignClient("guli-mall-order-service")
public interface OrderFeignService
{

    @ResponseBody
    @PostMapping("/listOrderPage")
    R listOrderPage(Map<String, Object> params);
}
