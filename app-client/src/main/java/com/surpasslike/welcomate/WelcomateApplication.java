package com.surpasslike.welcomate;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.surpasslike.welcomate.service.ServiceManager;

/**
 * Welcomate应用程序类
 * 负责应用程序级别的初始化工作
 */
public class WelcomateApplication extends Application {
    private static final String TAG = "WelcomateApplication";
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        // 初始化ServiceManager
        ServiceManager.getInstance().initialize(mContext);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        // 释放ServiceManager资源
        ServiceManager.getInstance().release();
    }

    public static Context getContext() {
        return mContext;
    }
}