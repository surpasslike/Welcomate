package com.surpasslike.welcomate.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.surpasslike.welcomate.R;
import com.surpasslike.welcomate.constants.AppConstants;
import com.surpasslike.welcomate.databinding.ActivityGuestHomeBinding;

/**
 * 游客主页面
 * 为游客用户提供基本功能入口，无需登录即可使用
 * 主要提供设置页面的访问
 */
public class GuestHomeActivity extends AppCompatActivity {

    /** 日志标签 */
    private static final String TAG = "GuestHomeActivity";
    
    // 视图绑定对象
    private ActivityGuestHomeBinding mActivityGuestHomeBinding;

    /**
     * 活动创建时的初始化方法
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityGuestHomeBinding = ActivityGuestHomeBinding.inflate(getLayoutInflater());
        setContentView(mActivityGuestHomeBinding.getRoot());

        // 初始化界面
        initViews();
    }
    
    /**
     * 初始化界面控件和设置事件监听器
     */
    private void initViews() {
        // 设置欢迎消息
        mActivityGuestHomeBinding.tvWelcomeMessage.setText(R.string.welcome_guest);

        // 设置设置按钮点击事件
        mActivityGuestHomeBinding.btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSettings();
            }
        });

        // 设置返回主页按钮点击事件
        mActivityGuestHomeBinding.btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 关闭当前活动，返回主页面
            }
        });
    }
    
    /**
     * 导航到设置页面
     */
    private void navigateToSettings() {
        Intent intent = new Intent(GuestHomeActivity.this, com.surpasslike.setting.SettingActivity.class);
        startActivity(intent);
    }
    
    /**
     * 活动销毁时的清理方法
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理视图绑定
        if (mActivityGuestHomeBinding != null) {
            mActivityGuestHomeBinding = null;
        }
    }
}