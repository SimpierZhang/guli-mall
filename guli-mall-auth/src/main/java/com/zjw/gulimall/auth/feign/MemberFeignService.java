package com.zjw.gulimall.auth.feign;

import com.zjw.common.utils.R;
import com.zjw.gulimall.auth.vo.UserInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-22 21:26
 * @Modifier:
 */
@FeignClient(value = "guli-mall-member-service")
public interface MemberFeignService
{
    @ResponseBody
    @PostMapping("/member/member/register")
    public R memberRegister(@RequestBody UserInfoVo userInfoVo);

    @ResponseBody
    @PostMapping("/member/member/login")
    public R login(@RequestBody UserInfoVo userInfoVo);
}
