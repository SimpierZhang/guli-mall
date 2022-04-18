package com.zjw.common.to;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-10 23:55
 * @Modifier:
 */
@Data
public class SkuLadderEntityTo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
    /**
     * spu_id
     */
    private Long skuId;
    /**
     * 满几件
     */
    private Integer fullCount;
    /**
     * 打几折
     */
    private BigDecimal discount;
    /**
     * 折后价
     */
    private BigDecimal price;
    /**
     * 是否叠加其他优惠[0-不可叠加，1-可叠加]
     */
    private Integer addOther;
}
