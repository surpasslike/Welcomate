package com.surpasslike.welcomateservice.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.surpasslike.welcomateservice.aidl.AdminApiImpl;

/**
 * 后台服务，作为 AIDL 接口的宿主
 * 这个服务的主要职责是在 onBind 方法中返回一个实现了 AIDL 接口的 Binder 对象
 * 客户端应用通过绑定到此服务来与服务端进行跨进程通信 (IPC)
 */
public class AdminService extends Service {
    private static final String TAG = "AdminService";

    /**
     * AIDL 接口的实现实例
     * 为了效率，我们只创建一个实例
     */
    private IBinder binder;

    /**
     * 服务首次创建时调用
     * 在这里始化 Binder 对象
     */
    @Override
    public void onCreate() {
        super.onCreate();
        binder = new AdminApiImpl();
        Log.d(TAG, "AdminService has been created.");
    }

    /**
     * 当客户端绑定到服务时调用
     *
     * @param intent 客户端绑定时传递的 Intent
     * @return 返回一个 IBinder 对象，客户端将使用它与服务进行交互
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "AdminService is being bound.");
        return binder;
    }

    /**
     * 服务销毁时调用
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "AdminService has been destroyed.");
    }
}
