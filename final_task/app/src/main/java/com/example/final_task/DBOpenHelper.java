package com.example.final_task;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {
    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {super(context, "My_memo.db", null, 1); }
    @Override
    //数据库第一次创建时被调用
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE memo_1(id INTEGER PRIMARY KEY AUTOINCREMENT ," +
                "title VARCHAR(40),content VARCHAR(255),modified_date TEXT, memoType VARCHAR(255)," +
                "alarm_date VARCHAR(255) )");
        db.execSQL("CREATE TABLE category(id INTEGER PRIMARY KEY AUTOINCREMENT ," +
                "memoType VARCHAR(255),color VARCHAR(255))");
        db.execSQL("INSERT INTO category(memoType,color) values(?,?)",
                new String[]{"未分类", "#2E3135"});
        db.execSQL("INSERT INTO category(memoType,color) values(?,?)",
                new String[]{"工作", "#DFDFE0"});
        db.execSQL("INSERT INTO category(memoType,color) values(?,?)",
                new String[]{"旅游", "#FFD700"});
        db.execSQL("INSERT INTO category(memoType,color) values(?,?)",
                new String[]{"学习", "#B0E0E6"});
    }
    //软件版本号发生改变时调用
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS memo_1");
    }
}
