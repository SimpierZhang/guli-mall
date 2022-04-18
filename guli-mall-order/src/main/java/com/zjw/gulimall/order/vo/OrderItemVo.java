package com.zjw.gulimall.order.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-25 20:25
 * @Modifier:
 */
public class OrderItemVo
{
    @Getter
    @Setter
    private Long skuId;

    /*** 是否被选中*/
    @Getter
    @Setter
    private Boolean check = true;

    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String image;
    @Getter
    @Setter
    private List<String> skuAttr;

    /*** 价格*/
    @Getter
    @Setter
    private BigDecimal price;

    /*** 数量*/
    @Getter
    @Setter
    private Integer count;

    //总价=价格*数量-折扣
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private BigDecimal totalPrice;

    //折扣
    @Getter
    @Setter
    private BigDecimal disCount = new BigDecimal(0);


    public BigDecimal getTotalPrice() {
        if(this.price == null || this.count == null || this.disCount == null) return null;
        return  this.price.multiply(new BigDecimal(this.count)).subtract(this.disCount);
    }


}
