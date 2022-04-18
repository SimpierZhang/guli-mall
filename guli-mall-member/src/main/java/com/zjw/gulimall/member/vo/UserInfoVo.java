package com.zjw.gulimall.member.vo;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-22 21:06
 * @Modifier:
 */
@Data
public class UserInfoVo
{

    private String userName;

    private String password;

    private String phone;

    private String code;

}
