package com.zjw.gulimall.ware.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zjw.gulimall.ware.entity.WmsWareInfoEntity;
import com.zjw.gulimall.ware.service.WmsWareInfoService;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;



/**
 * 仓库信息
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 19:00:46
 */
@RestController
@RequestMapping("ware/wareinfo")
public class WmsWareInfoController {
    @Autowired
    private WmsWareInfoService wmsWareInfoService;


    //根据收货地址获取邮费
    @GetMapping("/fare")
    public R getFare(@RequestParam("addressId") Long addressId){
        int fare = wmsWareInfoService.getFare(addressId);
        return R.ok().put("data", fare);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wmsWareInfoService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){

        WmsWareInfoEntity wmsWareInfo = wmsWareInfoService.getById(id);
        
        return R.ok().put("wmsWareInfo", wmsWareInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:wmswareinfo:save")
    public R save(@RequestBody WmsWareInfoEntity wmsWareInfo){
		wmsWareInfoService.save(wmsWareInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:wmswareinfo:update")
    public R update(@RequestBody WmsWareInfoEntity wmsWareInfo){
		wmsWareInfoService.updateById(wmsWareInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:wmswareinfo:delete")
    public R delete(@RequestBody Long[] ids){
		wmsWareInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
