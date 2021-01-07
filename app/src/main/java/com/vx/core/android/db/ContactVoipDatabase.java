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

import com.vx.core.android.contacts.ContactsModel;

public class ContactVoipDatabase {
    // the Activity or Application that is creating an object from this class.
    Context context;

    // a reference to the database used by this application/object
    private SQLiteDatabase db;

    // These constants are specific to the database.  They should be
    // changed to suit your needs.
    private final String DB_NAME = "database_contacts";
    private final int DB_VERSION = 4;

    // These constants are specific to the database table.  They should be
    // changed to suit your needs.
    private final String TABLE_NAME = "database_table_voip";
    private final String TABLE_NAME2 = "database_table_contact";
    private final String TABLE_ROW_ONE = "contactnumber";
    private final String TABLE_ROW_TWO = "contactname";
    private final String TABLE_ROW_THREE = "contactid";


    public ContactVoipDatabase(Context context) {
        this.context = context;
        // create or open the database
        CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
        this.db = helper.getWritableDatabase();

    }

    public void close() {
        if (this.db != null)
            this.db.close();
    }

    /**********************************************************************
     * ADDING A ROW TO THE DATABASE TABLE
     *
     * This is an example of how to add a row to a database table
     * using this class.  You should edit this method to suit your
     * needs.
     *
     * the key is automatically assigned by the database
     * @param phone the value for the row's first column
     */
    public void addRow(String phone, String name, String id) {
        // this is a key value pair holder used by android's SQLite functions
        ContentValues values = new ContentValues();


        values.put(TABLE_ROW_ONE, phone);
        values.put(TABLE_ROW_TWO, name);
        //	values.put(TABLE_ROW_THREE, id);

        // ask the database object to insert the new data
        try {
            db.insertOrThrow(TABLE_NAME, null, values);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public void addNativePhnum(String phone) {
        // this is a key value pair holder used by android's SQLite functions
        ContentValues values = new ContentValues();


        values.put(TABLE_ROW_ONE, phone);


        // ask the database object to insert the new data
        try {
            db.insertOrThrow(TABLE_NAME2, null, values);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    /**********************************************************************
     * DELETING A ROW FROM THE DATABASE TABLE
     *
     * This is an example of how to delete a row from a database table
     * using this class. In most cases, this method probably does
     * not need to be rewritten.
     *
     * @param rowID the SQLite database identifier for the row to delete.
     */
    public void deleteRows(String rowID) {
        try {
            db.delete(TABLE_NAME, TABLE_ROW_ONE + " = '" + rowID + "'", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAllRows() {
        // ask the databasleadIdValuee manager to delete the row of given id
        try {
            db.delete(TABLE_NAME, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ArrayList<ArrayList<Object>> getAllRowsAsArrays() {
        ArrayList<ArrayList<Object>> dataArrays = new ArrayList<ArrayList<Object>>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM database_table_voip order by contactname", null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                do {
                    ArrayList<Object> dataList = new ArrayList<Object>();

                    dataList.add(cursor.getString(0));
                    dataList.add(cursor.getString(1));
                    dataArrays.add(dataList);
                }
                while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (cursor != null)
            cursor.close();

        return dataArrays;
    }

    public String getContactNum(String num) {
        String some = null;
        Cursor cursor = null;
        try {
            String sss = "SELECT * FROM database_table_voip where contactnumber LIKE '" + num + "'";
            cursor = db.rawQuery(sss, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // your content
                    some = cursor.getString(0);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (cursor != null)
            cursor.close();

        return some;
    }

    public ArrayList<String> getDBPhoneRecords() {
        ArrayList<String> dataArrays = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + TABLE_ROW_ONE + " FROM " + TABLE_NAME, null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                do {
                    dataArrays.add(cursor.getString(0));
                }
                while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (cursor != null) {
            cursor.close();
        }

        return dataArrays;
    }


    public ArrayList<ContactsModel> getCCSContacs() {
        ArrayList<ContactsModel> dataArrays = new ArrayList<ContactsModel>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                do {
                    ContactsModel cmodel = new ContactsModel();

                    cmodel.setContactName(cursor.getString(1));
                    cmodel.setContactID(null);
                    cmodel.setContactNumber(cursor.getString(0));
                    cmodel.setContactPicture(null);

                    dataArrays.add(cmodel);

                }
                while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (cursor != null)
            cursor.close();

        return dataArrays;
    }


    public ArrayList<String> getNativeDBPhoneRecords() {
        ArrayList<String> dataArrays = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + TABLE_ROW_ONE + " FROM " + TABLE_NAME2, null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                do {

                    dataArrays.add(cursor.getString(0));
                }
                while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (cursor != null) {
            cursor.close();
        }

        return dataArrays;
    }


    public ArrayList<String> getDBPhoneNameRecords() {
        ArrayList<String> dataArrays = new ArrayList<String>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT " + TABLE_ROW_TWO + " FROM database_table_voip", null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                do {

                    dataArrays.add(cursor.getString(0));
                }
                while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (cursor != null) {
            cursor.close();
        }

        return dataArrays;
    }


    public int getTotalCount() {
    /*Cursor cursor = db.rawQuery("select count(*) database_table_voip",null);
    if(cursor!=null){
		cursor.moveToFirst();
		return cursor.getCount();
	}*/
        return 0;
    }


    private class CustomSQLiteOpenHelper extends SQLiteOpenHelper {
        public CustomSQLiteOpenHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            String newTableQueryString = "CREATE TABLE " + TABLE_NAME + " ("
                    + TABLE_ROW_ONE + " text unique," + TABLE_ROW_TWO + " text"
                    +
					/* TABLE_ROW_THREE + " text " + */
                    ");";

            db.execSQL(newTableQueryString);


            String newTableQueryString2 = "CREATE TABLE " + TABLE_NAME2 + " ("
                    + TABLE_ROW_ONE + " text unique" +

                    ");";

            db.execSQL(newTableQueryString2);

        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // NOTHING TO DO HERE. THIS IS THE ORIGINAL DATABASE VERSION.
            // OTHERWISE, YOU WOULD SPECIFIY HOW TO UPGRADE THE DATABASE.


            String newTableQueryString2 = "CREATE TABLE " +
                    TABLE_NAME2 +
                    " (" +
                    TABLE_ROW_ONE + " text unique" +

                    ");";
// execute the query string to the database   unique.
//			Log.i("ContactVOIPDatabase","table2 created");
            db.execSQL(newTableQueryString2);

        }
    }


    public boolean getDBPhoneRecordfound(String uname) {
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT * FROM database_table_voip WHERE contactnumber =" + "'" + uname + "'" + "order by contactname", null);


            if (cursor != null && cursor.getCount() > 0) {
                return true;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (cursor != null) {
            cursor.close();
        }

        return false;

    }

    public boolean getDBPhonenamefound(String uname) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM database_table_voip WHERE contactname =" + "'" + uname + "'", null);


            if (cursor != null && cursor.getCount() > 0) {
                return true;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (cursor != null) {
            cursor.close();
        }

        return false;

    }


    public void updateRow(String extension, String newname) {

        ContentValues values = new ContentValues();

        values.put(TABLE_ROW_ONE, extension);
        values.put(TABLE_ROW_TWO, newname);

        try {

            db.update(TABLE_NAME, values, TABLE_ROW_TWO + "=" + newname, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
