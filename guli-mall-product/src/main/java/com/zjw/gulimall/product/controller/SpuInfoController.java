package com.zjw.gulimall.product.controller;

import java.util.Arrays;
import java.util.Map;

import com.zjw.gulimall.product.vo.ProductInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zjw.gulimall.product.entity.SpuInfoEntity;
import com.zjw.gulimall.product.service.SpuInfoService;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;



/**
 * spu信息
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 17:10:36
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:spuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    //根据skuId查询对应的spu信息，用于远程调用
    @GetMapping("/getSpuInfoBySkuId")
    public R getSpuInfoBySkuId(@RequestParam("skuId") Long skuId){
        SpuInfoEntity spuInfoEntity = spuInfoService.getSpuInfoBySkuId(skuId);
        return R.ok().put("data", spuInfoEntity);
    }

    /**
     * 上架商品到es中
     *     //product/spuinfo/{spuId}/up
     * @param spuId
     * @return
     */
    @PostMapping("/{spuId}/up")
    public R upProduct(@PathVariable("spuId") Long spuId){
        boolean result = spuInfoService.upProduct(spuId);
        return R.ok();
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("product:spuinfo:info")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:spuinfo:save")
    public R save(@RequestBody ProductInfoVo productInfoVo){
		boolean saveResult = spuInfoService.saveProduct(productInfoVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:spuinfo:delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
