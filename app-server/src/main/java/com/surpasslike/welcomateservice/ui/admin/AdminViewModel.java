package com.surpasslike.welcomateservice.ui.admin;

import androidx.lifecycle.ViewModel;

import com.surpasslike.welcomateservice.data.UserRepository;
import com.surpasslike.welcomateservice.data.model.User;

import java.util.List;
import java.util.Objects;

/**
 * Admin UI 的 ViewModel
 * 负责为 UI 提供数据，并处理用户的交互逻辑
 * 它将所有数据操作委托给 UserRepository
 */
public class AdminViewModel extends ViewModel {
    private final UserRepository userRepository;

    // 登录成功
    static final int LOGIN_SUCCESS = 0;
    // 账户或密码错误
    static final int LOGIN_FAILED = -1;
    // 权限不足
    static final int PERMISSION_DENIED = -2;

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
     * @return 登录结果：0-成功，-1-账户密码错误，-2-权限不足
     */
    public int loginAdmin(String account, String password) {
        User user = userRepository.login(account, password);
        if (user == null) {
            return LOGIN_FAILED; // 账户或密码错误
        }
        if (!Objects.equals(user.getRole(), "ADMIN")) {
            return PERMISSION_DENIED; // 权限不足
        }
        return LOGIN_SUCCESS; // 登录成功
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
     * 添加一个新用户
     *
     * @param username 用户名
     * @param account  账户
     * @param password 原始密码
     * @param role     用户角色
     * @return 新插入行的行 ID，如果发生错误则为 -1
     */
    public long addUser(String username, String account, String password, String role) {
        return userRepository.addUser(username, account, password, role);
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