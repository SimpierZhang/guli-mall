package com.zjw.guliamll.seckill.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-25 23:06
 * @Modifier:
 */
@Configuration
public class GIFeignConfig
{
    //为了解决feign在远程调用时会丢失请求头
    //自己添加feign的拦截器并且将请求头手动给请求带上
    //该拦截器会在feign远程调用前生效，作用就是加强请求
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        RequestInterceptor requestInterceptor = new RequestInterceptor()
        {
            @Override
            public void apply(RequestTemplate template) {
                //获取上下文环境
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                //feign的调用可能还是在异步操作中完成的，这样获取的请求上下文环境就为null
                //所以应当在异步操作前，先设置其请求上下文环境
                if(requestAttributes != null){
                    HttpServletRequest request = requestAttributes.getRequest();
                    //拿到当前请求的cookie
                    String cookie = request.getHeader("Cookie");
                    //将其放到远程调用的请求中，这样就可以解决feign远程调用丢失cookie
                    template.header("Cookie", cookie);
                }
            }
        };
        return requestInterceptor;
    }
}
