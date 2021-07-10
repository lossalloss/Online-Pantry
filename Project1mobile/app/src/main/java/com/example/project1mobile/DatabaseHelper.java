package com.example.project1mobile;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TABLE_NAME = "Products";
    private static final String COL0 = "ID";
    private static final String COL1 = "name";
    private static final String COL2 = "description";
    private static final String COL3 = "type";

    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE "+TABLE_NAME+" ("+COL0+" INTEGER PRIMARY KEY AUTOINCREMENT, "+COL1+" TEXT, "+COL2+" TEXT, "+COL3+" INTEGER) ";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP IF TABLE EXISTS "+TABLE_NAME);
        onCreate(db);
    }
    public boolean addData(String name, String description){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, name);
        contentValues.put(COL2, description);
        contentValues.put(COL3, 0);

        long result = db.insert(TABLE_NAME, null, contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    public void addType(int type, int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME + " SET " + COL3 +
                " = '" + type + "' WHERE " + COL0 + " = '" + id + "'";
        db.execSQL(query);
    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor data = db.rawQuery("SELECT * FROM "+TABLE_NAME, null);
        return data;
    }

    public Cursor getItemID(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL0 + " FROM " + TABLE_NAME +
                " WHERE " + COL1 + " = '" + name + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    public void updateName(String newName, int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME + " SET " + COL1 +
                " = '" + newName + "' WHERE " + COL0 + " = '" + id + "'";
        db.execSQL(query);
    }

    public void updateDescription(String newDes, int id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME + " SET " + COL2 +
                " = '" + newDes + "' WHERE " + COL0 + " = '" + id + "'";
        db.execSQL(query);
    }

    public void deleteName(int id, String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE "
                + COL0 + " = '" + id + "'" +
                " AND " + COL1 + " = '" + name + "'";
        db.execSQL(query);
    }

    public void clearDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM "+TABLE_NAME;
        db.execSQL(query);
        query = "VACUUM";
        db.execSQL(query);
    }

    public Cursor sortASCbyName(){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] result = new String[]{};
        Cursor c = db.query(TABLE_NAME, result, null, null, null, null, COL1+" ASC");
        return c;
    }

    public Cursor sortDESCbyName(){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] result = new String[]{};
        Cursor c = db.query(TABLE_NAME, result, null, null, null, null, COL1+" DESC");
        return c;
    }

    public Cursor sortTime(){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] result = new String[]{};
        Cursor c = db.query(TABLE_NAME, result, null, null, null, null, COL0+" DESC");
        return c;
    }
}
