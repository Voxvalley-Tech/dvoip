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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "opxmldb";
	private static final int DATABASE_VERSION = 2;

	public static final String PROVISION_TABLE = "opxml2";

	public static final String PROVISION_BASE_TABLE = "opxml1";

	public static final String ID_COLUMN = "id";

	public static final String Registrar_IP = "Registrar_IP";
	public static final String Header = "Header";
	public static final String Vpn = "Vpn";
	public static final String Status = "Status";
	public static final String Send_Log = "Send_Log";
	public static final String Balance = "Balance";
	public static final String Helper_IP = "Helper_IP";
	public static final String SmsAPI = "SmsAPI";
	public static final String Sms_Status = "Sms_Status";
	public static final String Im = "Im";
	public static final String Threeway = "threeway";
	public static final String Presence_Status = "Presence_Status";
	public static final String Compact_Status = "Compact_Status";
	public static final String Expiry_Date = "Expiry_Date";
	public static final String P1 = "P1";
	public static final String P2 = "P2";

	public static final String Ip = "Ip";
	public static final String Key = "Key";
	public static final String Size = "Size";
	public static final String Oldkey = "Oldkey";
	public static final String Newkey = "Newkey";
	public static final String Prefix = "Prefix";
	public static final String Enpref = "Enpref";
	public static final String Encryption_Type = "Encryption_Type";
	public static final String Modern_Key = "Modern_Key";
	public static final String Modern_Level = "Modern_Level";
	public static final String Modern_Algo = "Modern_Algo";
	public static final String Modern_Matrix = "Modern_Matrix";
	public static final String Modern_Size = "Modern_Size";
	public static final String Modern_Prefix = "Modern_Prefix";
	public static final String Re_Reg = "Re_Reg";
	public static final String Sprt = "Sprt";
	public static final String Rtp_Port = "Rtp_Port";
	public static final String Keep = "Keep";
	public static final String Fip = "Fip";
	public static final String Fports = "Fports";
	public static final String Bal_udp_ip = "Bal_udp_ip";
	public static final String Webcdr = "webcdr_url";

	public static final String successCount = "successCount";
	public static final String FailCount = "FailCount";


	public static final String CREATE_BASE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ PROVISION_BASE_TABLE + "(" + ID_COLUMN + " INTEGER PRIMARY KEY, "
			+ Registrar_IP + " TEXT, "
			+ Header + " TEXT, "
			+ Vpn + " TEXT, "
			+ Status + " TEXT, "
			+ Send_Log + " TEXT, "
			+ Balance + " TEXT, "
			+ Helper_IP + " TEXT, "
			+ SmsAPI + " TEXT, "
			+ Sms_Status + " TEXT, "
			+ Im + " TEXT, "
			+ Threeway + " TEXT, "
			+ Presence_Status + " TEXT, "
			+ Compact_Status + " TEXT, "
			+ Expiry_Date + " TEXT, "
			+ P1 + " TEXT, "
			+ P2 + " TEXT, "
			+ Ip + " TEXT, "
			+ Key + " TEXT, "
			+ Size + " TEXT, "
			+ Oldkey + " TEXT, "
			+ Newkey + " TEXT, "
			+ Prefix + " TEXT, "
			+ Enpref + " TEXT, "
			+ Encryption_Type + " TEXT, "
			+ Modern_Key + " TEXT, "
			+ Modern_Level + " TEXT, "
			+ Modern_Algo + " TEXT, "
			+ Modern_Matrix + " TEXT, "
			+ Modern_Size + " TEXT, "
			+ Modern_Prefix + " TEXT, "
			+ Re_Reg + " TEXT, "
			+ Sprt + " TEXT, "
			+ Rtp_Port + " TEXT, "
			+ Keep + " TEXT, "
			+ Fip + " TEXT, "
			+ Fports + " TEXT, "
			+ Bal_udp_ip + " TEXT, "
			+ successCount + " TEXT, "
			+ FailCount + " TEXT, "
			+ Webcdr+" TEXT "
			+ ")";


	private static DataBaseHelper instance;


	public static synchronized DataBaseHelper getHelper(Context context) {
		if (instance == null)
			instance = new DataBaseHelper(context);
		return instance;
	}

	private DataBaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {
			// Enable foreign key constraints
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_BASE_TABLE);
		//db.execSQL(CREATE_PROVISION_TABLE);

		Log.i("DataBaseHelper", "Log opxml onCreate called");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i("DataBaseHelper", "Log opxml onUpgrade called , New version: " + newVersion);
		if (newVersion > oldVersion) {
			db.execSQL("ALTER TABLE "+ PROVISION_BASE_TABLE +" ADD "+ Webcdr +" TEXT");
		}
	}
}

