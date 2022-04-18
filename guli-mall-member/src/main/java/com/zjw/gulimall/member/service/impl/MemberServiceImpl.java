package com.zjw.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.zjw.common.constant.AuthConstant;
import com.zjw.common.constant.BizCodeEnum;
import com.zjw.common.to.MemberInfoTo;
import com.zjw.common.utils.R;
import com.zjw.gulimall.member.exception.PhoneExistException;
import com.zjw.gulimall.member.exception.UserNameExistException;
import com.zjw.gulimall.member.vo.UserInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.member.dao.MemberDao;
import com.zjw.gulimall.member.entity.MemberEntity;
import com.zjw.gulimall.member.service.MemberService;
import org.springframework.transaction.annotation.Transactional;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public boolean register(UserInfoVo userInfoVo) {
        //1.查看手机号和登录账号是否唯一
        checkExistUserName(userInfoVo.getUserName());
        checkExistPhone(userInfoVo.getPhone());
        //2.前两步都通过的基础上进行注册插入数据到数据库中
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setUsername(userInfoVo.getUserName());
        memberEntity.setNickname(userInfoVo.getUserName());
        memberEntity.setMobile(userInfoVo.getPhone());
        memberEntity.setLevelId(1L);
        memberEntity.setCreateTime(new Date());
        //3.通过md5+盐存储密码
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = encoder.encode(userInfoVo.getPassword());
        memberEntity.setPassword(password);
        return baseMapper.insert(memberEntity) == 1;
    }

    @Override
    public R login(UserInfoVo userInfoVo) {
        //1.查询用户是否存在
        if(userInfoVo == null) return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), "请输入账户密码");
        String userName = userInfoVo.getUserName();
        MemberEntity memberEntity = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", userName).or().eq("mobile", userName));
        if(memberEntity != null){
            //检验密码是否正确
            String dbPassword = memberEntity.getPassword();
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            boolean matches = encoder.matches(userInfoVo.getPassword(), dbPassword);
            if(matches){
                MemberInfoTo memberInfoTo = new MemberInfoTo();
                BeanUtils.copyProperties(memberEntity, memberInfoTo);
                //避免密码也存到session中
                memberEntity.setPassword(null);
                return R.ok().put(AuthConstant.LOGIN_USER, memberInfoTo);
            }else {
                return R.error(BizCodeEnum.LOGINACTT_PASSWORD_ERROR.getCode(), BizCodeEnum.LOGINACTT_PASSWORD_ERROR.getMsg());
            }
        }
        return R.error(BizCodeEnum.USER_NOT_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_NOT_EXIST_EXCEPTION.getMsg());
    }


    //检查用户名是否存在
    private void checkExistUserName(String userName){
        int count = count(new QueryWrapper<MemberEntity>().eq("username", userName));
        if(count > 0){
            throw new UserNameExistException();
        }
    }

    //检查手机号是否存在
    private void checkExistPhone(String phone){
        int count = count(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if(count > 0){
            throw new PhoneExistException();
        }
    }

}