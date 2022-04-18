package com.zjw.gulimall.auth.service;

import com.zjw.common.utils.R;
import com.zjw.gulimall.auth.vo.UserInfoVo;

/**
 * @Author: Zjw
 * @Description: 验证服务
 * @Create 2021-08-22 20:56
 * @Modifier:
 */
public interface AuthService
{
    R register(UserInfoVo userInfoVo);

    R login(UserInfoVo userInfoVo);

    void logout();
}
