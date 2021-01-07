/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.ui.contacts;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.dvoip.R;
import com.vx.core.android.contacts.ContactMethodHelper;
import com.vx.core.android.contacts.ContactsDetailsModel;
import com.vx.core.android.contacts.ContactsModel;
import com.vx.core.android.contacts.IVXContacts;
import com.vx.ui.Home;
import com.vx.ui.dialpad.DialerFragment;
import com.vx.utils.PermissionUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class gets native contacts list and shows on UI.
 */
public class ContactsFragment extends Fragment implements OnClickListener {

    private ListView mContactsListView;
    private ListView mAppContactsListView;
    private IVXContacts mContactsInterface;
    private ArrayList<ContactsModel> mContactsArray = new ArrayList<>();
    private ArrayList<ContactsModel> mContactsSearchArray;
    private ContactsAdapter mContactsAdapter;
    private SectionListAdapter mSectionAdapter;
    private TextView mNativeContactsTextView;
    private TextView mMosipContactsTextView;
    private Button mContactsAddContactsImageView;
    private TextView mNoContactsFoundTextView;
    private TextView mAppNoContactsFoundTextView;
    public static EditText mContactsSearchBarEditText;
    View contactsMainView;

    private GetNativeContactsAssync mNativeContactsAsyncTask;
    private MyContentObserver mMyContentObserver;
    private ArrayList<ContactsDetailsModel> mContactsDetailsArray;
    private String mLoadType;
    private boolean mFilteredClicked = false;
    private boolean mIsAppContacts = false;
    private Activity activity;
    private ArrayList<ContactsDetailsModel> mContactsFoveriteArray;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setRetainInstance(true);

        /* Below code need to comment to avoid crash in 2.x devices */
        if (contactsMainView == null) {
            contactsMainView = inflater.inflate(R.layout.fragment_contacts,
                    container, false);

            mContactsListView = (ListView) contactsMainView
                    .findViewById(R.id.contacts_listview);
            mAppContactsListView = (ListView) contactsMainView
                    .findViewById(R.id.applicationcontacts_listview);

            mNativeContactsTextView = (TextView) contactsMainView
                    .findViewById(R.id.contacts_native_tv);
            mMosipContactsTextView = (TextView) contactsMainView
                    .findViewById(R.id.contacts_mosip_tv);
            mNoContactsFoundTextView = (TextView) contactsMainView
                    .findViewById(R.id.nocontacts_found_tv);

            mAppNoContactsFoundTextView = (TextView) contactsMainView.findViewById(R.id.appnocontacts_found_tv);

            mContactsSearchBarEditText = (EditText) contactsMainView
                    .findViewById(R.id.contacts_searchbar_edit);
            mContactsAddContactsImageView = (Button) contactsMainView
                    .findViewById(R.id.contacts_addcontact_img);

            mNativeContactsTextView.setOnClickListener(this);
            mMosipContactsTextView.setOnClickListener(this);
            mContactsAddContactsImageView.setOnClickListener(this);

            mNativeContactsTextView.setSelected(true);

            mIsAppContacts = false;


            loadAllContacts();

            setmMyContentObserver();

            mContactsSearchBarEditText
                    .addTextChangedListener(contactsfilterTextWatcher);

            // Adding the empty view to ListView for extra Space
            WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            int screenHeight = display.getHeight();
            View layout = inflater.inflate(R.layout.emptyview, mContactsListView, false);
            ViewGroup.LayoutParams lp = layout.getLayoutParams();
            lp.height = screenHeight / 4;
            layout.setLayoutParams(lp);
            mContactsListView.addFooterView(layout);


        }

        return contactsMainView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.activity = activity;
    }

    private TextWatcher contactsfilterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {

            Log.i("ContactsFragment", "contactsFilterTextWatcher, search Text: " + s.toString());
            String searchText = s.toString();


            if (searchText.length() > 0) {

                mFilteredClicked = true;

                mContactsListView.setVisibility(View.VISIBLE);
                mAppContactsListView.setVisibility(View.GONE);

                mContactsAdapter = new ContactsAdapter(activity
                        .getApplicationContext(), mContactsSearchArray);
                mContactsAdapter.getFilter().filter(s.toString());
                mContactsAdapter.notifyDataSetChanged();

            } else {
                try {
                    mFilteredClicked = false;
                    mContactsAdapter = new ContactsAdapter(activity
                            .getApplicationContext(), mContactsArray);
                    mContactsAdapter.getFilter().filter("");
                    mContactsAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            // your search logic here
        }

    };

    @Override
    public void onResume() {
        super.onResume();

        Log.i("ContactsFragment", "Load ContactsFragment.onresume()");

        mFilteredClicked = false;
        mContactsAddContactsImageView.setVisibility(View.VISIBLE);

       /* if (mContactsAdapter != null)
            mContactsAdapter.notifyDataSetChanged();
        mAppContactsListView.setVisibility(View.GONE);
        mContactsListView.setVisibility(View.VISIBLE);*/

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (mMyContentObserver != null) {
                activity.getApplicationContext().getContentResolver().unregisterContentObserver(mMyContentObserver);
            }
            mNativeContactsAsyncTask.cancel(true);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void LoadUIEments(String contacts_type) {

        if (contacts_type.equalsIgnoreCase("Native")) {
            mIsAppContacts = false;
            mAppContactsListView.setVisibility(View.GONE);
            mContactsListView.setVisibility(View.VISIBLE);
            /*mContactsInterface = VXPhoneContacts.getinstance();
            mContactsArray = mContactsInterface.GetContact(activity
                    .getApplicationContext(), contacts_type);*/

            mContactsArray = ContactMethodHelper.getAllContactsWithPhoneNumber(getActivity());

            mContactsSearchArray = new ArrayList<>(mContactsArray);
            filterContactArray();
            mContactsAdapter = new ContactsAdapter(activity
                    .getApplicationContext(), mContactsArray);


            mSectionAdapter = new SectionListAdapter(activity, activity.getLayoutInflater(), mContactsAdapter, mContactsArray);

            mContactsListView.setAdapter(mSectionAdapter);
            mContactsListView.setEmptyView(mNoContactsFoundTextView);

            mAppNoContactsFoundTextView.setVisibility(View.GONE);
            mNoContactsFoundTextView.setVisibility(View.VISIBLE);

            if (mContactsArray != null && mContactsArray.size() > 0) {
                mNoContactsFoundTextView.setVisibility(View.GONE);
            } else {
                mNoContactsFoundTextView.setVisibility(View.VISIBLE);
            }
        }


    }

    /**
     * This method clears the search text
     */
    public void clearSearchText() {
        if (mContactsSearchBarEditText != null) {
            if (mContactsSearchBarEditText.getText().toString().length() > 0)
                mContactsSearchBarEditText.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.contacts_native_tv:

                mContactsSearchBarEditText.setText("");
                mContactsAddContactsImageView.setVisibility(View.VISIBLE);

                mMosipContactsTextView.setSelected(false);
                mNativeContactsTextView.setSelected(true);

                mAppContactsListView.setVisibility(View.GONE);
                mContactsListView.setVisibility(View.VISIBLE);

                mLoadType = "Native";

                mAppNoContactsFoundTextView.setVisibility(View.GONE);

                if (mIsAppContacts && mFilteredClicked) {

                    loadAllContacts();

                  /*  mNativeContactsAsyncTask = new GetNativeContactsAssync();
                    mNativeContactsAsyncTask.execute();*/
                }
                mIsAppContacts = false;

                break;

            case R.id.contacts_mosip_tv:

                mContactsSearchBarEditText.setText("");

                mContactsAddContactsImageView.setVisibility(View.INVISIBLE);

                mIsAppContacts = true;

                mAppContactsListView.setVisibility(View.VISIBLE);
                mContactsListView.setVisibility(View.GONE);

                mMosipContactsTextView.setSelected(true);
                mNativeContactsTextView.setSelected(false);

                mLoadType = "MoSIP";
                LoadUIEments(mLoadType);

                break;

            case R.id.contacts_addcontact_img:

                try {
                    Intent in = new Intent(Intent.ACTION_INSERT);
                    in.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    startActivity(in);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;

            default:
                break;
        }
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

            activity.runOnUiThread(new Runnable() {

                public void run() {

                    try {

                     /*   mLoadType = "Native";
                        mNativeContactsAsyncTask = new GetNativeContactsAssync();
                        mNativeContactsAsyncTask.execute();*/

                        loadAllContacts();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

        }

    }

    public class ContactsAdapter extends BaseAdapter implements Filterable {

        ArrayList<ContactsModel> contactsList;
        Context mcontext;

        LayoutInflater inflater = null;
        ContactsFilter listfilter = new ContactsFilter();

        public ContactsAdapter(Context context,
                               ArrayList<ContactsModel> contactsarray) {

            mcontext = context;
            contactsList = contactsarray;
            inflater = (LayoutInflater) mcontext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {

            if (contactsList != null && contactsList.size() > 0) {

                Log.i("ContactsFragment", "array list size: " + contactsList.size());

                return contactsList.size();

            } else {

                return 0;
            }

        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class Holder {

            TextView name_tv;
            TextView contactid_tv;
            ImageView profilepic_img;
            RelativeLayout contact_listitem_name_relative, contactListItemParentLayout;

        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            Holder holder = null;

            if (convertView == null) {
                /* There is no view at this position, we create a new one. In this case by inflating an xml layout */
                convertView = inflater.inflate(R.layout.adapter_contact_item, null);
                holder = new Holder();
                holder.contactListItemParentLayout = (RelativeLayout) convertView.findViewById(R.id.rl_contacts_item);
                holder.name_tv = (TextView) convertView.findViewById(R.id.contact_name_textview);
                holder.contactid_tv = (TextView) convertView.findViewById(R.id.contactid_tv);
                holder.profilepic_img = (ImageView) convertView.findViewById(R.id.contact_profilepic_imageview);
                holder.contact_listitem_name_relative = (RelativeLayout) convertView.findViewById(R.id.contact_listitem_name_relative);
                convertView.setTag(holder);
            } else {
                /* We recycle a View that already exists */
                holder = (Holder) convertView.getTag();
            }

            final ContactsModel cModel = contactsList.get(position);

            holder.name_tv.setSelected(true);
            holder.name_tv.setText(cModel.getContactName());
            holder.contactid_tv.setText(cModel.getContactID());
            /*Bitmap photo = getImagePhoto(holder.contactid_tv.getText().toString());
            if (photo != null) {
                holder.profilepic_img
                        .setImageBitmap(getRoundedCornerBitmap(photo, 15));
            } else {
                holder.profilepic_img
                        .setImageResource(R.drawable.contact_avathar);
            }*/

            holder.profilepic_img.setTag(holder.contactid_tv.getText().toString());
            new LoadContactImageAsyncTask(holder.profilepic_img).execute();
            holder.contactListItemParentLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        String contactName = cModel.getContactName();
                        String contactID = cModel.getContactID();
                        mContactsDetailsArray = new ArrayList<>();
                        mContactsDetailsArray = ContactMethodHelper.getMultipleNumbers(contactID);
                        Log.i("ContactsFragment", "mContactsDetailsArray Size " + mContactsDetailsArray.size());
                        if (mContactsDetailsArray.size() > 1) {

                            Intent contact_details_intent = new
                                    Intent(activity.getApplicationContext(),
                                    ContactsDetailsActivity.class);

                            contact_details_intent.putExtra("ContactList_contactName", contactName);
                            contact_details_intent
                                    .putExtra("ContactList_contactID", contactID);
                            contact_details_intent.putExtra("contact_type", "native");
                            startActivity(contact_details_intent);
                        } else {
                            if (mContactsDetailsArray != null && mContactsDetailsArray.size() > 0) {
                                final ContactsDetailsModel cModel = mContactsDetailsArray.get(0);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                                    if (Home.mViewPager != null) {
                                        Home.mViewPager.setCurrentItem(1);
                                    }
                                }

                                if (DialerFragment.dialerPhoneNumberEditText != null) {
                                    DialerFragment.dialerPhoneNumberEditText.setText(cModel.getContactNumber());
                                    DialerFragment.dialerPhoneNumberEditText.setSelection(DialerFragment.dialerPhoneNumberEditText.getText().length());
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

            return convertView;
        }

        @Override
        public Filter getFilter() {

            return listfilter;
        }

        public class ContactsFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                // NOTE: this function is *always* called from a background
                // thread,

                Log.i("ContactsFragment", "Search entered text: " + constraint);

                FilterResults result = new FilterResults();

                if (constraint != null && constraint.toString().length() > 0) {

                    String filterString = constraint.toString().toLowerCase();

                    ArrayList<ContactsModel> Items;
                    ArrayList<ContactsModel> filterList = new ArrayList<>();

                    synchronized (this) {
                        Items = mContactsSearchArray;
                    }

                    for (int i = 0; i < Items.size(); i++) {
                        ContactsModel item = Items.get(i);
                        if (item.getContactName().toLowerCase()
                                .contains(filterString.toLowerCase()) || item.getContactNumber().toLowerCase()
                                .contains(filterString.toLowerCase())) {
                            filterList.add(item);
                        }
                    }


                    result.count = filterList.size();
                    result.values = filterList;

                } else {
                    synchronized (this) {
                        if (contactsList != null) {
                            result.count = contactsList.size();
                            result.values = contactsList;
                            Log.i("ContactsFragment", " no search text contactsList.size=::" + contactsList.size());
                        }
                    }
                }
                return result;
            }

            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {

                try {

                    @SuppressWarnings("unchecked")
                    ArrayList<ContactsModel> filtered = (ArrayList<ContactsModel>) results.values;
                    Set set = new TreeSet(new Comparator<ContactsModel>() {
                        @Override
                        public int compare(ContactsModel o1, ContactsModel o2) {

                            if ((o1.getContactName() != null && o2.getContactName() != null) && o1.getContactName().equalsIgnoreCase(o2.getContactName())) {
                                return 0;
                            }
                            return 1;
                        }
                    });

                    // Filtering duplicates

                    if (filtered != null)
                        set.addAll(filtered);


                    filtered = new ArrayList<>(set);
                    if (filtered != null) {

                        Log.i("ContactsFragment", "publishResults filter size" + filtered.size());
                        if (filtered.size() > 0) {

                            try {
                                mContactsAdapter = new ContactsAdapter(activity
                                        .getApplicationContext(), filtered);

                                mSectionAdapter = new SectionListAdapter(activity, activity.getLayoutInflater(), mContactsAdapter, filtered);
                                mContactsListView.setAdapter(mSectionAdapter);
                                mContactsListView.setEmptyView(mNoContactsFoundTextView);
                                mNoContactsFoundTextView.setVisibility(View.GONE);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        } else {

                            mContactsAdapter = new ContactsAdapter(activity
                                    .getApplicationContext(), filtered);
                            mSectionAdapter = new SectionListAdapter(activity, activity.getLayoutInflater(), mContactsAdapter, filtered);
                            mContactsListView.setAdapter(mSectionAdapter);
                            mContactsListView.setEmptyView(mNoContactsFoundTextView);
                            mNoContactsFoundTextView.setVisibility(View.GONE);
                            mNoContactsFoundTextView.setVisibility(View.VISIBLE);

                        }
                    }

                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }

        }

    }

    /**
     * This class gets native native contacts from Database
     */
    private class GetNativeContactsAssync extends AsyncTask<Void, Void, ArrayList<ContactsModel>> {


        @Override
        protected void onPreExecute() {
            // Showing progress dialog before sending http request

			/*dialog = new CustomProgressDialog(activity);
                dialog.show();*/

            mAppNoContactsFoundTextView.setVisibility(View.GONE);
            //mNoContactsFoundTextView.setVisibility(View.VISIBLE);

            mNoContactsFoundTextView.setText("Contacts Loading please wait....");

            mIsAppContacts = false;

            mAppContactsListView.setVisibility(View.GONE);
            mContactsListView.setVisibility(View.VISIBLE);

        }

        @Override
        protected ArrayList<ContactsModel> doInBackground(Void... arg0) {


            // mContactsInterface = VXPhoneContacts.getinstance();
          /*  mContactsArray = mContactsInterface.GetContact(activity
                    .getApplicationContext(), "Native");*/

            mContactsArray = ContactMethodHelper.getAllContactsWithPhoneNumber(getActivity());
            mContactsSearchArray = new ArrayList<>(mContactsArray);
            filterContactArray();


            if (isCancelled()) {
                return null;
            }

            return mContactsArray;
        }


        @Override
        protected void onPostExecute(final ArrayList<ContactsModel> contactslist) {


            if (contactslist != null && contactslist.size() > 0) {

                if (mContactsListView != null) {

                    mContactsAdapter = new ContactsAdapter(activity.getApplicationContext(), contactslist);
                    mSectionAdapter = new SectionListAdapter(activity, activity.getLayoutInflater(), mContactsAdapter, contactslist);
                    mContactsListView.setAdapter(mSectionAdapter);
                    mContactsListView.setEmptyView(mNoContactsFoundTextView);

                    if (contactslist != null && contactslist.size() > 0) {
                        mNoContactsFoundTextView.setVisibility(View.GONE);
                    } else {
                        mNoContactsFoundTextView.setVisibility(View.VISIBLE);
                    }

                    // ListView on item click listener
                  /*  mContactsListView.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {

                            try {
                                String contactName = "";
                                String contactID = "";
                               *//* TextView contactTextView = ((TextView) view
                                        .findViewById(R.id.contact_name_textview));
                                if (contactTextView != null) {
                                    contactName = contactTextView.getText()
                                            .toString().trim();
                                }
                                TextView contactIDTextView = ((TextView) view
                                        .findViewById(R.id.contactid_tv));

                                if (contactIDTextView != null) {
                                    contactID = contactIDTextView.getText().toString()
                                            .trim();
                                }*//*

                                mContactsDetailsArray = new ArrayList<>();
                                mContactsDetailsArray = ContactMethodHelper.getMultipleNumbers(getActivity(), contactID);

                                if (mContactsDetailsArray.size() > 1) {

                                    Intent contact_details_intent = new
                                            Intent(activity.getApplicationContext(),
                                            ContactsDetailsActivity.class);

                                    contact_details_intent.putExtra("ContactList_contactName", contactName);
                                    contact_details_intent
                                            .putExtra("ContactList_contactID", contactID);
                                    contact_details_intent.putExtra("contact_type", "native");
                                    startActivity(contact_details_intent);
                                } else {
                                    if (mContactsDetailsArray != null && mContactsDetailsArray.size() > 0) {
                                        final ContactsDetailsModel cModel = mContactsDetailsArray.get(0);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                                            if (Home.mViewPager != null) {
                                                Home.mViewPager.setCurrentItem(1);
                                            }
                                        }

                                        if (DialerFragment.dialerPhoneNumberEditText != null) {
                                            DialerFragment.dialerPhoneNumberEditText.setText(cModel.getContactNumber());
                                            DialerFragment.dialerPhoneNumberEditText.setSelection(DialerFragment.dialerPhoneNumberEditText.getText().length());
                                        }
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }
                    });*/

                }

                mNoContactsFoundTextView.setText("No Contacts Found");
            }else {
                mNoContactsFoundTextView.setText("No Contacts Found");
            }
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


    public Bitmap getImagePhoto(String phoneNumber) {

        Uri photoUri = null;
        ContentResolver cr = activity.getContentResolver();
        try {
            try {
                long userid = Long.parseLong(phoneNumber);
                photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userid);
            } catch (Exception e) {
                e.printStackTrace();
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
    }

    class LoadContactImageAsyncTask extends AsyncTask<Object, Void, Bitmap> {

        private ImageView contactImageView;
        private String phoneNumber;

        public LoadContactImageAsyncTask(ImageView contactImageView) {
            this.contactImageView = contactImageView;
            this.phoneNumber = contactImageView.getTag().toString();
        }

        @Override
        protected Bitmap doInBackground(Object... params) {

            Uri photoUri = null;
            ContentResolver cr = activity.getContentResolver();
            try {
                try {
                    long userid = Long.parseLong(phoneNumber);
                    photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userid);
                } catch (Exception e) {
                    e.printStackTrace();
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

        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (!contactImageView.getTag().toString().equals(phoneNumber)) {
               /* The phoneNumber is not same. This means that this
                  image view is handled by some other async task.
                  We don't do anything and return. */
                return;
            }

            if (result != null) {
                contactImageView.setImageBitmap(getRoundedCornerBitmap(result, 15));
            } else {
                contactImageView
                        .setImageResource(R.drawable.ic_contacts_avatar);
            }

        }

    }

    private void filterContactArray() {
        Set set = new TreeSet(new Comparator<ContactsModel>() {
            @Override
            public int compare(ContactsModel o1, ContactsModel o2) {

                /*if (o1.getContactName() != null && o1.getContactNumber() != null && o2.getContactName() != null && o2.getContactNumber() != null && o1.getContactName().equalsIgnoreCase(o2.getContactName()) && o1.getContactNumber().equals(o2.getContactNumber())) {
                    return 0;
                }*/
                if ((o1.getContactID() != null && o2.getContactID() != null) && o1.getContactID().equalsIgnoreCase(o2.getContactID())) {
                    return 0;
                }
                return 1;
            }
        });

        // Filtering duplicates
        set.addAll(mContactsArray);
        mContactsArray = new ArrayList<>(set);
    }


    public void loadAllContacts() {

        try {
            mLoadType = "Native";
            mNativeContactsAsyncTask = new GetNativeContactsAssync();
            mNativeContactsAsyncTask.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setmMyContentObserver(){

        try {

            if (PermissionUtils.hasPermissions(activity, PermissionUtils.CONTACTSPERMISSION)) {
                Log.d("ContactsFragment", "setmMyContentObserver: called and inside if");
                mMyContentObserver = new MyContentObserver();
                if (mMyContentObserver!=null) {
                    activity
                            .getApplicationContext()
                            .getContentResolver()
                            .registerContentObserver(ContactsContract.Contacts.CONTENT_URI,
                                    true, mMyContentObserver);
                    Home.isContactObserverRegistered = true;
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
    Map getFavoriteContacts(){

        Map contactMap = new HashMap();

        Uri queryUri = ContactsContract.Contacts.CONTENT_URI;

        String[] projection = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.STARRED};

        String selection =ContactsContract.Contacts.STARRED + "='1'";

        Cursor cursor = getActivity().managedQuery(queryUri, projection, selection, null, null);

        while (cursor.moveToNext()) {
            String contactID = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts._ID));

            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactID));
            intent.setData(uri);
            String intentUriString = intent.toUri(0);

            String title = (cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
           /* Integer.parseInt((cursor.getString(
                    cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))));*/
          /*  String phone_no = cursor.getString(cursor
                    .getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));*/
            Log.e("mobile","mobile-->");

            contactMap.put(title,intentUriString.charAt(0));
        }










        cursor.close();
        return contactMap;
    }


   /* public void getlisr(){
        ContentResolver cr = getActivity().getContentResolver();
  *//*  Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
            null, null, null );*//*


        Cursor cur=cr.query(CallLog.Calls.CONTENT_URI,null,CallLog.Calls.DATE, null,null);
        String phone = null;
        String emailContact = null;
        String image_uri;
        Bitmap bitmap;

        final SQLiteDatabase mDb = db.getWritableDatabase();
        mDb.beginTransaction();

        if (cur.getCount() > 0)
        {
            while (cur.moveToNext())
            {
                String id = cur.getString(cur
                        .getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur
                        .getString(cur
                                .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                image_uri = cur
                        .getString(cur
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                if (Integer
                        .parseInt(cur.getString(cur
                                .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                {
                    System.out.println("name : " + name + ", ID : " + id);

                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                    + " = ?", new String[]{id}, null);
                    Log.e("pCur","dfgfdg  "+pCur.getCount());
                    while (pCur.moveToNext())
                    {
                        phone = pCur
                                .getString(pCur
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        // contactid=pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                   *//* phonenumber.add(pCur
                            .getString(pCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));`*//*

                    }
                    pCur.close();


                    Cursor emailCur = cr.query
                            (
                                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Email.CONTACT_ID
                                            + " = ?", new String[]{id}, null);

                    while (emailCur.moveToNext())
                    {
                        emailContact = emailCur
                                .getString(emailCur
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));

                        if(TextUtils.isEmpty(emailContact)||emailContact.equalsIgnoreCase(null)||emailContact.equalsIgnoreCase(""))
                        {
                            emailContact="";

                            Log.e("isEmpty","isEmpty " + emailContact);
                        }

                        else
                        {
                            Log.e("gfdszfg","Email " + emailContact);
                        }
                  *//*  emailType = emailCur
                            .getString(emailCur
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));*//*

                        Log.e("gfdszfg","Email " + emailContact);
                    }
                    emailCur.close();
                }

                if (image_uri != null)
                {
                    System.out.println(Uri.parse(image_uri));
                    try
                    {
                        bitmap = MediaStore.Images.Media
                                .getBitmap(getActivity().getContentResolver(),
                                        Uri.parse(image_uri));
                        System.out.println(bitmap);

                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                mList.add(new Contacts(name, phone, image_uri,emailContact));

                ContentValues contentValues = new ContentValues();
                contentValues.put("contact_name", name);
                contentValues.put("contact_number",phone);
                contentValues.put("contact_email",emailContact);
                contentValues.put("contact_image",image_uri);
                mDb.insert(TABLE_CONTACT, null, contentValues);

                emailContact="";
                phone="";
            }
            mDb.setTransactionSuccessful();

            mDb.endTransaction();
            cur.close();
        }
    }*/
}
