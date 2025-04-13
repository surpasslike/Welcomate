package com.surpasslike.welcomate;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.surpasslike.welcomate.databinding.ActivityRegisterBinding;
import com.surpasslike.welcomateservice.IAdminService;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private IAdminService adminService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 获取从上一个Activity传递的AdminService对象
        adminService = MainActivity.getAdminService();

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取输入的用户名、账号和密码
                String username = binding.etUsername.getText().toString();
                String account = binding.etAccount.getText().toString();
                String password = binding.etPassword.getText().toString();

                // 调用服务端的注册方法
                try {
                    if (adminService != null) {
                        boolean isRegistered = adminService.registerUser(username, account, password);
                        if (isRegistered) {
                            showToast("Registration successful!");
                            finish(); // 注册成功后关闭注册界面
                        } else {
                            showToast("Failed to register! Please try again.");
                        }
                    } else {
                        showToast("AdminService not available. Please try again later.");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
