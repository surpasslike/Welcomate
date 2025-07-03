package com.surpasslike.welcomate.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.surpasslike.welcomate.R;
import com.surpasslike.welcomate.service.ServiceManager;
import com.surpasslike.welcomate.utils.ToastUtils;
import com.surpasslike.welcomateservice.IAdminService;

/**
 * 主活动页面
 * 提供用户登录、注册和游客模式的入口
 * 负责绑定AdminService服务
 */
public class MainActivity extends AppCompatActivity {

    /** 日志标签 */
    private static final String TAG = "MainActivity";

    // ServiceManager实例，用于管理Service连接
    private ServiceManager mServiceManager;

    /**
     * 活动创建时的初始化方法
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取ServiceManager实例
        mServiceManager = ServiceManager.getInstance();
        
        // 检查服务连接状态并显示相应提示
        checkServiceConnection();

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
     * 检查服务连接状态
     */
    private void checkServiceConnection() {
        if (mServiceManager.isServiceConnected()) {
            ToastUtils.showShort(this, R.string.service_bound_success);
        } else {
            // 设置监听器等待服务连接
            mServiceManager.setServiceConnectionListener(new ServiceManager.ServiceConnectionListener() {
                @Override
                public void onServiceConnected(IAdminService service) {
                    runOnUiThread(() -> {
                        ToastUtils.showShort(MainActivity.this, R.string.service_bound_success);
                    });
                    // 移除监听器避免内存泄漏
                    mServiceManager.removeServiceConnectionListener();
                }

                @Override
                public void onServiceDisconnected() {
                    runOnUiThread(() -> {
                        ToastUtils.showShort(MainActivity.this, R.string.service_bind_failed);
                    });
                }
                
                @Override
                public void onServiceBindFailed() {
                    runOnUiThread(() -> {
                        ToastUtils.showShort(MainActivity.this, R.string.service_bind_failed);
                    });
                }
            });
        }
    }

    /**
     * 活动销毁时的清理方法
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理服务连接监听器
        if (mServiceManager != null) {
            mServiceManager.removeServiceConnectionListener();
        }
    }
}