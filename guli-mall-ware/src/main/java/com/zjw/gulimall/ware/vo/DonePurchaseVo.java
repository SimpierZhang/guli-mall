package com.zjw.gulimall.ware.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: Zjw
 * @Description: 采购采购单详情
 * @Create 2021-08-12 23:06
 * @Modifier:
 */
@Data
public class DonePurchaseVo implements Serializable
{
    //采购单id
    private Long id;
    private List<DonePurchaseDetailVo> items;

}
