/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class RecordingsDB {
    Context context;

    private SQLiteDatabase db;

    // These constants are specific to the database.  They should be
    // changed to suit your needs.
    private final String DB_NAME = "database_recordings";
    private final int DB_VERSION = 1;

    public final static String TABLE_NAME = "database_table_recordings";

    public final static String TABLE_ROW_ID = "id";
    public final static String TABLE_ROW_NUMBER = "table_row_number";
    public final static String TABLE_ROW_TIME = "table_row_time";
    public final static String TABLE_ROW_DURATION = "table_row_duration";
    public final static String TABLE_ROW_PATH = "table_row_path";

    CustomSQLiteOpenHelper helper;

    public RecordingsDB(Context context) {
        this.context = context;

        // create or open the database
        helper = new CustomSQLiteOpenHelper(context);
//		this.db = helper.getWritableDatabase();
    }

    private boolean opened = false;

    public RecordingsDB open() throws SQLException {
        db = helper.getWritableDatabase();
        opened = true;
        return this;
    }

    public void close() {
        helper.close();
        opened = false;
    }

    public boolean isOpen() {
        return opened;
    }


    //	public void addRow(String rowStringNumber, String rowStringTime, String rowStringDuration, String rowStringType,String rowStringUserName)
    public void addRow(ContentValues cv) {
        // this is a key value pair holder used by android's SQLite functions

        try {

            long record = db.insert(TABLE_NAME, null, cv);
            System.out.println("addrow count=" + record);
            //removeCallLogExpiredEntries();
            // RecentUpdated();

        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    //delete row
    public void deleteRow(String rowID) {
        // ask the databasleadIdValuee manager to delete the row of given id
        try {
            db.delete(TABLE_NAME, TABLE_ROW_ID + " LIKE '" + rowID + "'", null);

            //RecentUpdated();
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    public void deleteNoFileRecords() {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT table_row_path FROM database_table_recordings", null);
            while (cursor.moveToNext()) {
                File f = new File(cursor.getString(0));
                if (!f.exists()) {
                    db.delete(TABLE_NAME, TABLE_ROW_PATH + " = '" + cursor.getString(0) + "'", null);
                }
            }
        } catch (SQLException e) {
            Log.e("DB Error", e.toString());
            e.printStackTrace();
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public void deleteAllRows() {
        // ask the databasleadIdValuee manager to delete the row of given id
        try {
            db.delete(TABLE_NAME, null, null);
            //RecentUpdated();
        } catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    public ArrayList<RecordingBean> getAllRecordings() {
        ArrayList<RecordingBean> recentsArrays = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT id,table_row_number,table_row_time,table_row_duration,table_row_path FROM database_table_recordings ORDER BY table_row_time DESC LIMIT 30", null);

            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    do {
                        RecordingBean bean = new RecordingBean();
                        bean.setId(cursor.getString(0));
                        bean.setNumber(cursor.getString(1));
                        bean.setTime(cursor.getString(2));
                        bean.setDuration(cursor.getString(3));
                        bean.setPath(cursor.getString(4));
                        recentsArrays.add(bean);

                    } while (cursor.moveToNext());
                    // move the cursor's pointer up one position.
                }
            }
        } catch (SQLException e) {
            Log.e("DB Error", e.toString());
            e.printStackTrace();

        } finally {
            if (cursor != null)
                cursor.close();
        }
        return recentsArrays;
    }

    public String getRecordingPathByID(String id) {
        String path = "";
        Cursor cursor = db.rawQuery("SELECT table_row_path FROM database_table_recordings WHERE id=?", new String[]{id});
        if (cursor != null && cursor.moveToNext()) {
            path = cursor.getString(0);
            cursor.close();
        }
        return path;
    }

    private class CustomSQLiteOpenHelper extends SQLiteOpenHelper {
        public CustomSQLiteOpenHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // This string is used to create the database.  It should
            // be changed to suit your needs.
            String newTableQueryString = "create table IF NOT EXISTS " +
                    TABLE_NAME +
                    " (" +
                    TABLE_ROW_ID + " integer primary key autoincrement not null," +
                    TABLE_ROW_NUMBER + " text," +
                    TABLE_ROW_TIME + " text," +
                    TABLE_ROW_DURATION + " text, " +
                    TABLE_ROW_PATH + " text " +
                    ");";
            // execute the query string to the database   unique.
            db.execSQL(newTableQueryString);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // NOTHING TO DO HERE. THIS IS THE ORIGINAL DATABASE VERSION.
            // OTHERWISE, YOU WOULD SPECIFIY HOW TO UPGRADE THE DATABASE.
            if (oldVersion <= 2) {
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

            }
            onCreate(db);
        }
    }
}
