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
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class OPXMLDBDAO {


	protected SQLiteDatabase database;
    private DataBaseHelper dbHelper;
    private Context mContext;

    public OPXMLDBDAO(Context context) {
        this.mContext = context;
        dbHelper = DataBaseHelper.getHelper(mContext);
        open();

    }

    public void open() throws SQLException {
        if(dbHelper == null)
            dbHelper = DataBaseHelper.getHelper(mContext);
        database = dbHelper.getWritableDatabase();

    }
    public void close(){

    	if(database!=null){
    		database.close();
    	}
    }

}
