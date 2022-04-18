package com.zjw.gulimall;

import lombok.Data;
import lombok.ToString;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-15 11:07
 * @Modifier:
 */
@Data
@ToString
public class Account
{

    private int account_number;
    private int balance;
    private String firstname;
    private String lastname;
    private int age;
    private String gender;
    private String address;
    private String employer;
    private String email;
    private String city;
    private String state;
}
