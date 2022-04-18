package com.zjw.gulimall.auth.service.impl;

import com.zjw.common.constant.BizCodeEnum;
import com.zjw.common.constant.CommonConstant;
import com.zjw.common.utils.R;
import com.zjw.gulimall.auth.feign.MemberFeignService;
import com.zjw.gulimall.auth.service.AuthService;
import com.zjw.gulimall.auth.vo.UserInfoVo;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.validation.constraints.NotEmpty;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-22 20:56
 * @Modifier:
 */
@Service
public class AuthServiceImpl implements AuthService
{

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    @Resource
    private MemberFeignService memberFeignService;

    @Override
    public R register(UserInfoVo userInfoVo) {
        //1.校验验证码
        String verifyCode = userInfoVo.getCode();
        String phone = userInfoVo.getPhone();
        String verifyKey = CommonConstant.VERIFY_CODE_PREFIX + phone;
        String redisVerifyValue = redisTemplate.opsForValue().get(verifyKey);
        if (redisVerifyValue == null) return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), "验证码错误，请重试");
        String redisVerifyCode = redisVerifyValue.split("-")[0];
        if(!redisVerifyCode.equals(verifyCode)) return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), "验证码错误，请重试");
        //如果验证码正确，那么就要清楚redis中的验证码
        redisTemplate.delete(verifyKey);
        //远程调用member服务进行注册
        R r = memberFeignService.memberRegister(userInfoVo);
        return r;
    }

    @Override
    public R login(UserInfoVo userInfoVo) {
        return memberFeignService.login(userInfoVo);
    }

    @Override
    public void logout() {

    }
}
