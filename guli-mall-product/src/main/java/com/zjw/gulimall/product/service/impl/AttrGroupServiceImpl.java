package com.zjw.gulimall.product.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjw.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.zjw.gulimall.product.dao.AttrDao;
import com.zjw.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zjw.gulimall.product.entity.AttrEntity;
import com.zjw.gulimall.product.service.AttrAttrgroupRelationService;
import com.zjw.gulimall.product.service.AttrService;
import com.zjw.gulimall.product.vo.AttrEntityVo;
import com.zjw.gulimall.product.vo.AttrGroupWithAttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.product.dao.AttrGroupDao;
import com.zjw.gulimall.product.entity.AttrGroupEntity;
import com.zjw.gulimall.product.service.AttrGroupService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Resource
    private AttrService attrService;
    @Resource
    private AttrAttrgroupRelationService attrAttrgroupRelationService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, int catelogId) {
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        if(catelogId != 0){
            queryWrapper.eq("catelog_id", catelogId);
        }
        String cond = (String) params.get("key");
        if(StringUtils.isNotBlank(cond)){
            queryWrapper.and(q -> {
                q.eq("attr_group_id", cond).or().like("attr_group_name", cond);
            });
        }
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public boolean cascadeRemove(List<Long> attrGroupIdList) {
        attrGroupIdList.forEach(t -> {
            List<AttrAttrgroupRelationEntity> list = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", t));
            //??????attr???
            attrService.removeByIds(list.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList()));
            //??????attrGroupRel???
            attrAttrgroupRelationService.remove(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", t));
        });
        return removeByIds(attrGroupIdList);
    }

    @Override
    public List<AttrEntity> getLinkedAttrList(int attrGroupId) {
        List<AttrEntity> attrEntityList = null;
        AttrGroupEntity attrGroupEntity = getById(attrGroupId);
        if(attrGroupEntity != null){
            List<AttrAttrgroupRelationEntity> attrGroupRelList = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrGroupEntity.getAttrGroupId()));
            if(attrGroupRelList != null && attrGroupRelList.size() > 0){
                List<Long> attrIdList = attrGroupRelList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
                attrEntityList = attrService.listByIds(attrIdList);
            }
        }
        return attrEntityList;
    }

    @Override
    public PageUtils queryPageNoRel(Map<String, Object> params, int attrGroupId) {
        List<AttrEntity> resultList = null;
        AttrGroupEntity attrGroupEntity = getById(attrGroupId);
        //??????attr_group_id??????catelog_id
        if(attrGroupEntity != null){
            //??????catelog_id????????????????????????
            List<AttrEntity> attrEntityList = attrService.list(new QueryWrapper<AttrEntity>().eq("catelog_id", attrGroupEntity.getCatelogId()));
            if(attrEntityList != null && attrEntityList.size() > 0){
                //??????catelog_id???????????????????????????
                List<AttrGroupEntity> attrGroupEntityList = list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", attrGroupEntity.getCatelogId()));
                //????????????????????????????????????attrId
                List<AttrAttrgroupRelationEntity> attrRelEntityList = null;
                if(attrGroupEntityList != null && attrGroupEntityList.size() > 0){
                    attrRelEntityList = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", attrGroupEntityList.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList())));
                }
                QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
                //??????????????????
                String cond = (String) params.get("key");
                if(StringUtils.isNotBlank(cond)){
                    queryWrapper.eq("attr_id", cond).or().like("attr_name", cond);
                }
                //?????????????????????attrId
                if(attrRelEntityList != null && attrRelEntityList.size() > 0){
                    queryWrapper.notIn("attr_id", attrRelEntityList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList()));
                }

                IPage<AttrEntity> page = attrService.page(new Query<AttrEntity>().getPage(params), queryWrapper);
                return new PageUtils(page);
            }
        }
        return null;
    }

    @Override
    public List<AttrGroupWithAttrVo> getAttrGroupAndAttrByCatId(Long catelogId) {
        //??????catelogId?????????attrgroup??????
        List<AttrGroupEntity> attrGroupEntityList = list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        if(attrGroupEntityList != null && attrGroupEntityList.size() > 0){
            List<AttrGroupWithAttrVo> resultList = attrGroupEntityList.stream().map(ag -> {
                AttrGroupWithAttrVo groupWithAttrVo = new AttrGroupWithAttrVo();
                BeanUtils.copyProperties(ag, groupWithAttrVo);
                //??????attrGroupId??????????????????attrRel??????
                List<AttrAttrgroupRelationEntity> relationEntityList = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", ag.getAttrGroupId()));
                //??????attrRel????????????attrId????????????????????????attr????????????
                if (relationEntityList != null) {
                    List<AttrEntity> attrEntityList = attrService.list(new QueryWrapper<AttrEntity>().in("attr_id", relationEntityList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList())));
                    groupWithAttrVo.setAttrs(attrEntityList);
                }
                return groupWithAttrVo;
            }).collect(Collectors.toList());
            return resultList;
        }
        return null;
    }


}