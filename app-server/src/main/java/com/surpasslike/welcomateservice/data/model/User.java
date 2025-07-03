package com.surpasslike.welcomateservice.data.model;

/**
 * 数据模型类，代表一个用户实体
 * POJO (Plain Old Java Object)，用于封装用户数据
 */
public class User {
    private final String username;
    private final String account;
    private final String password; // 通常在模型中不建议直接持有密码，但此处为保持与原有结构一致
    private final String role;

    /**
     * 构造函数
     *
     * @param username 用户名
     * @param account  用户账户
     * @param password 用户密码（或密码哈希）
     * @param role     用户角色
     */
    public User(String username, String account, String password, String role) {
        this.username = username;
        this.account = account;
        this.password = password;
        this.role = role;
    }

    /**
     * 获取用户名
     *
     * @return 用户名字符串
     */
    public String getUsername() {
        return username;
    }

    /**
     * 获取用户账户
     *
     * @return 用户账户字符串
     */
    public String getAccount() {
        return account;
    }

    /**
     * 获取用户密码
     * 警告：返回的可能是原始密码或哈希值，取决于对象的创建方式
     * 在安全实现中，应避免暴露此字段
     *
     * @return 密码字符串
     */
    public String getPassword() {
        return password;
    }

    /**
     * 获取用户角色
     *
     * @return 用户角色字符串
     */
    public String getRole() {
        return role;
    }
}