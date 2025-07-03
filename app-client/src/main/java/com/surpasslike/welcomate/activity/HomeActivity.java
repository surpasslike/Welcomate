package com.surpasslike.welcomate.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.surpasslike.welcomate.R;
import com.surpasslike.welcomate.constants.AppConstants;
import com.surpasslike.welcomate.databinding.ActivityHomeBinding;
import com.surpasslike.welcomate.service.ServiceManager;
import com.surpasslike.welcomate.utils.ToastUtils;
import com.surpasslike.welcomate.utils.ValidationUtils;
import com.surpasslike.welcomateservice.IAdminService;

/**
 * 用户主页面
 * 显示用户信息，提供密码修改、用户删除和设置功能
 */
public class HomeActivity extends AppCompatActivity {

    /** 日志标签 */
    private static final String TAG = "HomeActivity";
    
    // 视图绑定对象
    private ActivityHomeBinding mActivityHomeBinding;
    
    // ServiceManager实例
    private ServiceManager mServiceManager;

    /**
     * 活动创建时的初始化方法
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityHomeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(mActivityHomeBinding.getRoot());

        // 获取传递的用户名
        String username = getIntent().getStringExtra(AppConstants.IntentExtra.USERNAME);
        
        // 获取ServiceManager实例
        mServiceManager = ServiceManager.getInstance();
        
        // 初始化界面
        initViews(username);
    }
    
    /**
     * 初始化界面控件和设置事件监听器
     * @param username 当前登录的用户名
     */
    private void initViews(String username) {
        // 设置欢迎消息
        String welcomeMessage = getString(R.string.welcome_message, username);
        mActivityHomeBinding.tvWelcomeMessage.setText(welcomeMessage);

        // 设置修改密码按钮点击事件
        mActivityHomeBinding.btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangePasswordDialog(username);
            }
        });

        // 设置删除用户按钮点击事件
        mActivityHomeBinding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteUserDialog(username);
            }
        });

        // 设置设置按钮点击事件
        mActivityHomeBinding.btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToSettings();
            }
        });
    }
    
    /**
     * 显示修改密码对话框
     * @param username 要修改密码的用户名
     */
    private void showChangePasswordDialog(final String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_change_password_title, username));

        // 使用自定义的对话框布局
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        final EditText etNewPassword = view.findViewById(R.id.etNewPassword);
        builder.setView(view);

        // 设置按钮但暂时不设置监听器
        builder.setPositiveButton(R.string.button_confirm, null);
        builder.setNegativeButton(R.string.button_cancel, null);
        
        // 创建并显示对话框
        final AlertDialog dialog = builder.create();
        dialog.show();
        
        // 手动设置确认按钮监听器，控制对话框关闭行为
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPassword = etNewPassword.getText().toString();
                if (validateNewPassword(newPassword)) {
                    changeUserPassword(username, newPassword);
                    dialog.dismiss(); // 只有验证通过才关闭对话框
                } else {
                    // 验证失败时清空输入框并重新获取焦点，让用户重新输入
                    etNewPassword.setText("");
                    etNewPassword.requestFocus();
                }
            }
        });
        
        // 取消按钮保持默认行为（关闭对话框）
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    
    /**
     * 验证新密码是否符合要求
     * @param newPassword 新密码
     * @return true表示密码有效，false表示密码无效
     */
    private boolean validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            ToastUtils.showShort(this, R.string.account_empty);
            return false;
        }
        
        if (!ValidationUtils.isValidPassword(newPassword)) {
            if (newPassword.length() < AppConstants.TextLimit.PASSWORD_MIN_LENGTH) {
                ToastUtils.showShort(this, getString(R.string.password_too_short, 
                    AppConstants.TextLimit.PASSWORD_MIN_LENGTH));
            } else {
                ToastUtils.showShort(this, getString(R.string.password_too_long, 
                    AppConstants.TextLimit.PASSWORD_MAX_LENGTH));
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * 修改用户密码
     * @param username 用户名
     * @param newPassword 新密码
     */
    private void changeUserPassword(String username, String newPassword) {
        executeServiceCall(() -> {
            IAdminService adminService = mServiceManager.getAdminService();
            adminService.updateUserPassword(username, newPassword);
            ToastUtils.showShort(this, getString(R.string.password_changed, username));
        }, "Failed to update password");
    }

    /**
     * 显示删除用户确认对话框
     * @param username 要删除的用户名
     */
    private void showDeleteUserDialog(final String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_delete_user_title);
        builder.setMessage(R.string.dialog_delete_user_message);

        // 确认删除按钮
        builder.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteUser(username);
            }
        });

        // 取消按钮
        builder.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }
    
    /**
     * 删除用户
     * @param username 要删除的用户名
     */
    private void deleteUser(String username) {
        executeServiceCall(() -> {
            IAdminService adminService = mServiceManager.getAdminService();
            adminService.deleteUser(username);
            ToastUtils.showShort(this, R.string.user_deleted);
            finish(); // 删除成功后关闭当前页面
        }, "Failed to delete user");
    }
    
    /**
     * 执行Service调用的通用方法
     * @param serviceCall 要执行的Service调用
     * @param errorMessage 出错时的日志信息
     */
    private void executeServiceCall(ServiceCall serviceCall, String errorMessage) {
        if (mServiceManager.isServiceConnected()) {
            try {
                serviceCall.execute();
            } catch (RemoteException e) {
                Log.e(TAG, errorMessage, e);
                ToastUtils.showShort(this, R.string.service_not_available);
            }
        } else {
            ToastUtils.showShort(this, R.string.service_not_available);
        }
    }
    
    /**
     * Service调用接口
     */
    private interface ServiceCall {
        void execute() throws RemoteException;
    }
    
    /**
     * 导航到设置页面
     */
    private void navigateToSettings() {
        Intent intent = new Intent(HomeActivity.this, com.surpasslike.setting.SettingActivity.class);
        startActivity(intent);
    }
    
    /**
     * 活动销毁时的清理方法
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理视图绑定
        if (mActivityHomeBinding != null) {
            mActivityHomeBinding = null;
        }
    }
}