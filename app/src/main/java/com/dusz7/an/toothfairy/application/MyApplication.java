package com.dusz7.an.toothfairy.application;

import android.app.Application;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by dusz2 on 2016/12/27 0027.
 */

public class MyApplication extends Application {


    @Override
    public void onCreate() {

        super.onCreate();

        //极光推送平台初始化
        JPushInterface.setDebugMode(true); 	// 设置开启日志,发布时请关闭日志
        JPushInterface.init(this);     		// 初始化 JPush

        //讯飞平台的应用初始化
        SpeechUtility.createUtility(this, SpeechConstant.APPID +"=585e8f4f");
    }
}
