package com.zjw.gulimall.ware.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rabbitmq.client.Channel;
import com.zjw.gulimall.ware.config.WareRabbitConfig;
import com.zjw.gulimall.ware.entity.WmsWareOrderTaskEntity;
import com.zjw.gulimall.ware.service.WmsWareOrderTaskService;
import com.zjw.gulimall.ware.service.WmsWareSkuService;
import com.zjw.common.to.OrderEntityVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;





/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-28 16:50
 * @Modifier:
 */

@Service
@Slf4j
@RabbitListener(queues = WareRabbitConfig.WARE_RELEASE_QUEUE)
public class WareRabbitListener
{
    @Resource
    private WmsWareSkuService wmsWareSkuService;
    @Resource
    private WmsWareOrderTaskService orderTaskService;

    //监听释放库存队列，当监听到消息时便表示可能需要完成取消库存操作
    @RabbitHandler
    public void unlockSkuStock(Message message, WmsWareOrderTaskEntity taskEntity, Channel channel){
        try {
            log.info("开始解锁库存....");
            wmsWareSkuService.unlockSkuStock(taskEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (Exception e){
            try {
                log.error("解锁库存失败>>>重试中....");
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    //监听释放库存队列，当监听到消息时便表示可能需要完成取消库存操作
    @RabbitHandler()
    public void unlockSkuStock(Message message, OrderEntityVo orderEntityVo, Channel channel){
        //根据订单号获取相应的taskEntity
        WmsWareOrderTaskEntity taskEntity = orderTaskService.getOne(new QueryWrapper<WmsWareOrderTaskEntity>().eq("order_sn", orderEntityVo.getOrderSn()));
        if(taskEntity != null){
            unlockSkuStock(message, taskEntity, channel);
        }
    }
}
