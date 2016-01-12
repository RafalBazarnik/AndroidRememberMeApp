package com.example.bazarnik.rafal.remembermeapp.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.example.bazarnik.rafal.remembermeapp.model.DatabaseHelper;

/**
 * Created by User on 2016-01-11.
 */
public class DatabaseAdapter {

    public static final String DATABASE_NAME = "taskDatabase";
    public static final String DATABASE_TABLE = "tasks";
    public static final int DATABASE_VERSION = 1;

    public static final String KEY_ROWID = "_id";
    public static final String KEY_TASK = "task";
    public static final String Key_DATE = "date";
    //TODO: add task_date, priority, done/status, attachement
    public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_TASK, Key_DATE};
    public static final int COLUMN_ROWID = 0;
    public static final int COLUMN_TASK = 1;
    public static final int COLUMN_DATE = 2;

    public static final String CREATE_SQL_QUERY =
            String.format("CREATE TABLE %s (" +
                    "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%s TEXT NOT NULL, " +
                    "%s TEXT" +
                    ");", DATABASE_TABLE, KEY_ROWID, KEY_TASK, Key_DATE);

    public final Context context;
    public DatabaseHelper databaseHelper;
    public SQLiteDatabase database;

    public DatabaseAdapter(Context cntxt) {
        this.context = cntxt;
        databaseHelper = new DatabaseHelper(context);
    }

    public DatabaseAdapter open() {
        database = databaseHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        databaseHelper.close();
    }

    public long insertRow(String task, String date) {
        ContentValues values = new ContentValues();
        values.put(KEY_TASK, task);
        values.put(Key_DATE, date);
        return database.insert(DATABASE_TABLE, null, values);
    }

    public boolean deleteRow(long rowId) {
        String queryWhere = DatabaseAdapter.KEY_ROWID + "=" + rowId;
        return database.delete(DATABASE_TABLE, queryWhere, null) != 0;
    }

    public void resetId() {
        String resetIdQuery = String.format("UPDATE SQLITE_SEQUENCE SET SEQ=0 WHERE NAME='%s';", DATABASE_TABLE);
        try {
            database.execSQL(resetIdQuery);
        }
        catch (SQLiteException e) {
        }

    }

    public void deleteAll() {
        Cursor crsr = getAllRows();
        long rowId = crsr.getColumnIndexOrThrow(KEY_ROWID);
        if (crsr.moveToFirst()) {
            do {
                deleteRow(crsr.getLong((int) rowId));
            }
            while (crsr.moveToNext());
        }
        resetId();
        crsr.close();
    }

    public Cursor getAllRows() {
        String queryWhere = null;
        Cursor crsr = database.query(true, DATABASE_TABLE, ALL_KEYS, queryWhere, null, null, null, null, null);
        if (crsr != null) {
            crsr.moveToFirst();
        }
        return crsr;
    }

    public Cursor getRow(long rowId) {
        String queryWhere = KEY_ROWID + "=" + rowId;
        Cursor cursor = database.query(true, DATABASE_TABLE, ALL_KEYS, queryWhere,
                null, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public boolean updateRow(long rowId, String task, String date) {
        String queryWhery = KEY_ROWID + "=" + rowId;
        ContentValues values = new ContentValues();
        values.put(KEY_TASK, task);
        values.put(Key_DATE, date);
        return database.update(DATABASE_TABLE, values, queryWhery, null) != 0;
    }

}

