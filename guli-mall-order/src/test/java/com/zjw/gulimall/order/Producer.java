package com.zjw.gulimall.order;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.zjw.common.utils.Query;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-28 10:34
 * @Modifier:
 */
public class Producer
{
    public static final String QUEUE_NAME = "simple_queue";

    public static void main(String[] args) {
        Channel channel = null;
        Connection connection = null;
        try {
            connection = ConnectionUtil.getConnection();
            //创建信道
            channel = connection.createChannel();
            //声明队列
            /**
             * 参数1：队列名称
             * 参数2：是否定义持久化队列
             * 参数3：是否独占本次连接
             * 参数4：是否在不使用的时候自动删除队列
             * 参数5：队列其它参数
             */
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String msg = "Hello Test";
            /**
             * 参数1：交换机名称，如果没有指定则使用默认Default Exchage
             * 参数2：路由key,简单模式可以传递队列名称
             * 参数3：消息其它属性
             * 参数4：消息内容
             */
            channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
            System.out.println("消息已经发送:" + msg);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (channel != null)
                    channel.close();
                if (connection != null)
                    connection.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
