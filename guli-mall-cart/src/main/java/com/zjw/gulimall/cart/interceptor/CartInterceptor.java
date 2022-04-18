package com.zjw.gulimall.cart.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zjw.common.constant.AuthConstant;
import com.zjw.common.to.MemberInfoTo;
import com.zjw.common.to.UserInfoTo;
import org.apache.commons.lang.StringUtils;
import org.omg.PortableInterceptor.Interceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-23 23:25
 * @Modifier:
 */
public class CartInterceptor implements HandlerInterceptor
{
    private static final String TEMP_CART_KEY = "user-key";
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();
    //设置cookie的有效期为一个月
    private static int TEMP_CART_KEY_EXPIRE_TIME = 3600 * 24 * 30;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断是否是第一次访问购物车，如果不是的话，需要将其信息保存到ThreadLocal中
        HttpSession session = request.getSession();
        UserInfoTo userInfoTo = new UserInfoTo();
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length > 0){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals(TEMP_CART_KEY)){
                    userInfoTo.setUserKey(cookie.getValue());
                }
            }
        }
        //如果仍然为空，表示是第一次访问购物车应用，为其分配一个user-key,并且还要将其存入cookie中
        if(!StringUtils.isNotBlank(userInfoTo.getUserKey())){
            String userKey = UUID.randomUUID().toString().replace("-", "");
            userInfoTo.setUserKey(userKey);
        }
        MemberInfoTo memberInfoTo = (MemberInfoTo) session.getAttribute(AuthConstant.LOGIN_USER);
        if(memberInfoTo != null){
            //表示已经登录
            userInfoTo.setUserId(memberInfoTo.getId());
            userInfoTo.setUsername(memberInfoTo.getUsername());
            userInfoTo.setTempUser(false);
        }
        threadLocal.set(userInfoTo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //否则表示
        UserInfoTo userInfoTo = threadLocal.get();
        // 如果是临时用户，返回临时购物车的cookie
        if(userInfoTo.isTempUser()){
            Cookie cookie = new Cookie(TEMP_CART_KEY, userInfoTo.getUserKey());
            // 设置这个cookie作用域 过期时间
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(TEMP_CART_KEY_EXPIRE_TIME);
            response.addCookie(cookie);
        }
    }
}
