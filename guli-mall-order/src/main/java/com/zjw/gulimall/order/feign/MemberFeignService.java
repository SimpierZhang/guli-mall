package com.zjw.gulimall.order.feign;

import com.zjw.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-25 20:41
 * @Modifier:
 */
@FeignClient("guli-mall-member-service")
public interface MemberFeignService
{
    @RequestMapping("/member/memberreceiveaddress/getAddressByMId")
    public R getAddressInfoByMemberId(@RequestParam("id") Long id);

    @RequestMapping("/member/memberreceiveaddress//info/{id}")
    public R getAddressInfoById(@PathVariable("id") Long id);
}
