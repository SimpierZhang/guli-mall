package com.zjw.common.to;

import lombok.Data;
import lombok.ToString;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-23 23:27
 * @Modifier:
 */
@Data
@ToString
public class UserInfoTo
{
    /**
     * 存储已登录用户在数据库中的ID
     */
    private Long userId;

    /**
     * 存储用户名
     */
    private String username;

    /**
     * 配分一个临时的user-key
     */
    private String userKey;

    /**
     * 判断是否是临时用户
     */
    private boolean tempUser = true;
}
