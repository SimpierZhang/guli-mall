package com.zjw.gulimall.auth.feign;

import com.zjw.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-22 22:14
 * @Modifier:
 */
@FeignClient("guli-mall-thirdparty-service")
public interface OssFeignService
{
    @ResponseBody
    @GetMapping("/thirdparty/oss/sms/sendCode")
    public R sendVerifyCode(@RequestParam(value = "phone") String phone);
}
