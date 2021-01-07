/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.ui.contacts;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.app.dvoip.R;
import com.vx.core.android.contacts.ContactMethodHelper;
import com.vx.core.android.contacts.ContactsDetailsModel;

/**
 * This class shows contact details with multiple phone numbers.
 */
public class ContactsDetailsActivity extends Activity {

    private ImageView mContactImageView;
    private TextView mContactNameTextView;
    private ImageView mBackImageView;

    private ListView mContactDetailsListView;
    private ContactsDetailsAdapter mContactDetailsAdapter;
    private ArrayList<ContactsDetailsModel> mContactsDetailsArray;
    private String mContactID;
    private String mContactName;
    private String mContactType;
    private String mPhoneNo;
    private String mType;
    private String TAG = "ContactsDetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_contacts_details);

        mContactDetailsListView = (ListView) findViewById(R.id.contactdetails_listview);
        mContactImageView = (ImageView) findViewById(R.id.contact_details_photo_img);
        mContactNameTextView = (TextView) findViewById(R.id.contact_details_name_tv);
        mBackImageView = (ImageView) findViewById(R.id.img_backbutton);
        mContactsDetailsArray = new ArrayList<>();

        Bundle data = getIntent().getExtras();

        mContactName = data.getString("ContactList_contactName");
        mContactID = data.getString("ContactList_contactID");
        mContactType = data.getString("contact_type");
        Log.i(TAG, "contact Name: " + mContactName);
        mContactNameTextView.setText("" + mContactName);
        mBackImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        //For CCS
        if (mContactType.equals("app")) {
            ContactsDetailsModel CDModel = new ContactsDetailsModel();
            CDModel.setContactNumber(mContactID);
            CDModel.setContactType("2");

            mContactsDetailsArray.add(CDModel);

        } else {
            //GetMultipleNumbers("" + mContactID);
            mContactsDetailsArray = ContactMethodHelper.getMultipleNumbers(getApplicationContext(), mContactID);
        }

        Log.i(TAG, "mContactsDetailsArray size " + mContactsDetailsArray.size());
        if (mContactsDetailsArray == null || mContactsDetailsArray.size() == 0) {
            mContactsDetailsArray = ContactMethodHelper.getMultipleNumbers(mContactID);
            Log.i(TAG, "mContactsDetailsArray size from store list" + mContactsDetailsArray.size());
        }
        mContactDetailsAdapter = new ContactsDetailsAdapter(
                this, mContactsDetailsArray, mContactType);
        mContactDetailsListView.setAdapter(mContactDetailsAdapter);

    }

    private void GetMultipleNumbers(String contactID) {

        try {
            Cursor phone = getApplicationContext().getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{contactID}, null);

            if (phone != null && phone.getCount() > 1) {
                String[] subContacts = new String[phone.getCount()];
                String[] subLookup = new String[phone.getCount()];
            }

            if (phone != null) {

                while (phone.moveToNext()) {

                    mPhoneNo = phone
                            .getString(phone
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    mType = phone
                            .getString(phone
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                    /*Uri contactPhotoUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, Integer.parseInt(contactID));
                    InputStream photoDataStream = Contacts.openContactPhotoInputStream(getApplicationContext().getContentResolver(), contactPhotoUri);
                    if (photoDataStream != null) {
                        Bitmap mPhoto = BitmapFactory.decodeStream(photoDataStream);
                        mContactImageView.setImageBitmap(getRoundedCornerBitmap(mPhoto, 15));
                    }*/
                    Bitmap contactPhotoBitmap = ContactMethodHelper.getContactImage(getApplicationContext(),
                            mPhoneNo);
                    if (contactPhotoBitmap != null)
                        mContactImageView.setImageBitmap(getRoundedCornerBitmap(contactPhotoBitmap, 15));

                    ContactsDetailsModel CDModel = new ContactsDetailsModel();
                    CDModel.setContactNumber(mPhoneNo);
                    CDModel.setContactType(mType);

                    mContactsDetailsArray.add(CDModel);


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ContactsDetailsModel CDMode2 = new ContactsDetailsModel();
            CDMode2.setContactNumber(mPhoneNo);
            CDMode2.setContactType(mType);

            mContactsDetailsArray.add(CDMode2);
        }
    }


    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }


}
