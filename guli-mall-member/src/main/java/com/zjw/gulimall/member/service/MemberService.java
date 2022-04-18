package com.zjw.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;
import com.zjw.gulimall.member.entity.MemberEntity;
import com.zjw.gulimall.member.vo.UserInfoVo;

import java.util.Map;

/**
 * 会员
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 18:39:56
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    boolean register(UserInfoVo userInfoVo);

    R login(UserInfoVo userInfoVo);


}

