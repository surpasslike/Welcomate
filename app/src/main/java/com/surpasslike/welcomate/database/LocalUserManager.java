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
    
    private LocalUserManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public static synchronized LocalUserManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocalUserManager(context.getApplicationContext());
        }
        return instance;
    }
    
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
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
    
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