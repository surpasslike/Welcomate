package com.surpasslike.welcomate.constants;

/**
 * 应用程序常量类
 * 定义应用中使用的各种常量值
 */
public class AppConstants {
    
    /**
     * 服务相关常量
     */
    public static class Service {
        /** AdminService服务包名 */
        public static final String ADMIN_SERVICE_PACKAGE = "com.surpasslike.welcomateservice";
        /** AdminService服务类名 */
        public static final String ADMIN_SERVICE_CLASS = "com.surpasslike.welcomateservice.service.AdminService";
    }
    
    /**
     * Intent参数常量
     */
    public static class IntentExtra {
        /** 用户名参数键 */
        public static final String USERNAME = "username";
    }
    
    
    /**
     * 文本长度限制常量
     */
    public static class TextLimit {
        /** 用户名最小长度 */
        public static final int USERNAME_MIN_LENGTH = 1;
        /** 用户名最大长度 */
        public static final int USERNAME_MAX_LENGTH = 20;
        /** 密码最小长度 */
        public static final int PASSWORD_MIN_LENGTH = 1;
        /** 密码最大长度 */
        public static final int PASSWORD_MAX_LENGTH = 20;
    }
}