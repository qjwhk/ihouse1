package com.lierda.kesi.ihouse.util;

import com.alibaba.fastjson.JSON;
import com.lierda.kesi.ihouse.activity.MainActivity;
import com.lierda.kesi.ihouse.po.Alarm;
import com.lierda.kesi.ihouse.po.Attributes;

import java.io.IOException;

/**
 * Created by Administrator on 2017/12/29.
 */

public class Test1 {

    public static void main(String[] args) {
        Alarm alarm=new Alarm("jky","reg","macAddr",new Attributes("123456","bbfXM","QQ"));
        try {
            RabbitMqHelper.sendMsg(JSON.toJSONString(alarm),"dfbvfbgbg.www");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
