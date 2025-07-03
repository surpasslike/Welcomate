package com.surpasslike.welcomateservice.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.surpasslike.welcomateservice.databinding.ActivityAdminLoginBinding;

/**
 * 管理员登录界面 Activity
 * 负责处理用户输入账户和密码，并通过 AdminViewModel 进行登录验证
 */
public class AdminLoginActivity extends AppCompatActivity {

    private ActivityAdminLoginBinding binding;
    private AdminViewModel adminViewModel;

    /**
     * Activity 创建时调用
     *
     * @param savedInstanceState 如果 Activity 被重新创建，此参数包含之前保存的状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 使用 ViewBinding 初始化界面
        binding = ActivityAdminLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化 ViewModel
        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        // 设置登录按钮的点击监听器
        binding.btnLogin.setOnClickListener(v -> handleLogin());

        // 设置注册文本的点击监听器，跳转到注册界面
        binding.tvRegister.setOnClickListener(view ->
                startActivity(new Intent(AdminLoginActivity.this, AdminRegisterActivity.class))
        );
    }

    /**
     * 处理登录逻辑
     * 获取用户输入，调用 ViewModel 进行验证，并根据结果进行界面跳转或提示
     */
    private void handleLogin() {
        String account = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // 输入验证
        if (account.isEmpty() || password.isEmpty()) {
            showToast("Account and password cannot be empty.");
            return;
        }

        // 通过 ViewModel 验证管理员登录
        int loginResult = adminViewModel.loginAdmin(account, password);

        if (loginResult == AdminViewModel.LOGIN_SUCCESS) {
            // 登录成功，跳转到管理员仪表盘
            showToast("Login successful!");
            startActivity(new Intent(AdminLoginActivity.this, AdminDashboardActivity.class));
            finish(); // 结束当前登录界面，防止用户通过返回键回到登录页
        } else if (loginResult == AdminViewModel.LOGIN_FAILED) {
            // 账户或密码错误
            showToast("Login failed! Please check your account and password.");
        } else if (loginResult == AdminViewModel.PERMISSION_DENIED) {
            // 权限不足
            showToast("Permission denied! This account does not have admin privileges.");
        }
    }

    /**
     * 显示一个短时间的 Toast 消息
     *
     * @param message 要显示的消息
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}