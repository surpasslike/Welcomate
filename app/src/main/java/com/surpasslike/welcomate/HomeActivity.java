package com.surpasslike.welcomate;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.surpasslike.welcomate.databinding.ActivityHomeBinding;
import com.surpasslike.welcomateservice.IAdminService;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding; // 声明 ViewBinding 对象

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater()); // 使用 ViewBinding 初始化布局
        View view = binding.getRoot();
        setContentView(view);


        String username = getIntent().getStringExtra("username");

        String welcomeMessage = "Welcome，" + username + "!";
        binding.tvWelcomeMessage.setText(welcomeMessage);

        binding.btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog(username);
            }
        });

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteUserDialog(username);
            }
        });
    }

    // 获取已经绑定的AdminService对象
    IAdminService adminService = MainActivity.getAdminService();

    private void showChangePasswordDialog(final String username) {
        // 创建对话框以更改所选用户的密码
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password for " + username);

        // 使用自定义的对话框布局
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        final EditText etNewPassword = view.findViewById(R.id.etNewPassword);
        builder.setView(view);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 获取新密码
                String newPassword = etNewPassword.getText().toString();

                // 调用方法以更改所选用户的密码
                try {
                    adminService.updateUserPassword(username, newPassword);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                showToast("Password changed for " + username);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 取消密码更改操作
            }
        });
        // 显示对话框
        builder.create().show();
    }


    private void showDeleteUserDialog(final String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm deletion of current user？");
        builder.setMessage("After deletion, the current user will not be able to access the app.");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 调用方法删除当前用户
                try {
                    adminService.deleteUser(username);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                showToast("User deleted");
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 取消删除当前用户操作
            }
        });

        builder.create().show();
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
