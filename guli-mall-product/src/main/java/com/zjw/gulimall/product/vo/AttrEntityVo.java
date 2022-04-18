package com.zjw.gulimall.product.vo;

import com.zjw.gulimall.product.entity.AttrEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: Zjw
 * @Description: 前端显示不仅要显示属性信息，还要加上所属分组名字和所属分类名字
 * @Create 2021-08-09 14:31
 * @Modifier:
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AttrEntityVo extends AttrEntity
{

    //分组名字
    private String groupName;
    //分类名字
    private String catelogName;
    //属性组id
    private Long attrGroupId;
    //分类完整路径，用于回显
    private Long[] catelogPath;
}
