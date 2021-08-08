package com.example.fallguardian;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Elderly.db";
    public static final String TABLE_NAME = "Fall_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "TIME";
    public static final String COL_3 = "FN";
    public static final String COL_4 = "FP";
    public static final String COL_5 = "TP";
    ///  0 -> false negative
    ///  1 -> false positive
    ///  2 -> true positive

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
        //SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table "+TABLE_NAME+" (ID INTEGER PRIMARY KEY AUTOINCREMENT,TIME LONG,FN INTEGER,FP INTEGER,TP INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(long time, int fn, int fp, int tp){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,time);
        contentValues.put(COL_3,fn);
        contentValues.put(COL_4,fp);
        contentValues.put(COL_5,tp);
        long result = db.insert(TABLE_NAME, null, contentValues);

        if(result==-1){
            return false;
        }
        return true;
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        return  res;
    }

    public Cursor getLastData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_NAME,null);
        res.moveToLast();
        return  res;
    }
    public boolean updateData(int id, long time, int fn, int fp, int tp){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL_1,id);
        contentValues.put(COL_2,time);
        contentValues.put(COL_3,fn);
        contentValues.put(COL_4,fp);
        contentValues.put(COL_5,tp);

        db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{ String.valueOf(id) });
        return true;
    }
}

