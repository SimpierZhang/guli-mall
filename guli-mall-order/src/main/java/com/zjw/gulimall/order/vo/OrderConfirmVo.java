package com.zjw.gulimall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-25 20:22
 * @Modifier:
 */
public class OrderConfirmVo
{
    @Setter @Getter
    List<MemberAddressVo> address;

    @Getter @Setter
    /** 所有选中的购物项 **/
    private List<OrderItemVo> items;

    /** 发票记录 **/
    @Getter @Setter
    /** 优惠券（会员积分） **/
    private Integer integration;

    /** 防止重复提交的令牌 **/
    @Getter @Setter
    private String orderToken;

    @Getter @Setter
    Map<Long,Boolean> stocks;

    //只要有get方法，就可以获取到属性值
    public Integer getCount() { // 总件数
        int count = 0;
        if(items != null){
            for(OrderItemVo item : items){
                count += item.getCount();
            }
        }
        return count;
    }


    /** 计算订单总额**/
    //BigDecimal total;
    public BigDecimal getTotal() {
        BigDecimal totalPrice = BigDecimal.ZERO;
        if(items != null){
            for(OrderItemVo item : items){
                totalPrice = totalPrice.add(item.getTotalPrice());
            }
        }
        return totalPrice;
    }


    /** 应付价格 **/
    //BigDecimal payPrice;
    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
