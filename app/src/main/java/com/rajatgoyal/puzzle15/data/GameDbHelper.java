package com.rajatgoyal.puzzle15.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.rajatgoyal.puzzle15.data.GameContract.GameEntry;

/**
 * Created by rajat on 15/9/17.
 */

public class GameDbHelper extends SQLiteOpenHelper {

    public static final String TAG = "GameDbHelper";

    private static final String DATABASE_NAME = "puzzle15_game.db";

    private static final int DATABASE_VERSION = 1;

    public GameDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = " CREATE TABLE " + GameEntry.TABLE_NAME + " ( " +
                GameEntry._ID + " INTEGER PRIMARY KEY, " +
                GameEntry.COLUMN_MOVES + " INTEGER NOT NULL, " +
                GameEntry.COLUMN_TIME + " INTEGER NOT NULL " + ");";
        Log.d(TAG, "onCreate: " + CREATE_TABLE);
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + GameEntry.TABLE_NAME);
        onCreate(db);
    }
}
