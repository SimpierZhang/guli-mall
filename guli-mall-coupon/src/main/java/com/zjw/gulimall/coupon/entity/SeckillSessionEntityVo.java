package com.zjw.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-31 22:15
 * @Modifier:
 */
@Data
public class SeckillSessionEntityVo
{
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private String startTime;
    /**
     * 每日结束时间
     */
    private String endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private String createTime;

}
