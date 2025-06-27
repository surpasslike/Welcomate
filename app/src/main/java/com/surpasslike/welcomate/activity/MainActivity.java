package com.surpasslike.welcomate.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.surpasslike.welcomate.R;
import com.surpasslike.welcomate.constants.AppConstants;
import com.surpasslike.welcomate.service.UserService;
import com.surpasslike.welcomate.utils.ToastUtils;
import com.surpasslike.welcomateservice.IAdminService;

/**
 * 主活动页面 - 应用程序入口
 * 
 * 功能职责：
 * 1. 提供用户登录、注册和游客模式的入口界面
 * 2. 负责绑定服务端AdminService，建立AIDL连接
 * 3. 初始化UserService，管理本地和远程服务的生命周期
 * 4. 自动触发双向数据同步（当服务端连接成功时）
 * 
 * 连接流程：
 * - onCreate: 初始化UserService并尝试绑定服务端
 * - onServiceConnected: 连接成功后设置远程服务，自动触发同步
 * - onServiceDisconnected: 连接断开时清理远程服务引用
 */
public class MainActivity extends AppCompatActivity {

    /** 日志标签 */
    private static final String TAG = "MainActivity";

    // AdminService实例，用于与服务端通信
    static IAdminService mAdminService;
    
    // UserService实例，统一管理本地和远程用户操作
    static UserService mUserService;

    // 服务连接对象，用于处理服务绑定和解绑
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /**
         * 服务连接成功时的回调
         * @param name 服务组件名
         * @param service 服务的IBinder对象
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 绑定成功时调用，获取AdminService的代理对象
            mAdminService = IAdminService.Stub.asInterface(service);
            Log.d(TAG, "AdminService connected successfully");
            Log.d(TAG, "AdminService object: " + (mAdminService != null ? "NOT NULL" : "NULL"));
            ToastUtils.showShort(MainActivity.this, R.string.service_bound_success);
            // 更新UserService的远程服务引用
            if (mUserService != null) {
                Log.d(TAG, "Setting remote service to UserService...");
                mUserService.setRemoteService(mAdminService);
                Log.d(TAG, "Remote service set to UserService successfully");
                Log.d(TAG, "UserService remote availability: " + mUserService.isRemoteServiceAvailable());
                
                // 设置远程服务会自动触发双向同步
            } else {
                Log.e(TAG, "UserService is null!");
            }
        }

        /**
         * 服务断开连接时的回调
         * @param name 服务组件名
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // 解绑时调用
            Log.d(TAG, "AdminService disconnected");
            mAdminService = null;
            // 清除UserService的远程服务引用
            if (mUserService != null) {
                mUserService.setRemoteService(null);
            }
        }
    };

    /**
     * 活动创建时的初始化方法
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化UserService
        mUserService = new UserService(this);
        
        // 绑定AdminService服务
        bindAdminService();

        // 初始化界面控件
        initViews();
    }

    /**
     * 初始化界面控件和设置点击事件
     * 设置登录、注册、游客模式按钮的点击监听器
     */
    private void initViews() {
        // 获取界面控件
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button_login = findViewById(R.id.button_login);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button_register = findViewById(R.id.button_register);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button_guest = findViewById(R.id.button_guest);

        // 设置登录按钮点击事件
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // 设置注册按钮点击事件
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 设置游客模式按钮点击事件
        button_guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GuestHomeActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 绑定AdminService服务
     * 用于与服务端进行通信
     */
    private void bindAdminService() {
        Log.d(TAG, "Attempting to bind AdminService...");
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(AppConstants.Service.ADMIN_SERVICE_PACKAGE, AppConstants.Service.ADMIN_SERVICE_CLASS));
        
        Log.d(TAG, "Service package: " + AppConstants.Service.ADMIN_SERVICE_PACKAGE);
        Log.d(TAG, "Service class: " + AppConstants.Service.ADMIN_SERVICE_CLASS);

        boolean isServiceBound = bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "Bind service result: " + isServiceBound);
        
        if (!isServiceBound) {
            Log.e(TAG, "Failed to bind AdminService!");
            ToastUtils.showShort(this, R.string.service_bind_failed);
        } else {
            Log.d(TAG, "AdminService bind initiated successfully");
        }
    }

    /**
     * 活动销毁时的清理方法
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑AdminService服务
        if (mAdminService != null) {
            unbindService(mServiceConnection);
        }
    }

    /**
     * 静态方法，用于获取已绑定的IAdminService对象
     *
     * @return IAdminService实例，如果未绑定则返回null
     */
    public static IAdminService getAdminService() {
        return mAdminService;
    }
    
    /**
     * 静态方法，用于获取UserService对象
     *
     * @return UserService实例
     */
    public static UserService getUserService() {
        return mUserService;
    }
}