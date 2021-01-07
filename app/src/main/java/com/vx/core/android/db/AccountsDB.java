/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.vx.core.android.getaccounts.ProfileData;
import com.vx.utils.PreferenceProvider;

public class AccountsDB {
    Context context;

    private SQLiteDatabase db;

    // These constants are specific to the database.  They should be
    // changed to suit your needs.
    private final String DB_NAME = "database_accounts";
    private final int DB_VERSION = 3;


    public final static String TABLE_NAME_PROFILE = "database_table_accounts";

    private final String TABLE_ROW_ACCID = "accountid";
    private final String TABLE_ROW_DISPLAYNAME = "DISPLAYNAME";
    private final String TABLE_ROW_SIPDOMAIN = "SIPDOMAIN";
    private final String TABLE_ROW_DOMAINPROXY = "DOMAINPROXY";
    private final String TABLE_ROW_SENDSIPKEEPALIVES = "SENDSIPKEEPALIVES";
    private final String TABLE_ROW_AUDIOCODECSLISTBASIC = "AUDIOCODECSLISTBASIC";
    private final String TABLE_ROW_KEEPALIVEEXP = "KEEPALIVEEXP";
    private final String TABLE_ROW_AUTHNAME = "AUTHNAME";
    private final String TABLE_ROW_CALLERID = "CALLERID";
    private final String TABLE_ROW_USERNAME = "USERNAME";
    private final String TABLE_ROW_PASSWORD = "PASSWORD";
    private final String TABLE_ROW_XMPPDOMAIN = "XMPPDOMAIN";
    private final String TABLE_ROW_XMPPFILETRANSDOMAIN = "XMPPFILETRANSDOMAIN";
    private final String TABLE_ROW_XMPPPROXYPROTOCOL = "XMPPPROXYPROTOCOL";
    private final String TABLE_ROW_XMPPPROXYHOST = "XMPPPROXYHOST";
    private final String TABLE_ROW_XMPPUSERNAME = "XMPPUSERNAME";
    private final String TABLE_ROW_XMPPPASSWORD = "XMPPPASSWORD";
    private final String TABLE_ROW_XMPPPROXYUSERNAME = "XMPPPROXYUSERNAME";
    private final String TABLE_ROW_XMPPPROXYPASSWORD = "XMPPPROXYPASSWORD";
    private final String TABLE_ROW_STUNSERVER = "STUNSERVER";
    private final String TABLE_ROW_VIDEOCODECSLIST = "VIDEOCODECSLIST";
    private final String TABLE_ROW_VMACCESSCODE = "VMACCESSCODE";
    private final String TABLE_ROW_VMACCOUNT = "VMACCOUNT";
    private final String TABLE_ROW_APIURL = "APIURL";
    private final String TABLE_ROW_APIUSER = "APIUSER";
    private final String TABLE_ROW_APIPASSWORD = "APIPASSWORD";
    private final String TABLE_ROW_PUSHURL = "PUSHURL";
    private final String TABLE_ROW_PREMAUDIOCODECSLIST = "PREMAUDIOCODECSLIST";

    CustomSQLiteOpenHelper helper;

    public AccountsDB(Context context) {
        this.context = context;

        // create or open the database
        helper = new CustomSQLiteOpenHelper(context);
//		this.db = helper.getWritableDatabase();
    }

    private boolean opened = false;

    public AccountsDB open() throws SQLException {
        db = helper.getWritableDatabase();
        opened = true;
        return this;
    }

    public void close() {
        helper.close();
        opened = false;
    }

    public boolean isOpen() {
        return opened;
    }
 


 /*// Delete missed calls	
    public void deleteMissed()
	{
		// ask the databasleadIdValuee manager to delete the row of given id
		try 
		{
			db.delete(TABLE_NAME, " table_row_type = '3'", null);
		}
		catch (Exception e)
		{
			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}
	}*/


	/*//delete row
	public void deleteRow(String rowID)
	{
		// ask the databasleadIdValuee manager to delete the row of given id
		try 
		{
			db.delete(TABLE_NAME, TABLE_ROW_NUMBER + " LIKE '"+rowID+"'", null);
		}
		catch (Exception e)
		{
			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}
	}*/

    //delete missed row
	
	
	/*public void deleteAllRows()
	{
		// ask the databasleadIdValuee manager to delete the row of given id
		try {
			db.delete(TABLE_NAME,null, null);
			}
		catch (Exception e)
		{
			Log.e("DB ERROR", e.toString());
			e.printStackTrace();
		}
	}*/
 
 
 
 
/*	public ArrayList<RecentsModel> getAllcallbygroup()
	{

		ArrayList<RecentsModel> recentsArrays = new ArrayList<RecentsModel>();
		
		Cursor cursor = null;
 
		try
		{
			// ask the database object to create the cursor.

			cursor=db.rawQuery("SELECT id,table_row_number,MAX(table_row_time),table_row_duration,COUNT(table_row_number),table_row_type,table_row_user_id FROM database_table_callLogs GROUP BY table_row_number ORDER BY id DESC",null);
		
			if(cursor!=null && cursor.getCount()>0){
						
			if (cursor.moveToFirst())
			{
				do
				{
					
					RecentsModel cModel = new RecentsModel();
					
					String contact_name = SipMethodHelper.getContactName(cursor.getString(1), context);
					String[] webser_details = contact_name.split("%24%");
					contact_name = webser_details[0];					
					cModel.setName(contact_name);
					
					boolean iscontactfound = SipMethodHelper.contactExists(context,cursor.getString(1));
					cModel.setIscontactfound(iscontactfound);
					
					String number = cursor.getString(1);					
					cModel.setNumber(number);
					cModel.setContactfound(false);
					
					//long date_l = Long.parseLong(cursor.getString(2));					
					//String date = SipMethodHelper.getDate(date_l, "dd/MM/yyyy hh:mm:ss");
					cModel.setDate(cursor.getString(2));
					
					String duration = cursor.getString(3);					
					//String callduration = SipMethodHelper.formatDuration(context,Long.parseLong(duration));
					cModel.setDuration(duration); 
					
					cModel.SetCount(cursor.getString(4));					
					cModel.setCalltype(cursor.getString(5));
					
					Bitmap img_bitmap=SipMethodHelper.getImagePhoto(context,number);
					
					cModel.setProfilepic(img_bitmap);
					
					recentsArrays.add(cModel);
										
					
				
					
				}while (cursor.moveToNext());
				// move the cursor's pointer up one position.
				
		 	}
			
			}
			
		}
		catch (SQLException e)
		{
			Log.e("DB Error", e.toString());
			e.printStackTrace();
			
		}finally {
			if(cursor!=null)
				 cursor.close();
	    }
 
		// return the ArrayList that holds the data collected from
		// the database.
		return recentsArrays;
	
		
	}*/


    private class CustomSQLiteOpenHelper extends SQLiteOpenHelper {
        public CustomSQLiteOpenHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {


            try {
                String newTableString = "create table " +
                        TABLE_NAME_PROFILE +
                        " (" +
                        TABLE_ROW_ACCID + " integer primary key," +
                        TABLE_ROW_DISPLAYNAME + " text," +
                        TABLE_ROW_SIPDOMAIN + " text," +
                        TABLE_ROW_DOMAINPROXY + " text," +
                        TABLE_ROW_SENDSIPKEEPALIVES + " text," +
                        TABLE_ROW_AUDIOCODECSLISTBASIC + " text," +
                        TABLE_ROW_KEEPALIVEEXP + " text," +
                        TABLE_ROW_AUTHNAME + " text," +
                        TABLE_ROW_CALLERID + " text," +
                        TABLE_ROW_USERNAME + " text," +
                        TABLE_ROW_PASSWORD + " text," +
                        TABLE_ROW_XMPPDOMAIN + " text," +
                        TABLE_ROW_XMPPFILETRANSDOMAIN + " text," +
                        TABLE_ROW_XMPPPROXYPROTOCOL + " text," +
                        TABLE_ROW_XMPPPROXYHOST + " text," +
                        TABLE_ROW_XMPPUSERNAME + " text," +
                        TABLE_ROW_XMPPPASSWORD + " text," +
                        TABLE_ROW_XMPPPROXYUSERNAME + " text," +
                        TABLE_ROW_XMPPPROXYPASSWORD + " text," +
                        TABLE_ROW_STUNSERVER + " text," +
                        TABLE_ROW_VIDEOCODECSLIST + " text," +
                        TABLE_ROW_VMACCESSCODE + " text," +
                        TABLE_ROW_VMACCOUNT + " text," +
                        TABLE_ROW_APIURL + " text," +
                        TABLE_ROW_APIUSER + " text," +
                        TABLE_ROW_APIPASSWORD + " text," +
                        TABLE_ROW_PUSHURL + " text," +
                        TABLE_ROW_PREMAUDIOCODECSLIST + " text" +

                        ");";
                //  execute the query string to the database   unique.
                db.execSQL(newTableString);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // NOTHING TO DO HERE. THIS IS THE ORIGINAL DATABASE VERSION.
            // OTHERWISE, YOU WOULD SPECIFIY HOW TO UPGRADE THE DATABASE.
        }
    }


    public void addAccount(ProfileData profileData) {

        ContentValues values = new ContentValues();

        values.put(TABLE_ROW_ACCID, profileData.getACCID());
        values.put(TABLE_ROW_DISPLAYNAME, "" + profileData.getDISPLAYNAME().toString());
        values.put(TABLE_ROW_SIPDOMAIN, "" + profileData.getSIPDOMAIN().toString());
        values.put(TABLE_ROW_DOMAINPROXY, profileData.getDOMAINPROXY().toString());
        values.put(TABLE_ROW_SENDSIPKEEPALIVES, profileData.getSENDSIPKEEPALIVES().toString());
        values.put(TABLE_ROW_AUDIOCODECSLISTBASIC, profileData.getAUDIOCODECSLISTBASIC().toString());
        values.put(TABLE_ROW_KEEPALIVEEXP, profileData.getKEEPALIVEEXP().toString());
        values.put(TABLE_ROW_AUTHNAME, profileData.getAUTHNAME().toString());
        values.put(TABLE_ROW_CALLERID, profileData.getCALLERID().toString());
        values.put(TABLE_ROW_USERNAME, profileData.getUSERNAME().toString());
        values.put(TABLE_ROW_PASSWORD, profileData.getPASSWORD().toString());
        values.put(TABLE_ROW_XMPPDOMAIN, profileData.getXMPPDOMAIN().toString());
        values.put(TABLE_ROW_XMPPFILETRANSDOMAIN, profileData.getXMPPFILETRANSDOMAIN().toString());
        values.put(TABLE_ROW_XMPPPROXYPROTOCOL, profileData.getXMPPPROXYPROTOCOL().toString());
        values.put(TABLE_ROW_XMPPPROXYHOST, profileData.getXMPPPROXYHOST().toString());
        values.put(TABLE_ROW_XMPPUSERNAME, "" + profileData.getXMPPUSERNAME().toString());
        values.put(TABLE_ROW_XMPPPASSWORD, profileData.getXMPPPASSWORD().toString());
        values.put(TABLE_ROW_XMPPPROXYUSERNAME, profileData.getXMPPPROXYUSERNAME().toString());
        values.put(TABLE_ROW_XMPPPROXYPASSWORD, profileData.getXMPPPROXYPASSWORD());
        values.put(TABLE_ROW_STUNSERVER, profileData.getSTUNSERVER().toString());
        values.put(TABLE_ROW_VIDEOCODECSLIST, profileData.getVIDEOCODECSLIST().toString());
        values.put(TABLE_ROW_VMACCESSCODE, profileData.getVMACCESSCODE().toString());
        values.put(TABLE_ROW_VMACCOUNT, "" + profileData.getVMACCOUNT().toString());
        values.put(TABLE_ROW_APIURL, profileData.getAPIURL().toString());
        values.put(TABLE_ROW_APIUSER, profileData.getAPIUSER().toString());
        values.put(TABLE_ROW_APIPASSWORD, profileData.getAPIPASSWORD().toString());
        values.put(TABLE_ROW_PUSHURL, profileData.getPUSHURL().toString());
        values.put(TABLE_ROW_PREMAUDIOCODECSLIST, profileData.getPREMAUDIOCODECSLIST().toString());

        // ask the database object to insert the new data
        try {
            db.insertOrThrow(TABLE_NAME_PROFILE, null, values);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveLoginInfo(PreferenceProvider preferenceProvider) {
        try {
            Cursor cursor;
            cursor = db.rawQuery("SELECT display_name , username,  data  from accounts", null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                do {
                    String displayname = cursor.getString(0);
                    String username = cursor.getString(1);
                    String pwd = cursor.getString(2);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ProfileData getAccount(int position) {


        ProfileData profiledata = new ProfileData();
        Cursor cursor;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME_PROFILE + " WHERE " + TABLE_ROW_ACCID + "=" + position, null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                do {
                    profiledata.setACCID(cursor.getInt(0));
                    profiledata.setDISPLAYNAME("" + cursor.getString(1));
                    profiledata.setSIPDOMAIN("" + cursor.getString(2));
                    profiledata.setDOMAINPROXY("" + cursor.getString(3));
                    profiledata.setSENDSIPKEEPALIVES("" + cursor.getString(4));
                    profiledata.setAUDIOCODECSLISTBASIC("" + cursor.getString(5));
                    profiledata.setKEEPALIVEEXP("" + cursor.getString(6));
                    profiledata.setAUTHNAME("" + cursor.getString(7));
                    profiledata.setCALLERID("" + cursor.getString(8));
                    profiledata.setUSERNAME("" + cursor.getString(9));
                    profiledata.setPASSWORD("" + cursor.getString(10));
                    profiledata.setXMPPDOMAIN("" + cursor.getString(11));
                    profiledata.setXMPPFILETRANSDOMAIN("" + cursor.getString(12));
                    profiledata.setXMPPPROXYPROTOCOL("" + cursor.getString(13));
                    profiledata.setXMPPPROXYHOST("" + cursor.getString(14));
                    profiledata.setXMPPUSERNAME("" + cursor.getString(15));
                    profiledata.setXMPPPASSWORD("" + cursor.getString(16));
                    profiledata.setXMPPPROXYUSERNAME("" + cursor.getString(17));
                    profiledata.setXMPPPROXYPASSWORD("" + cursor.getString(18));
                    profiledata.setSTUNSERVER("" + cursor.getString(19));
                    profiledata.setVIDEOCODECSLIST("" + cursor.getString(20));
                    profiledata.setVMACCESSCODE("" + cursor.getString(21));
                    profiledata.setVMACCOUNT("" + cursor.getString(22));
                    profiledata.setAPIURL("" + cursor.getString(23));
                    profiledata.setAPIUSER("" + cursor.getString(24));
                    profiledata.setAPIPASSWORD("" + cursor.getString(25));
                    profiledata.setPUSHURL("" + cursor.getString(26));
                    profiledata.setPREMAUDIOCODECSLIST("" + cursor.getString(27));
                }
                while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return profiledata;

    }


    public ArrayList<ProfileData> getAllAccounts() {


        ArrayList<ProfileData> profilelist = new ArrayList<ProfileData>();

        Cursor cursor;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME_PROFILE, null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                do {
                    ProfileData profiledata = new ProfileData();

                    profiledata.setACCID(cursor.getInt(0));
                    profiledata.setDISPLAYNAME("" + cursor.getString(1));
                    profiledata.setSIPDOMAIN("" + cursor.getString(2));
                    profiledata.setDOMAINPROXY("" + cursor.getString(3));
                    profiledata.setSENDSIPKEEPALIVES("" + cursor.getString(4));
                    profiledata.setAUDIOCODECSLISTBASIC("" + cursor.getString(5));
                    profiledata.setKEEPALIVEEXP("" + cursor.getString(6));
                    profiledata.setAUTHNAME("" + cursor.getString(7));
                    profiledata.setCALLERID("" + cursor.getString(8));
                    profiledata.setUSERNAME("" + cursor.getString(9));
                    profiledata.setPASSWORD("" + cursor.getString(10));
                    profiledata.setXMPPDOMAIN("" + cursor.getString(11));
                    profiledata.setXMPPFILETRANSDOMAIN("" + cursor.getString(12));
                    profiledata.setXMPPPROXYPROTOCOL("" + cursor.getString(13));
                    profiledata.setXMPPPROXYHOST("" + cursor.getString(14));
                    profiledata.setXMPPUSERNAME("" + cursor.getString(15));
                    profiledata.setXMPPPASSWORD("" + cursor.getString(16));
                    profiledata.setXMPPPROXYUSERNAME("" + cursor.getString(17));
                    profiledata.setXMPPPROXYPASSWORD("" + cursor.getString(18));
                    profiledata.setSTUNSERVER("" + cursor.getString(19));
                    profiledata.setVIDEOCODECSLIST("" + cursor.getString(20));
                    profiledata.setVMACCESSCODE("" + cursor.getString(21));
                    profiledata.setVMACCOUNT("" + cursor.getString(22));
                    profiledata.setAPIURL("" + cursor.getString(23));
                    profiledata.setAPIUSER("" + cursor.getString(24));
                    profiledata.setAPIPASSWORD("" + cursor.getString(25));
                    profiledata.setPUSHURL("" + cursor.getString(26));
                    profiledata.setPREMAUDIOCODECSLIST("" + cursor.getString(27));

                    profilelist.add(profiledata);
                }
                while (cursor.moveToNext());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return profilelist;

    }

    public void removeAll() {
        try {
            db.delete(TABLE_NAME_PROFILE, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}