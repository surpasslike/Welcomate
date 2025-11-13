package com.surpasslike.welcomate;

import android.app.Application;

import com.surpasslike.welcomate.service.ServiceManager;

/**
 * Welcomate应用程序类
 * 负责应用程序级别的初始化工作
 */
public class WelcomateApplication extends Application {
    private static final String TAG = "WelcomateApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化ServiceManager
        ServiceManager.getInstance().initialize(this);
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        
        // 释放ServiceManager资源
        ServiceManager.getInstance().release();
    }
}