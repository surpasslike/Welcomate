package com.surpasslike.welcomate.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.surpasslike.welcomate.database.LocalUserManager;
import com.surpasslike.welcomateservice.IAdminService;

import java.util.List;

/**
 * 客户端同步服务 - 跨进程数据同步接口
 * 
 * 功能职责：
 * 1. 提供AIDL接口，让服务端能够访问客户端的本地数据库A
 * 2. 响应服务端的数据同步请求，返回本地用户信息
 * 3. 接收服务端的数据变更通知，更新本地数据库A
 * 4. 作为客户端的数据代理，确保跨进程调用安全
 * 
 * AIDL接口实现：
 * - getLocalUsers(): 返回所有本地用户数据给服务端同步
 * - registerUser(): 接收服务端的用户注册请求
 * - updateUserPassword(): 接收服务端的密码修改请求
 * - deleteUser(): 接收服务端的用户删除请求
 * - loginAdmin(): 提供本地登录验证
 * 
 * 服务生命周期：
 * - onCreate(): 初始化LocalUserManager
 * - onBind(): 返回AIDL接口实例
 * - 通过WelcomateApplication自动启动，确保服务端能连接
 */
public class ClientSyncService extends Service {
    private static final String TAG = "ClientSyncService";
    
    private LocalUserManager localUserManager;
    
    /**
     * 服务创建时的初始化
     * 初始化LocalUserManager实例，准备数据库操作
     */
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            localUserManager = LocalUserManager.getInstance(this);
            Log.d(TAG, "ClientSyncService created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating ClientSyncService", e);
        }
    }
    
    /**
     * 服务绑定时的回调
     * 返回AIDL接口实例，供服务端调用
     * 
     * @param intent 绑定意图
     * @return AIDL接口的IBinder实例
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "ClientSyncService onBind");
        return new ClientSyncBinder();
    }
    
    /**
     * AIDL接口实现类 - 客户端数据同步绑定器
     * 处理服务端的跨进程调用请求
     */
    private class ClientSyncBinder extends IAdminService.Stub {
        
        /**
         * AIDL实现 - 本地登录验证
         * 提供客户端本地的用户登录验证功能
         * 
         * @param account 用户账号
         * @param password 用户密码
         * @return 登录成功返回用户名，失败返回null
         * @throws RemoteException AIDL调用异常
         */
        @Override
        public String loginAdmin(String account, String password) throws RemoteException {
            // 这个方法在客户端服务中主要用于验证服务端调用
            return localUserManager.loginUser(account, password);
        }
        
        /**
         * AIDL实现 - 接收服务端用户注册请求
         * 当服务端需要在客户端创建用户时调用
         * 
         * @param username 用户名
         * @param account 用户账号
         * @param password 用户密码
         * @return 注册成功返回true，失败返回false
         * @throws RemoteException AIDL调用异常
         */
        @Override
        public boolean registerUser(String username, String account, String password) throws RemoteException {
            // 这个方法在客户端服务中主要用于接收服务端的注册请求
            return localUserManager.registerUser(username, account, password);
        }
        
        /**
         * AIDL实现 - 接收服务端用户删除请求
         * 当服务端删除用户时，同步删除客户端本地数据
         * 
         * @param username 要删除的用户名
         * @throws RemoteException AIDL调用异常
         */
        @Override
        public void deleteUser(String username) throws RemoteException {
            localUserManager.deleteUser(username);
        }
        
        /**
         * AIDL实现 - 接收服务端密码更新请求
         * 当服务端修改用户密码时，同步更新客户端本地数据
         * 
         * @param username 用户名
         * @param newPassword 新密码
         * @throws RemoteException AIDL调用异常
         */
        @Override
        public void updateUserPassword(String username, String newPassword) throws RemoteException {
            Log.d(TAG, "Receiving password update from server for user: " + username);
            boolean success = localUserManager.updateUserPassword(username, newPassword);
            if (success) {
                localUserManager.markUserAsSynced(username);
                Log.d(TAG, "Password updated successfully for user: " + username);
            } else {
                Log.e(TAG, "Failed to update password for user: " + username);
            }
        }
        
        /**
         * AIDL实现 - 获取本地用户数据
         * 服务端调用此方法获取客户端本地数据库A中的所有用户信息
         * 返回格式：username|account|password 的字符串列表
         * 
         * @return 本地用户数据列表，用于服务端同步
         * @throws RemoteException AIDL调用异常
         */
        @Override
        public List<String> getLocalUsers() throws RemoteException {
            Log.d(TAG, "Service requesting local users data");
            try {
                List<String> users = localUserManager.getAllUsersForSync();
                Log.d(TAG, "Returning " + users.size() + " users to service");
                return users;
            } catch (Exception e) {
                Log.e(TAG, "Error getting local users", e);
                throw new RemoteException("Failed to get local users: " + e.getMessage());
            }
        }
        
        @Override
        public void clearLocalUsers() throws RemoteException {
            Log.d(TAG, "Service requesting to clear local users");
            boolean success = localUserManager.clearAllUsers();
            Log.d(TAG, "Clear local users result: " + success);
        }
        
        @Override
        public void notifyUserRegistered(String username, String account, String password) throws RemoteException {
            // 客户端服务不处理这些通知方法，它们是用于向服务端发送通知的
        }
        
        @Override
        public void notifyUserDeleted(String username) throws RemoteException {
            // 客户端服务不处理这些通知方法
        }
        
        @Override
        public void notifyPasswordUpdated(String username, String newPassword) throws RemoteException {
            // 客户端服务不处理这些通知方法
        }
        
        @Override
        public boolean userExists(String account) throws RemoteException {
            return localUserManager.userExists(account);
        }
    }
}