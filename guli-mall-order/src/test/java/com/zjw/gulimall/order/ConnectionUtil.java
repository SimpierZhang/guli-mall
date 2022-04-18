package com.zjw.gulimall.order;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-28 10:35
 * @Modifier:
 */
public class ConnectionUtil
{
    public static Connection getConnection() throws Exception {
        //1.创建连接工厂
        ConnectionFactory connectionFactory = new ConnectionFactory();
        //2.配置主机地址
        connectionFactory.setHost("192.168.233.216");
        //3.配置端口号
        connectionFactory.setPort(5672);
        //4.配置虚拟主机
        connectionFactory.setVirtualHost("/");
        //5.配置用户名和密码
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        //6.创建连接
        return connectionFactory.newConnection();
    }
}
