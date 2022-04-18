package com.zjw.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.zjw.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000118609797";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC78YwR6fQ9qjFYi2VzKXtMNoFdnxhXGcSjuLn6/+TKyC98VU6Tx1EM/chK2R6wMOyP/8qVp75Ky01GctC0rTOvgtIxZ86SohOQ0iq3G15SfzudMx368oGgTI+YTdWp7szyZiqNTahKP/sa50Tf2BksgjO28/Qr0NX6uGNh03ICILbpsOj2iQ+7D2fsHP0QIAjUxjcLBriGTeDwIMwCu6g6PSzun6AN5QwZ4mVby08xkVNoDCJ5ag5mfihyQzliyYki8awowZ4RsKF9ZgjG92tpz/vNOc8YizKxNlbPLrsqWpPCKFc2gIvpbVfM454fKnDMltOXlzF25bqsOnuYqFnjAgMBAAECggEAMbuDMyJTbRXiJOoOGGE7rTknFl0JFdNNw30YSaD5K+e5GQ+B0X0Gp4doPtYhVsI7dwda1isjIauWcTPkokrKyIC3mjc75HVa9BGJGBb36KCeZ8SlBEizoHVh8g12opb8SJBcZjPMeolfqwjTouAOGUcC5EC9tfJ3DPdJ5RMMsh79WXyNZ1pNerp5bpu4A/BmO/vF6alFGB3oSqKcQ9NrurcxDCdDqe2dMehitJHLKv+iJ5yr77e+TEpzvPY3Rs2X2l9oI5PsZLr4nHVrkqMo1lzLvO8ZEw42fev/Lr3w+ifVVbKZj+eiZYHLX+YS+2fqSqrB2UgQHHoI3MkTXDDLIQKBgQDys1Aqh0l9fk0aBSVICZs2Z3sgTj0o251yYisw7KI49TpgPOJjBflmDQiB9gWljTMCNK2VXFcpzXe13isZE6ipF+eMSFQloPT8mAJh1kmEMEKbe3NnEgVSiqRhmJidC8yBaeAVIatpdaudeYxoc44JCGomVncZFXCxeSjALbFWGQKBgQDGPhXFI0nGLw+JMwh/NXSU1bfxuRvkQt57mJMZ+QXtEWtgQ28fE4iyuRGt09RAUb/RI1Nl3u5fNYx8GAG+ZLoX7pbx7Fqxnj0G1acVck6bxPBlsl0+KJqUEOwMQqUercrAGUeXCwrkjbwa9g3M/Kt3hftscwWP+SXVZx7WpkaXWwKBgQCWzOXl1vPJ3v1nSoU0sotXjYrKsvJ7faHh3a96kb3maDEidRIxdxfU2gpMPiKaEBb+qt6kbKDYH4vfXDEpPrbQgkLkPljSnKU8gfMQ+YXqddYdJ+K3y1G1TOIApkFFa3xhePi8CJSqTWwZx9l/WTWK1Uink5gi5NiF3YN9jaafmQKBgDc8DSzETEXfzuBXS8LFzhm6AmpjPRY4W2zscfAGplQLSnOj1k4+TxQc5IQeRUE2hc9rnE0B9ECq1t2sgq/duMv6Oixlwk9C25YP6WHcW3KjSKfNLpWnEoq2vE4eM4t8uMUVRhEbjolnxEVu0SGQNOY0en0Y/y/CJ3xyIvGDmp+JAoGBAOWurPu6A9TgXRR87A3eSCEcP3SZLCB8C9iljXWV+OLBNCmABywNfd8WMablGYzNAKK9sA77zIZfApDp/aKDDtfbgiNKVnjWr88dkOBmUI5QMpWpxoD/SN/oA9oO2g0NpseR4K4IteEo0f+L5HQbJXxiC4Tr5x4+GPawhBs55lyI";
	// 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwv3hx/38lmF8WJhP+b5NwAGLbtB/J9hnqgEViF90AyLtDls42hEfvten9EAOUPxrN9mSTmb1zNy5GcHU8tT21OzoaO1k12YJkDX2aKumMqaiBjBHqeasxONGjq5dsxyb7GoBbEsmpaQWXMg/LY2WyCqJbdACsv6rxT1vTDS1WM4HJYmvtjDC5ITX0sfkldgpCVGeVlZTsddMUWXZ2kWLwjDsJ7lYEK+N+dxnjcue710MEogo7pVzjVDTPH1wqB4iJMM6hD6ow5BCcM68O/u0o8kcd7olwre7pV0Abj6Lc68xSp8U3ohJE4tgjwCv4Ef5aj8/QJul5WCjCMJZeZiCaQIDAQAB";
	// 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url = "http://testzjw.vaiwan.com/payListener";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 自动关单时间
    private String timeout = "15m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        // 30分钟内不付款就会自动关单
        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\"" + timeout + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        return result;
    }
}
