package com.surpasslike.welcomate.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.surpasslike.welcomate.constants.AppConstants;
import com.surpasslike.welcomateservice.IAdminService;

/**
 * Service管理器
 * 负责管理AdminService的连接和生命周期
 * 解决静态变量导致的内存泄漏问题
 */
public class ServiceManager {
    private static final String TAG = "ServiceManager";
    
    private static ServiceManager instance;
    private IAdminService adminService;
    private Context applicationContext;
    private boolean isServiceBound = false;
    private boolean hasBindFailed = false; // 新增：记录绑定失败的状态
    
    // 服务连接对象
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "AdminService connected");
            adminService = IAdminService.Stub.asInterface(service);
            isServiceBound = true;
            
            // 通知所有等待的监听器
            notifyServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "AdminService disconnected");
            adminService = null;
            isServiceBound = false;
            
            // 通知所有监听器服务断开
            notifyServiceDisconnected();
        }
    };
    
    // 服务连接状态监听器
    public interface ServiceConnectionListener {
        void onServiceConnected(IAdminService service);
        void onServiceDisconnected();
        void onServiceBindFailed();
    }
    
    private ServiceConnectionListener connectionListener;
    
    private ServiceManager() {
        // 私有构造函数，确保单例
    }
    
    /**
     * 获取ServiceManager单例实例
     * 确保整个应用只有一个Service连接管理器
     * @return ServiceManager实例
     */
    public static synchronized ServiceManager getInstance() {
        if (instance == null) {
            instance = new ServiceManager();
        }
        return instance;
    }
    
    /**
     * 初始化ServiceManager
     * Application Context生命周期等于应用生命周期，不会导致Activity内存泄漏
     * @param context Application Context
     */
    public void initialize(Context context) {
        this.applicationContext = context.getApplicationContext();
        bindAdminService();
    }
    
    /**
     * 绑定AdminService
     */
    private void bindAdminService() {
        if (applicationContext == null) {
            Log.e(TAG, "Application context is null, cannot bind service");
            notifyServiceBindFailed();
            return;
        }
        
        // 重置绑定失败状态
        hasBindFailed = false;
        
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(
            AppConstants.Service.ADMIN_SERVICE_PACKAGE, 
            AppConstants.Service.ADMIN_SERVICE_CLASS
        ));
        
        boolean success = applicationContext.bindService(
            intent, 
            serviceConnection, 
            Context.BIND_AUTO_CREATE
        );
        
        if (success) {
            Log.d(TAG, "AdminService binding initiated");
            // 设置超时检查，如果3秒后还没连接成功，认为绑定失败
            new android.os.Handler().postDelayed(() -> {
                if (!isServiceConnected()) {
                    Log.e(TAG, "Service binding timeout");
                    notifyServiceBindFailed();
                }
            }, 3000);
        } else {
            Log.e(TAG, "Failed to bind AdminService immediately");
            notifyServiceBindFailed();
        }
    }
    
    /**
     * 获取AdminService实例
     * @return IAdminService实例，如果未连接则返回null
     */
    public IAdminService getAdminService() {
        return adminService;
    }
    
    /**
     * 检查服务是否已连接
     * @return true表示已连接，false表示未连接
     */
    public boolean isServiceConnected() {
        return isServiceBound && adminService != null;
    }
    
    /**
     * 设置服务连接状态监听器
     * @param listener 监听器
     */
    public void setServiceConnectionListener(ServiceConnectionListener listener) {
        this.connectionListener = listener;
        
        // 如果服务已经连接，立即通知
        if (isServiceConnected()) {
            listener.onServiceConnected(adminService);
        } else if (hasBindFailed) {
            // 如果已经绑定失败，立即通知
            listener.onServiceBindFailed();
        }
    }
    
    /**
     * 移除服务连接状态监听器
     */
    public void removeServiceConnectionListener() {
        this.connectionListener = null;
    }
    
    /**
     * 通知服务连接成功
     */
    private void notifyServiceConnected() {
        if (connectionListener != null) {
            connectionListener.onServiceConnected(adminService);
        }
    }
    
    /**
     * 通知服务断开连接
     */
    private void notifyServiceDisconnected() {
        if (connectionListener != null) {
            connectionListener.onServiceDisconnected();
        }
    }
    
    /**
     * 通知服务绑定失败
     */
    private void notifyServiceBindFailed() {
        hasBindFailed = true; // 设置失败状态
        if (connectionListener != null) {
            connectionListener.onServiceBindFailed();
        }
    }
    
    /**
     * 释放资源
     * 在Application的onTerminate中调用
     */
    public void release() {
        if (applicationContext != null && isServiceBound) {
            try {
                applicationContext.unbindService(serviceConnection);
                Log.d(TAG, "AdminService unbound");
            } catch (Exception e) {
                Log.e(TAG, "Error unbinding service", e);
            }
        }
        
        adminService = null;
        isServiceBound = false;
        connectionListener = null;
        applicationContext = null;
    }
}