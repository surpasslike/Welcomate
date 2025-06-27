package com.surpasslike.welcomate.sync;

/**
 * 同步操作数据类
 */
public class SyncOperation {
    public enum Type {
        REGISTER, UPDATE_PASSWORD, DELETE_USER, SYNC_ALL
    }
    
    private final Type type;
    private final String username;
    private final String account;
    private final String password;
    private final long timestamp;
    
    public SyncOperation(Type type, String username, String account, String password) {
        this.type = type;
        this.username = username;
        this.account = account;
        this.password = password;
        this.timestamp = System.currentTimeMillis();
    }
    
    public SyncOperation(Type type, String username) {
        this(type, username, null, null);
    }
    
    // Getters
    public Type getType() { return type; }
    public String getUsername() { return username; }
    public String getAccount() { return account; }
    public String getPassword() { return password; }
    public long getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return "SyncOperation{" +
                "type=" + type +
                ", username='" + username + '\'' +
                ", account='" + account + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
    
    /**
     * 生成唯一的缓存键
     */
    public String getCacheKey() {
        return type + "_" + (username != null ? username : "null") + "_" + (account != null ? account : "null");
    }
}