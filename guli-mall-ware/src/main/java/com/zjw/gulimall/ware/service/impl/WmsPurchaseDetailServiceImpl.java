package com.zjw.gulimall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.zjw.common.to.SkuInfoEntityTo;
import com.zjw.common.utils.R;
import com.zjw.gulimall.ware.feign.ProductFeignService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.ware.dao.WmsPurchaseDetailDao;
import com.zjw.gulimall.ware.entity.WmsPurchaseDetailEntity;
import com.zjw.gulimall.ware.service.WmsPurchaseDetailService;

import javax.annotation.Resource;


@Service("wmsPurchaseDetailService")
public class WmsPurchaseDetailServiceImpl extends ServiceImpl<WmsPurchaseDetailDao, WmsPurchaseDetailEntity> implements WmsPurchaseDetailService {

    @Resource
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WmsPurchaseDetailEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(StringUtils.isNotBlank(key)){
            queryWrapper.eq("purchase_id", key).or().eq("sku_id", key).or().eq("id", key);
        }
        String wareId = (String) params.get("wareId");
        if(StringUtils.isNotBlank(wareId)){
            queryWrapper.eq("ware_id", wareId);
        }
        String status = (String) params.get("status");
        if(StringUtils.isNotBlank(status)){
            queryWrapper.eq("status", status);
        }
        IPage<WmsPurchaseDetailEntity> page = this.page(
                new Query<WmsPurchaseDetailEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public boolean savePurchaseDetail(WmsPurchaseDetailEntity wmsPurchaseDetail) {
        Long skuId = wmsPurchaseDetail.getSkuId();
        //调用远程服务查询对应sku的详细信息
        Object skuInfo = productFeignService.getSkuInfo(skuId).get("skuInfo");
        SkuInfoEntityTo skuInfoEntityTo = new SkuInfoEntityTo();
        if(skuInfo != null){
            HashMap<String, Object> map = (HashMap<String, Object>) skuInfo;
            BigDecimal price = new BigDecimal(map.get("price").toString());
            wmsPurchaseDetail.setSkuPrice(price);
        }
        return save(wmsPurchaseDetail);
    }

}