package com.zjw.gulimall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjw.common.to.WareSkuVo;
import com.zjw.gulimall.ware.vo.WareSkuLockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zjw.gulimall.ware.entity.WmsWareSkuEntity;
import com.zjw.gulimall.ware.service.WmsWareSkuService;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;



/**
 * 商品库存
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 19:00:46
 */
@RestController
@RequestMapping("/ware/waresku")
public class WmsWareSkuController {
    @Autowired
    private WmsWareSkuService wmsWareSkuService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:wmswaresku:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wmsWareSkuService.queryPage(params);

        return R.ok().put("page", page);
    }

    //根据skuIdList查询对应的sku是否有库存
    @PostMapping("/list/stock")
    public R listStock(@RequestBody List<Long> skuIdList){
        List<WmsWareSkuEntity> wareSkuEntityList = wmsWareSkuService.list(new QueryWrapper<WmsWareSkuEntity>().in("sku_id", skuIdList));
        //有库存的sku
        List<WmsWareSkuEntity> hasStockSkuEntityList = wareSkuEntityList.stream().filter(ware -> (ware.getStock() - ware.getStockLocked()) > 0).collect(Collectors.toList());
        List<WareSkuVo> wareSkuVoList = skuIdList.stream().map(skuId -> {
            WareSkuVo wareSkuVo = new WareSkuVo();
            wareSkuVo.setSkuId(skuId);
            wareSkuVo.setHasStock(false);
            //如果有库存，就设置库存以及仓库id，如果没有就设置无库存
            hasStockSkuEntityList.forEach(hs -> {
                if (skuId.equals(hs.getSkuId())) {
                    wareSkuVo.setHasStock(true);
                    wareSkuVo.setWareId(hs.getWareId());
                }
            });
            return wareSkuVo;
        }).collect(Collectors.toList());
        return R.ok().put("data", wareSkuVoList);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:wmswaresku:info")
    public R info(@PathVariable("id") Long id){
		WmsWareSkuEntity wmsWareSku = wmsWareSkuService.getById(id);

        return R.ok().put("wmsWareSku", wmsWareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:wmswaresku:save")
    public R save(@RequestBody WmsWareSkuEntity wmsWareSku){
		wmsWareSkuService.save(wmsWareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:wmswaresku:update")
    public R update(@RequestBody WmsWareSkuEntity wmsWareSku){
		wmsWareSkuService.updateById(wmsWareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:wmswaresku:delete")
    public R delete(@RequestBody Long[] ids){
		wmsWareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    /**
     * 锁库存
     * @param wareSkuLockVo
     * @return
     */
    @PostMapping("/lockWareItems")
    boolean lockWareItems(@RequestBody WareSkuLockVo wareSkuLockVo){
        return wmsWareSkuService.lockWareItems(wareSkuLockVo);
    }

}
