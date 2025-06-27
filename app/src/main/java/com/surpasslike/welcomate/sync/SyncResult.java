package com.surpasslike.welcomate.sync;

/**
 * 同步结果枚举
 * 统一定义所有同步操作的返回状态
 */
public enum SyncResult {
    SUCCESS("操作成功"),
    FAILED("操作失败"),
    CLIENT_UNAVAILABLE("客户端不可用"),
    SERVER_UNAVAILABLE("服务端不可用"),
    ALREADY_EXISTS("数据已存在"),
    NOT_FOUND("数据不存在"),
    NETWORK_ERROR("网络错误"),
    TIMEOUT("连接超时"),
    PERMISSION_DENIED("权限不足"),
    INVALID_DATA("数据无效"),
    CACHE_HIT("缓存命中，跳过同步");
    
    private final String message;
    
    SyncResult(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public boolean isSuccess() {
        return this == SUCCESS || this == CACHE_HIT;
    }
    
    public boolean shouldRetry() {
        return this == NETWORK_ERROR || this == TIMEOUT || this == SERVER_UNAVAILABLE;
    }
}