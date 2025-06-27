package com.surpasslike.welcomate;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.surpasslike.welcomate.service.ClientSyncService;

/**
 * Welcomate应用程序类
 * 负责应用程序级别的初始化，包括启动ClientSyncService
 */
public class WelcomateApplication extends Application {
    
    private static final String TAG = "WelcomateApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "WelcomateApplication onCreate");
        
        // 应用启动时立即启动ClientSyncService
        // 这样即使用户没有进入MainActivity，服务端也能连接到客户端
        startClientSyncService();
    }
    
    /**
     * 启动客户端同步服务
     */
    private void startClientSyncService() {
        try {
            Intent serviceIntent = new Intent(this, ClientSyncService.class);
            startService(serviceIntent);
            Log.d(TAG, "ClientSyncService started from Application");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start ClientSyncService from Application", e);
        }
    }
}