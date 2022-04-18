package com.zjw.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.zjw.gulimall.order.config.AlipayTemplate;
import com.zjw.gulimall.order.service.OrderService;
import com.zjw.gulimall.order.vo.PayAsyncVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sun.rmi.runtime.Log;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Author: Zjw
 * @Description: 订单支付成功后的处理，用于支付宝发送回调
 * @Create 2021-08-29 0:14
 * @Modifier:
 */
@RestController
@Slf4j
public class OrderPayedListener
{
    @Resource
    private OrderService orderService;
    @Resource
    private AlipayTemplate alipayTemplate;

    @PostMapping("/payListener")
    public String payedResultListener(PayAsyncVo payAsyncVo, HttpServletRequest request) throws AlipayApiException {
        try {
            log.info("收到支付宝支付成功的信息");
            //进行验签
            // 验签
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
                String name = iter.next();
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i]
                            : valueStr + values[i] + ",";
                }
                //乱码解决，这段代码在出现乱码时使用
//			valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
                params.put(name, valueStr);
            }
            // 只要我们收到了支付宝给我们的异步通知 验签成功 我们就要给支付宝返回success
            if (AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(), alipayTemplate.getCharset(), alipayTemplate.getSign_type())) {
                return orderService.handlePayedOrder(payAsyncVo);
            }
            log.error("\n受到恶意验签攻击");
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return "fail";
        }
        return "success";
    }
}
