package com.zjw.common.to;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author: Zjw
 * @Description: 用于远程服务调用的to类，对应coupon中的SpuBoundsEntity
 * @Create 2021-08-10 23:47
 * @Modifier:
 */
@Data
public class SpuBoundsEntityTo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;
    /**
     *
     */
    private Long spuId;
    /**
     * 成长积分
     */
    private BigDecimal growBounds;
    /**
     * 购物积分
     */
    private BigDecimal buyBounds;
    /**
     * 优惠生效情况[1111（四个状态位，从右到左）;0 - 无优惠，成长积分是否赠送;1 - 无优惠，购物积分是否赠送;2 - 有优惠，成长积分是否赠送;3 - 有优惠，购物积分是否赠送【状态位0：不赠送，1：赠送】]
     */
    private Integer work;
}
