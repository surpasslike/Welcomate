package com.surpasslike.welcomateservice.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.surpasslike.welcomateservice.databinding.ActivityAdminRegisterBinding;

/**
 * 管理员注册界面 Activity
 * 负责处理新用户的注册，包括输入用户名、账户和密码
 */
public class AdminRegisterActivity extends AppCompatActivity {

    private ActivityAdminRegisterBinding binding;
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
        binding = ActivityAdminRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化 ViewModel
        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);

        // 设置注册按钮的点击监听器
        binding.btnRegister.setOnClickListener(v -> handleRegister());
    }

    /**
     * 处理注册逻辑
     * 获取用户输入，进行基本验证，然后调用 ViewModel 进行用户注册
     */
    private void handleRegister() {
        String username = binding.etUsername.getText().toString().trim();
        String account = binding.etAccount.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // 输入验证
        if (username.isEmpty() || account.isEmpty() || password.isEmpty()) {
            showToast("Username, account, and password cannot be empty.");
            return;
        }

        // 通过 ViewModel 添加新用户
        long rowId = adminViewModel.addUser(username, account, password);

        if (rowId != -1) {
            showToast("Registration successful!");
            // 注册成功后关闭当前页面，返回到登录页面
            finish();
        } else {
            showToast("Registration failed! The account might already exist.");
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
