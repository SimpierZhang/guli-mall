package com.zjw.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.zjw.gulimall.product.vo.ProductInfoVo;
import com.zjw.gulimall.product.vo.SkuPriceVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zjw.gulimall.product.entity.SkuInfoEntity;
import com.zjw.gulimall.product.service.SkuInfoService;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;



/**
 * sku信息
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 17:10:36
 */
@RestController
@RequestMapping("product/skuinfo")
public class SkuInfoController {
    @Autowired
    private SkuInfoService skuInfoService;


    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:skuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuInfoService.queryPage(params);

        return R.ok().put("page", page);
    }

    //根据idList返回对应的数据
    @PostMapping("/listByIds")
    public R listByIds(@RequestBody List<Long> idList){
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.listByIds(idList);
        return R.ok().put("data", skuInfoEntities);
    }

    //根据idList返回对应的商品列表的价格
    @GetMapping("/listPriceByIds")
    public R listPriceByIds(@RequestParam("idList") List<Long> idList){
        List<SkuPriceVo> priceVoList = skuInfoService.listPriceByIds(idList);
        return R.ok().put("data", priceVoList);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{skuId}")
    //@RequiresPermissions("product:skuinfo:info")
    public R info(@PathVariable("skuId") Long skuId){
		SkuInfoEntity skuInfo = skuInfoService.getById(skuId);

        return R.ok().put("skuInfo", skuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:skuinfo:save")
    public R save(@RequestBody SkuInfoEntity skuInfoEntity){
        boolean saveResult = skuInfoService.save(skuInfoEntity);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:skuinfo:update")
    public R update(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.updateById(skuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:skuinfo:delete")
    public R delete(@RequestBody Long[] skuIds){
		skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok();
    }

}
