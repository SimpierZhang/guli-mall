package com.zjw.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjw.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zjw.gulimall.product.entity.AttrEntity;
import com.zjw.gulimall.product.service.AttrAttrgroupRelationService;
import com.zjw.gulimall.product.service.AttrService;
import com.zjw.gulimall.product.service.CategoryService;
import com.zjw.gulimall.product.vo.AttrEntityVo;
import com.zjw.gulimall.product.vo.AttrGroupEntityVo;
import com.zjw.gulimall.product.vo.AttrGroupWithAttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zjw.gulimall.product.entity.AttrGroupEntity;
import com.zjw.gulimall.product.service.AttrGroupService;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;

import javax.annotation.Resource;


/**
 * 属性分组
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 17:10:36
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Resource
    private CategoryService categoryService;

    @Resource
    private AttrAttrgroupRelationService relationService;



    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId") int catelogId){
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }

    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrAttrgroupRelationEntity> relationEntityList){
        if(relationEntityList == null || relationEntityList.size() <= 0) return R.error(200, "错误的请求参数");
        relationService.saveBatch(relationEntityList);
        return R.ok();
    }


    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody List<AttrAttrgroupRelationEntity> relationEntityList){
        if(relationEntityList == null || relationEntityList.size() <= 0) return R.error(400, "错误的请求参数");
        QueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("attr_id", relationEntityList.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList()));
        relationService.remove(queryWrapper);
        return R.ok();
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
        AttrGroupEntityVo attrGroupEntityVo = new AttrGroupEntityVo();
        //查出一些属性组的基本信息
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        BeanUtils.copyProperties(attrGroup, attrGroupEntityVo);
        List<Long> catelogIds = categoryService.selectFullPath(attrGroup.getCatelogId());
        attrGroupEntityVo.setCatelogPath(catelogIds.toArray(new Long[catelogIds.size()]));
        return R.ok().put("attrGroup", attrGroupEntityVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除,要修改成级联删除，不仅要删除attrGroup表，还要删除attrGroupRel表
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
        List<Long> attrGroupIdList = Arrays.asList(attrGroupIds);
        attrGroupService.cascadeRemove(attrGroupIdList);
        return R.ok();
    }

    /**
    * @Author: Zjw
    * @Description: 获取attrGroupId已有的关联属性
    * @Param:
    * @Return:
    * @Date: 2021/8/9 9:53
    */
    @GetMapping("/{attrGroupId}/attr/relation")
    public R getAttrGroupDetail(@PathVariable("attrGroupId") int attrGroupId){
        List<AttrEntity> attrEntityVoList = attrGroupService.getLinkedAttrList(attrGroupId);
        return R.ok().put("data", attrEntityVoList);
    }

    /**
    * @Author: Zjw
    * @Description:
    * @Param: 获取attrGroupId没有的关联属性
    * @Return: 
    * @Date: 2021/8/9 17:31
    */
    @GetMapping("/{attrGroupId}/noattr/relation")
    public R getAttrGroupDetailNoRel(@RequestParam Map<String, Object> params, @PathVariable("attrGroupId") int attrGroupId){
        PageUtils page = attrGroupService.queryPageNoRel(params, attrGroupId);
        return R.ok().put("data", page);
    }


    /**
     * 获取分类下所有分组&关联属性
     * @return
     */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupAndAttrByCatId(@PathVariable("catelogId") Long catelogId){

        List<AttrGroupWithAttrVo> attrGroupEntityList = attrGroupService.getAttrGroupAndAttrByCatId(catelogId);
        return R.ok().put("data", attrGroupEntityList);

    }

}
