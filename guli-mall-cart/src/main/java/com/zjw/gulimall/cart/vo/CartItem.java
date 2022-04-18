package com.zjw.gulimall.cart.vo;

import com.zjw.common.to.SkuEsModel;
import com.zjw.common.to.SkuInfoEntityTo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author: Zjw
 * @Description: 购物车商品项
 * @Create 2021-08-23 22:49
 * @Modifier:
 */
public class CartItem
{
    private Long skuId;

    /*** 是否被选中*/
    private Boolean check = true;

    private String title;
    private String image;

    private List<String> skuAttr;

    /*** 价格*/
    private BigDecimal price;
    /*** 数量*/
    private Integer count;
    //总价=价格*数量-折扣
    private BigDecimal totalPrice;
    //折扣
    private BigDecimal disCount = new BigDecimal(0);

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Boolean getCheck() {
        return check;
    }

    public void setCheck(Boolean check) {
        this.check = check;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getSkuAttr() {
        return skuAttr;
    }

    public void setSkuAttr(List<String> skuAttr) {
        this.skuAttr = skuAttr;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getTotalPrice() {
        return  this.price.multiply(new BigDecimal(this.count)).subtract(this.disCount);
    }


    public BigDecimal getDisCount() {
        return disCount;
    }

    public void setDisCount(BigDecimal disCount) {
        this.disCount = disCount;
    }
}
