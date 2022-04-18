package com.zjw.gulimall.ware.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Random;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.ware.dao.WmsWareInfoDao;
import com.zjw.gulimall.ware.entity.WmsWareInfoEntity;
import com.zjw.gulimall.ware.service.WmsWareInfoService;


@Service("wmsWareInfoService")
public class WmsWareInfoServiceImpl extends ServiceImpl<WmsWareInfoDao, WmsWareInfoEntity> implements WmsWareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WmsWareInfoEntity> queryWrapper = new QueryWrapper<>();
        String cond = (String) params.get("key");
        if(StringUtils.isNotBlank(cond)){
            queryWrapper.eq("id", cond).or().eq("name", cond).or().eq("address", cond).or().eq("areacode", cond);
        }
        IPage<WmsWareInfoEntity> page = this.page(
                new Query<WmsWareInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public int getFare(Long addressId) {
        //随机返回20以内的随机数作为邮费
        return new Random().nextInt(20);
    }

}