package com.example.bazarnik.rafal.remembermeapp.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.example.bazarnik.rafal.remembermeapp.model.DatabaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * Created by User on 2016-01-11.
 */
public class DatabaseAdapter {

    public static final String DATABASE_NAME = "taskDatabase";
    public static final String DATABASE_TABLE = "tasks";
    public static final int DATABASE_VERSION = 1;

    public static final String KEY_ROWID = "_id";
    public static final String KEY_TASK = "task";
    public static final String KEY_DATE = "date";
    public static final String KEY_STATE = "state";
    public static final String KEY_DEADLINE = "deadline";
    public static final String KEY_PRIORITY = "priority";
    //TODO: add task_date, priority, done/status, attachement
    public static final String[] ALL_KEYS = new String[] {KEY_ROWID, KEY_TASK, KEY_DATE, KEY_STATE, KEY_DEADLINE, KEY_PRIORITY};
    public static final int COLUMN_ROWID = 0;
    public static final int COLUMN_TASK = 1;
    public static final int COLUMN_DATE = 2;
    public static final int COLUMN_STATE = 3;
    public static final int COLUMN_DEADLINE = 4;
    public static final int COLUMN_PRIORITY = 5;

    public static final String CREATE_SQL_QUERY =
            String.format("CREATE TABLE %s (" +
                    "%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "%s TEXT NOT NULL, " +
                    "%s TEXT," +
                    "%s INTEGER NOT NULL DEFAULT 0," +
                    "%s TEXT," +
                    "%s INTEGER DEFAULT 1" +
                    ");", DATABASE_TABLE, KEY_ROWID, KEY_TASK, KEY_DATE, KEY_STATE, KEY_DEADLINE, KEY_PRIORITY);

    public final Context context;
    public DatabaseHelper databaseHelper;
    public SQLiteDatabase database;
    String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

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

    public long insertRow(String task, String date, int done, String deadline, int priority) {
        ContentValues values = new ContentValues();
        values.put(KEY_TASK, task);
        values.put(KEY_DATE, date);
        values.put(KEY_STATE, done);
        values.put(KEY_DEADLINE, deadline);
        values.put(KEY_PRIORITY, priority);
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

    public boolean updateRow(long rowId, String task, String date, int done, String deadline, int priority) {
        String queryWhery = KEY_ROWID + "=" + rowId;
        ContentValues values = new ContentValues();
        values.put(KEY_TASK, task);
        values.put(KEY_DATE, date);
        values.put(KEY_STATE, done);
        values.put(KEY_DEADLINE, deadline);
        values.put(KEY_PRIORITY, priority);
        return database.update(DATABASE_TABLE, values, queryWhery, null) != 0;
    }

    public boolean saveToFile() {
        try {
            String separator = System.getProperty("line.separator");
            String filepath = String.format("/sdcard/tasks_database_dump_%s.txt", currentDateTimeString);
            File myFile = new File(filepath);
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            Cursor cursor = getAllRows();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String line = "";
                    line += Long.toString(cursor.getInt(0)) + ", ";
                    line += cursor.getString(1);
                    line += cursor.getString(2);
                    line += Integer.toString(cursor.getInt(3));
                    line += cursor.getString(4);
                    line += Integer.toString(cursor.getInt(5));
                    myOutWriter.append(line);
                    myOutWriter.append(separator);
                }

            }
            myOutWriter.close();
            fOut.close();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public String[] getRowAsArray(long id) {
        Cursor cursor = getRow(id);
        String[] row;
        Long raw_id = cursor.getLong(cursor.getColumnIndex("_id"));
        String row_id = Long.toString(raw_id);
        String task = cursor.getString(1);
        String date = cursor.getString(2);
        String done = Integer.toString(cursor.getInt(3));
        String deadline = cursor.getString(4);
        String priority = Integer.toString(cursor.getInt(5));

        row = new String[]{row_id, task, date, done, deadline, priority};
        return row;
    }

    public void addTestTasks(){
        for (int counter=0; counter<5; counter++){
            Random generator = new Random();
            int testPriority = generator.nextInt(5) + 1;
            int doneState = 0;
            insertRow(UUID.randomUUID().toString().replace("-", ""), currentDateTimeString, doneState,
                    currentDateTimeString, testPriority);
        }
    }

    public void setTaskRowStatus(long id, int status) {
        String resetIdQuery = String.format("UPDATE %s SET %s=%s WHERE %s='%s';", DATABASE_TABLE, KEY_STATE, status, KEY_ROWID, id);
        try {
            database.execSQL(resetIdQuery);
        }
        catch (SQLiteException e) {
        }
    }

}

