package com.zjw.gulimall.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjw.common.utils.PageUtils;
import com.zjw.gulimall.product.entity.AttrEntity;
import com.zjw.gulimall.product.entity.AttrGroupEntity;
import com.zjw.gulimall.product.vo.AttrEntityVo;
import com.zjw.gulimall.product.vo.AttrGroupWithAttrVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 16:35:30
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, int catelogId);


    boolean cascadeRemove(List<Long> attrGroupIdList);

    //获取已经关联的属性信息列表
    List<AttrEntity> getLinkedAttrList(int attrGroupId);


    //获取未关联的属性信息列表，同时还要保证是同一种类下
    PageUtils queryPageNoRel(Map<String, Object> params, int attrGroupId);

    List<AttrGroupWithAttrVo> getAttrGroupAndAttrByCatId(Long catelogId);
}

