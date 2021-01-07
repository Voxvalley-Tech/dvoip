/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.contacts;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;

public interface IVXContacts {
  
	  public void AddContact();
	  public void DeleteContact();
	  public ArrayList<ContactsModel> GetContact(Context context,String contacts_type);
	  public void UpdateContact();
	 ArrayList<HashMap<String, String>> GetContactc5(Context context,
			String contacts_type);
	
}
