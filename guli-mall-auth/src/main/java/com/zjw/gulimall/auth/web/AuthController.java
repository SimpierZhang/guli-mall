package com.zjw.gulimall.auth.web;

import com.alibaba.fastjson.TypeReference;
import com.zjw.common.constant.AuthConstant;
import com.zjw.common.constant.BizCodeEnum;
import com.zjw.common.to.MemberInfoTo;
import com.zjw.common.utils.R;
import com.zjw.gulimall.auth.feign.MemberFeignService;
import com.zjw.gulimall.auth.feign.OssFeignService;
import com.zjw.gulimall.auth.service.AuthService;
import com.zjw.gulimall.auth.vo.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-22 19:10
 * @Modifier:
 */
@Slf4j
@Controller
public class AuthController
{
    @Resource
    private AuthService authService;
    @Resource
    private OssFeignService ossFeignService;


    @GetMapping("/reg.html")
    public String toReg() {
        return "reg";
    }

    @PostMapping("/register")
    public String register(@Valid UserInfoVo userInfoVo, BindingResult bindingResult,
                           RedirectAttributes attributes) {
        Map<String, String> errorMap = new HashMap<>();
        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            fieldErrors.forEach(fe -> {
                errorMap.put(fe.getField(), fe.getDefaultMessage());
            });
            attributes.addFlashAttribute("errors", errorMap);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        R res = authService.register(userInfoVo);
        if (res.getCode() == 0) {
            //?????????????????????????????????
            return "redirect:http://auth.gulimall.com/login.html";
        }
        else {
            attributes.addFlashAttribute("errors", res.get("msg", new TypeReference<String>()
            {
            }));
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendVerifyCode(String phone) {
        R r = ossFeignService.sendVerifyCode(phone);
        return r;
    }

    @PostMapping("/login")
    public String login(UserInfoVo userInfoVo, RedirectAttributes attributes,
                        HttpSession session){
        Map<String, String> errors = new HashMap<>();
        if(userInfoVo == null){
            errors.put("msg", BizCodeEnum.LOGINACTT_PASSWORD_ERROR.getMsg());
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
        //??????
        R r = authService.login(userInfoVo);
        if (r.getCode() == 0) {
            MemberInfoTo memberInfoTo = r.get(AuthConstant.LOGIN_USER, new TypeReference<MemberInfoTo>(){});
            //?????????????????????????????????????????????session??????????????????????????????session?????????
            log.info("??????[{}]??????", memberInfoTo.getUsername());
            session.setAttribute(AuthConstant.LOGIN_USER, memberInfoTo);
            return "redirect:http://gulimall.com/";
        }else {// ???????????????????????????????????????????????????
            String msg = (String) r.get("msg");
            errors.put("msg", msg);
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    /**
     * ????????????????????????????????????????????????
     * @return
     */
    @GetMapping(value = {"/", "/login.html", "/index", "/index.html"})
    public String toLoginPage(HttpSession session){
        //1.??????session????????????????????????????????????????????????????????????????????????????????????????????????
        // TODO ????????????????????????+ThreadLocal???????????????????????????????????????session?????????????????????redis?????????
        Object userInfo = session.getAttribute(AuthConstant.LOGIN_USER);
        if(userInfo != null){
            log.info("????????????????????????????????????");
            //????????????
            return "redirect:http://gulimall.com";
        }else {
            return "login";
        }
    }

    //????????????
    @GetMapping("/oauth2.0/logout")
    public String logout(HttpSession httpSession){
        //??????session,????????????
        httpSession.removeAttribute(AuthConstant.LOGIN_USER);
        return "redirect:http://gulimall.com";
    }
}
