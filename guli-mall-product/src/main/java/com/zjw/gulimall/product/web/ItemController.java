package com.zjw.gulimall.product.web;

import com.zjw.gulimall.product.service.SkuInfoService;
import com.zjw.gulimall.product.vo.SkuItemVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-22 11:36
 * @Modifier:
 */
@Controller
public class ItemController
{
    @Resource
    private SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String getItemBySkuId(@PathVariable("skuId") Long skuId, Model model){
        //根据skuId返回详情
        SkuItemVo item = skuInfoService.getSkuItemBySkuId(skuId);
        model.addAttribute("item", item);
        return "item";
    }
}
