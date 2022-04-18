package com.zjw.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjw.common.utils.PageUtils;
import com.zjw.gulimall.product.entity.AttrEntity;
import com.zjw.gulimall.product.vo.AttrEntityVo;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 16:35:30
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params, String attrType, Long catelogId);

    boolean cascadeSave(AttrEntity attr);

    AttrEntityVo getAttrDetailById(Long attrId);

    boolean cascadeUpdate(AttrEntityVo attrVo);

    boolean removeDetailByIds(List<Long> asList);

    //查询出可以被搜索的idList，即search_type=1的id
    List<Long> listQueryIdList();
}

