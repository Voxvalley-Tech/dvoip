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
import android.database.Cursor;
import android.provider.ContactsContract;

public class VXPhoneContacts implements IVXContacts {

	static IVXContacts vxcontactsobjext = new VXPhoneContacts();

	private static String[] subContacts;
	private static String[] subLookup;
	
	private VXPhoneContacts() {

	}

	@Override
	public void AddContact() {
		// TODO Auto-generated method stub

	}

	@Override
	public void DeleteContact() {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<ContactsModel> GetContact(Context context,
			String contacts_type) {

		// TODO Auto-generated method stub

		ArrayList<ContactsModel> contactslist = new ArrayList<ContactsModel>();

		if (contacts_type.equalsIgnoreCase("Native")) {

			try {

				String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
						+ " COLLATE LOCALIZED ASC";

				Cursor cursor = context.getContentResolver().query(
						ContactsContract.Contacts.CONTENT_URI, null, null,
						null, sortOrder);

				if (cursor != null) {

					while (cursor.moveToNext()) {

						String contactId = cursor.getString(cursor
								.getColumnIndex(ContactsContract.Contacts._ID));

						String contactName = cursor
								.getString(cursor
										.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

/*
						String imageUri = cursor
								.getString(cursor
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI));*/
						String hasPhone = cursor
								.getString(cursor
										.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

						/*
						Bitmap contact_bitmap = null;

						if (imageUri != null) {
							try {

								Uri myUri = Uri.parse(imageUri);

								contact_bitmap = BitmapFactory
										.decodeStream(context
												.getContentResolver()
												.openInputStream(myUri));

							} catch (Exception e) {
								e.printStackTrace();

								contact_bitmap = null;
							}
						}
*/
						
						//String phoneNo = GetMultipleNumbers(context,contactId) ;
										
						
						if (Integer.parseInt(hasPhone) > 0) {

							if (contactName != null) {

								ContactsModel cmodel = new ContactsModel();

								cmodel.setContactID("" + contactId);
								cmodel.setContactName("" + contactName);
								cmodel.setContactNumber("");
								cmodel.setContactPicture(null);
								contactslist.add(cmodel);

							}
						}
					}
				}
				if (cursor != null) {
					cursor.close();
					cursor = null;
				}

			} catch (Exception e) {

			}

		}

		return contactslist;

	}

	@Override
	public void UpdateContact() {
		// TODO Auto-generated method stub

	}

	public static IVXContacts getinstance() {

		return vxcontactsobjext;
	}

	@Override
	public ArrayList<HashMap<String, String>> GetContactc5(Context context,
			String contacts_type) {
		// TODO Auto-generated method stub
		return null;
	}

	
	private String GetMultipleNumbers(Context context,String contactID) {
		// TODO Auto-generated method stub

		String phoneNo = "";
		Cursor phone = context.getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
				new String[] { contactID }, null);

		int i = 0;

		if (phone != null && phone.getCount() > 1) {
			subContacts = new String[phone.getCount()];
			subLookup = new String[phone.getCount()];
		}

		if (phone != null) {

			while (phone.moveToNext()) {

				 phoneNo = phone
						.getString(phone
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

				
			}
		}
		
		if(phone!=null){
			
			phone.close();
		}
		return phoneNo;
	}
	
}
