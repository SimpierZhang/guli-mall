package com.zjw.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.zjw.common.constant.OrderConstant;
import com.zjw.common.constant.WareConstant;
import com.zjw.common.exception.StockLackException;
import com.zjw.common.utils.R;
import com.zjw.gulimall.ware.config.WareRabbitConfig;
import com.zjw.gulimall.ware.entity.WmsWareOrderTaskDetailEntity;
import com.zjw.gulimall.ware.entity.WmsWareOrderTaskEntity;
import com.zjw.gulimall.ware.feign.OrderFeignService;
import com.zjw.gulimall.ware.service.WmsWareOrderTaskDetailService;
import com.zjw.gulimall.ware.service.WmsWareOrderTaskService;
import com.zjw.common.to.OrderEntityVo;
import com.zjw.gulimall.ware.vo.OrderItemVo;
import com.zjw.gulimall.ware.vo.WareSkuLockVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.ware.dao.WmsWareSkuDao;
import com.zjw.gulimall.ware.entity.WmsWareSkuEntity;
import com.zjw.gulimall.ware.service.WmsWareSkuService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Slf4j
@Service("wmsWareSkuService")
public class WmsWareSkuServiceImpl extends ServiceImpl<WmsWareSkuDao, WmsWareSkuEntity> implements WmsWareSkuService
{

    @Resource
    private OrderFeignService orderFeignService;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private WmsWareOrderTaskService wmsWareOrderTaskService;
    @Resource
    private WmsWareOrderTaskDetailService wmsWareOrderTaskDetailService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WmsWareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (StringUtils.isNotBlank(skuId)) {
            queryWrapper.eq("sku_id", skuId);
        }
        String wareId = (String) params.get("wareId");
        if (StringUtils.isNotBlank(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }
        IPage<WmsWareSkuEntity> page = this.page(
                new Query<WmsWareSkuEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public boolean lockWareItems(WareSkuLockVo wareSkuLockVo) {
        lockSkuStock(wareSkuLockVo);
        return true;
    }

    //解锁商品库存
    @Override
    public void unlockSkuStock(WmsWareOrderTaskEntity taskEntity) {
        //什么情况需要解锁商品库存？
        if(taskEntity == null) return;
        String orderSn = taskEntity.getOrderSn();
        //根据订单号查询订单信息
        try {
            R r = orderFeignService.getOrderInfoByOrderSn(orderSn);
            OrderEntityVo orderEntityVo = r.getData(new TypeReference<OrderEntityVo>()
            {
            });
            if(orderEntityVo == null || orderEntityVo.getStatus() == OrderConstant.OrderStatus.ORDER_STATUS_CANCELED.getStatusCode()){
                //1.订单自动取消或者手动取消之后，库存需要解锁
                //2.监听到解锁库存的消息，但是查询订单发现无相应订单，说明订单已经回滚，库存需要解锁
                unlockStock(taskEntity);
            }
        }catch (Exception e){
            log.error("查询订单信息失败");
            //将异常重新抛出，这样消息队列的消息便会重发，然后会重新调用该方法
            throw new RuntimeException(e);
        }

    }

    private void unlockStock(WmsWareOrderTaskEntity taskEntity) {
        //1.根据taskId查出相应的锁定库存的详情
        List<WmsWareOrderTaskDetailEntity> detailEntityList =
                wmsWareOrderTaskDetailService.list(new QueryWrapper<WmsWareOrderTaskDetailEntity>().eq("task_id", taskEntity.getId()));
        //要进行解锁的库存
        List<WmsWareSkuEntity> unlockSkuEntityList = new ArrayList<>();
        detailEntityList.stream().filter(Objects::nonNull).forEach(detail -> {
            Integer lockNum = detail.getSkuNum();
            Long wareSkuId = detail.getWareSkuId();
            //并且只有当其处于锁定状态时才可以解锁
            if(detail.getLockStatus() == WareConstant.WareOrderTaskDetailStatusEnum.TASK_DETAIL_STATUS_LOCKED.getCode()){
                if(lockNum != null && wareSkuId != null){
                    baseMapper.unlockStock(lockNum, wareSkuId);
                    detail.setLockStatus(WareConstant.WareOrderTaskDetailStatusEnum.TASK_DETAIL_STATUS_UNLOCKED.getCode());
                }
            }
        });
        //更新一下锁定状态
        wmsWareOrderTaskDetailService.updateBatchById(detailEntityList);
    }

    //锁定商品库存
    private void lockSkuStock(WareSkuLockVo wareSkuLockVo) {
        //锁库存的逻辑是>>根据skuId到wms_ware_sku查询哪个仓库拥有该sku的库存
        //然后对每一个仓库进行遍历，如果该仓库该sku的数量>=购买数量 >>> 对该仓库的该sku产品进行锁库存，并且还要将锁好的库存数量进行保存，以便回滚，剩下的仓库也无需再遍历
        //如果所有仓库遍历之后都不满足该sku的要求，那么该sku锁库存失败，返回锁库存失败的skuInfo >>> 并且由于锁库存失败，应当进行回滚
        String orderSn = wareSkuLockVo.getOrderSn();
        List<OrderItemVo> lockOrderItems = wareSkuLockVo.getLocks();
        //两种提高查询效率的办法
        //1.给skuId加上索引，然后一个个的sku进行查询，这样每次查询的效率可以到达ref
        //2.不加索引，一次将所有sku的仓库进行查询，然后遍历得到每个sku的库存信息，这样只需要查询一次，但查询效率为range
        List<Long> lockSkuIdList = lockOrderItems.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
        List<WmsWareSkuEntity> wareSkuInfoList = list(new QueryWrapper<WmsWareSkuEntity>().in("sku_id", lockSkuIdList));
        WmsWareOrderTaskEntity lockTaskEntity = new WmsWareOrderTaskEntity();
        lockTaskEntity.setOrderSn(orderSn);
        wmsWareOrderTaskService.save(lockTaskEntity);
        Long taskId = lockTaskEntity.getId();
        List<WmsWareOrderTaskDetailEntity> wmsWareOrderTaskDetailEntityList = new ArrayList<>();
        //锁定库存成功后应当更新的库存信息
        List<WmsWareSkuEntity> lockedSkuEntityList = new ArrayList<>();
        boolean hasStock = false;
        for(OrderItemVo orderItemVo : lockOrderItems){
            for(WmsWareSkuEntity wareSkuEntity : wareSkuInfoList){
                if(wareSkuEntity.getSkuId().equals(orderItemVo.getSkuId())){
                    if(wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() >= orderItemVo.getCount()){
                        WmsWareOrderTaskDetailEntity detailEntity = new WmsWareOrderTaskDetailEntity();
                        detailEntity.setSkuNum(orderItemVo.getCount());
                        detailEntity.setSkuId(wareSkuEntity.getSkuId());
                        detailEntity.setSkuName(wareSkuEntity.getSkuName());
                        detailEntity.setTaskId(taskId);
                        detailEntity.setWareId(wareSkuEntity.getWareId());
                        detailEntity.setLockStatus(WareConstant.WareOrderTaskDetailStatusEnum.TASK_DETAIL_STATUS_LOCKED.getCode());
                        detailEntity.setWareSkuId(wareSkuEntity.getId());
                        wmsWareOrderTaskDetailEntityList.add(detailEntity);
                        //更新库存信息
                        wareSkuEntity.setStockLocked(wareSkuEntity.getStockLocked() + orderItemVo.getCount());
                        wareSkuEntity.setStock(wareSkuEntity.getStock() - orderItemVo.getCount());
                        lockedSkuEntityList.add(wareSkuEntity);
                        hasStock = true;
                        break;
                    }
                }
            }
            if(!hasStock){
                log.error("{}号商品库存不足",  orderItemVo.getSkuId().toString());
                throw new StockLackException(orderItemVo.getSkuId().toString() + "号商品库存不足");
            }
            hasStock = false;
        }
        wmsWareOrderTaskDetailService.saveBatch(wmsWareOrderTaskDetailEntityList);
        updateBatchById(lockedSkuEntityList);
        //锁定库存之后应当发送消息给MQ，用于事务失败后回滚解锁库存
        rabbitTemplate.convertAndSend(WareRabbitConfig.WARE_EVENT_EXCHANGE, WareRabbitConfig.wareDelayRouteKey, lockTaskEntity);
        log.info("锁定库存成功，并发送信息给解锁库存队列");
    }

}