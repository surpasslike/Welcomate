package com.surpasslike.welcomate.sync;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.surpasslike.welcomate.config.SyncConfig;
import com.surpasslike.welcomateservice.IAdminService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 批量同步管理器
 * 收集同步操作并批量执行，提高性能
 */
public class BatchSyncManager {
    private static final String TAG = "BatchSyncManager";
    private static volatile BatchSyncManager instance;
    
    private final BlockingQueue<SyncOperation> pendingOperations = new LinkedBlockingQueue<>();
    private final SyncCache syncCache = SyncCache.getInstance();
    private final AtomicBoolean isSyncing = new AtomicBoolean(false);
    
    private BatchSyncManager() {}
    
    public static BatchSyncManager getInstance() {
        if (instance == null) {
            synchronized (BatchSyncManager.class) {
                if (instance == null) {
                    instance = new BatchSyncManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 添加同步操作到队列
     * 会先检查缓存，避免重复添加相同的操作
     * 
     * @param operation 要添加的同步操作
     */
    public void addSyncOperation(SyncOperation operation) {
        // 检查缓存，避免重复操作
        if (syncCache.isOperationCached(operation)) {
            if (SyncConfig.DEBUG_SYNC) {
                Log.d(TAG, "Operation cached, skipping: " + operation);
            }
            return;
        }
        
        pendingOperations.offer(operation);
        if (SyncConfig.DEBUG_SYNC) {
            Log.d(TAG, "Added operation to queue: " + operation + ", queue size: " + pendingOperations.size());
        }
    }
    
    /**
     * 执行批量同步
     * 收集队列中的操作并批量发送到服务端，提高同步效率
     * 使用线程安全的方式确保同一时间只有一个批量同步在执行
     * 
     * @param context Android上下文，用于服务绑定
     */
    public void executeBatchSync(Context context) {
        if (isSyncing.getAndSet(true)) {
            if (SyncConfig.DEBUG_SYNC) {
                Log.d(TAG, "Batch sync already in progress, skipping");
            }
            return;
        }
        
        new Thread(() -> {
            try {
                List<SyncOperation> operations = collectOperations();
                if (operations.isEmpty()) {
                    if (SyncConfig.DEBUG_SYNC) {
                        Log.d(TAG, "No operations to sync");
                    }
                    return;
                }
                
                performBatchSync(context, operations);
                
            } catch (Exception e) {
                Log.e(TAG, "Error in batch sync", e);
            } finally {
                isSyncing.set(false);
            }
        }).start();
    }
    
    /**
     * 收集待同步的操作
     */
    private List<SyncOperation> collectOperations() {
        List<SyncOperation> operations = new ArrayList<>();
        
        // 批量收集操作，但不超过最大批量大小
        for (int i = 0; i < SyncConfig.MAX_BATCH_SIZE && !pendingOperations.isEmpty(); i++) {
            SyncOperation operation = pendingOperations.poll();
            if (operation != null) {
                operations.add(operation);
            }
        }
        
        if (SyncConfig.DEBUG_SYNC) {
            Log.d(TAG, "Collected " + operations.size() + " operations for batch sync");
        }
        
        return operations;
    }
    
    /**
     * 执行批量同步操作
     */
    private void performBatchSync(Context context, List<SyncOperation> operations) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(SyncConfig.SERVER_PACKAGE, SyncConfig.SERVER_SERVICE));
        
        WeakServiceConnection connection = new WeakServiceConnection(context, "BatchSync") {
            @Override
            protected void onServiceConnectedSafely(Context context, ComponentName name, IBinder service) {
                Log.d(TAG, "Connected to server for batch sync");
                
                IAdminService remoteService = IAdminService.Stub.asInterface(service);
                executeBatchOperations(remoteService, operations);
            }
            
            @Override
            protected void onServiceDisconnectedSafely(ComponentName name) {
                Log.d(TAG, "Server service disconnected after batch sync");
            }
        };
        
        boolean bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        if (!bound) {
            Log.w(TAG, "Failed to bind to server for batch sync");
            // 将操作重新加入队列
            for (SyncOperation op : operations) {
                pendingOperations.offer(op);
            }
        }
    }
    
    /**
     * 执行批量操作
     */
    private void executeBatchOperations(IAdminService remoteService, List<SyncOperation> operations) {
        int successCount = 0;
        int failCount = 0;
        
        for (SyncOperation operation : operations) {
            try {
                SyncResult result = executeOperation(remoteService, operation);
                
                // 缓存操作结果
                syncCache.cacheOperation(operation, result);
                
                if (result.isSuccess()) {
                    successCount++;
                } else {
                    failCount++;
                    
                    // 如果需要重试，重新加入队列
                    if (result.shouldRetry()) {
                        pendingOperations.offer(operation);
                    }
                }
                
                if (SyncConfig.DEBUG_SYNC) {
                    Log.d(TAG, "Operation result: " + operation.getType() + " -> " + result);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error executing operation: " + operation, e);
                failCount++;
                
                // 异常情况下也可以选择重试
                pendingOperations.offer(operation);
            }
        }
        
        Log.i(TAG, "Batch sync completed: " + successCount + " success, " + failCount + " failed");
    }
    
    /**
     * 执行单个同步操作
     */
    private SyncResult executeOperation(IAdminService remoteService, SyncOperation operation) throws RemoteException {
        switch (operation.getType()) {
            case REGISTER:
                boolean registerSuccess = remoteService.registerUser(
                        operation.getUsername(), 
                        operation.getAccount(), 
                        operation.getPassword()
                );
                return registerSuccess ? SyncResult.SUCCESS : SyncResult.ALREADY_EXISTS;
                
            case UPDATE_PASSWORD:
                remoteService.updateUserPassword(operation.getUsername(), operation.getPassword());
                return SyncResult.SUCCESS;
                
            case DELETE_USER:
                remoteService.deleteUser(operation.getUsername());
                return SyncResult.SUCCESS;
                
            default:
                return SyncResult.INVALID_DATA;
        }
    }
    
    /**
     * 获取待同步操作数量
     */
    public int getPendingOperationsCount() {
        return pendingOperations.size();
    }
    
    /**
     * 清空待同步操作
     */
    public void clearPendingOperations() {
        pendingOperations.clear();
        Log.d(TAG, "Cleared all pending operations");
    }
    
    /**
     * 检查是否正在同步
     */
    public boolean isSyncing() {
        return isSyncing.get();
    }
}