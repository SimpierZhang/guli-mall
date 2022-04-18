package com.zjw.gulimall.coupon.service.impl;

import com.zjw.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.zjw.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.coupon.dao.SeckillSessionDao;
import com.zjw.gulimall.coupon.entity.SeckillSessionEntity;
import com.zjw.gulimall.coupon.service.SeckillSessionService;

import javax.annotation.Resource;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService
{
    @Resource
    private SeckillSkuRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getSessionsInfoByTime(int duration) {
        //1.获取当前时间
        String startTime = getStartTime();
        String endTime = getEndTime(duration);
        //2.查出一段时间内的所有秒杀场次
        List<SeckillSessionEntity> entityList = list(new QueryWrapper<SeckillSessionEntity>().ge("end_time", startTime).and(q -> {
            q.le("start_time", endTime);
        }));
        //3.初始化场次信息中的商品信息
        if (entityList != null && entityList.size() > 0) {
            List<Long> sessionIdList = entityList.stream().map(SeckillSessionEntity::getId).collect(Collectors.toList());
            List<SeckillSkuRelationEntity> relationEntityList =
                    relationService.list(new QueryWrapper<SeckillSkuRelationEntity>().in("promotion_session_id", sessionIdList));
            if(relationEntityList != null && relationEntityList.size() > 0){
                entityList.forEach(entity -> {
                    List<SeckillSkuRelationEntity> list = new ArrayList<>();
                    relationEntityList.forEach(relationEntity -> {
                        if(entity.getId().equals(relationEntity.getPromotionSessionId())){
                            list.add(relationEntity);
                        }
                    });
                    entity.setRelationSkus(list);
                });
            }
        }
        return entityList;
    }

    private String getEndTime(int duration) {
        //假设今天是8.31
        LocalDate now = LocalDate.now();
        //endDayTime表示的就是8.31 + 指定天数 - 1(默认是3，减一的原因是因为当天已经计算了) >> 9.2
        LocalDate endDayTime = now.plusDays(duration - 1);
        //endTime表示的就是9.2 23:59
        LocalDateTime endTime = endDayTime.atTime(LocalTime.MAX);
        return endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
    }

    private String getStartTime() {
        LocalDate now = LocalDate.now();
        //这个表示今天的零点，假设今天是8.31 --》 这个就表示8.31 00：00
        //因为要统计的是今天之后的一段时间后的秒杀活动，所以今天也要算上
        LocalDateTime startTime = now.atTime(LocalTime.MIN);
        System.out.println(startTime);
        return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
    }

}