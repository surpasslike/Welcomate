package com.surpasslike.welcomate.activity;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.surpasslike.welcomate.R;
import com.surpasslike.welcomate.constants.AppConstants;
import com.surpasslike.welcomate.databinding.ActivityRegisterBinding;
import com.surpasslike.welcomate.service.UserService;
import com.surpasslike.welcomate.utils.ToastUtils;
import com.surpasslike.welcomate.utils.ValidationUtils;

/**
 * 用户注册页面
 * 提供用户注册功能，创建新的用户账户
 */
public class RegisterActivity extends AppCompatActivity {

    /** 日志标签 */
    private static final String TAG = "RegisterActivity";
    
    // 视图绑定对象
    private ActivityRegisterBinding mActivityRegisterBinding;
    
    // UserService实例
    private UserService mUserService;

    /**
     * 活动创建时的初始化方法
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityRegisterBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(mActivityRegisterBinding.getRoot());

        // 获取UserService实例
        mUserService = MainActivity.getUserService();
        Log.d(TAG, "UserService from MainActivity: " + (mUserService != null ? "NOT NULL" : "NULL"));
        
        // 初始化界面
        initViews();
    }
    
    /**
     * 初始化界面控件和设置事件监听器
     */
    private void initViews() {
        // 设置注册按钮点击事件
        mActivityRegisterBinding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRegister();
            }
        });
    }
    
    /**
     * 执行注册操作
     * 验证输入并调用服务进行注册
     */
    private void performRegister() {
        String username = mActivityRegisterBinding.etUsername.getText().toString().trim();
        String account = mActivityRegisterBinding.etAccount.getText().toString().trim();
        String password = mActivityRegisterBinding.etPassword.getText().toString();
        String confirmPassword = mActivityRegisterBinding.etConfirmPassword.getText().toString();
        
        // 验证输入
        if (!validateInput(username, account, password, confirmPassword)) {
            return;
        }
        
        // 调用注册服务
        registerUser(username, account, password);
    }
    
    /**
     * 验证用户输入
     * @param username 用户名
     * @param account 账号
     * @param password 密码
     * @param confirmPassword 确认密码
     * @return true表示输入有效，false表示输入无效
     */
    private boolean validateInput(String username, String account, String password, String confirmPassword) {
        // 验证用户名
        if (!ValidationUtils.isValidUsername(username)) {
            if (username.isEmpty()) {
                ToastUtils.showShort(this, R.string.invalid_input);
            } else if (username.length() < AppConstants.TextLimit.USERNAME_MIN_LENGTH) {
                ToastUtils.showShort(this, getString(R.string.username_too_short, 
                    AppConstants.TextLimit.USERNAME_MIN_LENGTH));
            } else {
                ToastUtils.showShort(this, getString(R.string.username_too_long, 
                    AppConstants.TextLimit.USERNAME_MAX_LENGTH));
            }
            mActivityRegisterBinding.etUsername.requestFocus();
            return false;
        }
        
        // 验证账号
        if (!ValidationUtils.isValidAccount(account)) {
            ToastUtils.showShort(this, R.string.account_empty);
            mActivityRegisterBinding.etAccount.requestFocus();
            return false;
        }
        
        // 验证密码
        if (!ValidationUtils.isValidPassword(password)) {
            if (password.isEmpty()) {
                ToastUtils.showShort(this, R.string.invalid_input);
            } else if (password.length() < AppConstants.TextLimit.PASSWORD_MIN_LENGTH) {
                ToastUtils.showShort(this, getString(R.string.password_too_short, 
                    AppConstants.TextLimit.PASSWORD_MIN_LENGTH));
            } else {
                ToastUtils.showShort(this, getString(R.string.password_too_long, 
                    AppConstants.TextLimit.PASSWORD_MAX_LENGTH));
            }
            mActivityRegisterBinding.etPassword.requestFocus();
            return false;
        }
        
        // 验证确认密码
        if (!password.equals(confirmPassword)) {
            ToastUtils.showShort(this, R.string.password_not_match);
            mActivityRegisterBinding.etConfirmPassword.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * 注册新用户
     * @param username 用户名
     * @param account 账号
     * @param password 密码
     */
    private void registerUser(String username, String account, String password) {
        if (mUserService != null) {
            Log.d(TAG, "Starting registration for user: " + username + ", account: " + account);
            Log.d(TAG, "Remote service available: " + mUserService.isRemoteServiceAvailable());
            boolean isRegistered = mUserService.register(username, account, password);
            Log.d(TAG, "Registration result: " + isRegistered);
            handleRegisterResult(isRegistered);
        } else {
            Log.e(TAG, "UserService is null!");
            ToastUtils.showShort(this, R.string.service_not_available);
        }
    }
    
    /**
     * 处理注册结果
     * @param isSuccess true表示注册成功，false表示注册失败
     */
    private void handleRegisterResult(boolean isSuccess) {
        if (isSuccess) {
            ToastUtils.showShort(this, R.string.register_success);
            finish(); // 注册成功后关闭注册界面
        } else {
            ToastUtils.showShort(this, R.string.register_failed);
            // 清空密码输入框
            mActivityRegisterBinding.etPassword.setText("");
            mActivityRegisterBinding.etConfirmPassword.setText("");
            mActivityRegisterBinding.etUsername.requestFocus();
        }
    }
    
    /**
     * 活动销毁时的清理方法
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理视图绑定
        if (mActivityRegisterBinding != null) {
            mActivityRegisterBinding = null;
        }
    }
}