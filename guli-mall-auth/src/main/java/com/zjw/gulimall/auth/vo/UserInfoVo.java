package com.zjw.gulimall.auth.vo;

import lombok.Data;
import lombok.ToString;
import org.aspectj.weaver.ast.Or;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-22 20:50
 * @Modifier:
 */
@ToString
@Data
public class UserInfoVo
{
    @NotEmpty(message = "账户名不能为空")
    @Length(min = 3,max = 20,message = "用户名长度必须在3-20之间")
    private String userName;

    @NotEmpty(message = "密码不能为空")
    @Length(min = 6,max = 20,message = "用户名长度必须在6-20之间")
    private String password;

    @Pattern(regexp = "^1([3-9])[0-9]{9}$", message = "手机号格式错误，请重新输入")
    private String phone;

    @NotEmpty(message = "请输入正确的验证码")
    private String code;

}
