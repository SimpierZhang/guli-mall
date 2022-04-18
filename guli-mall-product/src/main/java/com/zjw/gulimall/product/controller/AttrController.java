package com.zjw.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjw.gulimall.product.entity.ProductAttrValueEntity;
import com.zjw.gulimall.product.service.ProductAttrValueService;
import com.zjw.gulimall.product.vo.AttrEntityVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.zjw.gulimall.product.service.AttrService;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;

import javax.annotation.Resource;


/**
 * 商品属性
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 17:10:36
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Resource
    private ProductAttrValueService productAttrValueService;
    /**
     * 列表
     */
    @RequestMapping("/{attrType}/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("attrType") String attrType,
                  @PathVariable("catelogId") Long catelogId){
        PageUtils page = attrService.queryPage(params, attrType, catelogId);

        return R.ok().put("page", page);
    }



    /**
     * //product/attr/base/listforspu/{spuId}
     * 获取spu规格
     * @param spuId
     * @return
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R listForSpuId(@PathVariable("spuId")Long spuId){
        List<ProductAttrValueEntity> productAttrEntityList = productAttrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        return R.ok().put("data", productAttrEntityList);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		AttrEntityVo attrEntityVo = attrService.getAttrDetailById(attrId);
        return R.ok().put("attr", attrEntityVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrEntityVo attrEntityVo){
		boolean result = attrService.cascadeSave(attrEntityVo);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrEntityVo attrVo){
        boolean saveResult = attrService.cascadeUpdate(attrVo);
        return R.ok();
    }

    /**
     * 修改单个spuId对应的属性值
     */
    @Transactional
    @PostMapping("/update/{spuId}")
    public R update(@RequestBody List<ProductAttrValueEntity> productAttrValueEntityList, @PathVariable("spuId") Long spuId){
        //更新属性，可以先删除，然后再插入，因为没有根据attr_id批量更新的方法
        productAttrValueEntityList.forEach(p -> p.setSpuId(spuId));
        productAttrValueService.remove(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", spuId));
        productAttrValueService.saveBatch(productAttrValueEntityList);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		boolean removeRes = attrService.removeDetailByIds(Arrays.asList(attrIds));
        return R.ok();
    }

}
