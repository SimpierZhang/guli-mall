package com.zjw.gulimall.cart.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: Zjw
 * @Description: 购物车
 * @Create 2021-08-23 22:49
 * @Modifier:
 */
public class Cart
{
    private List<CartItem> items;

    /*** 商品的数量*/
    private Integer countNum;
    /*** 商品的类型数量*/
    private Integer countType;

    /*** 整个购物车的总价*/
    private BigDecimal totalAmount;

    /*** 减免的价格*/
    private BigDecimal reduce = new BigDecimal("0.00");

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int sum = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                sum += item.getCount();
            }
        }
        return sum;
    }


    public Integer getCountType() {
        return items == null ? 0 : items.size();
    }


    public BigDecimal getTotalAmount() {
        BigDecimal totalAmount = new BigDecimal(0);
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                if(item.getCheck()){
                    //只要计算被选中的商品
                    totalAmount = totalAmount.add(item.getTotalPrice());
                }
            }
        }
        return totalAmount.subtract(this.reduce);
    }


    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
