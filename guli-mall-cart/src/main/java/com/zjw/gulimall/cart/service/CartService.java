package com.zjw.gulimall.cart.service;

import com.zjw.gulimall.cart.vo.Cart;
import com.zjw.gulimall.cart.vo.CartItem;

import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-23 22:35
 * @Modifier:
 */
public interface CartService
{
    void addToCart(Long skuId, int num);

    CartItem toSuccess(Long skuId);

    Cart getCart();

    public void mergeCartItem();

    //删除购物车中一项商品
    void deleteItem(Long skuId);

    //清空购物车
    boolean deleteCart();

    void countItem(Long skuId, int num);

    //购物项是否被选中
    void checkItem(Long skuId, boolean check);

    List<CartItem> getLoginCartItems();
}
