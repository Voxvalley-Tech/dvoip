package com.vx.core.android.contacts;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;

import com.vx.ui.Home;
import com.vx.utils.PermissionUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by sivaprasad on 2/19/2018.
 */

public class ContactMethodHelper {

    private static String TAG = "ContactMethodHelper";
    static ArrayList<ContactsModel> contactListForCallLogs = new ArrayList<>();

    public static ArrayList<ContactsModel> getAllContactsWithPhoneNumber(Context context) {



        ArrayList<ContactsModel> allContacts = new ArrayList<>();

        // If OS version is more than Lollipop we ask all below permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionUtils.hasPermissions(context, PermissionUtils.CONTACTSPERMISSION)) {
                Home.isContactsLoaded = false;
                return allContacts;
            }
        }
        Home.isContactsLoaded = true;
        try {

            Log.d(TAG, "getAllContactsWithPhoneNumber: called");
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] projection = new String[]{
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER};

            String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
                    + " COLLATE LOCALIZED ASC";
            Cursor people = context.getContentResolver().query(uri, projection,
                    null, null, sortOrder);

            int indexID = people
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
            int indexName = people
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int indexNumber = people
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            while (people.moveToNext()) {
                String id = people.getString(indexID);
                String name = people.getString(indexName);
                String number = people.getString(indexNumber);

                ContactsModel contactData = new ContactsModel();
                contactData.setContactID(id);
                contactData.setContactName(name);
                //Log.i("ContactsHelper","id: "+id+" , Name: "+name+", Number: "+number);

                String filteredNumber = validateMobileNumber(number);
                if (filteredNumber != null && filteredNumber.length() > 0) {
                    contactData.setContactNumber(filteredNumber);
                    allContacts.add(contactData);
                }
            }

            Log.i(TAG, "Total phone contacts size: " + allContacts.size());
            contactListForCallLogs = new ArrayList<>(allContacts);

        } catch (Throwable e) {
            e.printStackTrace();
            return allContacts;
        }


        return allContacts;

    }

    /**
     * This method validates the given phone number.
     *
     * @param number phone number
     * @return filtered number
     */
    public static String validateMobileNumber(String number) {

        number = number.replace(" ", "").replace("\u00A0", "").replace("+", "").replace("#",
                "").replace("$", "").replace("#", "").replace("*", "").replace("(", "").replace(")", "").replace("-", "").replace("/", "");
        int len = number.length();

        for (int i = 0; i < len; ++i) {
            if (!(number.charAt(i) == '#' || number.charAt(i) == '*' || (number.charAt(i) >= '0' && number.charAt(i) <= '9'))) {
                Log.i(TAG, "Invalid Number: " + number);
                number = "";
                break;
            }
        }

        return number;
    }

    /**
     * This method gets all contact numbers on given contactID
     *
     * @param context   application context
     * @param contactID contact id
     */
    public static ArrayList<ContactsDetailsModel> getMultipleNumbers(Context context, String contactID) {

        ArrayList<ContactsDetailsModel> allNumbers = new ArrayList<>();
        try {

            // If OS version is more than Lollipop we ask all below permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!PermissionUtils.hasPermissions(context, PermissionUtils.CONTACTSPERMISSION)) {
                    return allNumbers;
                }
            }

            Cursor phone = context.getApplicationContext().getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{contactID}, null);

            if (phone != null) {

                while (phone.moveToNext()) {
                    String phoneNo = phone
                            .getString(phone
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    int type = phone
                            .getInt(phone
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                    ContactsDetailsModel contactDetails = new ContactsDetailsModel();
                    contactDetails.setContactType(type + "");

                    String filteredNumber = validateMobileNumber(phoneNo);
                    if (filteredNumber != null && filteredNumber.length() > 0) {
                        contactDetails.setContactNumber(filteredNumber);
                        allNumbers.add(contactDetails);
                    }
                }
            }

            Set set = new TreeSet(new Comparator<ContactsDetailsModel>() {
                @Override
                public int compare(ContactsDetailsModel o1, ContactsDetailsModel o2) {

                    if (o1.getContactNumber() != null && o2.getContactNumber() != null && o1.getContactNumber().equals(o2.getContactNumber())) {
                        return 0;
                    }
                    return 1;
                }
            });

            // Filtering duplicates
            set.addAll(allNumbers);
            allNumbers = new ArrayList<>(set);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return allNumbers;
    }

    /**
     * This method gets contact name based on phone number.
     *
     * @param phoneNumber given number
     * @param context     application context
     * @return contact name
     */
    public static String getContactName(final String phoneNumber, Context context) {

        String contactName = "";

        // If OS version is more than Lollipop we ask all below permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionUtils.hasPermissions(context, PermissionUtils.CONTACTSPERMISSION)) {
                return contactName;
            }
        }


        try {
            if (contactName == null || contactName.length() == 0) {
                Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

                String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
                Log.i(TAG, "Cursor " + cursor + " Context " + context);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        contactName = cursor.getString(0);
                    }
                    cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "contactName " + contactName);
        if (contactName.equals("")) {
            return phoneNumber;
        }
        return contactName;
    }

    /**
     * This method will give the contact name for given contact number from contactListForCallLogs
     * if contact name not exist it will give passed phone number as contact name
     *
     * @param phoneNumber
     * @param context
     * @return
     */
    public static String getContactNameForCallLogs(final String phoneNumber, Context context) {

        if (contactListForCallLogs == null || contactListForCallLogs.size() == 0) {
            getAllContactsWithPhoneNumber(context);
        }
        if (contactListForCallLogs != null && contactListForCallLogs.size() > 0) {
            ContactsModel contactsModel = null;
            for (int i = 0; i < contactListForCallLogs.size(); i++) {
                contactsModel = contactListForCallLogs.get(i);
                if (contactsModel != null && contactsModel.getContactNumber().equals(phoneNumber)) {
                    return contactsModel.getContactName();
                }
            }
        }
        return phoneNumber;
    }

    /**
     * This method will give the multiple numbers for single contact ID from contactListForCallLogs
     *
     * @param contactID
     * @return
     */
    public static ArrayList<ContactsDetailsModel> getMultipleNumbers(String contactID) {
        ArrayList<ContactsDetailsModel> multipleNumbers = new ArrayList<>();

        try {
            for (int i = 0; i < contactListForCallLogs.size(); i++) {
                ContactsModel contactsModel = contactListForCallLogs.get(i);
                if (contactsModel.getContactID().equals(contactID)) {
                    ContactsDetailsModel contactDetails = new ContactsDetailsModel();
                    contactDetails.setContactNumber(contactsModel.getContactNumber());
                    contactDetails.setContactID(contactID);
                    contactDetails.setContactType("2");
                    multipleNumbers.add(contactDetails);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return multipleNumbers;
    }

    /**
     * This method will check if given number is exists in the contacts
     *
     * @param context
     * @param number
     * @return
     */
    public static boolean contactExists(Context context, String number) {
        try {

            // If OS version is more than Lollipop we ask all below permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!PermissionUtils.hasPermissions(context, PermissionUtils.CONTACTSPERMISSION)) {
                    return false;
                }
            }

            // / number is the phone number
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                    Uri.encode(number));
            String[] mPhoneNumberProjection = {ContactsContract.PhoneLookup._ID,
                    ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME};
            Cursor cur = context.getContentResolver().query(lookupUri,
                    mPhoneNumberProjection, null, null, null);
            try {
                if (cur != null && cur.moveToFirst()) {
                    return true;
                }
            } finally {
                if (cur != null)
                    cur.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This method will give the contact Bitmap for given number
     *
     * @param context
     * @param phoneNumber
     * @return
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static Bitmap getContactImage(Context context, String phoneNumber) {

        // If OS version is more than Lollipop we ask all below permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionUtils.hasPermissions(context, PermissionUtils.CONTACTSPERMISSION)) {
                return null;
            }
        }

        Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Uri photoUri = null;
        ContentResolver cr = context.getContentResolver();
        try {
            Cursor contact = cr.query(phoneUri,
                    new String[]{ContactsContract.Contacts._ID}, null, null,
                    null);
            if (contact != null)
                if (contact.moveToFirst()) {
                    long userId = contact.getLong(contact
                            .getColumnIndex(ContactsContract.Contacts._ID));
                    photoUri = ContentUris.withAppendedId(
                            ContactsContract.Contacts.CONTENT_URI, userId);

                } else {
                    // Bitmap defaultPhoto =
                    // BitmapFactory.decodeResource(getResources(),
                    // R.drawable.incall_2_normal);
                    return null;
                }
            if (photoUri != null) {
                // This one is getting crashed in 2.3 version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    InputStream input = ContactsContract.Contacts
                            .openContactPhotoInputStream(cr, photoUri, true);
                    if (input != null) {
                        return BitmapFactory.decodeStream(input);
                    }
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static ArrayList<ContactsModel> getFvouritePhoneNumber(Context context) {



        ArrayList<ContactsModel> allContacts = new ArrayList<>();

        // If OS version is more than Lollipop we ask all below permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionUtils.hasPermissions(context, PermissionUtils.CONTACTSPERMISSION)) {
                Home.isContactsLoaded = false;
                return allContacts;
            }
        }
        Home.isContactsLoaded = true;
        try {

            Log.d(TAG, "getAllContactsWithPhoneNumber: called");
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] projection = new String[]{
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER};
            String selection =ContactsContract.Contacts.STARRED + "='1'";
            String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
                    + " COLLATE LOCALIZED ASC";
            Cursor people = context.getContentResolver().query(uri, projection,
                    selection, null, sortOrder);

            int indexID = people
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
            int indexName = people
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int indexNumber = people
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            while (people.moveToNext()) {
                String id = people.getString(indexID);
                String name = people.getString(indexName);
                String number = people.getString(indexNumber);

                ContactsModel contactData = new ContactsModel();
                contactData.setContactID(id);
                contactData.setContactName(name);
                //Log.i("ContactsHelper","id: "+id+" , Name: "+name+", Number: "+number);

                String filteredNumber = validateMobileNumber(number);
                if (filteredNumber != null && filteredNumber.length() > 0) {
                    contactData.setContactNumber(filteredNumber);
                    allContacts.add(contactData);
                }
            }

            Log.i(TAG, "Total phone contacts size: " + allContacts.size());
            contactListForCallLogs = new ArrayList<>(allContacts);

        } catch (Throwable e) {
            e.printStackTrace();
            return allContacts;
        }


        return allContacts;

    }
}
