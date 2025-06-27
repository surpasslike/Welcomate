package com.surpasslike.welcomateservice;

interface IAdminService {
    String loginAdmin(String account, String password);//登录
    boolean registerUser(String username, String account, String password);//注册
    void deleteUser(String username);//删除
    void updateUserPassword(String username, String newPassword);//改密
    
    // 新增：让服务端获取客户端本地数据的方法
    List<String> getLocalUsers();//获取本地所有用户信息，格式：username|account|password
    void clearLocalUsers();//清空本地用户数据（同步完成后调用）
    
    // 新增：实时通知服务端数据变化
    void notifyUserRegistered(String username, String account, String password);//通知用户注册
    void notifyUserDeleted(String username);//通知用户删除
    void notifyPasswordUpdated(String username, String newPassword);//通知密码更新
    
    // 新增：检查用户是否存在
    boolean userExists(String account);//检查用户是否存在
}