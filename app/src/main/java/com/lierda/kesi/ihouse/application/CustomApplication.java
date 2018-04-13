package com.lierda.kesi.ihouse.application;

import android.app.Activity;
import android.app.Application;

/**
 * Created by zhaochong on 2017/11/24.
 */

public class CustomApplication extends Application {

    public final static String APPNAME = "Lierda";
    public final String TAG = "CustomApplication";
    public static String LECHANGE_ID = "lc4ed0924fbf884253";
    public static String LECHANGE_PASSWORD = "566dd560597744a2a390e44faaec43";
//    public static String LECHANGE_ADMIN_ACCOUNT = "86ce42202e23496c";
    public static String LECHANGE_ADMIN_ACCOUNT = "17357155010";
    public static boolean MARK_1 = false;
    public static boolean MARK_2 = false;
    public static boolean MARK_3 = false;
    public static Activity MAIN_ACTIVITY;

    @Override
    public void onCreate() {
        super.onCreate();
    }
}