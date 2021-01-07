/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.recents;

import android.graphics.Bitmap;

public class RecentsModel implements Comparable<RecentsModel> {

	private String name;
	private String number;
	private String date;
	private String duration;
	private String calltype;
	private Bitmap profilepic;
	private String count;
	private boolean iscontactfound;
	
	public boolean isIscontactfound() {
		return iscontactfound;
	}
	public void setIscontactfound(boolean iscontactfound) {
		this.iscontactfound = iscontactfound;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public String getCount() {
		return count;
	}
	public void SetCount(String count) {
		this.count = count;
	}
	
	private boolean isContactfound;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getCalltype() {
		return calltype;
	}
	public void setCalltype(String calltype) {
		this.calltype = calltype;
	}
	public Bitmap getProfilepic() {
		return profilepic;
	}
	public void setProfilepic(Bitmap profilepic) {
		this.profilepic = profilepic;
	}
	public boolean isContactfound() {
		return isContactfound;
	}
	public void setContactfound(boolean isContactfound) {
		this.isContactfound = isContactfound;
	}

	@Override
	public int compareTo(RecentsModel o) {
		if (getDate() == null || o.getDate() == null)
			return 0;
		return getDate().compareTo(o.getDate());
	}
	
}
