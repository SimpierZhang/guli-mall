package com.zjw.gulimall.controller;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.zjw.common.constant.BizCodeEnum;
import com.zjw.common.constant.CommonConstant;
import com.zjw.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-05 22:59
 * @Modifier:
 */
@RestController
@RequestMapping("/thirdparty/oss")
public class OssController
{
    @Autowired(required = false)
    private OSS ossClient;

    @Value("${spring.cloud.alicloud.access-key}")
    private String accessId;
    @Value("${spring.cloud.alicloud.oss.endpoint}")
    private String endpoint;
    @Value("${spring.cloud.alicloud.oss.bucketName}")
    private String bucketName;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    //阿里云oss上传图片
    @GetMapping("/policy")
    public R getPolicy(){

        R result = new R();
        // callbackUrl为上传回调服务器的URL，请将下面的IP和Port配置为您自己的真实信息。
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dir = sdf.format(new Date()); // 用户上传文件时指定的前缀。

        // 创建OSSClient实例。
        try {
            String host = "https://" + bucketName + "." + endpoint;
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            result.put("data", respMap);
        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            System.out.println(e.getMessage());
        } finally {
            ossClient.shutdown();
        }
        return result;
    }

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendVerifyCode(@RequestParam(value = "phone") String phone){
        String verifyCode = "123456-" + System.currentTimeMillis();
        String verifyKey = CommonConstant.VERIFY_CODE_PREFIX + phone;
        if(redisTemplate.hasKey(verifyKey)){
            //如果有值，表明之前发送过验证码，那么就需要检测一下是不是60s之内发的
            String verifyValue = redisTemplate.opsForValue().get(verifyKey);
            if(verifyValue != null){
                String[] split = verifyValue.split("-");
                long mod = System.currentTimeMillis() - Long.parseLong(split[1]);
                if(TimeUnit.SECONDS.convert(mod, TimeUnit.MILLISECONDS) < 60L){
                    return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
                }
            }
        }
        //如果没值，表示可以发送验证码,并设置验证码十分钟失效
        redisTemplate.opsForValue().set(verifyKey, verifyCode, 10L, TimeUnit.MINUTES);
        return R.ok();
    }
}
