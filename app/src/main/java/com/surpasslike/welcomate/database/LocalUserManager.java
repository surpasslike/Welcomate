package com.surpasslike.welcomate.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地用户数据管理器 - 客户端数据库A管理类
 * 
 * 功能职责：
 * 1. 管理客户端本地SQLite数据库（数据库A）
 * 2. 提供用户数据的CRUD操作（增删改查）
 * 3. 维护用户同步状态，跟踪哪些数据已与服务端同步
 * 4. 支持密码的哈希存储和明文备份（用于同步）
 * 5. 提供单例模式，确保全局唯一实例
 * 
 * 数据表结构：
 * - id: 主键
 * - username: 用户名
 * - account: 用户账号
 * - password: 哈希后的密码（用于本地验证）
 * - raw_password: 明文密码（用于同步到服务端）
 * - is_synced: 同步状态（0=未同步，1=已同步）
 * - created_time: 创建时间
 * - updated_time: 更新时间
 * 
 * 同步机制：
 * - 新用户默认未同步状态，等待推送到服务端
 * - 同步成功后标记为已同步，避免重复操作
 * - 支持获取未同步用户列表，用于批量同步
 */
public class LocalUserManager extends SQLiteOpenHelper {
    private static final String TAG = "LocalUserManager";
    
    private static final String DATABASE_NAME = "local_users.db";
    private static final int DATABASE_VERSION = 2;
    
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_ACCOUNT = "account";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_RAW_PASSWORD = "raw_password";
    private static final String COLUMN_IS_SYNCED = "is_synced";
    private static final String COLUMN_CREATED_TIME = "created_time";
    private static final String COLUMN_UPDATED_TIME = "updated_time";
    
    private static LocalUserManager instance;
    
    /**
     * 私有构造函数 - 单例模式
     * 
     * @param context Android上下文
     */
    private LocalUserManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    /**
     * 获取单例实例
     * 确保全局只有一个LocalUserManager实例，避免数据库连接冲突
     * 
     * @param context Android上下文
     * @return LocalUserManager单例实例
     */
    public static synchronized LocalUserManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocalUserManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * 数据库首次创建时的回调
     * 创建用户表，定义表结构和索引
     * 
     * @param db SQLite数据库实例
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, " +
                COLUMN_ACCOUNT + " TEXT NOT NULL UNIQUE, " +
                COLUMN_PASSWORD + " TEXT NOT NULL, " +
                COLUMN_RAW_PASSWORD + " TEXT NOT NULL, " +
                COLUMN_IS_SYNCED + " INTEGER DEFAULT 0, " +
                COLUMN_CREATED_TIME + " INTEGER DEFAULT (strftime('%s','now')), " +
                COLUMN_UPDATED_TIME + " INTEGER DEFAULT (strftime('%s','now'))" +
                ")";
        db.execSQL(createTableQuery);
        Log.d(TAG, "Local users table created");
    }
    
    /**
     * 数据库版本升级时的回调
     * 处理数据表结构的变更和数据迁移
     * 
     * @param db SQLite数据库实例
     * @param oldVersion 旧版本号
     * @param newVersion 新版本号
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
    
    /**
     * 密码哈希处理
     * 使用SHA-256算法对密码进行不可逆加密，确保本地存储安全
     * 
     * @param password 明文密码
     * @return 哈希后的密码字符串
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password", e);
            return password;
        }
    }
    
    /**
     * 用户注册
     * 在本地数据库A中创建新用户记录，默认为未同步状态
     * 
     * @param username 用户名（必须唯一）
     * @param account 用户账号（必须唯一）
     * @param password 用户密码（明文）
     * @return 注册成功返回true，失败返回false
     */
    public boolean registerUser(String username, String account, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_USERNAME, username);
            values.put(COLUMN_ACCOUNT, account);
            values.put(COLUMN_PASSWORD, hashPassword(password));
            values.put(COLUMN_RAW_PASSWORD, password); // 保存原始密码用于同步
            values.put(COLUMN_IS_SYNCED, 0);
            values.put(COLUMN_CREATED_TIME, System.currentTimeMillis() / 1000);
            values.put(COLUMN_UPDATED_TIME, System.currentTimeMillis() / 1000);
            
            long result = db.insert(TABLE_USERS, null, values);
            if (result != -1) {
                Log.d(TAG, "User registered locally: " + username);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error registering user locally", e);
        } finally {
            db.close();
        }
        return false;
    }
    
    /**
     * 用户登录验证
     * 使用哈希密码在本地数据库A中验证用户身份
     * 
     * @param account 用户账号
     * @param password 用户密码（明文）
     * @return 登录成功返回用户名，失败返回null
     */
    public String loginUser(String account, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String hashedPassword = hashPassword(password);
            String selection = COLUMN_ACCOUNT + " = ? AND " + COLUMN_PASSWORD + " = ?";
            String[] selectionArgs = {account, hashedPassword};
            
            Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USERNAME}, 
                    selection, selectionArgs, null, null, null);
            
            if (cursor.moveToFirst()) {
                String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
                cursor.close();
                Log.d(TAG, "User logged in locally: " + username);
                return username;
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error logging in user locally", e);
        } finally {
            db.close();
        }
        return null;
    }
    
    /**
     * 更新用户密码
     * 更新本地数据库A中用户的密码，同时重置同步状态
     * 
     * @param username 用户名
     * @param newPassword 新密码（明文）
     * @return 更新成功返回true，失败返回false
     */
    public boolean updateUserPassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PASSWORD, hashPassword(newPassword));
            values.put(COLUMN_RAW_PASSWORD, newPassword); // 更新原始密码用于同步
            values.put(COLUMN_IS_SYNCED, 0);
            values.put(COLUMN_UPDATED_TIME, System.currentTimeMillis() / 1000);
            
            String selection = COLUMN_USERNAME + " = ?";
            String[] selectionArgs = {username};
            
            int rowsAffected = db.update(TABLE_USERS, values, selection, selectionArgs);
            if (rowsAffected > 0) {
                Log.d(TAG, "Password updated locally for user: " + username);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating password locally", e);
        } finally {
            db.close();
        }
        return false;
    }
    
    /**
     * 删除用户
     * 从本地数据库A中删除指定用户的记录
     * 
     * @param username 要删除的用户名
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String selection = COLUMN_USERNAME + " = ?";
            String[] selectionArgs = {username};
            
            int rowsDeleted = db.delete(TABLE_USERS, selection, selectionArgs);
            if (rowsDeleted > 0) {
                Log.d(TAG, "User deleted locally: " + username);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting user locally", e);
        } finally {
            db.close();
        }
        return false;
    }
    
    /**
     * 按账号删除用户
     * 根据用户账号从本地数据库A中删除用户记录
     * 主要用于同步时删除服务端已删除的用户
     * 
     * @param account 用户账号
     * @return 删除成功返回true，失败返回false
     */
    public boolean deleteUserByAccount(String account) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            String selection = COLUMN_ACCOUNT + " = ?";
            String[] selectionArgs = {account};
            
            int rowsDeleted = db.delete(TABLE_USERS, selection, selectionArgs);
            if (rowsDeleted > 0) {
                Log.d(TAG, "User deleted locally by account: " + account);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting user by account locally", e);
        } finally {
            db.close();
        }
        return false;
    }
    
    /**
     * 获取未同步的用户列表
     * 返回所有is_synced=0的用户，用于批量同步到服务端
     * 
     * @return 未同步用户的列表
     */
    public List<LocalUser> getUnsyncedUsers() {
        List<LocalUser> unsyncedUsers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String selection = COLUMN_IS_SYNCED + " = ?";
            String[] selectionArgs = {"0"};
            
            Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, null);
            
            while (cursor.moveToNext()) {
                LocalUser user = new LocalUser();
                user.id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                user.username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
                user.account = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACCOUNT));
                user.password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                user.rawPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RAW_PASSWORD));
                user.isSynced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SYNCED)) == 1;
                user.createdTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_TIME));
                user.updatedTime = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_UPDATED_TIME));
                unsyncedUsers.add(user);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting unsynced users", e);
        } finally {
            db.close();
        }
        return unsyncedUsers;
    }
    
    public boolean markUserAsSynced(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_IS_SYNCED, 1);
            
            String selection = COLUMN_USERNAME + " = ?";
            String[] selectionArgs = {username};
            
            int rowsAffected = db.update(TABLE_USERS, values, selection, selectionArgs);
            if (rowsAffected > 0) {
                Log.d(TAG, "User marked as synced: " + username);
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error marking user as synced", e);
        } finally {
            db.close();
        }
        return false;
    }
    
    public boolean userExists(String account) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String selection = COLUMN_ACCOUNT + " = ?";
            String[] selectionArgs = {account};
            
            Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID}, 
                    selection, selectionArgs, null, null, null);
            
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            return exists;
        } catch (Exception e) {
            Log.e(TAG, "Error checking if user exists", e);
        } finally {
            db.close();
        }
        return false;
    }
    
    public List<String> getAllUsersForSync() {
        List<String> userDataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.query(TABLE_USERS, null, null, null, null, null, null);
            
            while (cursor.moveToNext()) {
                String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
                String account = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACCOUNT));
                String rawPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RAW_PASSWORD));
                
                // 格式：username|account|password
                String userData = username + "|" + account + "|" + rawPassword;
                userDataList.add(userData);
            }
            cursor.close();
        } catch (Exception e) {
            Log.e(TAG, "Error getting all users for sync", e);
        } finally {
            db.close();
        }
        return userDataList;
    }
    
    public boolean clearAllUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            int deletedRows = db.delete(TABLE_USERS, null, null);
            Log.d(TAG, "Cleared " + deletedRows + " users from local database");
            return deletedRows >= 0;
        } catch (Exception e) {
            Log.e(TAG, "Error clearing all users", e);
        } finally {
            db.close();
        }
        return false;
    }
    
    public static class LocalUser {
        public int id;
        public String username;
        public String account;
        public String password;
        public String rawPassword; // 原始密码用于同步到远程服务
        public boolean isSynced;
        public long createdTime;
        public long updatedTime;
    }
}