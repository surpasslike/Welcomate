package com.surpasslike.welcomate.note.data;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.surpasslike.welcomate.WelcomateApplication;

import java.util.ArrayList;
import java.util.List;

public class NoteRepository {
    private static final String TAG = "NoteRepository";
    private final NoteDatabaseHelper dbHelper;
    private static volatile NoteRepository INSTANCE;

    private NoteRepository() {
        this.dbHelper = new NoteDatabaseHelper(WelcomateApplication.getContext());
    }

    public static NoteRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (NoteRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NoteRepository();
                }
            }
        }
        return INSTANCE;
    }

    public long addNote(String title, String content) {
        Log.d(TAG, "Adding note with title: " + title);
        // 获取可写数据库, 意思就是准备写入
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // 用 ContentValues 装数据（类似 Map）
        ContentValues values = new ContentValues();
        values.put(NoteDatabaseHelper.COLUMN_TITLE, title);
        values.put(NoteDatabaseHelper.COLUMN_CONTENT, content);
        // 获取当前时间
        String currentTime = String.valueOf(System.currentTimeMillis());
        values.put(NoteDatabaseHelper.COLUMN_CREATE_TIME, currentTime);
        values.put(NoteDatabaseHelper.COLUMN_UPDATE_TIME, currentTime);
        // 插入并返回新行的 ID
        long id = db.insert(NoteDatabaseHelper.TABLE_NOTES, null, values);
        if (id != -1) {
            Log.d(TAG, "Note added successfully with id: " + id);
        } else {
            Log.e(TAG, "Failed to add note");
        }
        return id;
    }


    @SuppressLint("Range")
    public List<Note> getAllNotes() {
        Log.d(TAG, "Getting all notes...");
        List<Note> noteList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {NoteDatabaseHelper.COLUMN_ID, NoteDatabaseHelper.COLUMN_TITLE, NoteDatabaseHelper.COLUMN_CONTENT, NoteDatabaseHelper.COLUMN_CREATE_TIME, NoteDatabaseHelper.COLUMN_UPDATE_TIME};

        try (Cursor cursor = db.query(NoteDatabaseHelper.TABLE_NOTES, columns, null, null, null, null, null)) {
            if (cursor.moveToFirst()) { // 先移动到第一行
                do {
                    String id = cursor.getString(cursor.getColumnIndex(NoteDatabaseHelper.COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndex(NoteDatabaseHelper.COLUMN_TITLE));
                    String content = cursor.getString(cursor.getColumnIndex(NoteDatabaseHelper.COLUMN_CONTENT));
                    String create_time = cursor.getString(cursor.getColumnIndex(NoteDatabaseHelper.COLUMN_CREATE_TIME));
                    String update_time = cursor.getString(cursor.getColumnIndex(NoteDatabaseHelper.COLUMN_UPDATE_TIME));
                    noteList.add(new Note(id, title, content, create_time, update_time));
                } while (cursor.moveToNext());
            }
        }
        Log.d(TAG, "Found " + noteList.size() + " notes");
        return noteList;
    }

    // 查询某一条笔记
    @SuppressLint("Range")
    public List<Note> searchNotes(String keyWord) {
        Log.d(TAG, "Searching notes with keyword: " + keyWord);
        List<Note> noteList = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {NoteDatabaseHelper.COLUMN_ID, NoteDatabaseHelper.COLUMN_TITLE, NoteDatabaseHelper.COLUMN_CONTENT, NoteDatabaseHelper.COLUMN_CREATE_TIME, NoteDatabaseHelper.COLUMN_UPDATE_TIME};

        // 这个是精确的匹配
//        String selection = NoteDatabaseHelper.COLUMN_TITLE + " = ?";
//        String[] selectionArgs = {keyWord};

        // 这个是模糊匹配,包含即可
        String selection = NoteDatabaseHelper.COLUMN_TITLE + " LIKE ?";
        String[] selectionArgs = {"%" + keyWord + "%"};  // 包含关键字

        try (Cursor cursor = db.query(NoteDatabaseHelper.TABLE_NOTES, columns, selection, selectionArgs, null, null, null, null)) {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndex(NoteDatabaseHelper.COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndex(NoteDatabaseHelper.COLUMN_TITLE));
                    String content = cursor.getString(cursor.getColumnIndex(NoteDatabaseHelper.COLUMN_CONTENT));
                    String create_time = cursor.getString(cursor.getColumnIndex(NoteDatabaseHelper.COLUMN_CREATE_TIME));
                    String update_time = cursor.getString(cursor.getColumnIndex(NoteDatabaseHelper.COLUMN_UPDATE_TIME));
                    noteList.add(new Note(id, title, content, create_time, update_time));
                } while (cursor.moveToNext());
            }
        }
        Log.d(TAG, "Search found " + noteList.size() + " notes");
        return noteList;
    }

    // 删除某一条笔记
    public int deleteNote(String id) {
        Log.d(TAG, "Deleting note with id: " + id);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = NoteDatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {id};
        int rowsDeleted = db.delete(NoteDatabaseHelper.TABLE_NOTES, selection, selectionArgs);
        if (rowsDeleted > 0) {
            Log.d(TAG, "Note deleted successfully");
        } else {
            Log.w(TAG, "No note found with id: " + id);
        }
        return rowsDeleted;
    }

    // 修改某一条笔记的标题
    public int modifyNoteTitle(String id, String title) {
        Log.d(TAG, "Modifying note title, id: " + id + ", new title: " + title);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(NoteDatabaseHelper.COLUMN_TITLE, title);
        values.put(NoteDatabaseHelper.COLUMN_UPDATE_TIME, String.valueOf(System.currentTimeMillis()));

        String selection = NoteDatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {id};

        int rowsUpdated = db.update(NoteDatabaseHelper.TABLE_NOTES, values, selection, selectionArgs);
        if (rowsUpdated > 0) {
            Log.d(TAG, "Note title modified successfully");
        } else {
            Log.w(TAG, "No note found with id: " + id);
        }
        return rowsUpdated;
    }

    // 修改某一条笔记的内容
    public int modifyNoteContent(String id, String content) {
        Log.d(TAG, "Modifying note content, id: " + id);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NoteDatabaseHelper.COLUMN_CONTENT, content);
        values.put(NoteDatabaseHelper.COLUMN_UPDATE_TIME, String.valueOf(System.currentTimeMillis())); // 更新一下时间

        String selection = NoteDatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = {id};

        int rowsUpdated = db.update(NoteDatabaseHelper.TABLE_NOTES, values, selection, selectionArgs);
        if (rowsUpdated > 0) {
            Log.d(TAG, "Note content modified successfully");
        } else {
            Log.w(TAG, "No note found with id: " + id);
        }
        return rowsUpdated;
    }
}
