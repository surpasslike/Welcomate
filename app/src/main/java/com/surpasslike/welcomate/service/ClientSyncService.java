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

public class ClientSyncService extends Service {
    private static final String TAG = "ClientSyncService";
    
    private LocalUserManager localUserManager;
    
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
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "ClientSyncService onBind");
        return new ClientSyncBinder();
    }
    
    private class ClientSyncBinder extends IAdminService.Stub {
        
        @Override
        public String loginAdmin(String account, String password) throws RemoteException {
            // 这个方法在客户端服务中主要用于验证服务端调用
            return localUserManager.loginUser(account, password);
        }
        
        @Override
        public boolean registerUser(String username, String account, String password) throws RemoteException {
            // 这个方法在客户端服务中主要用于接收服务端的注册请求
            return localUserManager.registerUser(username, account, password);
        }
        
        @Override
        public void deleteUser(String username) throws RemoteException {
            localUserManager.deleteUser(username);
        }
        
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