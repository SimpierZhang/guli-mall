package com.zjw.gulimall.coupon.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zjw.common.utils.DateUtil;
import com.zjw.gulimall.coupon.entity.SeckillSessionEntityVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zjw.gulimall.coupon.entity.SeckillSessionEntity;
import com.zjw.gulimall.coupon.service.SeckillSessionService;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;


/**
 * 秒杀活动场次
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 18:35:13
 */
@RestController
@RequestMapping("coupon/seckillsession")
public class SeckillSessionController
{
    @Autowired
    private SeckillSessionService seckillSessionService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("coupon:seckillsession:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = seckillSessionService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("coupon:seckillsession:info")
    public R info(@PathVariable("id") Long id) {
        SeckillSessionEntity seckillSession = seckillSessionService.getById(id);

        return R.ok().put("seckillSession", seckillSession);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("coupon:seckillsession:save")
    public R save(@RequestBody SeckillSessionEntity seckillSession) {
        seckillSession.setCreateTime(new Date());
        seckillSessionService.save(seckillSession);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("coupon:seckillsession:update")
    public R update(@RequestBody SeckillSessionEntity seckillSession) {
        seckillSessionService.updateById(seckillSession);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("coupon:seckillsession:delete")
    public R delete(@RequestBody Long[] ids) {
        seckillSessionService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }


    //查出距离当前时间一段时间内的所有秒杀场次及对应的商品
    @GetMapping("/getSessionsInfoByTime")
    R getSessionsInfoByTime(@RequestParam("duration") int duration) {
        List<SeckillSessionEntity> entityList = seckillSessionService.getSessionsInfoByTime(duration);
        return R.ok().put("data", entityList);
    }


}
