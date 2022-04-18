package com.zjw.gulimall.order.feign;

import com.zjw.common.constant.WareConstant;
import com.zjw.common.utils.R;
import com.zjw.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-25 22:16
 * @Modifier:
 */
@FeignClient("guli-mall-ware-service")
public interface WareFeignService
{
    @PostMapping("/ware/waresku/list/stock")
    public R listStock(@RequestBody List<Long> skuIdList);

    @PostMapping("/ware/waresku/lockWareItems")
    boolean lockWareItems(@RequestBody WareSkuLockVo wareSkuLockVo);

    @GetMapping("/ware/wmswareordertaskdetail/updateWareTasksDetailStatus")
    R updateWareTasksDetailStatus(@RequestParam("orderSn") String orderSn, @RequestParam("status") int status);
}
