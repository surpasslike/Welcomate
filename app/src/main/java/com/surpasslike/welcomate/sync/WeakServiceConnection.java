package com.surpasslike.welcomate.sync;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * 使用WeakReference的ServiceConnection包装器
 * 防止内存泄漏
 */
public abstract class WeakServiceConnection implements ServiceConnection {
    private static final String TAG = "WeakServiceConnection";
    
    private final WeakReference<Context> contextRef;
    private final String connectionId;
    
    public WeakServiceConnection(Context context, String connectionId) {
        this.contextRef = new WeakReference<>(context);
        this.connectionId = connectionId;
    }
    
    @Override
    public final void onServiceConnected(ComponentName name, IBinder service) {
        Context context = contextRef.get();
        if (context == null) {
            Log.w(TAG, "Context is null for connection: " + connectionId);
            return;
        }
        
        try {
            onServiceConnectedSafely(context, name, service);
        } catch (Exception e) {
            Log.e(TAG, "Error in onServiceConnected for " + connectionId, e);
        } finally {
            // 自动解绑服务，防止内存泄漏
            safeUnbindService(context);
        }
    }
    
    @Override
    public final void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "Service disconnected for connection: " + connectionId);
        onServiceDisconnectedSafely(name);
    }
    
    /**
     * 安全的服务连接回调
     */
    protected abstract void onServiceConnectedSafely(Context context, ComponentName name, IBinder service);
    
    /**
     * 安全的服务断开回调
     */
    protected void onServiceDisconnectedSafely(ComponentName name) {
        // 默认空实现
    }
    
    /**
     * 安全地解绑服务
     */
    private void safeUnbindService(Context context) {
        try {
            context.unbindService(this);
            Log.d(TAG, "Service unbound successfully for: " + connectionId);
        } catch (Exception e) {
            Log.w(TAG, "Error unbinding service for " + connectionId + ": " + e.getMessage());
        }
    }
    
    /**
     * 检查Context是否仍然有效
     */
    protected boolean isContextValid() {
        return contextRef.get() != null;
    }
    
    /**
     * 获取ConnectionId用于调试
     */
    public String getConnectionId() {
        return connectionId;
    }
}