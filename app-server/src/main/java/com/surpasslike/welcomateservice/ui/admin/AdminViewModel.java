package com.surpasslike.welcomateservice.ui.admin;

import androidx.lifecycle.ViewModel;

import com.surpasslike.welcomateservice.data.UserRepository;
import com.surpasslike.welcomateservice.data.model.User;

import java.util.List;

/**
 * Admin UI 的 ViewModel
 * 负责为 UI 提供数据，并处理用户的交互逻辑
 * 它将所有数据操作委托给 UserRepository
 */
public class AdminViewModel extends ViewModel {
    private final UserRepository userRepository;

    /**
     * 构造函数
     * 初始化用户仓库
     */
    public AdminViewModel() {
        this.userRepository = UserRepository.getInstance();
    }

    /**
     * 验证管理员登录
     *
     * @param account  用户输入的账户
     * @param password 用户输入的原始密码
     * @return 如果登录成功，返回 true；否则返回 false
     */
    public boolean loginAdmin(String account, String password) {
        // loginAdmin 返回的是用户名，如果不为 null 则表示成功
        return userRepository.loginAdmin(account, password) != null;
    }

    /**
     * 添加一个新用户
     *
     * @param username 用户名
     * @param account  账户
     * @param password 原始密码
     * @return 新插入行的行 ID，如果发生错误则为 -1
     */
    public long addUser(String username, String account, String password) {
        return userRepository.addUser(username, account, password);
    }

    /**
     * 获取所有用户的列表
     *
     * @return 包含所有用户的 List
     */
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    /**
     * 根据用户名删除一个用户
     *
     * @param username 要删除的用户的用户名
     */
    public void deleteUser(String username) {
        userRepository.deleteUser(username);
    }

    /**
     * 更新指定用户的密码
     *
     * @param username    要更新密码的用户的用户名
     * @param newPassword 新的原始密码
     */
    public void changeUserPassword(String username, String newPassword) {
        userRepository.updateUserPassword(username, newPassword);
    }
}

