package com.zjw.gulimall.order;

import com.rabbitmq.client.*;

import java.io.IOException;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-28 10:46
 * @Modifier:
 */
public class Consumer
{
    public static void main(String[] args) {
        try {
            Connection connection = ConnectionUtil.getConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(Producer.QUEUE_NAME, false, false, false, null);
            DefaultConsumer defaultConsumer = new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //路由key
                    System.out.println("路由key为：" + envelope.getRoutingKey());
                    //交换机
                    System.out.println("交换机为：" + envelope.getExchange());
                    //消息id
                    System.out.println("消息id为：" + envelope.getDeliveryTag());
                    //收到的消息
                    System.out.println("接收到的消息为：" + new String(body, "utf-8"));
                }
            };
            channel.basicConsume(Producer.QUEUE_NAME, true, defaultConsumer);
            //不关闭资源，应该一直监听消息
            //channel.close();
            //connection.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
