package com.lierda.kesi.ihouse.util;

import android.content.Intent;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import java.io.IOException;
/**
 * Created by Administrator on 2017/11/29.
 */

public class RabbitMqHelper {
    private static String host = "202.107.200.162";
    private static String user = "admin";
    private static String psw = "admin";
    private static int port = 8010;
    private static String EXCHANGE_NAME = "otherdev";
    private static Connection connection = null;
    private static Channel channel;

    public RabbitMqHelper(String host, String user, String psw, int port) {
        this.host = host;
        this.user = user;
        this.psw = psw;
        this.port = port;
    }

    public RabbitMqHelper() {
        super();
    }

    public void close() throws IOException {
        if(connection!=null){
            connection.close();
            connection=null;
        }
        if(channel!=null){
            channel.close();
            channel=null;
        }
    }

    private static Channel initChannel() throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(user);
        factory.setPassword(psw);
        connection=factory.newConnection();
        channel=connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "topic",true);
        channel=channel;
        return  channel;
    }


    public static void sendMsg(String msg,String binding) throws IOException {
        Channel channel=initChannel();
        channel.basicPublish(EXCHANGE_NAME,binding,null,msg.getBytes());
        channel.close();
        connection.close();
    }

    public static QueueingConsumer getQueueingConsumer(String binding) throws IOException {
        initChannel();
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName,EXCHANGE_NAME,binding);
        QueueingConsumer queueingConsumer=new QueueingConsumer(channel);
        channel.basicConsume(queueName,true,queueingConsumer);
        return  queueingConsumer;
    }

    public static void recvMsg(String binding) throws IOException, InterruptedException {
        String msg="";
        initChannel();
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName,EXCHANGE_NAME,binding);
        QueueingConsumer queueingConsumer=new QueueingConsumer(channel);
        channel.basicConsume(queueName,true,queueingConsumer);
        while (true){
            QueueingConsumer.Delivery delivery=queueingConsumer.nextDelivery();
            msg=new String(delivery.getBody());
            System.out.println(msg);
        }
    }
}
