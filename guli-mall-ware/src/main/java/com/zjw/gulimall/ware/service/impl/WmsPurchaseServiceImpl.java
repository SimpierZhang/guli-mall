package com.zjw.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.zjw.common.constant.WareConstant;
import com.zjw.common.to.SkuInfoEntityTo;
import com.zjw.gulimall.ware.entity.WmsPurchaseDetailEntity;
import com.zjw.gulimall.ware.entity.WmsWareSkuEntity;
import com.zjw.gulimall.ware.feign.ProductFeignService;
import com.zjw.gulimall.ware.service.WmsPurchaseDetailService;
import com.zjw.gulimall.ware.service.WmsWareSkuService;
import com.zjw.gulimall.ware.vo.DonePurchaseDetailVo;
import com.zjw.gulimall.ware.vo.DonePurchaseVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.ware.dao.WmsPurchaseDao;
import com.zjw.gulimall.ware.entity.WmsPurchaseEntity;
import com.zjw.gulimall.ware.service.WmsPurchaseService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Slf4j
@Service("wmsPurchaseService")
public class WmsPurchaseServiceImpl extends ServiceImpl<WmsPurchaseDao, WmsPurchaseEntity> implements WmsPurchaseService
{

    @Resource
    private WmsPurchaseDetailService wmsPurchaseDetailService;
    @Resource
    private WmsWareSkuService wmsWareSkuService;
    @Resource
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WmsPurchaseEntity> queryWrapper = new QueryWrapper<>();
        String status = (String) params.get("status");
        if(StringUtils.isNotBlank(status)){
            queryWrapper.eq("status", status);
        }
        String key = (String) params.get("key");
        if(StringUtils.isNotBlank(key)){
            //????????????id???????????????????????????????????????
            queryWrapper.eq("id", key).or().eq("ware_id", key).or().like("assignee_name", key);
        }
        IPage<WmsPurchaseEntity> page = this.page(
                new Query<WmsPurchaseEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Override
    public PageUtils getUnreceivePurchaseList() {
        //??????????????????????????????
        QueryWrapper<WmsPurchaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", WareConstant.PurchaseStatusEnum.PURCHASE_STATUS_NEW.getCode());
        IPage<WmsPurchaseEntity> page = this.page(new Query<WmsPurchaseEntity>().getPage(new HashMap<>()), queryWrapper);
        return new PageUtils(page);
    }

    @Override
    @Transactional
    public boolean merge(Long purchaseId, List<Long> purchaseDetailId) {
        //???????????????????????????
        List<WmsPurchaseDetailEntity> purchaseDetailEntityList = wmsPurchaseDetailService.listByIds(purchaseDetailId);
        if (purchaseDetailEntityList == null || purchaseDetailEntityList.size() == 0) return false;
        if (purchaseId != null) {
            //????????????????????????????????????
            WmsPurchaseEntity purchaseEntity = getById(purchaseId);
            BigDecimal totalPrice = purchaseEntity.getAmount();
            for(WmsPurchaseDetailEntity item : purchaseDetailEntityList){
                item.setPurchaseId(purchaseId);
                item.setStatus(WareConstant.PurchaseStatusEnum.PURCHASE_STATUS_ASSIGN.getCode());
                totalPrice = totalPrice.add(item.getSkuPrice());
            }
            purchaseEntity.setAmount(totalPrice);
            purchaseEntity.setUpdateTime(new Date());
            updateById(purchaseEntity);
        }else {
            //???????????????????????????????????????????????????????????????
            WmsPurchaseEntity wmsPurchaseEntity = new WmsPurchaseEntity();
            wmsPurchaseEntity.setCreateTime(new Date());
            wmsPurchaseEntity.setUpdateTime(new Date());
            wmsPurchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.PURCHASE_STATUS_NEW.getCode());
            wmsPurchaseEntity.setPriority(1);
            wmsPurchaseEntity.setWareId(purchaseDetailEntityList.get(0).getWareId());
            save(wmsPurchaseEntity);//?????????????????????????????????id
            BigDecimal totalPrice = new BigDecimal(0);
            for(WmsPurchaseDetailEntity item : purchaseDetailEntityList){
                item.setPurchaseId(wmsPurchaseEntity.getId());
                item.setStatus(WareConstant.PurchaseStatusEnum.PURCHASE_STATUS_ASSIGN.getCode());
                totalPrice = totalPrice.add(item.getSkuPrice());
            }
            wmsPurchaseEntity.setAmount(totalPrice);
            updateById(wmsPurchaseEntity);
        }
        return wmsPurchaseDetailService.updateBatchById(purchaseDetailEntityList);
    }

    @Transactional
    @Override
    public void receivePurchaseList(List<Long> purchaseIds) {
        //1.????????????????????????
        List<WmsPurchaseEntity> purchaseEntityList = list(new QueryWrapper<WmsPurchaseEntity>().in("id", purchaseIds));
        if(purchaseEntityList != null && purchaseEntityList.size() > 0){
            //2.?????????????????????????????????
            List<WmsPurchaseEntity> updateList = purchaseEntityList.stream().map(p -> {
                p.setUpdateTime(new Date());
                p.setStatus(WareConstant.PurchaseStatusEnum.PURCHASE_STATUS_RECEIVED.getCode());
                return p;
            }).collect(Collectors.toList());
            updateBatchById(updateList);
            //3.????????????????????????????????????????????????
            List<WmsPurchaseDetailEntity> purchaseDetailEntityList = wmsPurchaseDetailService.list(new QueryWrapper<WmsPurchaseDetailEntity>().in("purchase_id", purchaseEntityList.stream().
                    map(WmsPurchaseEntity::getId).collect(Collectors.toList())));
            if(purchaseDetailEntityList != null && purchaseDetailEntityList.size() > 0){
                purchaseDetailEntityList.forEach(pd -> pd.setStatus(WareConstant.PurchaseDetailStatusEnum.PURCHASE_STATUS_BUYING.getCode()));
                wmsPurchaseDetailService.updateBatchById(purchaseDetailEntityList);
            }
        }
    }

    @Override
    @Transactional
    public void donePurchase(DonePurchaseVo donePurchaseVo) {
        //????????????
        List<DonePurchaseDetailVo> items = donePurchaseVo.getItems();
        boolean doneSuccess = true;
        //???????????????????????????????????????????????????????????????????????????
        for(DonePurchaseDetailVo item : items){
            if(item.getStatus() != WareConstant.PurchaseDetailStatusEnum.PURCHASE_STATUS_FINISHED.getCode()){
                doneSuccess = false;
                break;
            }
        }
        //????????????????????????,?????????????????????
        List<WmsPurchaseDetailEntity> purchaseDetailList = items.stream().map(item -> {
            WmsPurchaseDetailEntity detailEntity = new WmsPurchaseDetailEntity();
            detailEntity.setId(item.getItemId());
            detailEntity.setStatus(item.getStatus());
            return detailEntity;
        }).collect(Collectors.toList());
        if(purchaseDetailList.size() > 0){
            //????????????????????????
            wmsPurchaseDetailService.updateBatchById(purchaseDetailList);
            //??????????????????????????????
            List<Long> detailIdList = purchaseDetailList.stream().map(WmsPurchaseDetailEntity::getId).collect(Collectors.toList());
            List<WmsPurchaseDetailEntity> purchaseDetailEntityList = wmsPurchaseDetailService.listByIds(detailIdList);
            //?????????????????????????????????????????????sku???????????????
            List<SkuInfoEntityTo> skuInfoEntityToList = productFeignService.getSkuInfoList(purchaseDetailEntityList.stream().map(WmsPurchaseDetailEntity::getSkuId).
                    collect(Collectors.toList())).getData(new TypeReference<List<SkuInfoEntityTo>>(){});
            List<WmsWareSkuEntity> wareSkuEntityList = purchaseDetailEntityList.stream().map(detail -> {
                WmsWareSkuEntity wareSkuEntity = new WmsWareSkuEntity();
                wareSkuEntity.setSkuId(detail.getSkuId());
                wareSkuEntity.setWareId(detail.getWareId());
                wareSkuEntity.setStock(detail.getSkuNum());
                for(SkuInfoEntityTo item : skuInfoEntityToList){
                    if(item.getSkuId().equals(detail.getSkuId())){
                        wareSkuEntity.setSkuName(item.getSkuName());
                        break;
                    }
                }
                wareSkuEntity.setStockLocked(0);
                return wareSkuEntity;
            }).collect(Collectors.toList());
            wmsWareSkuService.saveBatch(wareSkuEntityList);
        }
        //?????????????????????,???????????????????????????
        Long purchaseId = donePurchaseVo.getId();
        if(purchaseId != null){
            WmsPurchaseEntity purchaseEntity = getById(purchaseId);
            if(purchaseEntity != null){
                purchaseEntity.setStatus(doneSuccess ? WareConstant.PurchaseStatusEnum.PURCHASE_STATUS_FINISHED.getCode() :
                        WareConstant.PurchaseStatusEnum.PURCHASE_STATUS_FAIL.getCode());
                updateById(purchaseEntity);
            }
        }
    }

    @Override
    public boolean savePurchase(WmsPurchaseEntity wmsPurchase) {
        wmsPurchase.setStatus(WareConstant.PurchaseStatusEnum.PURCHASE_STATUS_NEW.getCode());
        wmsPurchase.setCreateTime(new Date());
        wmsPurchase.setUpdateTime(new Date());
        return save(wmsPurchase);
    }

}