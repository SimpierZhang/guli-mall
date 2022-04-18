package com.zjw.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjw.common.utils.PageUtils;
import com.zjw.gulimall.ware.entity.WmsPurchaseEntity;
import com.zjw.gulimall.ware.vo.DonePurchaseVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 19:00:46
 */
public interface WmsPurchaseService extends IService<WmsPurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils getUnreceivePurchaseList();

    boolean merge(Long purchaseId, List<Long> purchaseDetailId);

    void receivePurchaseList(List<Long> purchaseIds);

    void donePurchase(DonePurchaseVo donePurchaseVo);

    boolean savePurchase(WmsPurchaseEntity wmsPurchase);
}

