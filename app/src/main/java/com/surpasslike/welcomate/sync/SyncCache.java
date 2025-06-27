package com.surpasslike.welcomate.sync;

import android.util.Log;

import com.surpasslike.welcomate.config.SyncConfig;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 同步缓存管理器
 * 避免重复同步操作，提高性能
 */
public class SyncCache {
    private static final String TAG = "SyncCache";
    private static volatile SyncCache instance;
    
    // 使用 ConcurrentHashMap 保证线程安全
    private final ConcurrentMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    
    private SyncCache() {}
    
    public static SyncCache getInstance() {
        if (instance == null) {
            synchronized (SyncCache.class) {
                if (instance == null) {
                    instance = new SyncCache();
                }
            }
        }
        return instance;
    }
    
    /**
     * 检查操作是否在缓存中（避免重复同步）
     */
    public boolean isOperationCached(SyncOperation operation) {
        if (!SyncConfig.ENABLE_SYNC_CACHE) {
            return false;
        }
        
        String key = operation.getCacheKey();
        CacheEntry entry = cache.get(key);
        
        if (entry == null) {
            return false;
        }
        
        // 检查缓存是否过期
        long currentTime = System.currentTimeMillis();
        if (currentTime - entry.timestamp > SyncConfig.SYNC_CACHE_DURATION) {
            cache.remove(key);
            if (SyncConfig.DEBUG_SYNC) {
                Log.d(TAG, "Cache expired for operation: " + key);
            }
            return false;
        }
        
        if (SyncConfig.DEBUG_SYNC) {
            Log.d(TAG, "Cache hit for operation: " + key);
        }
        return true;
    }
    
    /**
     * 缓存同步操作
     */
    public void cacheOperation(SyncOperation operation, SyncResult result) {
        if (!SyncConfig.ENABLE_SYNC_CACHE) {
            return;
        }
        
        String key = operation.getCacheKey();
        cache.put(key, new CacheEntry(System.currentTimeMillis(), result));
        
        if (SyncConfig.DEBUG_SYNC) {
            Log.d(TAG, "Cached operation: " + key + " with result: " + result);
        }
    }
    
    /**
     * 清空特定类型的缓存
     */
    public void clearOperationCache(SyncOperation.Type type) {
        cache.entrySet().removeIf(entry -> entry.getKey().startsWith(type.name()));
        if (SyncConfig.DEBUG_SYNC) {
            Log.d(TAG, "Cleared cache for operation type: " + type);
        }
    }
    
    /**
     * 清空所有缓存
     */
    public void clearAllCache() {
        cache.clear();
        if (SyncConfig.DEBUG_SYNC) {
            Log.d(TAG, "Cleared all sync cache");
        }
    }
    
    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        return cache.size();
    }
    
    /**
     * 缓存条目内部类
     */
    private static class CacheEntry {
        final long timestamp;
        final SyncResult result;
        
        CacheEntry(long timestamp, SyncResult result) {
            this.timestamp = timestamp;
            this.result = result;
        }
    }
}