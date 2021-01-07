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

public interface IVXRecents {


	  public void DeleteCallLogs();
	  public ArrayList<RecentsModel> GetCallLogs(Context context, String contacts_type);
	  public void UpdateCallLogs();
	  
}
