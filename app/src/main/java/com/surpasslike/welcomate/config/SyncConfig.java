package com.surpasslike.welcomate.config;

/**
 * 同步配置管理类
 * 统一管理所有同步相关的配置参数
 */
public class SyncConfig {
    
    // 客户端配置
    public static final String CLIENT_PACKAGE = "com.surpasslike.welcomate";
    public static final String CLIENT_SERVICE = CLIENT_PACKAGE + ".service.ClientSyncService";
    
    // 服务端配置
    public static final String SERVER_PACKAGE = "com.surpasslike.welcomateservice";
    public static final String SERVER_SERVICE = SERVER_PACKAGE + ".AdminService";
    
    // 时间配置 (毫秒)
    public static final int STARTUP_SYNC_DELAY = 2000;          // 启动同步延迟
    public static final int CONNECTION_TIMEOUT = 5000;         // 连接超时
    public static final int SYNC_CACHE_DURATION = 30000;       // 同步缓存持续时间 (30秒)
    
    // 同步配置
    public static final int MAX_BATCH_SIZE = 50;               // 最大批量同步数量
    public static final boolean ENABLE_SYNC_CACHE = true;      // 是否启用同步缓存
    
    // 广播配置
    public static final String DATABASE_CHANGE_ACTION = "com.surpasslike.welcomateservice.DATABASE_CHANGED";
    
    // 调试配置
    public static final boolean DEBUG_SYNC = true;             // 是否启用同步调试日志
    
    private SyncConfig() {
        // 工具类，禁止实例化
    }
}