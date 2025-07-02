package com.surpasslike.welcomateservice.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;

import com.surpasslike.welcomateservice.MyApplication;
import com.surpasslike.welcomateservice.data.db.DatabaseHelper;
import com.surpasslike.welcomateservice.data.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据的仓库层
 * 作为应用中所有用户数据的唯一真实来源 (Single Source of Truth)
 * 负责处理所有数据操作，无论是来自本地数据库还是未来的网络 API
 * 这是一个单例，以确保整个应用只有一个数据源实例
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    private final DatabaseHelper dbHelper;

    // 单例模式实现
    private static volatile UserRepository INSTANCE;

    /**
     * 私有构造函数，防止外部直接实例化
     */
    private UserRepository() {
        this.dbHelper = new DatabaseHelper(MyApplication.getContext());
    }

    /**
     * 获取 UserRepository 的单例
     *
     * @return UserRepository 的唯一实例
     */
    public static UserRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (UserRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserRepository();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 对给定的密码字符串进行 SHA-256 哈希处理
     *
     * @param password 要哈希的原始密码
     * @return 哈希后并经过 Base64 编码的字符串如果算法不可用，则返回 null
     */
    private String hashPassword(String password) {
        if (password == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "SHA-256 algorithm not found", e);
            return null;
        }
    }

    /**
     * 验证管理员或用户登录
     *
     * @param account  用户输入的账户
     * @param password 用户输入的原始密码
     * @return 如果登录成功，返回包含用户信息的 User 对象；否则返回 null
     */
    public User login(String account, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {
                DatabaseHelper.COLUMN_USERNAME,
                DatabaseHelper.COLUMN_PASSWORD,
                DatabaseHelper.COLUMN_ROLE
        };
        String selection = DatabaseHelper.COLUMN_ACCOUNT + " = ?";
        String[] selectionArgs = {account};

        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null)) {
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") String storedPasswordHash = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD));
                String inputPasswordHash = hashPassword(password);

                if (inputPasswordHash != null && inputPasswordHash.equals(storedPasswordHash)) {
                    @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME));
                    @SuppressLint("Range") String role = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ROLE));
                    // 登录成功，返回 User 对象
                    return new User(username, account, null, role);
                }
            }
        }
        return null; // 账户不存在或密码错误
    }

    /**
     * 添加新用户（供服务端使用，默认为管理员）
     *
     * @param username 用户名
     * @param account  账户
     * @param password 原始密码
     * @return 新插入行的行 ID，如果发生错误则为 -1
     */
    public long addUser(String username, String account, String password) {
        return addUser(username, account, password, "ADMIN");
    }

    /**
     * 添加新用户（核心方法）
     *
     * @param username 用户名
     * @param account  账户
     * @param password 原始密码
     * @param role     用户角色 ("ADMIN" 或 "USER")
     * @return 新插入行的行 ID，如果发生错误则为 -1
     */
    public long addUser(String username, String account, String password, String role) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, username);
        values.put(DatabaseHelper.COLUMN_ACCOUNT, account);
        values.put(DatabaseHelper.COLUMN_ROLE, role);

        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            return -1; // 哈希失败
        }
        values.put(DatabaseHelper.COLUMN_PASSWORD, hashedPassword);

        return db.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    /**
     * 获取所有用户的列表
     *
     * @return 包含所有用户的 List
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {
                DatabaseHelper.COLUMN_USERNAME,
                DatabaseHelper.COLUMN_ACCOUNT,
                DatabaseHelper.COLUMN_ROLE
        };
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, columns, null, null, null, null, null)) {
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") String username = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USERNAME));
                    @SuppressLint("Range") String account = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ACCOUNT));
                    @SuppressLint("Range") String role = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ROLE));
                    userList.add(new User(username, account, null, role));
                } while (cursor.moveToNext());
            }
        }
        return userList;
    }

    /**
     * 根据用户名删除一个用户
     *
     * @param username 要删除的用户的用户名
     */
    public void deleteUser(String username) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = DatabaseHelper.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        db.delete(DatabaseHelper.TABLE_USERS, selection, selectionArgs);
    }

    /**
     * 更新指定用户的密码
     *
     * @param username    要更新密码的用户的用户名
     * @param newPassword 新的原始密码
     */
    public void updateUserPassword(String username, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        String hashedPassword = hashPassword(newPassword);
        if (hashedPassword == null) {
            Log.e(TAG, "Password hashing failed, update aborted.");
            return;
        }
        values.put(DatabaseHelper.COLUMN_PASSWORD, hashedPassword);

        String selection = DatabaseHelper.COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};
        db.update(DatabaseHelper.TABLE_USERS, values, selection, selectionArgs);
    }
}