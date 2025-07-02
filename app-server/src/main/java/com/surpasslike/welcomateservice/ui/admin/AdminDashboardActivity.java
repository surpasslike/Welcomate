package com.surpasslike.welcomateservice.ui.admin;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.surpasslike.welcomateservice.R;
import com.surpasslike.welcomateservice.data.model.User;
import com.surpasslike.welcomateservice.databinding.ActivityAdminDashboardBinding;

import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;
    private AdminViewModel adminViewModel;
    private List<User> userList;
    private AdminUserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        adminViewModel = new ViewModelProvider(this).get(AdminViewModel.class);
        // 初始化 RecyclerView 和用户列表
        RecyclerView recyclerView = binding.recyclerView;
        userList = adminViewModel.getAllUsers();
        adapter = new AdminUserAdapter(userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        binding.btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //实现修改指定用户密码功能
                showUserListDialog();
            }
        });

        binding.btnAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 实现增加用户的功能
                showAddUserDialog();

            }
        });

        binding.btnDeleteUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 实现删除用户的功能
                showDeleteUserDialog();
            }
        });
    }

    // 显示Toast提示信息的方法
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    //显示用户列表对话框
    private void showUserListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select User to Change Password");

        //将其替换为数据库中的实际用户列表
        List<User> userList = adminViewModel.getAllUsers();

        // 将用户列表转换为要在对话框中显示的用户名数组
        String[] usernames = new String[userList.size()];
        for (int i = 0; i < userList.size(); i++) {
            usernames[i] = userList.get(i).getUsername();
        }

        // 在对话框中显示用户名列表
        builder.setItems(usernames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 获取所选用户名
                String selectedUsername = userList.get(which).getUsername();

                // 调用方法以更改所选用户的密码
                showChangePasswordDialog(selectedUsername);
            }
        });

        //显示对话框
        builder.create().show();
    }

    //显示更改密码对话框
    private void showChangePasswordDialog(String username) {
        // 创建对话框以更改所选用户的密码
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password for " + username);

        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        final EditText etNewPassword = view.findViewById(R.id.etNewPassword);
        builder.setView(view);

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 获取新密码
                String newPassword = etNewPassword.getText().toString();

                // 调用方法以更改所选用户的密码
                adminViewModel.changeUserPassword(username, newPassword);
                userList = adminViewModel.getAllUsers(); // 添加用户后刷新用户列表
                adapter.setUserList(userList); // 使用新用户列表更新适配器
                showToast("Password changed for " + username);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 取消密码更改操作
            }
        });
        //显示对话框
        builder.create().show();
    }

    // 增加用户的功能
    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New User");

        View view = getLayoutInflater().inflate(R.layout.dialog_add_user, null);
        final EditText etNewUsername = view.findViewById(R.id.etNewUsername);
        final EditText etNewAccount = view.findViewById(R.id.etNewAccount);
        final EditText etNewPassword = view.findViewById(R.id.etNewPassword);
        builder.setView(view);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 从输入字段中获取新用户的详细信息
                String newUsername = etNewUsername.getText().toString();
                String newAccount = etNewAccount.getText().toString();
                String newPassword = etNewPassword.getText().toString();

                // 调用方法以添加新用户
                long rowId = adminViewModel.addUser(newUsername, newAccount, newPassword, "USER");
                if (rowId != -1) {
                    showToast("User added");
                    userList = adminViewModel.getAllUsers(); // 添加用户后刷新用户列表
                    adapter.setUserList(userList); // 使用新用户列表更新适配器
                } else {
                    showToast("Failed to add user");
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 取消用户添加操作
            }
        });

        builder.create().show();
    }

    private void showDeleteUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete User");

        View view = getLayoutInflater().inflate(R.layout.dialog_delete_user, null);
        final EditText etUsernameToDelete = view.findViewById(R.id.etUsernameToDelete);
        builder.setView(view);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 从输入字段中获取要删除的用户名
                String usernameToDelete = etUsernameToDelete.getText().toString();

                // 调用方法删除用户
                adminViewModel.deleteUser(usernameToDelete);
                showToast("User deleted");
                userList = adminViewModel.getAllUsers(); // 删除用户后刷新用户列表
                adapter.setUserList(userList); // 使用更新的用户列表更新适配器
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 取消用户删除操作
            }
        });

        builder.create().show();
    }
}