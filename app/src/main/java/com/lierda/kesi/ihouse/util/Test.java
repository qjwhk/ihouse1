package com.lierda.kesi.ihouse.util;

import java.io.IOException;

/**
 * Created by Administrator on 2017/12/19.
 */

public class Test {

    public static void main(String[] args) {
            long time= System.currentTimeMillis()/1000;//获取系统时间的10位的时间戳
            String str= String.valueOf(time);
            System.out.print(str);
//        try {
//            RabbitMqHelper.recvMsg("*.jky");
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
