/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.ui;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import com.github.siyamed.shapeimageview.BuildConfig;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.app.dvoip.R;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.vx.core.android.asynctask.AppVersionCheckAsyncTask;
import com.vx.core.android.db.AccountsDB;
import com.vx.core.android.db.DataBaseHelper;
import com.vx.core.android.db.OPXMLDAO;
import com.vx.core.android.getaccounts.ProfileData;
import com.vx.core.android.service.NotificationService;
import com.vx.core.android.service.SIPService;
import com.vx.core.android.utils.MethodHelper;
import com.vx.core.jni.SipManager;
import com.vx.ui.adapters.ViewPagerAdapter;
import com.vx.ui.contacts.ContactsFragment;
import com.vx.ui.dialpad.DialerFragment;
import com.vx.ui.recents.RecentFragment;
import com.vx.utils.Constants;
import com.vx.utils.DataParser;
import com.vx.utils.PermissionUtils;
import com.vx.utils.PreferenceProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import vx.plt.SWIGTYPE_p__VX_ERROR;

/**
 * This class shows the main screen of the application contains all fragment tabs.
 * Here we initialise SIP registration.
 */
public class Home extends AppCompatActivity {
    private static final String TAG = "Home";

    public static ViewPager mViewPager;
    private ImageView mSettingsButton;

    private SWIGTYPE_p__VX_ERROR mSwigPointerInstance;
    //private int mJNIInitStatus = Constants.PJSIP_RETURN_STATUS_DEFAULT;

    static PreferenceProvider preferenceProvider;
    private Dialog mPopupDialog;

    static AccountsDB accountsDB;
    static ProfileData profiledata;

    private static int accId = Constants.PJSIP_RETURN_STATUS_DEFAULT;
    public static String packageName = "";
    private String mRemoteContactNumber = "";
    private int mRemoteContactDuration = 0;
    private int mDBAccId;
    private int mCallState;
    private boolean isCallDisconnectRequested = false;

    public static TabLayout mTabLayout;
    private ContactsFragment mContactFragment;
    private RecentFragment mRecentFragment;
    private DialerFragment mDialpadFragment;
    private SettingsFragment mSettingsFragment;
    private InputMethodManager mInputMethodManager;
    public static boolean isContactObserverRegistered = false;
    public static boolean isContactsLoaded = false;

    private final int mIntervalToSendRegRequest = 5000; // 30 Seconds
    private Handler mRegRequestHandler = new Handler();

    private Runnable mRegisterRequestHandle = new Runnable() {
        public void run() {
            Log.i(TAG, "mRegisterRequestHandle Called is NetworkAvailable " + MethodHelper.isNetworkAvailable(getApplicationContext()));
            if (MethodHelper.isNetworkAvailable(getApplicationContext())) {
                sendRegisterRequest();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Called home onCreate");
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_home);

        checkDevicePermissions();

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.i(TAG,"Ad mobs got initialised: "+initializationStatus.toString());
            }
        });

        packageName = getApplicationContext().getPackageName();
        preferenceProvider = PreferenceProvider.getPrefInstance(getApplicationContext());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mSettingsButton = (ImageView) findViewById(R.id.settings_btn);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);

        mSwigPointerInstance = SipManager.getSwigPointerInstance();
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mContactFragment = new ContactsFragment();
        mRecentFragment = new RecentFragment();
        mDialpadFragment = new DialerFragment();
        mSettingsFragment = new SettingsFragment();

        // Set the ViewPagerAdapter into ViewPager

        setupViewPager(mViewPager);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setSelectedTabIndicatorHeight(0);
        mDBAccId = preferenceProvider.getPrefInt("AccountID");
        updatetabs(1);


        accountsDB = new AccountsDB(this);
        accountsDB.open();
        profiledata = accountsDB.getAccount(1);
        accountsDB.close();


        IntentFilter filIntentFilter2 = new IntentFilter(Home.packageName + ".alertReceiver");
        registerReceiver(alertReceiver, filIntentFilter2);

        registerReceiver(userFeedBackreceiver, new IntentFilter(Home.packageName + ".SendFeedback"));

        /*Thread t = new Thread("StartSip") {
            public void run() {
                Intent serviceIntent = new Intent(Home.this, RecentCloseService.class);
                startService(serviceIntent);
            }
        };
        t.start();*/

        //ringer = new Ringer(this);


        MethodHelper.startSIPService(Home.this);

        /*Log.i(TAG, "Invoke Home.onResume() int ");
        try {
            if (mJNIInitStatus == Constants.PJSIP_RETURN_STATUS_DEFAULT) {
                int status = initializeJNI();
                if (status == 0) {
                    register(mDBAccId);
                } else {
                    // TODO - ???
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    // TODO - onPageScrolled
                }

                @Override
                public void onPageSelected(int position) {
                    if (position == 0) {
                        // Clears the search text in contacts fragment
                       /* if (mViewpagerAdapter != null && mViewpagerAdapter.contactsFragment != null)
                            mViewpagerAdapter.contactsFragment.clearSearchText();*/
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    // Todo- onPageScrollStateChanged
                }
            });
        }

        mSettingsButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent login_intent = new Intent(Home.this
                        .getApplicationContext(), LoginActivity.class);
                preferenceProvider.setPrefBoolean("settingslogin", true);
                startActivity(login_intent);
            }
        });

        // App version check for auto update
        // versionCheck();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {

                    Log.i(TAG, "Crrent Position" + position);

                    updatetabs(position);
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    // Todo- onPageScrollStateChanged
                }
            });
        }

    }


    private void setupViewPager(ViewPager viewPager) {
        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(mRecentFragment, "");
        adapter.addFragment(mDialpadFragment, "");
        adapter.addFragment(mContactFragment, "");
        adapter.addFragment(mSettingsFragment, "");
        viewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(4);
        // Set the ViewPagerAdapter into ViewPager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            //mViewPager.setAdapter(mViewpagerAdapter);
            mViewPager.setCurrentItem(1);


        }
    }

    private void updatetabs(int currenttab) {
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setSelectedTabIndicatorHeight(0);

        try {
            if (mInputMethodManager != null && ContactsFragment.mContactsSearchBarEditText != null) {
                mInputMethodManager.hideSoftInputFromWindow(
                        ContactsFragment.mContactsSearchBarEditText.getWindowToken(), 0);
            }
            if (ContactsFragment.mContactsSearchBarEditText != null && !ContactsFragment.mContactsSearchBarEditText.getText().toString().isEmpty()) {
                ContactsFragment.mContactsSearchBarEditText.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        switch (currenttab) {

            case 0:
                preferenceProvider.setPrefString("navigationScreen", "0");
                mTabLayout.getTabAt(0).setCustomView(customRecentsView("Recents", R.drawable.ic_recents_hover));
                mTabLayout.getTabAt(1).setCustomView(customNumberView("Numpad", R.drawable.ic_numpad_normal));
                mTabLayout.getTabAt(2).setCustomView(customContactsView("Contacts", R.drawable.ic_contacts_normal));
                mTabLayout.getTabAt(3).setCustomView(customMoreView("Settings", R.drawable.ic_settings_normal));

                try {
                    if (!isContactObserverRegistered)
                        mRecentFragment.setmContactsContentObserver();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;

            case 1:

                preferenceProvider.setPrefString("navigationScreen", "1");
                mTabLayout.getTabAt(0).setCustomView(customRecentsView("Recents", R.drawable.ic_recents_normal));
                mTabLayout.getTabAt(1).setCustomView(customNumberView("Numpad", R.drawable.ic_numpad_hover));
                mTabLayout.getTabAt(2).setCustomView(customContactsView("Contacts", R.drawable.ic_contacts_normal));
                mTabLayout.getTabAt(3).setCustomView(customMoreView("Settings", R.drawable.ic_settings_normal));
                break;

            case 2:

                preferenceProvider.setPrefString("navigationScreen", "2");
                mTabLayout.getTabAt(0).setCustomView(customRecentsView("Recents", R.drawable.ic_recents_normal));
                mTabLayout.getTabAt(1).setCustomView(customNumberView("Numpad", R.drawable.ic_numpad_normal));
                mTabLayout.getTabAt(2).setCustomView(customContactsView("Contacts", R.drawable.ic_contacts_hover));
                mTabLayout.getTabAt(3).setCustomView(customMoreView("Settings", R.drawable.ic_settings_normal));

                try {
                    if (!isContactObserverRegistered)
                        mContactFragment.setmMyContentObserver();

                    Log.i(TAG, "Tab changed called and isContactLoaded: "+isContactsLoaded);

                    if (!isContactsLoaded)
                        mContactFragment.loadAllContacts();

                } catch (Exception e) {
                    e.printStackTrace();
                }


                break;

            case 3:

                mTabLayout.getTabAt(0).setCustomView(customRecentsView("Recents", R.drawable.ic_recents_normal));
                mTabLayout.getTabAt(1).setCustomView(customNumberView("Numpad", R.drawable.ic_numpad_normal));
                mTabLayout.getTabAt(2).setCustomView(customContactsView("Contacts", R.drawable.ic_contacts_normal));
                mTabLayout.getTabAt(3).setCustomView(customMoreView("Settings", R.drawable.ic_settings_hover));
                try {

                    if (mInputMethodManager != null && ContactsFragment.mContactsSearchBarEditText != null) {
                        mInputMethodManager.hideSoftInputFromWindow(
                                ContactsFragment.mContactsSearchBarEditText.getWindowToken(), 0);
                    }
                    mSettingsFragment.showpwd_check.setButtonDrawable(R.drawable.ic_password_show);
                    mSettingsFragment.m_login_password_edt
                            .setInputType(129);
                    mSettingsFragment.updateData();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private View customRecentsView(String tabName, int tabDrawable) {
        View view = customUpdateView(tabName, tabDrawable);

        return view;

    }

    private View customNumberView(String tabName, int tabDrawable) {
        View view = customUpdateView(tabName, tabDrawable);

        return view;

    }

    private View customContactsView(String tabName, int tabDrawable) {
        View view = customUpdateView(tabName, tabDrawable);

        return view;

    }

    private View customMoreView(String tabName, int tabDrawable) {
        View view = customUpdateView(tabName, tabDrawable);

        return view;

    }

    private View customUpdateView(String tabName, int tabDrawable) {

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.tab_custom,
                null, false);

        TextView tv_tab = (TextView) view.findViewById(R.id.tabTitleText);
        //tv_tab.setText(tabName);
        final Drawable drawable_tab = getResources().getDrawable(tabDrawable);
        tv_tab.setBackgroundDrawable(drawable_tab);
        return view;
    }

    /**
     * This method will checks version check for app update.
     */
    private void versionCheck() {

        long diffHours = 7;
        String previousTimeStamp = preferenceProvider.getPrefString("appUpdateTimestamp");

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String currentTime = format.format(calendar.getTime());

        Log.i(TAG, "Previous Time: " + previousTimeStamp);
        if (previousTimeStamp != null && previousTimeStamp.length() > 0) {
            Date d1 = null;
            Date d2 = null;
            try {
                d1 = format.parse(currentTime);
                d2 = format.parse(previousTimeStamp);
                //in milliseconds
                long diff = d1.getTime() - d2.getTime();
                diffHours = diff / (60 * 60 * 1000) % 24;
                Log.i(TAG, "Hours difference: " + diffHours);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (MethodHelper.isNetworkAvailable(Home.this)) {
            // minimum time difference is 6 hours for Remind later.
            if (diffHours > 6) {
                new AppVersionCheckAsyncTask(Home.this, BuildConfig.VERSION_NAME).execute();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i(TAG, "Called home onResume and isContactLoaded: "+isContactsLoaded);

        Intent intent = new Intent(this, NotificationService.class);
        startService(intent);

        if (!MethodHelper.isNetworkAvailable(getApplicationContext())) {
            Log.i(TAG, "network not available called thread");
            mRegRequestHandler.postDelayed(mRegisterRequestHandle, mIntervalToSendRegRequest);
            Log.i(TAG, "Home onResume after thread is network available " + MethodHelper.isNetworkAvailable(Home.this));
        } else {
            if (mDialpadFragment != null) {
                sendRegisterRequest();
            }
        }

        if (MethodHelper.isNetworkAvailable(Home.this)) {
            mDBAccId = preferenceProvider.getPrefInt("AccountID");

            if (preferenceProvider.getPrefBoolean("settingslogin")) {
                Log.i(TAG, "Home.onResume() settings login ");
                preferenceProvider.setPrefBoolean("settingslogin", false);

                try {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                                if (mViewPager != null) {
                                    mViewPager.setCurrentItem(1);
                                }
                            }
                        }
                    }, 200);


                    /*boolean jni_app_status = false;
                    boolean IsAppInitialized = false;
                    int status = 0;
                    try {
                        jni_app_status = VoxEngine.JNI_VX_IsAppInitialized(p__VX_ERROR);
                    } catch (Throwable e) {
                        status = initializeJNI();

                        if (status == 0) {
                            register(mDBAccId);
                        }
                        IsAppInitialized = true;
                    }

                    if (jni_app_status && !IsAppInitialized) {
                        VoxEngine.JNI_VX_UnRegisterAccount(mDBAccId, p__VX_ERROR);
                        Log.i(TAG, "unregister status=" + status);
                        register(mDBAccId);
                    }*/

                    /*if (mJNIInitStatus == Constants.PJSIP_RETURN_STATUS_SUCCESS) {
                        VoxEngine.JNI_VX_UnRegisterAccount(mDBAccId, mSwigPointerInstance);
                        Log.i(TAG, "unregister status=" + mJNIInitStatus);
                        register(mDBAccId);
                    } else {
                        initializeJNI();
                        if (mJNIInitStatus == Constants.PJSIP_RETURN_STATUS_SUCCESS) {
                            register(mDBAccId);
                        }
                    }*/
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

    }


    @Override
    public void onDestroy() {
        Log.i(TAG, "Home.onDestroy()");
        try {
            if (alertReceiver != null) {
                unregisterReceiver(alertReceiver);
            }
            if (userFeedBackreceiver != null) {
                unregisterReceiver(userFeedBackreceiver);
            }
            Log.i(TAG, "Unregistered alertReceiver");
            // if (Constants.IS_MAKECALL_CALLED) {

            Log.i(TAG, "after call log update");
            //   }
            Intent intent = new Intent(Home.this, NotificationService.class);
            stopService(intent);
            //Intent closeServiceIntent = new Intent(Home.this, RecentCloseService.class);
            //stopService(closeServiceIntent);
            shutdown();
            stopService(new Intent(Home.this, SIPService.class));
            Log.i(TAG, "Stop SIPService() called");
        } catch (Throwable e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    protected void shutdown() {
        try {
            accId = Constants.PJSIP_RETURN_STATUS_DEFAULT;
            Log.i(TAG, "Invoke shutdown");

            NotificationService obj = NotificationService.getInstance();
            if (obj != null)
                obj.cancelAll();

            preferenceProvider.setPrefString("Registration", "Registering...");

            int status = 0;
            /*if (mJNIInitStatus == Constants.PJSIP_RETURN_STATUS_SUCCESS) {
                status = VoxEngine.JNI_VX_ShutdownApp(mSwigPointerInstance);
                Log.i(TAG, "shutdown" + status);
            }*/
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        /** Below code avoids back button functionality and shows below pop up. **/
        // super.onBackPressed();
        invokeCloseDialog("Are you sure want to exit?");
    }

    private void invokeCloseDialog(final String msg) {
        if (mPopupDialog == null) {
            try {
                mPopupDialog = new Dialog(Home.this);
                mPopupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mPopupDialog.setContentView(R.layout.dialog);
                mPopupDialog.setCancelable(false);
                mPopupDialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));

                TextView tvAlertMessage = (TextView) mPopupDialog
                        .findViewById(R.id.tv_alert_title);

                Button yes = (Button) mPopupDialog
                        .findViewById(R.id.btn_alert_ok);
                Button no = (Button) mPopupDialog
                        .findViewById(R.id.btn_alert_cancel);

                tvAlertMessage.setText("" + msg);
                yes.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mPopupDialog != null)
                            mPopupDialog.dismiss();

                        mPopupDialog = null;

                        finishAffinity();

                    }
                });
                no.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mPopupDialog != null)
                            mPopupDialog.dismiss();

                        mPopupDialog = null;
                    }
                });

                if (mPopupDialog != null)
                    mPopupDialog.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    BroadcastReceiver alertReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String st = arg1.getExtras().getString("status");
            boolean isopxmlUpdate = arg1.getExtras().getBoolean("opxmlupdate");

            int status = Integer.parseInt(st);
            showAlert(status, isopxmlUpdate);
        }
    };

    private void showAlert(int status, boolean isopxmlUpdate) {
        String msg = "Something went wrong. Please try again.";

        switch (status) {
            case DataParser.OPXML_WRONG_BRAND_PIN:
                msg = "Wrong BrandPin";
                break;
            case DataParser.OPXML_INACTIVE_BRAND_PIN:
                msg = "Inactive BrandPin";
                break;
            case DataParser.OPXML_SERVER_ERROR:
                msg = "Something went wrong. Please try again.";
                break;
        }

        if (status != DataParser.OPXML_SUCCESS) {
            try {
                if (preferenceProvider != null)
                    preferenceProvider.setPrefString("Registration", msg);
                if (DialerFragment.registrationStatusTextView != null) {
                    DialerFragment.registrationStatusTextView
                            .setText(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                mPopupDialog = new Dialog(this);
                mPopupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mPopupDialog.setContentView(R.layout.dialog);
                mPopupDialog.setCancelable(false);
                mPopupDialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));

                TextView tv_title = (TextView) mPopupDialog
                        .findViewById(R.id.tv_alert_title);

                Button yes = (Button) mPopupDialog
                        .findViewById(R.id.btn_alert_ok);
                Button no = (Button) mPopupDialog
                        .findViewById(R.id.btn_alert_cancel);

                tv_title.setText("" + msg);

                no.setVisibility(View.GONE);

                yes.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mPopupDialog != null)
                            mPopupDialog.dismiss();
                        mPopupDialog = null;
                        finish();
                    }
                });

                no.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mPopupDialog != null)
                            mPopupDialog.dismiss();
                        mPopupDialog = null;

                    }
                });

                if (mPopupDialog != null)
                    mPopupDialog.show();

            } catch (Exception e) {
                e.printStackTrace();

            }

        } else {
            if (isopxmlUpdate) {
                try {
                    if (preferenceProvider != null)
                        preferenceProvider.setPrefBoolean("isbalancehit", true);

                    OPXMLDAO opxmldao = new OPXMLDAO(getApplicationContext());
                    opxmldao.getProvisionRecords(DataBaseHelper.PROVISION_BASE_TABLE);
                    HashMap<String, String> provisionBaseInfo = opxmldao
                            .getProvisionBaseInfo();
                    opxmldao.close();
                    if (provisionBaseInfo != null) {
                        DataParser.setProvisionBaseInfo(provisionBaseInfo);
                    }

                    mDBAccId = preferenceProvider.getPrefInt("AccountID");

                    /*if (mJNIInitStatus == Constants.PJSIP_RETURN_STATUS_DEFAULT)
                        return;

                    VoxEngine.JNI_VX_UnRegisterAccount(mDBAccId, mSwigPointerInstance);
                    register(mDBAccId);*/

                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Check the network states
    public class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                preferenceProvider.setPrefBoolean("isNetwork", true);
            } else {
                preferenceProvider.setPrefBoolean("isNetwork", false);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        boolean isNotificationMissed = preferenceProvider
                .getPrefBoolean("NotificationMsgMissed");

        String isNotification = null;
        if (intent != null) {
            isNotification = intent.getStringExtra("IsNotification");
        }
        Log.i(TAG, "Home.onNewIntent(): " + isNotificationMissed);

        if (isNotificationMissed && isNotification != null && isNotification.equalsIgnoreCase("Missed")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    preferenceProvider.setPrefBoolean("NotificationMsgMissed",
                            false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                        if (mViewPager != null)
                            mViewPager.setCurrentItem(2);
                    }
                }

            }, 100);
        }
    }

    private BroadcastReceiver userFeedBackreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           /* String action = intent.getAction();
            if (action.equals(Home.packageName + ".SendFeedback")) {

                String RTPDumpStatus = intent.getStringExtra("RTPDumpStatus");
                showDialogForFeedback(RTPDumpStatus);
            }*/
        }
    };

    /**
     * This Method will show the dialog to user to give ratting and comments
     * If user click submit button will Call API to send User feedback to Control panel
     * If user click Not Now button will simply close the dialog
     *
     * @param RTPDumpStatus
     */

    private void showDialogForFeedback(final String RTPDumpStatus) {
        final Dialog userFeedBackDialog = new Dialog(Home.this);
        userFeedBackDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        userFeedBackDialog.setContentView(R.layout.user_feedback_dialog);
        userFeedBackDialog.setCancelable(false);
        userFeedBackDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        final EditText commentBoxEdt = (EditText) userFeedBackDialog.findViewById(R.id.comment_box_edt);
        final RatingBar starsRatingBar = (RatingBar) userFeedBackDialog.findViewById(R.id.ratingBar);
        Button ignoreButton = (Button) userFeedBackDialog.findViewById(R.id.ignore_btn);
        Button submitButton = (Button) userFeedBackDialog.findViewById(R.id.submit_feedback_btn);
        ignoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userFeedBackDialog.dismiss();
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder feedBackString = new StringBuilder();
                feedBackString.append(String.valueOf(starsRatingBar.getRating()).charAt(0) + "|");
                feedBackString.append(preferenceProvider.getPrefString("login_brandpin") + "|");
                feedBackString.append(preferenceProvider.getPrefString("login_username") + "|");
                feedBackString.append(MethodHelper.getTheDataForFeedback(Home.this));
                feedBackString.append(commentBoxEdt.getText().toString().trim() + "|");
                feedBackString.append(RTPDumpStatus);
                Log.i(TAG, "USer FeedBack " + feedBackString.toString());
                new sendUserFeedBackToServer(feedBackString.toString()).execute();
                userFeedBackDialog.dismiss();
            }
        });
        userFeedBackDialog.show();
    }

    /**
     * This broadcast receiver will receive the data from {@link com.vx.core.jni.SIPCallBacks } Class after call disconnects
     */
    private class sendUserFeedBackToServer extends AsyncTask<Void, Void, Void> {
        String feedBackString = "";

        public sendUserFeedBackToServer(String feedBackString) {
            this.feedBackString = feedBackString;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            DataParser.sendFeedBack("104.156.58.150;3023-3025", feedBackString, getApplicationContext());

            return null;
        }
    }

    private void sendRegisterRequest() {

        if (mRegRequestHandler != null)
            mRegRequestHandler.removeCallbacks(mRegisterRequestHandle);
        String regStatus = preferenceProvider.getPrefString("Registration");
        Log.i(TAG, "sendRegisterRequest called reg status is " + regStatus);

        if (regStatus.equals("Please check your internet connection")
                || regStatus.equals("Not Registered.") ||
                !MethodHelper.isGivenServiceRunning(getApplicationContext(), SIPService.class)) {
            if (mDialpadFragment.registrationStatusTextView != null
                    && preferenceProvider.getPrefBoolean("isWrongOrInactiveBrandPin") == true) {
                Log.i(TAG, "entered into if statement");
                mDialpadFragment.registrationStatusTextView.setText(preferenceProvider.getPrefString("WrongOrInactiveBrandPin"));
            } else {
                preferenceProvider.setPrefBoolean("isbalancehit", true);
                MethodHelper.stopAndStartSIPService(getApplicationContext());
            }

        } else {
            if (regStatus.equals("Registered") && mDialpadFragment.balanceTextView != null && (mDialpadFragment.balanceTextView.getVisibility() == View.INVISIBLE ||
                    mDialpadFragment.balanceTextView.getVisibility() == View.GONE ||
                    mDialpadFragment.balanceTextView.getText().toString().trim().length() == 0)) {

                new Thread() {
                    public void run() {
                        String usr = preferenceProvider.getPrefString("login_username");
                        String pwd = preferenceProvider.getPrefString("login_password");
                        Log.i(TAG, "Calling getBalance method ");
                        final String balValue = DataParser.getBalance(preferenceProvider.getPrefString(PreferenceProvider.BALANCE_URL), usr, pwd,Home.this
                        );
                        preferenceProvider.setPrefString(PreferenceProvider.BALANCE_VALUE, balValue);
                        mDialpadFragment.balanceTextView.post(new Runnable() {

                            @Override
                            public void run() {
                                Log.i(TAG, "Bal:registrationStatusReceiver" + balValue);
                                if (balValue != null && balValue.length() > 0) {
                                    mDialpadFragment.balanceTextView.setVisibility(View.VISIBLE);
                                    mDialpadFragment.balanceTextView.setText("Bal: $ " + balValue);
                                } else {
                                    mDialpadFragment.balanceTextView.setVisibility(View.INVISIBLE);
                                    mDialpadFragment.balanceTextView.setText("");
                                }

                            }
                        });
                    }
                }.start();
            }
        }
    }

    private void checkDevicePermissions() {
        // If OS version is more than Lollipop we ask all below permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "hasPermissions: " + PermissionUtils.hasPermissions(Home.this, PermissionUtils.PERMISSIONS));
            if (!PermissionUtils.hasPermissions(Home.this, PermissionUtils.PERMISSIONS))
                PermissionUtils.requestForAllPermission(Home.this);
        }

    }


}
