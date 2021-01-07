
/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.vx.utils.PreferenceProvider;

public class DBAdapter {

	private final Context context;
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase db;
	public DBAdapter(Context aContext) {
		context = aContext;
		databaseHelper = new DatabaseHelper(context);
	}
	
	public static class DatabaseHelper extends SQLiteOpenHelper {
		
		private static final int DATABASE_VERSION = 36;

		
		
		
		DatabaseHelper(Context context) {
			super(context, "com.bestarabia.app.db", null, DATABASE_VERSION);
		}
		
		
		@Override
		public void onCreate(SQLiteDatabase db) {
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			
		}
	}

	private boolean opened = false;
	/**
	 * Open database
	 * 
	 * @return database adapter
	 * @throws SQLException
	 */
	public DBAdapter open() throws SQLException {		
		db = databaseHelper.getWritableDatabase();
		opened = true;
		return this;
	}
	/**
	 * Close database
	 */
	public void close() {
		databaseHelper.close();
		opened = false;
	}
	
	public boolean isOpen() {
		return opened;
	}	
	
	public void saveLoginInfo(PreferenceProvider prefs){
		try{
			Log.i("DBAdapter","getting display_name,username,pwd");
		Cursor cursor;
		cursor=db.rawQuery("SELECT display_name , username,  data  from accounts", null);
		cursor.moveToFirst();
		if (!cursor.isAfterLast())
		{
			do
			{
				String displayname =cursor.getString(0);
				prefs.setPrefString("login_brandpin", "" + displayname);
				String username= cursor.getString(1);
				prefs.setPrefString("login_username", "" + username);
				String password=cursor.getString(2);
				prefs.setPrefString("login_password", "" + password);
				String acc_id=cursor.getString(3);
				String callid=acc_id.substring(0,acc_id.indexOf("<") );
				if(callid!=null&&callid.length()>0){
				prefs.setPrefString("login_phone", "" + callid);
				}
				
			}while (cursor.moveToNext());
		}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void dropTables(){
		db.execSQL("DROP TABLE IF EXISTS  accounts");
	}
	

}
