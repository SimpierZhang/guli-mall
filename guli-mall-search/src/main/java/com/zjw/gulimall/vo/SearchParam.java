package com.zjw.gulimall.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: Zjw
 * @Description: 检索参数，前端传来的参数用于全文检索
 * @Create 2021-08-21 16:21
 * @Modifier:
 * 前端检索字段
 * keyword=小米&
 * sort=saleCount_desc/asc&
 * hasStock=0/1&
 * skuPrice=400_1900&
 * brandId=1&
 * catalog3Id=1&
 * attrs=1_3G:4G:5G&
 * attrs=2_骁龙845&
 * attrs=4_高清屏
 */
@Data
public class SearchParam
{
    // 页面传递过来的全文匹配关键字
    private String keyword;

    /** 三级分类id*/
    private Long catalog3Id;
    //排序条件：sort=price/salecount/hotscore_desc/asc
    private String sort;
    // 仅显示有货
    private Integer hasStock;

    /*** 价格区间 */
    private String skuPrice;

    /*** 品牌id 可以多选 */
    private List<Long> brandId;

    /*** 按照属性进行筛选 */
    private List<String> attrs;

    /*** 页码*/
    private Integer pageNum = 1;

    /*** 原生所有查询属性*/
    private String _queryString;
}
