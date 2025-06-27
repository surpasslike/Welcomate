package com.surpasslike.welcomate.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.surpasslike.welcomate.config.SyncConfig;
import com.surpasslike.welcomate.constants.AppConstants;
import com.surpasslike.welcomate.database.LocalUserManager;
import com.surpasslike.welcomate.sync.BatchSyncManager;
import com.surpasslike.welcomate.sync.SyncCache;
import com.surpasslike.welcomate.sync.SyncOperation;
import com.surpasslike.welcomate.sync.SyncResult;
import com.surpasslike.welcomate.sync.WeakServiceConnection;
import com.surpasslike.welcomateservice.IAdminService;

import java.util.List;

/**
 * 用户服务管理类 - 客户端核心业务逻辑
 * 
 * 功能职责：
 * 1. 统一管理本地数据库A和服务端数据库B的双向同步
 * 2. 提供离线优先的用户操作接口（注册、登录、改密、删除）
 * 3. 智能选择本地模式或远程模式执行操作
 * 4. 实现批量同步和直接通知的双重保障机制
 * 5. 管理与服务端的连接状态和生命周期
 * 
 * 同步策略：
 * - 服务端可用时：先写服务端B，再写本地A，确保数据一致性
 * - 服务端不可用时：先写本地A，然后通过直接通知+批量队列双重机制同步到服务端
 * - 启动时：执行双向同步，合并本地A和服务端B的数据
 */
public class UserService {
    private static final String TAG = "UserService";
    
    private LocalUserManager localUserManager;
    private IAdminService remoteService;
    private Context context;
    private Handler mainHandler;
    private BatchSyncManager batchSyncManager;
    private SyncCache syncCache;
    
    /**
     * 构造函数 - 初始化用户服务
     * 
     * @param context Android上下文，用于数据库操作和服务绑定
     */
    public UserService(Context context) {
        this.context = context;
        this.localUserManager = LocalUserManager.getInstance(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.batchSyncManager = BatchSyncManager.getInstance();
        this.syncCache = SyncCache.getInstance();
    }
    
    /**
     * 设置远程服务连接
     * 当MainActivity成功连接到服务端时调用，会自动触发双向同步
     * 
     * @param remoteService 服务端AIDL接口实例，为null表示连接断开
     */
    public void setRemoteService(IAdminService remoteService) {
        this.remoteService = remoteService;
        if (remoteService != null) {
            Log.d(TAG, "Remote service connected, starting bidirectional sync");
            performBidirectionalSync();
        }
    }
    
    /**
     * 检查远程服务是否可用
     * 
     * @return true表示可以直接调用服务端接口，false表示需要离线模式
     */
    public boolean isRemoteServiceAvailable() {
        return remoteService != null;
    }
    
    /**
     * 用户登录验证
     * 
     * 登录策略：
     * - 服务端可用：优先使用服务端数据库B验证，确保最新状态
     * - 服务端不可用：使用本地数据库A验证，支持离线登录
     * - 连接异常：自动降级到本地验证
     * 
     * @param account 用户账号
     * @param password 用户密码
     * @return 登录成功返回用户名，失败返回null
     */
    public String login(String account, String password) {
        Log.d(TAG, "Login attempt for account: " + account);
        
        if (isRemoteServiceAvailable()) {
            // 服务端存在，以服务端为准
            try {
                Log.d(TAG, "Using server data (B) for login");
                String remoteResult = remoteService.loginAdmin(account, password);
                if (remoteResult != null) {
                    Log.d(TAG, "Server login successful: " + remoteResult);
                    return remoteResult;
                } else {
                    Log.d(TAG, "Server login failed - invalid credentials");
                    return null;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Server connection error, falling back to local data (A)", e);
            }
        }
        
        // 服务端不存在或连接失败，使用本地数据A
        Log.d(TAG, "Using local data (A) for login");
        String localResult = localUserManager.loginUser(account, password);
        if (localResult != null) {
            Log.d(TAG, "Local login successful: " + localResult);
        } else {
            Log.d(TAG, "Local login failed");
        }
        return localResult;
    }
    
    /**
     * 用户注册
     * 
     * 注册策略：
     * - 服务端可用：双向写入（先写服务端B，再写本地A），确保数据一致性
     * - 服务端不可用：先写本地A，然后通过直接通知+批量队列双重机制同步到服务端
     * 
     * 同步保障：
     * 1. 直接通知：立即尝试连接服务端并注册
     * 2. 批量队列：作为备用机制，确保数据最终一致性
     * 
     * @param username 用户名
     * @param account 用户账号
     * @param password 用户密码
     * @return 注册成功返回true，失败返回false
     */
    public boolean register(String username, String account, String password) {
        Log.d(TAG, "Register user: " + username + ", account: " + account);
        
        if (isRemoteServiceAvailable()) {
            // 服务端存在，双向同步：同时写入A和B
            Log.d(TAG, "Server available, performing bidirectional write");
            try {
                // 先写服务端B
                boolean remoteSuccess = remoteService.registerUser(username, account, password);
                if (remoteSuccess) {
                    Log.d(TAG, "Server registration successful");
                    // 再写本地A
                    boolean localSuccess = localUserManager.registerUser(username, account, password);
                    if (localSuccess) {
                        localUserManager.markUserAsSynced(username);
                        Log.d(TAG, "Local registration successful, A and B synced");
                        return true;
                    } else {
                        Log.e(TAG, "Local registration failed after server success");
                        return false;
                    }
                } else {
                    Log.d(TAG, "Server registration failed - user may already exist");
                    return false;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Server connection error during registration", e);
            }
        }
        
        // 服务端不存在或连接失败，只写本地A
        Log.d(TAG, "Server not available, writing to local data (A) only");
        boolean localSuccess = localUserManager.registerUser(username, account, password);
        if (localSuccess) {
            Log.d(TAG, "Local registration successful, will try to notify server");
            // 先尝试直接连接并注册到服务端
            tryRegisterToServerDirectly(username, account, password);
            // 同时添加到批量同步队列作为备用
            SyncOperation operation = new SyncOperation(SyncOperation.Type.REGISTER, username, account, password);
            batchSyncManager.addSyncOperation(operation);
        }
        return localSuccess;
    }
    
    /**
     * 修改用户密码
     * 
     * 修改策略：
     * - 服务端可用：先修改服务端B，再修改本地A，并额外通知AdminDashboard刷新
     * - 服务端不可用：先修改本地A，然后通过直接通知+批量队列双重机制同步到服务端
     * 
     * @param username 用户名
     * @param newPassword 新密码
     * @return 修改成功返回true，失败返回false
     */
    public boolean updatePassword(String username, String newPassword) {
        Log.d(TAG, "Update password for user: " + username);
        
        if (isRemoteServiceAvailable()) {
            // 服务端存在，双向同步：同时修改A和B
            Log.d(TAG, "Server available, performing bidirectional password update");
            try {
                // 先修改服务端B
                remoteService.updateUserPassword(username, newPassword);
                Log.d(TAG, "Server password update successful");
                
                // 再修改本地A
                boolean localSuccess = localUserManager.updateUserPassword(username, newPassword);
                if (localSuccess) {
                    localUserManager.markUserAsSynced(username);
                    Log.d(TAG, "Local password update successful, A and B synced");
                    
                    // 通知服务端数据库直接更新（为了确保AdminDashboard能看到变化）
                    notifyServerDatabaseUpdate("password_update", username, newPassword);
                    return true;
                } else {
                    Log.e(TAG, "Local password update failed after server success");
                    return false;
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Server connection error during password update", e);
            }
        }
        
        // 服务端不存在或连接失败，只修改本地A
        Log.d(TAG, "Server not available, updating local data (A) only");
        boolean localSuccess = localUserManager.updateUserPassword(username, newPassword);
        if (localSuccess) {
            Log.d(TAG, "Local password update successful, will try to notify server");
            // 先尝试直接通知服务端
            tryNotifyServerDirectly("password_update", username, newPassword);
            // 同时添加到批量同步队列作为备用
            SyncOperation operation = new SyncOperation(SyncOperation.Type.UPDATE_PASSWORD, username, null, newPassword);
            batchSyncManager.addSyncOperation(operation);
        }
        return localSuccess;
    }
    
    /**
     * 删除用户
     * 
     * 删除策略：
     * - 服务端可用：先删除服务端B，再删除本地A，并额外通知AdminDashboard刷新
     * - 服务端不可用：先删除本地A，然后通过直接通知+批量队列双重机制同步到服务端
     * 
     * @param username 要删除的用户名
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteUser(String username) {
        Log.d(TAG, "Delete user: " + username);
        
        if (isRemoteServiceAvailable()) {
            // 服务端存在，双向同步：同时删除A和B
            Log.d(TAG, "Server available, performing bidirectional delete");
            try {
                // 先删除服务端B
                remoteService.deleteUser(username);
                Log.d(TAG, "Server deletion successful");
                
                // 再删除本地A
                boolean localSuccess = localUserManager.deleteUser(username);
                Log.d(TAG, "Local deletion successful, A and B synced");
                
                // 通知服务端数据库直接更新（为了确保AdminDashboard能看到变化）
                notifyServerDatabaseUpdate("user_delete", username, null);
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "Server connection error during deletion", e);
                return false;
            }
        }
        
        // 服务端不存在或连接失败，只删除本地A
        Log.d(TAG, "Server not available, deleting from local data (A) only");
        boolean localSuccess = localUserManager.deleteUser(username);
        if (localSuccess) {
            Log.d(TAG, "Local deletion successful, will try to notify server");
            // 先尝试直接通知服务端
            tryNotifyServerDirectly("user_delete", username, null);
            // 同时添加到批量同步队列作为备用
            SyncOperation operation = new SyncOperation(SyncOperation.Type.DELETE_USER, username);
            batchSyncManager.addSyncOperation(operation);
        }
        return localSuccess;
    }
    
    /**
     * 执行双向同步
     * 当服务端连接建立时自动调用，将本地数据A和服务端数据B合并同步
     * 
     * 同步流程：
     * 1. 把本地未同步的数据推送到服务端（A -> B）
     * 2. 检查服务端的数据变化并更新本地（B -> A）
     * 
     * 异步执行，不阻塞主线程
     */
    private void performBidirectionalSync() {
        if (!isRemoteServiceAvailable()) {
            Log.w(TAG, "Cannot perform sync - remote service not available");
            return;
        }
        
        new Thread(() -> {
            try {
                Log.d(TAG, "Starting bidirectional sync between A and B");
                
                // 第一步：把本地未同步的数据A推送到服务端B
                syncLocalToServer();
                
                // 第二步：从服务端B拉取数据到本地A（处理服务端独有的数据）
                syncServerToLocal();
                
                Log.d(TAG, "Bidirectional sync completed");
                
            } catch (Exception e) {
                Log.e(TAG, "Error during bidirectional sync", e);
            }
        }).start();
    }
    
    /**
     * 同步本地数据到服务端（A -> B）
     * 获取本地所有未同步的用户数据，逐个推送到服务端
     * 推送成功后标记为已同步，避免重复操作
     */
    private void syncLocalToServer() {
        try {
            List<LocalUserManager.LocalUser> unsyncedUsers = localUserManager.getUnsyncedUsers();
            Log.d(TAG, "Syncing " + unsyncedUsers.size() + " unsynced users from A to B");
            
            for (LocalUserManager.LocalUser user : unsyncedUsers) {
                try {
                    boolean success = remoteService.registerUser(user.username, user.account, user.rawPassword);
                    if (success) {
                        localUserManager.markUserAsSynced(user.username);
                        Log.d(TAG, "Synced user A->B: " + user.username);
                    } else {
                        Log.w(TAG, "Failed to sync user A->B: " + user.username + " (may already exist)");
                        // 即使失败也标记为已同步，避免重复尝试
                        localUserManager.markUserAsSynced(user.username);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Error syncing user A->B: " + user.username, e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in syncLocalToServer", e);
        }
    }
    
    /**
     * 同步服务端数据到本地（B -> A）
     * 检查本地用户在服务端是否还存在，如果服务端已删除则同步删除本地数据
     * 确保本地数据与服务端保持一致
     */
    private void syncServerToLocal() {
        try {
            // 这个方法需要服务端提供一个获取所有用户的接口
            // 暂时先检查现有用户是否在服务端存在
            List<String> localUsers = localUserManager.getAllUsersForSync();
            Log.d(TAG, "Checking " + localUsers.size() + " local users against server");
            
            for (String userData : localUsers) {
                String[] parts = userData.split("\\|");
                if (parts.length >= 2) {
                    String account = parts[1];
                    try {
                        boolean existsOnServer = remoteService.userExists(account);
                        if (!existsOnServer) {
                            // 用户在服务端不存在，可能被服务端删除了
                            Log.d(TAG, "User deleted on server, removing from local: " + account);
                            localUserManager.deleteUserByAccount(account);
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG, "Error checking user existence on server: " + account, e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in syncServerToLocal", e);
        }
    }
    
    /**
     * 通知服务端数据库直接更新
     * 确保客户端操作后，服务端数据库和AdminDashboard能立即看到变化
     */
    private void notifyServerDatabaseUpdate(String operation, String username, String data) {
        if (!isRemoteServiceAvailable()) {
            Log.w(TAG, "Cannot notify server - remote service not available");
            return;
        }
        
        new Thread(() -> {
            try {
                Log.d(TAG, "Notifying server database update: " + operation + " for user: " + username);
                
                switch (operation) {
                    case "password_update":
                        // 密码更新通知 - 这里可以添加额外的通知逻辑
                        remoteService.notifyPasswordUpdated(username, data);
                        Log.d(TAG, "Password update notification sent to server");
                        break;
                        
                    case "user_delete":
                        // 用户删除通知
                        remoteService.notifyUserDeleted(username);
                        Log.d(TAG, "User deletion notification sent to server");
                        break;
                        
                    default:
                        Log.w(TAG, "Unknown operation: " + operation);
                        break;
                }
                
            } catch (RemoteException e) {
                Log.e(TAG, "Error notifying server database update", e);
            } catch (Exception e) {
                Log.e(TAG, "Error in notifyServerDatabaseUpdate", e);
            }
        }).start();
    }
    
    /**
     * 按需同步
     * 仅在服务端连接可用时执行双向同步
     * 通常在关键操作完成后调用，确保数据一致性
     */
    private void performOnDemandSync() {
        if (isRemoteServiceAvailable()) {
            Log.d(TAG, "Performing on-demand sync");
            performBidirectionalSync();
        } else {
            Log.d(TAG, "Remote service not available, skipping on-demand sync");
        }
    }
    
    /**
     * 尝试连接服务端并同步
     * 当检测到服务端不可用时，主动尝试重新连接并执行同步
     * 使用WeakServiceConnection确保连接安全，避免内存泄漏
     */
    private void tryConnectAndSync() {
        if (isRemoteServiceAvailable()) {
            // 服务端已连接，直接执行批量同步
            batchSyncManager.executeBatchSync(context);
            return;
        }
        
        Log.d(TAG, "Trying to connect to server for sync");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(AppConstants.Service.ADMIN_SERVICE_PACKAGE, AppConstants.Service.ADMIN_SERVICE_CLASS));
        
        WeakServiceConnection connection = new WeakServiceConnection(context, "TryConnectAndSync") {
            @Override
            protected void onServiceConnectedSafely(Context context, ComponentName name, IBinder service) {
                Log.d(TAG, "Connected to server for sync");
                IAdminService tempService = IAdminService.Stub.asInterface(service);
                
                // 设置远程服务
                setRemoteService(tempService);
                
                // 执行批量同步
                batchSyncManager.executeBatchSync(context);
            }
            
            @Override
            protected void onServiceDisconnectedSafely(ComponentName name) {
                Log.d(TAG, "Server service disconnected after sync attempt");
            }
        };
        
        boolean bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        if (!bound) {
            Log.w(TAG, "Failed to bind to server for sync");
        }
    }
    
    /**
     * 尝试直接注册到服务端
     */
    private void tryRegisterToServerDirectly(String username, String account, String password) {
        Log.d(TAG, "Trying to register user directly to server: " + username);
        
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(AppConstants.Service.ADMIN_SERVICE_PACKAGE, AppConstants.Service.ADMIN_SERVICE_CLASS));
        
        WeakServiceConnection connection = new WeakServiceConnection(context, "DirectRegister") {
            @Override
            protected void onServiceConnectedSafely(Context context, ComponentName name, IBinder service) {
                Log.d(TAG, "Connected to server for direct registration");
                try {
                    IAdminService tempService = IAdminService.Stub.asInterface(service);
                    boolean success = tempService.registerUser(username, account, password);
                    if (success) {
                        Log.d(TAG, "Direct registration successful for user: " + username);
                        localUserManager.markUserAsSynced(username);
                        // 清除该操作的缓存，因为已经成功同步
                        SyncOperation operation = new SyncOperation(SyncOperation.Type.REGISTER, username, account, password);
                        syncCache.cacheOperation(operation, SyncResult.SUCCESS);
                    } else {
                        Log.w(TAG, "Direct registration failed for user: " + username);
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "Error during direct registration", e);
                }
            }
        };
        
        boolean bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        if (!bound) {
            Log.w(TAG, "Failed to bind to server for direct registration");
        }
    }
    
    /**
     * 尝试直接通知服务端数据变更
     */
    private void tryNotifyServerDirectly(String operation, String username, String data) {
        Log.d(TAG, "Trying to notify server directly: " + operation + " for user: " + username);
        
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(AppConstants.Service.ADMIN_SERVICE_PACKAGE, AppConstants.Service.ADMIN_SERVICE_CLASS));
        
        WeakServiceConnection connection = new WeakServiceConnection(context, "DirectNotify") {
            @Override
            protected void onServiceConnectedSafely(Context context, ComponentName name, IBinder service) {
                Log.d(TAG, "Connected to server for direct notification");
                try {
                    IAdminService tempService = IAdminService.Stub.asInterface(service);
                    
                    switch (operation) {
                        case "password_update":
                            tempService.notifyPasswordUpdated(username, data);
                            Log.d(TAG, "Direct password update notification sent");
                            break;
                            
                        case "user_delete":
                            tempService.notifyUserDeleted(username);
                            Log.d(TAG, "Direct user deletion notification sent");
                            break;
                    }
                    
                    // 通知成功，缓存该操作
                    SyncOperation.Type type = operation.equals("password_update") ? 
                        SyncOperation.Type.UPDATE_PASSWORD : SyncOperation.Type.DELETE_USER;
                    SyncOperation syncOp = new SyncOperation(type, username, null, data);
                    syncCache.cacheOperation(syncOp, SyncResult.SUCCESS);
                    
                } catch (RemoteException e) {
                    Log.e(TAG, "Error during direct notification", e);
                }
            }
        };
        
        boolean bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        if (!bound) {
            Log.w(TAG, "Failed to bind to server for direct notification");
        }
    }
}