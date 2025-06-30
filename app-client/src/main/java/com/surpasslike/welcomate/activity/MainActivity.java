package com.surpasslike.welcomate.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;

import com.surpasslike.welcomate.R;
import com.surpasslike.welcomate.utils.ToastUtils;
import com.surpasslike.welcomateservice.IAdminService;
import com.surpasslike.welcomateservice.service.AdminService;
import com.surpasslike.welcomateservice.ui.admin.AdminDashboardActivity;

/**
 * 主活动页面
 * 提供用户登录、注册和游客模式的入口
 * 负责绑定AdminService服务
 */
public class MainActivity extends AppCompatActivity {

    /** 日志标签 */
    private static final String TAG = "MainActivity";

    // AdminService实例，用于与服务端通信
    static IAdminService mAdminService;

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
        }

        /**
         * 服务断开连接时的回调
         * @param name 服务组件名
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            // 解绑时调用
            mAdminService = null;
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

        // 绑定AdminService服务
        bindAdminService();

        // 初始化界面控件
        initViews();
    }

    /**
     * 初始化界面控件和设置点击事件
     */
    private void initViews() {
        // 获取界面控件
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button_login = findViewById(R.id.button_login);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button_register = findViewById(R.id.button_register);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button_guest = findViewById(R.id.button_guest);
        Button button_user_management = findViewById(R.id.button_user_management);

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

        // 设置用户管理按钮点击事件
        button_user_management.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 绑定AdminService服务
     * 用于与服务端进行通信
     */
    private void bindAdminService() {
        // 由于 app-service 是一个库模块，可以直接通过类引用来创建 Intent，更安全、更简单。
        Intent intent = new Intent(this, AdminService.class);

        boolean isServiceBound = bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        if (isServiceBound) {
            ToastUtils.showShort(this, R.string.service_bound_success);
        } else {
            ToastUtils.showShort(this, R.string.service_bind_failed);
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
}