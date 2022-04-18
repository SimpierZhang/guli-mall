package com.zjw.gulimall.ware.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.zjw.gulimall.ware.vo.DonePurchaseVo;
import com.zjw.gulimall.ware.vo.MergeEntityVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zjw.gulimall.ware.entity.WmsPurchaseEntity;
import com.zjw.gulimall.ware.service.WmsPurchaseService;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;



/**
 * 采购信息
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 19:00:46
 */
@RestController
@RequestMapping("ware/purchase")
public class WmsPurchaseController {
    @Autowired
    private WmsPurchaseService wmsPurchaseService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:wmspurchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = wmsPurchaseService.queryPage(params);

        return R.ok().put("page", page);
    }



    /**
     * //ware/purchase/merge
     * @return
     */
    @PostMapping("/merge")
    public R mergePurchaseDetail(@RequestBody MergeEntityVo mergeEntityVo){
        //注：@RequestBody注解一个方法只能有一个，不然的话就会报400错误
        //所以对于多个参数的请求，有两种解决方法
        //1.封装成一个实体类
        //2.借用Map<String,Object>进行接受，然后自行进行Json转化
        boolean result = wmsPurchaseService.merge(mergeEntityVo.getPurchaseId(), mergeEntityVo.getItems());
        return R.ok();
    }

    /**
     * //ware/purchase/received
     * 领取购物单
     * @param
     * @return
     */
    @PostMapping("/received")
    public R receivePurchase(@RequestBody List<Long> purchaseIds){
        wmsPurchaseService.receivePurchaseList(purchaseIds);
        return R.ok();
    }

    /**
     * 完成采购
     * @param donePurchaseVo
     * @return
     */
    @PostMapping("/done")
    public R done(@RequestBody DonePurchaseVo donePurchaseVo){
        wmsPurchaseService.donePurchase(donePurchaseVo);
        return R.ok();
    }

    /**
     * /ware/purchase/unreceive/list
     * 查询未被领取的采购单
     * @return
     */
    @GetMapping("/unreceive/list")
    public R unReceiveList(){
        PageUtils page = wmsPurchaseService.getUnreceivePurchaseList();
        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:wmspurchase:info")
    public R info(@PathVariable("id") Long id){
		WmsPurchaseEntity wmsPurchase = wmsPurchaseService.getById(id);

        return R.ok().put("wmsPurchase", wmsPurchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:wmspurchase:save")
    public R save(@RequestBody WmsPurchaseEntity wmsPurchase){
		boolean result = wmsPurchaseService.savePurchase(wmsPurchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:wmspurchase:update")
    public R update(@RequestBody WmsPurchaseEntity wmsPurchase){
		wmsPurchaseService.updateById(wmsPurchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:wmspurchase:delete")
    public R delete(@RequestBody Long[] ids){
		wmsPurchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
