package com.zjw.guliamll.seckill.interceptor;

import com.zjw.common.constant.AuthConstant;
import com.zjw.common.to.MemberInfoTo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-09-01 0:40
 * @Modifier:
 */
@Component
public class SeckillInterceptor implements HandlerInterceptor
{
    public static ThreadLocal<MemberInfoTo> memberInfo = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //检查是否登录
        HttpSession session = request.getSession();
        MemberInfoTo memberInfoTo = (MemberInfoTo) session.getAttribute(AuthConstant.LOGIN_USER);
        if(memberInfoTo == null){
            session.setAttribute("msg", "用户未登录，请先登录");
            response.sendRedirect("http://auth.gulimall.com");
            return false;
        }
        memberInfo.set(memberInfoTo);
        return true;
    }
}
