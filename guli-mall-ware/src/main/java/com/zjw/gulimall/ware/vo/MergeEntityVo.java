package com.zjw.gulimall.ware.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-12 19:41
 * @Modifier:
 */
@Data
public class MergeEntityVo implements Serializable
{
    private Long purchaseId;
    private List<Long> items;
}
