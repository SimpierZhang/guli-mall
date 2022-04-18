package com.zjw.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.common.constant.ProductConstant;
import com.zjw.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zjw.gulimall.product.entity.AttrGroupEntity;
import com.zjw.gulimall.product.entity.CategoryEntity;
import com.zjw.gulimall.product.service.AttrAttrgroupRelationService;
import com.zjw.gulimall.product.service.AttrGroupService;
import com.zjw.gulimall.product.service.CategoryService;
import com.zjw.gulimall.product.vo.AttrEntityVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.product.dao.AttrDao;
import com.zjw.gulimall.product.entity.AttrEntity;
import com.zjw.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource
    private AttrGroupService attrGroupService;
    @Resource
    private AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Resource
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params, String attrType, Long catelogId) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
        //判断是销售属性还是基本属性
        queryWrapper.eq("attr_type", attrType.equals("base") ? ProductConstant.AttrTypeEnum.ATTR_TYPE_BASE.getCode() :
                ProductConstant.AttrTypeEnum.ATTR_TYPE_SALE.getCode());
        //判断是全部查询还是根据catelogId进行查询
        if(catelogId != 0){
            queryWrapper.eq("catelog_id", catelogId);
        }
        //判断是否加上条件查询
        String cond = (String) params.get("key");
        if(StringUtils.isNotBlank(cond)){
            queryWrapper.and(q -> {
                q.eq("attr_id", cond).or().like("attr_name", cond);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        List<AttrEntity> attrEntityList = page.getRecords();
        List<AttrEntityVo> responseVoList = attrEntityList.stream().map(attrEntity -> {
            AttrEntityVo attrEntityVo = new AttrEntityVo();
            BeanUtils.copyProperties(attrEntity, attrEntityVo);
            //1.封装分组名称
            //1.1 根据attrId获取其对应的attrGroupId
            AttrAttrgroupRelationEntity relEntity = attrAttrgroupRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
            if (relEntity != null) {
                //1.2 根据attrGroupId获取对应的分组名称
                AttrGroupEntity attrGroupEntity = attrGroupService.getById(relEntity.getAttrGroupId());
                if (attrGroupEntity != null) attrEntityVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }

            //2.封装分类名称
            CategoryEntity categoryEntity = categoryService.getById(attrEntity.getCatelogId());
            if (categoryEntity != null) attrEntityVo.setCatelogName(categoryEntity.getName());
            return attrEntityVo;
        }).collect(Collectors.toList());
        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(responseVoList);
        return pageUtils;
    }

    @Override
    @Transactional
    public boolean cascadeSave(AttrEntity attr) {
        AttrEntityVo attrEntityVo = (AttrEntityVo) attr;
        //1.保存到属性表中
        boolean attrSaveResult = save(attrEntityVo);
        boolean attrRelSaveResult = false;
        //2.保存到属性属性组关系表中
        if(attrEntityVo.getAttrGroupId() != null){
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntityVo.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(attrEntityVo.getAttrGroupId());
            attrRelSaveResult = attrAttrgroupRelationService.save(attrAttrgroupRelationEntity);
        }
        return attrSaveResult && attrRelSaveResult;
    }

    @Override
    public AttrEntityVo getAttrDetailById(Long attrId) {
        AttrEntityVo attrEntityVo = new AttrEntityVo();
        //1.根据attrId查询出对应的属性信息attr
        AttrEntity attr = getById(attrId);
        BeanUtils.copyProperties(attr, attrEntityVo);
        //2.根据attr.catelogId查询出完整路径
        List<Long> fullPath = categoryService.selectFullPath(attr.getCatelogId());
        attrEntityVo.setCatelogPath(fullPath.toArray(new Long[0]));
        return attrEntityVo;
    }

    @Override
    @Transactional
    public boolean cascadeUpdate(AttrEntityVo attrVo) {
        //1.先更新属性表
        boolean saveAttrResult = updateById(attrVo);
        boolean saveAttrRelResult = true;
        //2.需要时更新属性属性组关联表
        if(attrVo.getAttrGroupId() != null){
            AttrAttrgroupRelationEntity attrRelEntity = attrAttrgroupRelationService.getOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            attrRelEntity.setAttrId(attrVo.getAttrId());
            attrRelEntity.setAttrGroupId(attrVo.getAttrGroupId());
            saveAttrRelResult = attrAttrgroupRelationService.update(attrRelEntity,
                    new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
        }
        return saveAttrRelResult && saveAttrRelResult;
    }

    @Override
    @Transactional
    public boolean removeDetailByIds(List<Long> attrIdList) {
        if(attrIdList == null || attrIdList.size() <= 0) return false;
        //1.根据attrId查出属性属性组关系表中相应的记录List
        //并获取对应的主键列表，然后将其删除
        QueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new QueryWrapper<>();
        attrIdList.forEach(ai -> {
            queryWrapper.or().eq("attr_id", ai);
        });
        List<AttrAttrgroupRelationEntity> attrRelList = attrAttrgroupRelationService.list(queryWrapper);
        if(attrRelList != null && attrIdList.size() > 0){
            attrAttrgroupRelationService.removeByIds(attrRelList.stream().map(ar -> ar.getId()).collect(Collectors.toList()));
        }
        //2.删除属性表
        return removeByIds(attrIdList);
    }

    @Override
    public List<Long> listQueryIdList() {
        return baseMapper.listQueryIdList();
    }
}