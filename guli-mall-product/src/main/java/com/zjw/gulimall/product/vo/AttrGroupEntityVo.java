package com.zjw.gulimall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-09 10:41
 * @Modifier:
 */
@Data
public class AttrGroupEntityVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * 分组id
     */
    @TableId
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    //catelogId的完整路径，用于前端回显
    //例如catlelogId是225，那么对应的catelogPath就是[2,34,225]
    private Long[] catelogPath;

}
