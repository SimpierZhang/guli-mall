package com.zjw.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjw.common.utils.PageUtils;
import com.zjw.gulimall.ware.entity.WmsWareOrderTaskEntity;
import com.zjw.gulimall.ware.entity.WmsWareSkuEntity;
import com.zjw.gulimall.ware.vo.WareSkuLockVo;

import java.util.Map;

/**
 * εεεΊε­
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 19:00:46
 */
public interface WmsWareSkuService extends IService<WmsWareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    boolean lockWareItems(WareSkuLockVo wareSkuLockVo);

    void unlockSkuStock(WmsWareOrderTaskEntity taskEntity);
}

