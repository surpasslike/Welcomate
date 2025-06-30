package com.surpasslike.welcomateservice.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 数据库帮助类，用于创建和管理应用的 SQLite 数据库
 * 此类负责处理数据库的创建、版本升级等
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    // 数据库和表的常量定义
    private static final String DATABASE_NAME = "users.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_ACCOUNT = "account";
    public static final String COLUMN_PASSWORD = "password";

    /**
     * 创建用户表的 SQL 语句
     * 定义了表的结构，包括用户ID（主键）、用户名、账户和密码
     */
    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT, " +
                    COLUMN_ACCOUNT + " TEXT UNIQUE, " + // 账户应该是唯一的
                    COLUMN_PASSWORD + " TEXT" +
                    ")";

    /**
     * 构造函数
     *
     * @param context 应用上下文
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * 在数据库首次创建时调用
     *
     * @param db SQLiteDatabase 实例
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database and users table...");
        db.execSQL(CREATE_TABLE_USERS);
        Log.d(TAG, "Database created successfully.");
    }

    /**
     * 在数据库版本需要升级时调用
     *
     * @param db         SQLiteDatabase 实例
     * @param oldVersion 旧版本号
     * @param newVersion 新版本号
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data.");
        // 简单的升级策略：删除旧表并重新创建
        // 注意：在生产环境中，这应该被一个迁移策略所取代，以保留用户数据
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
        Log.d(TAG, "Database upgraded successfully.");
    }
}
