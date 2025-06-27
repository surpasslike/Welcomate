package com.surpasslike.welcomate;


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
import android.widget.Toast;

import com.surpasslike.welcomateservice.IAdminService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    static IAdminService adminService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 绑定成功时调用，获取AdminService的代理对象
            adminService = IAdminService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // 解绑时调用
            adminService = null;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 绑定服务
        bindAdminService();

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button_login = findViewById(R.id.button_login);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button_register = findViewById(R.id.button_register);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button button_guest = findViewById(R.id.button_guest);
        
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        button_guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GuestHomeActivity.class);
                startActivity(intent);
            }
        });

    }

    private void bindAdminService() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.surpasslike.welcomateservice", "com.surpasslike.welcomateservice.admin.AdminService"));//在此绑定
        boolean isServiceBound = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        if (isServiceBound) {
            showToast("AdminService bound successfully!");
        } else {
            showToast("Failed to bind AdminService. Please try again later.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解绑AdminService
        unbindService(serviceConnection);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // 静态方法，用于获取已绑定的IAdminService对象
    public static IAdminService getAdminService() {
        return adminService;
    }
}