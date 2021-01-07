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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.app.dvoip.R;
import com.vx.core.android.contacts.ContactMethodHelper;
import com.vx.core.android.db.CallLogsDB;
import com.vx.core.android.recents.IVXRecents;
import com.vx.core.android.recents.RecentsModel;
import com.vx.core.android.recents.VXRecentsAll;
import com.vx.core.android.utils.MethodHelper;
import com.vx.core.jni.SipMethodHelper;
import com.vx.ui.Home;
import com.vx.ui.incall.InCallCardActivity;
import com.vx.utils.Constants;
import com.vx.utils.PermissionUtils;
import com.vx.utils.PreferenceProvider;
import com.vx.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * This class shows call logs and displayed in list view.
 */
public class RecentFragment extends Fragment implements OnClickListener {

    private static final String TAG = "RecentFragment";
    private ListView mRecentListView;
    private IVXRecents mRecentHelper;
    private ArrayList<RecentsModel> mRecentArray = new ArrayList<>();
    private RecentsAdapter mRecentAdapter;
    private ImageView mRecentDeleteImageView;
    private TextView mNoLogsFoundTextView;
    private View mRecentView;

    private CallLogsDB mCallLogsDB;
    private MyContentObserver mContactsContentObserver;
    private PreferenceProvider mPrefProvider;
    private Activity mActivity;
    // Below parameter used before for Missed, All Call logs differentiation.
    private String mRecentFilterType = "All";

    private long mLastClickTime = 0;
    int left, right, top, bottom;
    private static final int SAVE_CALL = 2;
    public AlertDialog myAlertDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i("RecentFragment", "Called onCreateView ");
        /* Below code need to comment to avoid crash in 2.x devices */

        if (mRecentView == null) {

            mRecentView = inflater.inflate(R.layout.fragment_recents,
                    container, false);


            mRecentListView = (ListView) mRecentView
                    .findViewById(R.id.recents_listview);
            mRecentDeleteImageView = (ImageView) mRecentView
                    .findViewById(R.id.recents_removecalllogs_img);
            mNoLogsFoundTextView = (TextView) mRecentView
                    .findViewById(R.id.norecents_found_tv);

            mRecentDeleteImageView.setOnClickListener(this);

            mPrefProvider = PreferenceProvider.getPrefInstance(mActivity.getApplicationContext());

            try {
                mCallLogsDB = new CallLogsDB(mActivity);
                new GetRecentsAssync().execute();
                setmContactsContentObserver();

            } catch (Exception e) {
                e.printStackTrace();
            }

            mActivity.registerReceiver(recentUpdateReceiver, new IntentFilter(Home.packageName + ".RECENTUPDATE"));

            // ListView on item click listener
            mRecentListView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {


                    try {

                        RecentsModel rmodel = mRecentArray.get(position);

                        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                            return;
                        }
                        mLastClickTime = SystemClock.elapsedRealtime();

                        // New changes to make reusable code.
                        int result = SipMethodHelper.makeCall(mActivity, mPrefProvider, mPrefProvider.getPrefInt("AccID"), rmodel.getNumber());

                        if (result == Constants.MAKE_CALL_ERROR_CODE)
                            return;

                        startCallingActivity((rmodel.getNumber()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            });
            //we got this issue(Add contact option is not displaying in pop up after contact deleted) so that comment this code

            mRecentListView.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> arg0,
                                               View view, int arg2, long arg3) {
                    try {
                        if (myAlertDialog != null && myAlertDialog.isShowing()) return false;
                        final String contactNumber = ((TextView)
                                view.findViewById(R.id.recents_number_tv)).getText()
                                .toString().trim();
                        //invokeCloseDialog("Are you sure you want to delete this call log?", "Single", contactNumber);


                        final RecentsModel cModel = mRecentArray.get(arg2);

                        boolean name_found = cModel.isIscontactfound();

                        CharSequence[] items = {"Delete This Call"};
                        CharSequence[] items_two = {"Delete This Call", "Add to Contacts"};
                        CharSequence[] items_call = null;
                        if (!name_found) {
                            items_call = new CharSequence[2];
                            items_call = items_two;
                        } else {
                            items_call = new CharSequence[1];
                            items_call = items;
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                getActivity());
                        builder.setTitle("Make your selection");
                        builder.setItems(items_call, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int item) {

                                switch (item) {

                                    case 0:

                                        mCallLogsDB.open();
                                        mCallLogsDB.deleteRow(contactNumber);
                                        mCallLogsDB.close();
                                        LoadUIRecents();

                                        break;

                                    case 1:
                                        // storing the unknown calllogs into native address book
                                        // . //mcp
                                        Intent in = new Intent(Intent.ACTION_INSERT);
                                        in.setType(ContactsContract.Contacts.CONTENT_TYPE);
                                        in.putExtra(ContactsContract.Intents.Insert.PHONE,
                                                contactNumber);
                                        startActivityForResult(in, SAVE_CALL);

                                        break;
                                }
                            }
                        });

                        // AlertDialog alert = builder.create();
                        // alert.show();
                        myAlertDialog = builder.create();
                        myAlertDialog.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            });


            // Adding the empty view to ListView for extra Space
            WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            int screenHeight = display.getHeight();
            View layout = inflater.inflate(R.layout.emptyview, mRecentListView, false);
            ViewGroup.LayoutParams lp = layout.getLayoutParams();
            lp.height = screenHeight / 4;
            layout.setLayoutParams(lp);
            mRecentListView.addFooterView(layout);

            getImageViewPosition();


        }

        return mRecentView;
    }


    private void getImageViewPosition() {
        /* Rect rt = mRecentDeleteImageView.getDrawable().getBounds();

         int left = rt.left;
         int top = rt.top;
         int right = rt.right;
         int bottom = rt.bottom;*/

        int[] location = new int[2];

        mRecentDeleteImageView.getLocationOnScreen(location);

        int left = location[0];
        int top = location[1];

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        this.mActivity = activity;
        Log.i("RecentFragment", "Called onAttach");

    }

    public void LoadUIRecents() {

        mRecentHelper = VXRecentsAll.getinstance();

        try {
            mRecentArray = mRecentHelper.GetCallLogs(mActivity
                    .getApplicationContext(), "All");

            mRecentAdapter = new RecentsAdapter(mActivity
                    .getApplicationContext(), mRecentArray);
            mRecentListView.setAdapter(mRecentAdapter);

            if (mRecentArray != null && mRecentArray.size() > 0) {
                mRecentDeleteImageView.setVisibility(View.VISIBLE);
                mNoLogsFoundTextView.setVisibility(View.VISIBLE);
            } else {
                mRecentDeleteImageView.setVisibility(View.GONE);
                mNoLogsFoundTextView.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        mRecentListView.setEmptyView(mNoLogsFoundTextView);

       /* mRecentListView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0,
                                           View view, int arg2, long arg3) {

                final String contactNumber = ((TextView)
                        view.findViewById(R.id.recents_number_tv)).getText()
                        .toString().trim();
                invokeCloseDialog("Are you sure you want to delete this call log?", "Single", contactNumber);

                return true;
            }
        });*/
    }

    @Override
    public void onResume() {
        super.onResume();

        View view = mActivity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        if (mRecentAdapter != null) {
            mRecentAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.recents_removecalllogs_img:

                Log.i("RecentFragment", "Delete Call logs click, mRecentFilterType: " + mRecentFilterType);
                if (mRecentFilterType.equals("All")) {
                    invokeCloseDialog(
                            "Are you sure you want to delete all call logs?", "All", "All");

                }


                break;

            default:
                break;
        }
    }

    BroadcastReceiver recentUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            mRecentFilterType = "All";

            //LoadUIRecents("All");
            Log.i("RecentFragment", "RecentActivity recentUpdateReceiver ");
            new GetRecentsAssync().execute();

        }
    };

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

            mActivity.runOnUiThread(new Runnable() {

                public void run() {

                    try {
                        LoadUIRecents();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                }
            });

        }

    }

    @Override
    public void onDestroy() {
        try {
            if (mContactsContentObserver != null) {
                mActivity.getApplicationContext().getContentResolver()
                        .unregisterContentObserver(mContactsContentObserver);
            }
            if (recentUpdateReceiver != null) {
                mActivity.unregisterReceiver(recentUpdateReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    // Recent adapter
    public class RecentsAdapter extends BaseAdapter {

        ArrayList<RecentsModel> recentsList;
        Context mcontext;
        SharedPreferences sharedpreferences;
        CallLogsDB db_callLogs;
        LayoutInflater inflater = null;

        public RecentsAdapter(Context context,
                              ArrayList<RecentsModel> recentsarray) {

            mcontext = context;
            recentsList = recentsarray;
            inflater = (LayoutInflater) mcontext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            sharedpreferences = mcontext.getSharedPreferences("MoSIP",
                    Context.MODE_PRIVATE);
            db_callLogs = new CallLogsDB(mcontext);

        }

        @Override
        public int getCount() {
            if (recentsList != null && recentsList.size() > 0) {
                return recentsList.size();
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
            TextView number_tv;
            TextView date_tv;
            TextView duration_tv;
            ImageView calltype_img;
            ImageView profilepic_img;
            ImageView info_img;
            LinearLayout ll_info_img;

        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            final Holder holder = new Holder();
            View rowView;

            rowView = inflater.inflate(R.layout.adapter_recents_item, null);
            holder.name_tv = (TextView) rowView
                    .findViewById(R.id.recents_name_textview);
            holder.number_tv = (TextView) rowView
                    .findViewById(R.id.recents_number_tv);
            holder.date_tv = (TextView) rowView
                    .findViewById(R.id.recents_date_tv);
            holder.duration_tv = (TextView) rowView
                    .findViewById(R.id.recents_duration_tv);

            holder.ll_info_img = (LinearLayout) rowView
                    .findViewById(R.id.ll_info_img);

            holder.calltype_img = (ImageView) rowView
                    .findViewById(R.id.recents_calltype_img);
            holder.calltype_img.setVisibility(View.GONE);
            holder.profilepic_img = (ImageView) rowView
                    .findViewById(R.id.recents_profilepic_imageview);

            holder.info_img = (ImageView) rowView.findViewById(R.id.info_img);

            final RecentsModel cModel = recentsList.get(position);

            String multicolortext = null;


            /*holder.recents_listitem_options_linear = (LinearLayout) rowView
                    .findViewById(R.id.recents_listitem_options_linear);*/

            if ((cModel.getCalltype()).equals(Constants.CALL_STATE_REJECTED)) {
                holder.calltype_img.setImageResource(R.drawable.reject);

                multicolortext = "<font color=#000000>" + " ("
                        + cModel.getCount() + ")" + "</font>";

            } else if ((cModel.getCalltype()).equals(Constants.CALL_STATE_MISSED_CALL)) {
                holder.calltype_img.setImageResource(R.drawable.missed);

                multicolortext = "<font color=#000000>" + " ("
                        + cModel.getCount() + ")" + "</font>";

            } else if ((cModel.getCalltype()).equals(Constants.CALL_STATE_OUTGOING)) {
                holder.calltype_img.setImageResource(R.drawable.outgoing);

                multicolortext = "<font color=#000000>" + " ("
                        + cModel.getCount() + ")" + "</font>";

            } else if ((cModel.getCalltype()).equals(Constants.CALL_STATE_IN_COMING)) {
                holder.calltype_img.setImageResource(R.drawable.incoming);

                multicolortext = "<font color=#000000>" + " ("
                        + cModel.getCount() + ")" + "</font>";

            }

            holder.name_tv.setSelected(true);
            String contact_name = ContactMethodHelper.getContactName(cModel.getNumber(),
                    getActivity());
            if (contact_name != null) {
                holder.name_tv.setText(Html.fromHtml(contact_name
                        + multicolortext));
            } else {

                holder.name_tv.setText(Html.fromHtml(cModel.getName()
                        + multicolortext));
            }
            boolean is24fromat = DateFormat.is24HourFormat(mActivity.getApplicationContext());

            SimpleDateFormat formatter = null;

            try {
                if (is24fromat) {
                    formatter = new SimpleDateFormat(
                            "dd/MM/yyyy HH:mm:ss");
                } else {

                    formatter = new SimpleDateFormat(
                            "dd/MM/yyyy hh:mm:ss a");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            //	Date date = null;
            try {

                if (cModel.getDate() != null) {
                    Calendar cal1 = Calendar.getInstance();
                    if (cal1 != null) {
                        cal1.setTimeInMillis(Long.parseLong(cModel.getDate()));

                        String dateString = formatter.format(cal1.getTime());
                        holder.date_tv.setText("" + dateString);
                    }


                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            try {
                holder.duration_tv.setText(cModel.getDuration());
            } catch (Exception e) {
                holder.duration_tv.setText(cModel.getDuration());
                e.printStackTrace();
            }
            holder.number_tv.setText(cModel.getNumber());


            Bitmap photo = Utils.getIMAGEPhoto(mcontext, holder.number_tv.getText().toString());
            if (photo != null) {

                holder.profilepic_img
                        .setImageBitmap(MethodHelper.getRoundedCornerBitmap(photo, 15));
                Log.i("RecentFragment", "image_pic");

            } else {
                holder.profilepic_img
                        .setImageResource(R.drawable.ic_contacts_avatar);
            }

            holder.ll_info_img
                    .setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent recents_details_intent = new Intent(
                                    mcontext, RecentDetailsActivity.class);
                            recents_details_intent
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            recents_details_intent.putExtra(
                                    "Recentslist_contactName", cModel.getName());
                            recents_details_intent.putExtra(
                                    "Recentslist_contactnumber",
                                    cModel.getNumber());
                            recents_details_intent.putExtra(
                                    "Recentslist_contactfound",
                                    cModel.isIscontactfound());

                            mcontext.startActivity(recents_details_intent);

                        }
                    });

            return rowView;
        }

    }

    /*// Popup
    public Bitmap getIMAGEPhoto(String phoneNumber) {

        Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Uri photoUri = null;
        ContentResolver cr = mActivity.getContentResolver();
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


    private void invokeCloseDialog(final String msg, final String calltype, final String contactNumber) {
        try {

            final Dialog dialog1 = new Dialog(mActivity);

            dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog1.setContentView(R.layout.dialog);
            dialog1.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));

            dialog1.show();

            TextView tv_title = (TextView) dialog1
                    .findViewById(R.id.tv_alert_title);
            Button yes = (Button) dialog1.findViewById(R.id.btn_alert_ok);
            Button no = (Button) dialog1.findViewById(R.id.btn_alert_cancel);

            tv_title.setText("" + msg);

            yes.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    dialog1.dismiss();

                    mCallLogsDB.open();

                    if (calltype.equals("Single")) {
                        mCallLogsDB.deleteRow(contactNumber);
                        mCallLogsDB.close();
                    } else {
                        if (calltype.equals("All")) {
                            mCallLogsDB.deleteAllRows();
                            mCallLogsDB.close();
                        } else {
                            mCallLogsDB.deleteMissed();
                            mCallLogsDB.close();
                        }
                    }

                    LoadUIRecents();

                }
            });
            no.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog1.dismiss();

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * This method calls calling activity
     *
     * @param phoneNumber mobile number
     */
    private void startCallingActivity(final String phoneNumber) {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {

                mPrefProvider.setPrefBoolean("incallspeaker", false);
                mPrefProvider.setPrefBoolean("speakerEnabled", false);
                mPrefProvider.setPrefBoolean("incallmute", false);

                Intent inCallIntent = new Intent(mActivity.getApplicationContext(), InCallCardActivity.class);
                inCallIntent.putExtra("ISCall", "outgoing");
                inCallIntent.putExtra("ContactNum", "" + phoneNumber);
                inCallIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(inCallIntent);

            }
        });

    }

    // Get Native Contacts AsyncTask
    private class GetRecentsAssync extends AsyncTask<Void, Void, ArrayList<RecentsModel>> {

        @Override
        protected ArrayList<RecentsModel> doInBackground(Void... arg0) {

            mRecentHelper = VXRecentsAll.getinstance();

            try {
                mRecentArray = mRecentHelper.GetCallLogs(mActivity
                        .getApplicationContext(), "All");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return mRecentArray;

        }


        @Override
        protected void onPostExecute(ArrayList<RecentsModel> recentarray) {


            try {
                mRecentAdapter = new RecentsAdapter(mActivity
                        .getApplicationContext(), mRecentArray);
                mRecentListView.setAdapter(mRecentAdapter);
                Log.i("RecentFragment", "RecentActivity listview updated");

                if (mRecentArray != null && mRecentArray.size() > 0) {
                    mRecentDeleteImageView.setVisibility(View.VISIBLE);
                    mNoLogsFoundTextView.setVisibility(View.VISIBLE);
                } else {
                    mRecentDeleteImageView.setVisibility(View.GONE);
                    mNoLogsFoundTextView.setVisibility(View.GONE);
                }

                mRecentListView.setEmptyView(mNoLogsFoundTextView);
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }
    }

    public void setmContactsContentObserver() {

        try {

            if (PermissionUtils.hasPermissions(mActivity, PermissionUtils.CONTACTSPERMISSION)) {
                mContactsContentObserver = new MyContentObserver();
                if (mContactsContentObserver != null) {
                    mActivity
                            .getApplicationContext()
                            .getContentResolver()
                            .registerContentObserver(ContactsContract.Contacts.CONTENT_URI,
                                    true, mContactsContentObserver);
                    Home.isContactObserverRegistered = true;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

}
