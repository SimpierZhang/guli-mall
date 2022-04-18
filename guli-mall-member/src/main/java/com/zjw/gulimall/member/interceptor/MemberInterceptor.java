package com.zjw.gulimall.member.interceptor;

import com.zjw.common.constant.AuthConstant;
import com.zjw.common.to.MemberInfoTo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-25 19:03
 * @Modifier:
 */
@Component
public class MemberInterceptor implements HandlerInterceptor
{

    public static ThreadLocal<MemberInfoTo> loginThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberInfoTo memberInfoTo = (MemberInfoTo) session.getAttribute(AuthConstant.LOGIN_USER);
        if(memberInfoTo == null){
            //未登录，重定位到登录页面，并携带需要登录信息
            session.setAttribute("msg", "用户未登录，请先登录");
            response.sendRedirect("http://auth.gulimall.com");
            return false;
        }else {
            loginThreadLocal.set(memberInfoTo);
        }
        return true;
    }
}
