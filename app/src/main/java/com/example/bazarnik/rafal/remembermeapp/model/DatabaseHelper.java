package com.example.bazarnik.rafal.remembermeapp.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.bazarnik.rafal.remembermeapp.model.DatabaseAdapter;

/**
 * Created by User on 2016-01-11.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, DatabaseAdapter.DATABASE_NAME, null, DatabaseAdapter.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DatabaseAdapter.CREATE_SQL_QUERY);
    }

    // on upgrade drops table and creates new
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS "+ DatabaseAdapter.DATABASE_TABLE);
        onCreate(database);
    }
}

