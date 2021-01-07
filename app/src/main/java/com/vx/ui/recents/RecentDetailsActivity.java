/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.ui.recents;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.app.dvoip.R;
import com.vx.core.android.db.CallLogsDB;
import com.vx.core.android.db.ContactVoipDatabase;
import com.vx.core.android.recents.RecentsModel;
import com.vx.core.android.service.NotificationService;
import com.vx.core.android.utils.MethodHelper;
import com.vx.core.jni.SipMethodHelper;
import com.vx.ui.Home;
import com.vx.ui.incall.InCallCardActivity;
import com.vx.utils.Constants;
import com.vx.utils.PermissionUtils;
import com.vx.utils.PreferenceProvider;
import com.vx.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class shows the call log details.
 */
public class RecentDetailsActivity extends Activity {

    private static final String TAG = "RecentDetailsActivity";
    private ListView mListView;
    private TextView mContactsNameTextView;
    private ImageView mContactPicImageView;
    private Button  mRecentMakeCallImageView;
    private ImageView mBackImageView;
    private ImageView mAddContactImageView;

    private CallLogsDB mCallLogsDB;
    private ArrayList<RecentsModel> mRecentDetails;
    private RecentsDetailsAdapter mRecentDetailsAdapter;
    private PreferenceProvider mPrefProvider;
    private ContactVoipDatabase mContactsDB;
    private MyContentObserver mMyContentObserver;

    private static final int ADD_DATA = 2;
    private String mContactName;
    private String mContactNumber;
    private boolean mIsContactFound;
    private long mLastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_recent_details);


        mListView = (ListView) findViewById(R.id.recentsdetails_listview);
        mContactsNameTextView = (TextView) findViewById(R.id.recents_details_name_tv);
        mContactPicImageView = (ImageView) findViewById(R.id.recents_details_photo_img);
        mBackImageView =  findViewById(R.id.img_back);
        mAddContactImageView = (ImageView) findViewById(R.id.recents_addcall_img);
        mRecentMakeCallImageView = (Button) findViewById(R.id.recents_call_img);

        mRecentDetails = new ArrayList<>();

        mPrefProvider = PreferenceProvider.getPrefInstance(getApplicationContext());

        Bundle data = getIntent().getExtras();

        mContactName = data.getString("Recentslist_contactName");
        mContactNumber = data.getString("Recentslist_contactnumber");
        mIsContactFound = data.getBoolean("Recentslist_contactfound");

        mContactsNameTextView.setText("" + mContactName);

        try {
            if (PermissionUtils.hasPermissions(getApplicationContext(), PermissionUtils.CONTACTSPERMISSION)) {
                mMyContentObserver = new MyContentObserver();
                if (mMyContentObserver != null) {
                    getApplicationContext()
                            .getContentResolver()
                            .registerContentObserver(ContactsContract.Contacts.CONTENT_URI,
                                    true, mMyContentObserver);
                }
            }

            mContactsDB = new ContactVoipDatabase(this);
            mCallLogsDB = new CallLogsDB(this);

        } catch (Throwable e) {
            e.printStackTrace();
        }

        updateCallLoghistory(mContactNumber);
        registerReceiver(recentUpdateReceiver, new IntentFilter(Home.packageName + ".RECENTUPDATE"));

        mRecentDetailsAdapter = new RecentsDetailsAdapter(getApplicationContext(), mRecentDetails);
        mListView.setAdapter(mRecentDetailsAdapter);

        try {

            Bitmap photo = Utils.getIMAGEPhoto(RecentDetailsActivity.this, mContactNumber);

            if (photo != null) {
                Bitmap roundedbitmap = MethodHelper.getRoundedCornerBitmap(photo, 15);
                mContactPicImageView.setImageBitmap(roundedbitmap);
            } else {
                mContactPicImageView.setBackgroundResource(R.drawable.ic_contacts_avatar);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mBackImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (mIsContactFound) {
            mAddContactImageView.setEnabled(false);
            mAddContactImageView.setVisibility(View.INVISIBLE);
        } else {
            boolean iscontactfound = mContactsDB.getDBPhoneRecordfound(mContactNumber);
            boolean iscontactnamefound = mContactsDB.getDBPhonenamefound(mContactNumber);

            if (iscontactfound || iscontactnamefound) {
                mAddContactImageView.setEnabled(false);
                mAddContactImageView.setVisibility(View.INVISIBLE);
            } else {
                mAddContactImageView.setEnabled(true);
                mAddContactImageView.setVisibility(View.VISIBLE);
            }

        }

        mAddContactImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent in = new Intent(Intent.ACTION_INSERT);
                in.setType(ContactsContract.Contacts.CONTENT_TYPE);
                in.putExtra(ContactsContract.Intents.Insert.PHONE, mContactNumber);
                startActivityForResult(in, ADD_DATA);

            }
        });


        mRecentMakeCallImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                // New changes to make reusable code.
                int result = SipMethodHelper.makeCall(RecentDetailsActivity.this, mPrefProvider, mPrefProvider.getPrefInt("AccID"), mContactNumber);

                if (result == Constants.MAKE_CALL_ERROR_CODE)
                    return;

                startCallingActivity((mContactNumber));

            }
        });


        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();

                // New changes to make reusable code.
                int result = SipMethodHelper.makeCall(RecentDetailsActivity.this, mPrefProvider, mPrefProvider.getPrefInt("AccID"), mContactNumber);

                if (result == Constants.MAKE_CALL_ERROR_CODE)
                    return;

                startCallingActivity((mContactNumber));

            }
        });
    }

    /**
     * This method calls calling activity
     *
     * @param phoneNumber mobile number
     */
    private void startCallingActivity(final String phoneNumber) {

        runOnUiThread(new Runnable() {
            public void run() {

                mPrefProvider.setPrefBoolean("incallspeaker", false);
                mPrefProvider.setPrefBoolean("speakerEnabled", false);
                mPrefProvider.setPrefBoolean("incallmute", false);

                Intent inCallIntent = new Intent(RecentDetailsActivity.this, InCallCardActivity.class);
                inCallIntent.putExtra("ISCall", "outgoing");
                inCallIntent.putExtra("ContactNum", "" + phoneNumber);
                inCallIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(inCallIntent);

            }
        });

    }

    /*public Bitmap getIMAGEPhoto(String phoneNumber) {
        Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Uri photoUri = null;
        ContentResolver cr = this.getContentResolver();
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
                InputStream input = ContactsContract.Contacts
                        .openContactPhotoInputStream(cr, photoUri);
                if (input != null) {
                    return BitmapFactory.decodeStream(input);
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/


    BroadcastReceiver recentUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            try {
                if (mContactNumber != null) {
                    updateCallLoghistory(mContactNumber);
                    mRecentDetailsAdapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };


    private void updateCallLoghistory(String number) {

        mRecentDetails.clear();

        mCallLogsDB.open();

        ArrayList<ArrayList<Object>> data2 = mCallLogsDB.getAllRowsAsArraysAllNums(number);
        Log.i("RecentDetailsActivity", "recent details size=" + data2.size());

        for (int position = 0; position < data2.size(); position++) {

            final ArrayList<Object> row = data2.get(position);

            RecentsModel rmodel = new RecentsModel();

            if ((row.get(1).toString()).equals(number)) {

                String type = row.get(4).toString();
                String name = row.get(1).toString();
                String date = row.get(2).toString();
                String duration = row.get(3).toString();

                //long date_l=Long.parseLong(date);

                //date = SipMethodHelper.getDate(date_l, "dd/MM/yyyy hh:mm:ss");
                //duration = SipMethodHelper.formatDuration(getApplicationContext(),Long.parseLong(duration));

                rmodel.setCalltype(type);
                rmodel.setDate(date);
                rmodel.setDuration(duration);
                rmodel.setNumber(row.get(1).toString());
                rmodel.setName(name);

                mRecentDetails.add(rmodel);
            }

        }

        Collections.sort(mRecentDetails, Collections.reverseOrder());
        mCallLogsDB.close();
    }

    @Override
    public void onResume() {

        try {
            NotificationService obj = NotificationService.getInstance();
            if (obj != null)
                obj.cancelMissedCalls();

            updateCallLoghistory(mContactNumber);
            mRecentDetailsAdapter.notifyDataSetChanged();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        try {
            if (mMyContentObserver != null) {
                getApplicationContext().getContentResolver()
                        .unregisterContentObserver(mMyContentObserver);
            }

            if (recentUpdateReceiver != null) {
                unregisterReceiver(recentUpdateReceiver);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("RecentDetailsActivity", "onActivity Result called");
        //super.onActivityResult(requestCode, resultCode, data);
    }


    private class MyContentObserver extends ContentObserver {

        public MyContentObserver() {
            super(null);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            runOnUiThread(new Runnable() {

                public void run() {

                    try {
                        int contactId = getContactIDFromNumber(mContactNumber);
                        if (contactId > 0) {
                            mAddContactImageView
                                    .setVisibility(View.INVISIBLE);
                            String contactName = getContactNameFromNumber(mContactNumber);
                            mContactsNameTextView.setText(contactName);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

        }

    }

    private int getContactIDFromNumber(String contactNumber) {
        contactNumber = Uri.encode(contactNumber);
        int phoneContactID = 0;
        Cursor contactLookupCursor = getApplicationContext()
                .getContentResolver().query(
                        Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                                contactNumber),
                        new String[]{PhoneLookup.DISPLAY_NAME,
                                PhoneLookup._ID}, null, null, null);
        while (contactLookupCursor.moveToNext()) {
            phoneContactID = contactLookupCursor.getInt(contactLookupCursor
                    .getColumnIndexOrThrow(PhoneLookup._ID));

        }
        contactLookupCursor.close();

        return phoneContactID;
    }

    private String getContactNameFromNumber(String contactNumber) {
        contactNumber = Uri.encode(contactNumber);
        String contactName = "";
        Cursor contactLookupCursor = getApplicationContext()
                .getContentResolver().query(
                        Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                                contactNumber),
                        new String[]{PhoneLookup.DISPLAY_NAME}, null, null,
                        null);
        while (contactLookupCursor.moveToNext()) {
            contactName = contactLookupCursor.getString(contactLookupCursor
                    .getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));

        }
        contactLookupCursor.close();
        return contactName;
    }


}
