package com.surpasslike.welcomate.note.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NoteDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "NoteDatabaseHelper";

    private static final String DATABASE_NAME = "note.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NOTES = "notes";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_CREATE_TIME = "createTime";
    public static final String COLUMN_UPDATE_TIME = "updateTime";


    private static final String CREATE_TABLE_NOTES =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // 递增的id
                    COLUMN_TITLE + " TEXT, " + // 注意TEXT前面有空格
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_CREATE_TIME + " TEXT, " +
                    COLUMN_UPDATE_TIME + " TEXT" +
                    ")";

    /**
     * 构造函数
     *
     * @param context 应用上下文
     */
    public NoteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * 数据库首次创建时调用
     * 表格的创建和表的初始填充应该会完成
     *
     * @param db SQLiteDatabase 实例
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database and notes table...");
        db.execSQL(CREATE_TABLE_NOTES);
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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
        Log.d(TAG, "Database upgraded successfully.");
    }
}