package com.zjw.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.zjw.common.constant.OrderConstant;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;
import com.zjw.gulimall.order.config.AlipayTemplate;
import com.zjw.gulimall.order.service.OrderService;
import com.zjw.gulimall.order.vo.OrderConfirmVo;
import com.zjw.gulimall.order.vo.OrderSubmitVo;
import com.zjw.gulimall.order.vo.PayVo;
import com.zjw.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-25 19:10
 * @Modifier:
 */
@Controller
public class OrderWebController
{

    @Resource
    private OrderService orderService;
    @Resource
    private AlipayTemplate alipayTemplate;

    @GetMapping("/{path}.html")
    public String testPath(@PathVariable("path") String path) {
        return path;
    }

    @ResponseBody
    @PostMapping("/listOrderPage")
    R listOrderPage(Map<String, Object> params){
        //1.根据当前登录的人查出所有订单号
        //2.根据订单号查出所有订单详细信息
        PageUtils pageUtils = orderService.listOrderPage(params);
        return R.ok().put("data", pageUtils);
    }

    @ResponseBody
    @GetMapping(value = "/payOrder", produces = "text/html")
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        System.out.println("接收到订单信息orderSn："+orderSn);
        //获取当前订单并设置支付订单相关信息
        PayVo payVo = orderService.getOrderPay(orderSn);
        if(payVo == null) return "订单已经过期了，请重新下单";
        //直接返回的是一个页面
        String payPage = alipayTemplate.pay(payVo);
        return payPage;
    }

    //去往订单确认页
    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        OrderConfirmVo orderConfirmVo = orderService.getConfirmInfo();
        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }

    //提交订单，保存订单信息，并去往支付页面
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes) {
        try {
            SubmitOrderResponseVo responseVo = orderService.submitOrder(orderSubmitVo);
            if (responseVo.getCode() == OrderConstant.OrderSubmitStatusEnum.ORDER_SUBMIT_SUCCESS.getSubmitStatusCode()) {
                model.addAttribute("submitOrderResp", responseVo);
                return "pay";
            }
            else {
                OrderConstant.OrderSubmitStatusEnum[] values = OrderConstant.OrderSubmitStatusEnum.values();
                String errorMsg = "";
                for (OrderConstant.OrderSubmitStatusEnum error : values) {
                    if (responseVo.getCode() == error.getSubmitStatusCode()) {
                        errorMsg = error.getMsg();
                        break;
                    }
                }
                redirectAttributes.addFlashAttribute("msg", errorMsg);
                return "redirect:http://order.gulimall.com/toTrade";
            }
        }
        catch (Exception e) {
            String message = e.getMessage();
            redirectAttributes.addFlashAttribute("msg", message);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
