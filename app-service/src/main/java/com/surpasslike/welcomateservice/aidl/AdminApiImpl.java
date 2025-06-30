package com.surpasslike.welcomateservice.aidl;

import com.surpasslike.welcomateservice.IAdminService;
import com.surpasslike.welcomateservice.data.UserRepository;

/**
 * AIDL 接口 IAdminService 的具体实现
 * 此类处理所有客户端通过 AIDL 请求的业务逻辑，包括用户认证和管理
 * 它将所有数据操作委托给 UserRepository
 * 注意：此类中的方法将在 Binder 线程池中执行
 */
public class AdminApiImpl extends IAdminService.Stub {
    private final UserRepository userRepository;

    /**
     * 构造函数，初始化用户仓库
     */
    public AdminApiImpl() {
        this.userRepository = UserRepository.getInstance();
    }

    /**
     * 验证管理员登录
     *
     * @param account  用户输入的账户
     * @param password 用户输入的原始密码
     * @return 如果登录成功，返回用户名；否则返回 null
     */
    @Override
    public String loginAdmin(String account, String password) {
        return userRepository.loginAdmin(account, password);
    }

    /**
     * 注册一个新用户
     *
     * @param username 用户名
     * @param account  账户
     * @param password 原始密码
     * @return 如果注册成功，返回 true；否则返回 false
     */
    @Override
    public boolean registerUser(String username, String account, String password) {
        long rowId = userRepository.addUser(username, account, password);
        return rowId != -1;
    }

    /**
     * 根据用户名删除一个用户
     *
     * @param username 要删除的用户的用户名
     */
    @Override
    public void deleteUser(String username) {
        userRepository.deleteUser(username);
    }

    /**
     * 更新指定用户的密码
     *
     * @param username    要更新密码的用户的用户名
     * @param newPassword 新的原始密码
     */
    @Override
    public void updateUserPassword(String username, String newPassword) {
        userRepository.updateUserPassword(username, newPassword);
    }
}
