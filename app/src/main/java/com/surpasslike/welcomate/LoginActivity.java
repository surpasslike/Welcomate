package com.surpasslike.welcomate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;

import com.surpasslike.welcomate.databinding.ActivityLoginBinding;
import com.surpasslike.welcomateservice.IAdminService;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = binding.etAccount.getText().toString();
                String password = binding.etPassword.getText().toString();

                // 获取已经绑定的AdminService对象
                IAdminService adminService = MainActivity.getAdminService();

                if (adminService != null) {
                    try {
                        String username = adminService.loginAdmin(account, password);
                        if (username != null) {
                            showToast("Login successful!");

                            // 登录成功后跳转到HomeActivity并传递用户名
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.putExtra("username", username);
                            startActivity(intent);
                            finish();
                        } else {
                            showToast("Login failed! Please check your account and password.");
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    showToast("AdminService not available. Please try again later.");
                }
            }
        });

    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
