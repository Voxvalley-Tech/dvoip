/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

import com.vx.core.android.contacts.ContactMethodHelper;
import com.vx.core.android.recents.RecentsModel;
import com.vx.core.android.utils.MethodHelper;

public class CallLogsDB {
    Context context;

    private SQLiteDatabase db;

    // These constants are specific to the database.  They should be
    // changed to suit your needs.
    private final String DB_NAME = "database_callLogs";
    private final int DB_VERSION = 4;

    public final static String TABLE_NAME = "database_table_callLogs";

    public final static String TABLE_ROW_ID = "id";
    public final static String TABLE_ROW_NUMBER = "table_row_number";
    public final static String TABLE_ROW_TIME = "table_row_time";
    public final static String TABLE_ROW_DURATION = "table_row_duration";
    public final static String TABLE_ROW_TYPE = "table_row_type";
    public final static String TABLE_ROW_USER_ID = "table_row_user_id";
    public final static String TABLE_CONTACT_TYPE = "table_contact_type";


    CustomSQLiteOpenHelper helper;

    public CallLogsDB(Context context) {
        this.context = context;

        // create or open the database
        helper = new CustomSQLiteOpenHelper(context);
//		this.db = helper.getWritableDatabase();
    }

    private boolean opened = false;

    public CallLogsDB open() throws SQLException {
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

    private void removeCallLogExpiredEntries() {
        db.delete(TABLE_NAME, TABLE_ROW_ID + " NOT IN " +
                "(SELECT " + TABLE_ROW_ID + " FROM " + TABLE_NAME + " ORDER BY " + TABLE_ROW_ID + " DESC LIMIT 2)", null);

        //db.rawQuery("SELECT id,table_row_number,MAX(table_row_time),table_row_duration,COUNT(table_row_number),table_row_type,table_row_user_id FROM database_table_callLogs GROUP BY table_row_number ORDER BY id DESC",null);

    }

    private void removeCallLogExpiredEntries2() {
        db.delete(TABLE_NAME, TABLE_ROW_NUMBER + " NOT IN " +
                "(SELECT " + TABLE_ROW_NUMBER + " FROM " + TABLE_NAME + " ORDER BY " + TABLE_ROW_NUMBER + " DESC LIMIT 2)", null);

        //db.rawQuery("SELECT id,table_row_number,MAX(table_row_time),table_row_duration,COUNT(table_row_number),table_row_type,table_row_user_id FROM database_table_callLogs GROUP BY table_row_number ORDER BY id DESC",null);

    }


    //	public void addRow(String rowStringNumber, String rowStringTime, String rowStringDuration, String rowStringType,String rowStringUserName)
    public void addRow(String tableName, ContentValues cv) {
        // this is a key value pair holder used by android's SQLite functions

        try {

            long record = db.insert(TABLE_NAME, null, cv);
            Log.i("CallLogsDB", "addRow count=" + record);
            //removeCallLogExpiredEntries();
            // RecentUpdated();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Get call logs Database count
    public int allRecordsCount() {

        int count = 0;

        try {

            String selectQuery = "SELECT * FROM database_table_callLogs";
            Cursor c = db.rawQuery(selectQuery, null);

            if (c != null && c.getCount() > 0) {

                count = c.getCount();

                c.close();
            } else {

                count = 0;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;

    }

    // Delete missed calls
    public void deleteMissed() {
        // ask the databasleadIdValuee manager to delete the row of given id
        try {
            db.delete(TABLE_NAME, " table_row_type = '3'", null);
            // RecentUpdated();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //delete row
    public void deleteRow(String rowID) {
        // ask the databasleadIdValuee manager to delete the row of given id
        try {
            db.delete(TABLE_NAME, TABLE_ROW_NUMBER + " LIKE '" + rowID + "'", null);

            //RecentUpdated();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //delete missed row
    public void deleteMissedRow(String rowID) {
        // ask the databasleadIdValuee manager to delete the row of given id
        try {
            db.delete(TABLE_NAME, " table_row_type = '3'" + " and table_row_number = '" + rowID + "'", null);
            // RecentUpdated();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAllRows() {
        // ask the databasleadIdValuee manager to delete the row of given id
        try {
            db.delete(TABLE_NAME, null, null);
            //RecentUpdated();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ArrayList<RecentsModel> getAllcallbygroup() {

        ArrayList<RecentsModel> recentsArrays = new ArrayList<RecentsModel>();

        Cursor cursor = null;

        try {
            // ask the database object to create the cursor.

            cursor = db.rawQuery("SELECT id,table_row_number,MAX(table_row_time),table_row_duration,COUNT(table_row_number),table_row_type,table_row_user_id FROM database_table_callLogs GROUP BY table_row_number ORDER BY id DESC", null);

            if (cursor.getCount() > 0) {

                if (cursor.moveToFirst()) {
                    do {

                        RecentsModel cModel = new RecentsModel();

                        String contact_name = "";
                        boolean iscontactfound = false;
                        try {
                            contact_name = ContactMethodHelper.getContactNameForCallLogs(cursor.getString(1), context);
                            // contact_name = MethodHelper.getContactName(cursor.getString(1), context);
                           /* String[] webser_details = contact_name.split("%24%");
                            contact_name = webser_details[0];*/

                            iscontactfound = ContactMethodHelper.contactExists(context, cursor.getString(1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        cModel.setName(contact_name);
                        cModel.setIscontactfound(iscontactfound);

                        String number = cursor.getString(1);
                        cModel.setNumber(number);
                        cModel.setContactfound(false);

                        //long date_l = Long.parseLong(cursor.getString(2));
                        //String date = SipMethodHelper.getDate(date_l, "dd/MM/yyyy hh:mm:ss");
                        cModel.setDate(cursor.getString(2));

                        String duration = cursor.getString(3);
                        //String callduration = SipMethodHelper.formatDuration(context,Long.parseLong(duration));
                        cModel.setDuration(duration);

                        cModel.SetCount(cursor.getString(4));
                        cModel.setCalltype(cursor.getString(5));


                        //TODO to enable this to get profile pic
                        //Bitmap img_bitmap=SipMethodHelper.getImagePhoto(context,number);

                        cModel.setProfilepic(null);

                        recentsArrays.add(cModel);


                    } while (cursor.moveToNext());
                    // move the cursor's pointer up one position.

                }

            }

        } catch (SQLException e) {
            e.printStackTrace();

        } finally {
            if (cursor != null)
                cursor.close();
        }

        // return the ArrayList that holds the data collected from
        // the database.
        return recentsArrays;


    }


    public ArrayList<RecentsModel> getAllcallbygroup_missed() {

        ArrayList<RecentsModel> recentsArrays = new ArrayList<RecentsModel>();

        Cursor cursor = null;

        try {
            // ask the database object to create the cursor.

            cursor = db.rawQuery("SELECT id,table_row_number,MAX(table_row_time),table_row_duration,COUNT(table_row_number),table_row_type,table_row_user_id FROM database_table_callLogs WHERE table_row_type='3' GROUP BY table_row_number ORDER BY id DESC", null);

            if (cursor != null && cursor.getCount() > 0) {

                if (cursor.moveToFirst()) {
                    do {
                        RecentsModel cModel = new RecentsModel();
                        String contact_name = ContactMethodHelper.getContactNameForCallLogs(cursor.getString(1), context);
                        // String contact_name = MethodHelper.getContactName(cursor.getString(1), context);
                        /*String[] webser_details = contact_name.split("%24%");
                        contact_name = webser_details[0];*/
                        cModel.setName(contact_name);

                        String number = cursor.getString(1);
                        cModel.setNumber(number);
                        cModel.setContactfound(false);

                        //long date_l = Long.parseLong(cursor.getString(2));
                        //String date = SipMethodHelper.getDate(date_l, "dd/MM/yyyy hh:mm:ss");
                        cModel.setDate(cursor.getString(2));

                        String duration = cursor.getString(3);
                        //String callduration = SipMethodHelper.formatDuration(context,Long.parseLong(duration));
                        cModel.setDuration(duration);

                        cModel.SetCount(cursor.getString(4));
                        cModel.setCalltype(cursor.getString(5));

                        Bitmap img_bitmap = ContactMethodHelper.getContactImage(context, number);

                        cModel.setProfilepic(img_bitmap);

                        recentsArrays.add(cModel);

                    } while (cursor.moveToNext());
                    // move the cursor's pointer up one position.

                }
            }

			/*if(cursor!=null)
                cursor.close();*/

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }

        // return the ArrayList that holds the data collected from
        // the database.
        return recentsArrays;


    }


    public ArrayList<ArrayList<Object>> getAllRowsAsArraysAllNums(String num) {

        ArrayList<ArrayList<Object>> dataArrays = new ArrayList<ArrayList<Object>>();

        Cursor cursor;

        try {
            cursor = db.rawQuery("SELECT id,table_row_number,table_row_time,table_row_duration,table_row_type FROM database_table_callLogs WHERE TABLE_ROW_NUMBER='" + num + "' ORDER BY id DESC", null);
            //cursor=db.rawQuery("SELECT id,table_row_number,table_row_time,table_row_duration,table_row_type FROM database_table_callLogs ORDER BY id DESC",null);

            cursor.moveToFirst();

            if (!cursor.isAfterLast()) {
                do {
                    ArrayList<Object> dataList = new ArrayList<Object>();

                    dataList.add(cursor.getLong(0));
                    dataList.add(cursor.getString(1));
                    dataList.add(cursor.getString(2));
                    dataList.add(cursor.getString(3));
                    dataList.add(cursor.getString(4));
                    dataArrays.add(dataList);
                }
                // move the cursor's pointer up one position.
                while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // return the ArrayList that holds the data collected from
        // the database.
        return dataArrays;
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
                    TABLE_ROW_TYPE + " text, " +
                    TABLE_ROW_USER_ID + " text, " +
                    TABLE_CONTACT_TYPE + "text" +
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

	/*public void RecentUpdated(){
		
		Intent networkintent = new Intent(Home.packageName+".RecentsUpdate");
    	networkintent.putExtra("Recentupdated", true);        	
    	context.sendBroadcast(networkintent);
		
	}*/


}
