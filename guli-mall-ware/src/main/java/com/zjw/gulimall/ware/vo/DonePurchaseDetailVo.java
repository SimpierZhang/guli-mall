package com.zjw.gulimall.ware.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Zjw
 * @Description: 采购采购需求详情
 * @Create 2021-08-12 23:08
 * @Modifier:
 */
@Data
public class DonePurchaseDetailVo implements Serializable
{
    private Long itemId;
    private int status;
    private String reason;


}
