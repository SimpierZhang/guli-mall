package com.zjw.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.zjw.common.constant.BizCodeEnum;
import com.zjw.gulimall.member.exception.PhoneExistException;
import com.zjw.gulimall.member.exception.UserNameExistException;
import com.zjw.gulimall.member.vo.UserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zjw.gulimall.member.entity.MemberEntity;
import com.zjw.gulimall.member.service.MemberService;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;



/**
 * 会员
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 18:39:56
 */
@RestController
@RequestMapping("/member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }



    @ResponseBody
    @PostMapping("/register")
    public R register(@RequestBody UserInfoVo userInfoVo){
        if(userInfoVo == null) return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), BizCodeEnum.VAILD_EXCEPTION.getMsg());
        try {
            boolean regResult = memberService.register(userInfoVo);
            if(regResult){
                return R.ok();
            }else {
                return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), "注册失败，请重试！！！");
            }
        }catch (PhoneExistException e){
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        }catch (UserNameExistException e){
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }
    }

    @ResponseBody
    @PostMapping("/login")
    public R login(@RequestBody UserInfoVo userInfoVo){
        return memberService.login(userInfoVo);
    }

}
