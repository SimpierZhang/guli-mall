package com.zjw.gulimall.product.feign;

import com.zjw.common.to.MemberPriceEntityTo;
import com.zjw.common.to.SkuFullReductionEntityTo;
import com.zjw.common.to.SkuLadderEntityTo;
import com.zjw.common.to.SpuBoundsEntityTo;
import com.zjw.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-10 23:42
 * @Modifier:
 */
@FeignClient("guli-mall-coupon-service")
public interface CouponFeignService
{
    @RequestMapping("/coupon/spubounds/save")
    public R saveBounds(@RequestBody SpuBoundsEntityTo spuBounds);

    @RequestMapping("/coupon/skuladder/save")
    //@RequiresPermissions("coupon:skuladder:save")
    public R saveLadder(@RequestBody SkuLadderEntityTo skuLadder);

    @RequestMapping("/coupon/skufullreduction/save")
    //@RequiresPermissions("coupon:skufullreduction:save")
    public R saveFullReduction(@RequestBody SkuFullReductionEntityTo skuFullReduction);

    @PostMapping("/coupon/memberprice/save/bacth")
    public R saveBatchMemberPrice(@RequestBody List<MemberPriceEntityTo> memberPriceEntityList);
}
