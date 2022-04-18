package com.zjw.gulimall.ware.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.ware.dao.WmsWareOrderTaskDetailDao;
import com.zjw.gulimall.ware.entity.WmsWareOrderTaskDetailEntity;
import com.zjw.gulimall.ware.service.WmsWareOrderTaskDetailService;


@Service("wmsWareOrderTaskDetailService")
public class WmsWareOrderTaskDetailServiceImpl extends ServiceImpl<WmsWareOrderTaskDetailDao, WmsWareOrderTaskDetailEntity> implements WmsWareOrderTaskDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WmsWareOrderTaskDetailEntity> page = this.page(
                new Query<WmsWareOrderTaskDetailEntity>().getPage(params),
                new QueryWrapper<WmsWareOrderTaskDetailEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void updateWareTasksDetailStatus(String orderSn, int status) {
        List<WmsWareOrderTaskDetailEntity> detailList = list(new QueryWrapper<WmsWareOrderTaskDetailEntity>().eq("order_sn", orderSn));
        if(detailList != null && detailList.size() > 0){
            detailList = detailList.stream().map(detail -> {
                detail.setLockStatus(status);
                return detail;
            }).collect(Collectors.toList());
            updateBatchById(detailList);
        }
    }

}