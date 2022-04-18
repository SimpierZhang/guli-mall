package com.zjw.gulimall.member.web;

import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;
import com.zjw.gulimall.member.feign.OrderFeignService;
import com.zjw.gulimall.member.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-29 0:41
 * @Modifier:
 */
@Controller
public class MemberWebController
{
    @Resource
    private OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public String toMemberOrderPage(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum, Model model){
        Map<String, Object> params = new HashMap<>();
        params.put("page", pageNum);
        R r = orderFeignService.listOrderPage(params);
        model.addAttribute("orders", r);
        return "orderList";
    }
}
