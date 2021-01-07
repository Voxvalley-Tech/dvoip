/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.recents;

import java.util.ArrayList;

import android.content.Context;


import com.vx.core.android.db.CallLogsDB;

public class VXRecentsAll implements IVXRecents {

	static IVXRecents vxrecentsobjext = new VXRecentsAll();

	private VXRecentsAll() {

	}

	@Override
	public void DeleteCallLogs() {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<RecentsModel> GetCallLogs(Context context,String contacts_type) {

		ArrayList<RecentsModel> recentsarray = new ArrayList<RecentsModel>();
		
		CallLogsDB callLogs_db = new CallLogsDB(context);
		
		callLogs_db.open();
		
		 
		if (contacts_type.equalsIgnoreCase("All")) {
						
			recentsarray = callLogs_db.getAllcallbygroup();				 
		
		} else if (contacts_type.equalsIgnoreCase("Missed")) {

			recentsarray = callLogs_db.getAllcallbygroup_missed();	
		}
		
		callLogs_db.close();
		return recentsarray;

	}

	@Override
	public void UpdateCallLogs() {
		// TODO Auto-generated method stub

	}

	public static IVXRecents getinstance() {

		return vxrecentsobjext;
	}
}
