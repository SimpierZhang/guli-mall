package com.zjw.gulimall.product.vo;

import com.zjw.gulimall.product.entity.AttrEntity;
import com.zjw.gulimall.product.entity.AttrGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-10 21:37
 * @Modifier:
 */
@Data
public class AttrGroupWithAttrVo extends AttrGroupEntity
{
    private List<AttrEntity> attrs;
}
