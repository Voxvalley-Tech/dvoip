/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.contacts;

import android.graphics.Bitmap;

public class ContactsModel {

	private String ContactID;
	private String ContactName;
	private String ContactNumber;
	private Bitmap ContactPicture;

	
	
	public Bitmap getContactPicture() {
		return ContactPicture;
	}
	public void setContactPicture(Bitmap contactPicture) {
		ContactPicture = contactPicture;
	}
	public String getContactID() {
		return ContactID;
	}
	public void setContactID(String contactID) {
		ContactID = contactID;
	}
	public String getContactName() {
		return ContactName;
	}
	public void setContactName(String contactName) {
		ContactName = contactName;
	}
	public String getContactNumber() {
		return ContactNumber;
	}
	public void setContactNumber(String contactNumber) {
		ContactNumber = contactNumber;
	}

}
