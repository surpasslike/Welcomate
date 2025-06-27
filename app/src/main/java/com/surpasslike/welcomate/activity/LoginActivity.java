package com.surpasslike.welcomate.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.surpasslike.welcomate.R;
import com.surpasslike.welcomate.constants.AppConstants;
import com.surpasslike.welcomate.databinding.ActivityLoginBinding;
import com.surpasslike.welcomate.service.UserService;
import com.surpasslike.welcomate.utils.ToastUtils;
import com.surpasslike.welcomate.utils.ValidationUtils;

/**
 * 用户登录页面
 * 提供用户登录功能，验证用户身份并跳转到主页
 */
public class LoginActivity extends AppCompatActivity {

    /** 日志标签 */
    private static final String TAG = "LoginActivity";
    
    // 视图绑定对象
    private ActivityLoginBinding mActivityLoginBinding;

    /**
     * 活动创建时的初始化方法
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityLoginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(mActivityLoginBinding.getRoot());
        
        // 初始化界面
        initViews();
    }
    
    /**
     * 初始化界面控件和设置事件监听器
     */
    private void initViews() {
        // 设置登录按钮点击事件
        mActivityLoginBinding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });
    }
    
    /**
     * 执行登录操作
     * 验证输入并调用服务进行登录
     */
    private void performLogin() {
        String account = mActivityLoginBinding.etAccount.getText().toString().trim();
        String password = mActivityLoginBinding.etPassword.getText().toString();
        
        // 验证输入
        if (!validateInput(account, password)) {
            return;
        }
        
        // 获取UserService对象
        UserService userService = MainActivity.getUserService();
        
        if (userService != null) {
            String username = userService.login(account, password);
            handleLoginResult(username);
        } else {
            ToastUtils.showShort(this, R.string.service_not_available);
        }
    }
    
    /**
     * 验证用户输入
     * @param account 账号
     * @param password 密码
     * @return true表示输入有效，false表示输入无效
     */
    private boolean validateInput(String account, String password) {
        if (!ValidationUtils.isValidAccount(account)) {
            ToastUtils.showShort(this, R.string.account_empty);
            mActivityLoginBinding.etAccount.requestFocus();
            return false;
        }
        
        if (!ValidationUtils.isValidPassword(password)) {
            ToastUtils.showShort(this, getString(R.string.password_too_short, 
                AppConstants.TextLimit.PASSWORD_MIN_LENGTH));
            mActivityLoginBinding.etPassword.requestFocus();
            return false;
        }
        
        return true;
    }
    
    /**
     * 处理登录结果
     * @param username 登录成功返回的用户名，null表示登录失败
     */
    private void handleLoginResult(String username) {
        if (username != null) {
            ToastUtils.showShort(this, R.string.login_success);
            
            // 登录成功后跳转到HomeActivity并传递用户名
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra(AppConstants.IntentExtra.USERNAME, username);
            startActivity(intent);
            finish(); // 结束当前活动，防止用户返回到登录页面
        } else {
            ToastUtils.showShort(this, R.string.login_failed);
            // 清空密码输入框
            mActivityLoginBinding.etPassword.setText("");
            mActivityLoginBinding.etPassword.requestFocus();
        }
    }
    
    /**
     * 活动销毁时的清理方法
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理视图绑定
        if (mActivityLoginBinding != null) {
            mActivityLoginBinding = null;
        }
    }
}