/**
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 * <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 */

package com.vx.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.siyamed.shapeimageview.BuildConfig;
import com.vx.utils.GifView;
import com.app.dvoip.R;
import com.vx.core.android.db.AccountsDB;
import com.vx.core.android.db.DataBaseHelper;
import com.vx.core.android.db.OPXMLDAO;
import com.vx.core.android.service.OpxmlService;
import com.vx.core.android.utils.MethodHelper;
import com.vx.utils.Config;
import com.vx.utils.DataParser;
import com.vx.utils.PreferenceProvider;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class shows the splash screen of the application from here will decide whether to go login or home screen.
 *
 * @author Ramesh Reddy
 */
public class SplashActivity extends Activity {

    // Splash screen timer
    private static final int SPLASH_TIME_OUT = 400;
    public static final String LIB_FILENAME = "pjsua";
    public static String userSettings_httpLink = Config.PRIMARY_OPXML_URL;
    public static boolean isregisterhit;
    public static String versionNum = "";
    public static String deviceName, deviceType;
    public boolean isTab;
    private TextView mVersionTextView;
    private PreferenceProvider mPrefs;
    private Dialog mPopupDialog;
    private AccountsDB mAccountDB;
    private SharedPreferences sp;

    private String mAppVersion = "";
    private int mCurrentVerCode;
    private int mOldVerCode;
    private int mStatus;
    boolean mIsBackPress, mStatusReceived;
    AccountsDB sipdb;
    public static boolean islogin = false;

    public static boolean isloadcontacts = true;
    private GifView pg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        try {
            if (!isTaskRoot()) {
                finish();
                return;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        isregisterhit = true;
        setContentView(R.layout.activity_splash);
        mVersionTextView = (TextView) findViewById(R.id.spversion);
        mPrefs = PreferenceProvider.getPrefInstance(getApplicationContext());
        sipdb = new AccountsDB(this);
        mAccountDB = new AccountsDB(this);
        pg = (GifView) findViewById(R.id.viewGif);
        pg.setImageResource(R.drawable.spalsh_gif);
        final boolean islogin = mPrefs.getPrefBoolean("islogin");
        mPrefs.setPrefBoolean("isbalancehit", true);

        try {
            isloadcontacts = true;

            PackageInfo pinfo = getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            versionNum = pinfo.versionName;

            deviceName = getDeviceName();

            isTab = isTablet(getApplicationContext());

            if (isTab) {

                deviceType = "TABLET";

            } else {
                deviceType = "PHONE";
            }

        } catch (Exception e) {

            versionNum = "1.0.0";

        }

        try {
            IntentFilter networkCheckIntentFilter = new IntentFilter();
            networkCheckIntentFilter.addAction(Home.packageName
                    + ".NetworkStatus");
            registerReceiver(networkReceiver, networkCheckIntentFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int libStatus = initLib();

        if (libStatus != 0) {
            Toast.makeText(getApplicationContext(), "libraries are not loaded",
                    Toast.LENGTH_SHORT).show();

            /** If library not loaded, Need to close the activity **/
            finish();

            return;
        }


        sp = getApplicationContext().getSharedPreferences("opxmllink",
                MODE_PRIVATE);
        String OPXMLLink = sp.getString("opxmllink", "");

        try {
            mAppVersion = BuildConfig.VERSION_NAME;
            mOldVerCode = mPrefs.getPrefInt("verCode");

            if (mOldVerCode == 0) {
                mPrefs.setPrefInt("verCode", BuildConfig.VERSION_CODE);
            }
            mCurrentVerCode = BuildConfig.VERSION_CODE;

            mVersionTextView.setText("Version: " + mAppVersion);

            if (mCurrentVerCode > mOldVerCode) {
                mPrefs.setPrefInt("verCode", BuildConfig.VERSION_CODE);
                userSettings_httpLink = Config.PRIMARY_OPXML_URL;
                SharedPreferences.Editor save_link = sp.edit();
                save_link.putString("opxmllink", userSettings_httpLink);
                Log.i("SplashActivity", "length zero or http:"
                        + userSettings_httpLink);
                save_link.commit();

                OPXMLDAO opxmldao = new OPXMLDAO(getApplicationContext());
                ArrayList<HashMap<String, String>> records = opxmldao
                        .getProvisionRecords(DataBaseHelper.PROVISION_BASE_TABLE);
                if (records.size() != 0) {
                    opxmldao.delete(DataBaseHelper.PROVISION_BASE_TABLE, null, null);
                }
                opxmldao.close();
            }

            userSettings_httpLink = sp.getString("opxmllink", "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (OPXMLLink == null || OPXMLLink.length() == 0) {
            SharedPreferences.Editor save_link = sp.edit();
            save_link.putString("opxmllink", userSettings_httpLink);
            save_link.commit();
        }

        if (!MethodHelper.isNetworkAvailable(SplashActivity.this)) {
            mStatusReceived = true;
            invokeCloseDialog("Please check your internet connection.");

            return;
        }

        new Handler().postDelayed(new Runnable() {

			/*
             * Showing splash screen with a timer. This will be useful when you
			 * want to show case your app logo / company
			 */

            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity

                if (islogin) {
                    try {
                        new GetOPXMLData().execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (mIsBackPress == false && !mStatusReceived) {
                        mStatusReceived = false;

                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        if (intent != null) {
                            mPrefs.setPrefBoolean("settingslogin", false);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
                // close this activity
            }
        }, SPLASH_TIME_OUT);
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    // get the device name
    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }


    @Override
    public void onBackPressed() {
        mIsBackPress = true;
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(networkReceiver);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /**
     * This method loads the JNI libraries.
     *
     * @return status
     */
    private int initLib() {
        // Load pjsua for audio calling
        try {
            System.loadLibrary(LIB_FILENAME);
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            return -1;
        }

        return 0;
    }

    /**
     * This class gets the OPXML data from web service.
     */
    private class GetOPXMLData extends AsyncTask<Void, Void, Void> {
        String username;
        String password;
        String ip;
        String portRange;
        String brandpin;
        public int status;

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                username = mPrefs.getPrefString("login_username");
                password = mPrefs.getPrefString("login_password");

                OPXMLDAO opxmldao = new OPXMLDAO(getApplicationContext());

                ArrayList<HashMap<String, String>> records = opxmldao
                        .getProvisionRecords(DataBaseHelper.PROVISION_BASE_TABLE);
                int size = records.size();

                if (size == 0) {
                    opxmldao.close();

                    try {

                        boolean ishitsuccess = false;
                        out:
                        for (int i = 0; i < 3; ++i) {



                            ishitsuccess = false;

                            String username = mPrefs.getPrefString("login_username");
                            String password = mPrefs.getPrefString("login_password");

                            // status =
                            // DataParser.GetstaticData(username,password,sipdb,prefs);



                                // this.brandpin=brandpin;
                                // String[]
                                // ipPorts=SplashActivity.iplist.get(j).toString().split(":");
                                ip = Config.PRIMARY_OPXML_URL;// ipPorts[0];
                                portRange = "";// ipPorts[1];
                                mStatus = DataParser.setOpxmlUDPNew(ip, portRange,
                                        mPrefs.getPrefString("login_brandpin"),SplashActivity.this);
                                if (mStatus == 5) {
                                    DataParser.setstaticData(username, password, sipdb,
                                            mPrefs);
                                }
                                if (mStatus != 3 && mStatus != 0) {
                                    ishitsuccess = true;
                                    break out;
                                }


                        }

                    } catch (Throwable e) {
                        System.out.println("Exception in Reading the Opxml UserInfo");
                        if (mStatus == DataParser.UNKNOWN_HOST_EXCEPTION) {
                            return null;
                        }
                    }
                   /* mStatus = DataParser.setOpxmlUDP(
                            Config.PRIMARY_OPXML_URL, "",
                            mPrefs.getPrefString("login_brandpin"),
                            getApplicationContext());

                    // Don't go to fail over conditions to avoid delay, This logic suggested by Raghava
                    if (mStatus == DataParser.UNKNOWN_HOST_EXCEPTION) {
                        return null;
                    }*/
                } else {
                    mStatus = DataParser.OPXML_SUCCESS;

                    opxmldao.getProvisionRecords(DataBaseHelper.PROVISION_BASE_TABLE);
                    HashMap<String, String> provisionBaseInfo = opxmldao
                            .getProvisionBaseInfo();
                    opxmldao.close();
                    if (provisionBaseInfo != null) {
                        DataParser.setProvisionBaseInfo(provisionBaseInfo);
                    }

                    Intent intent = new Intent(SplashActivity.this,
                            OpxmlService.class);
                    startService(intent);
                }

               /* if (mStatus == DataParser.OPXML_SERVER_ERROR) {
                    try {
                        mStatus = getFailOverStatus(Config.SECONDARY_OPXML_URL); // Added by Ramesh
                        if (mStatus == DataParser.OPXML_SERVER_ERROR) {
                            mStatus = getFailOverStatus(DataParser.fip);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }*/

            } catch (Exception e) {
                mStatus = DataParser.OPXML_SERVER_ERROR;
                Log.i("SplashActivity", "Exception, mStatus :: " + mStatus);
                e.printStackTrace();
            }

            return null;
        }



        protected void onPostExecute(Void unused) {
            if (mStatus == DataParser.OPXML_SUCCESS) {
                try {
                    if (mIsBackPress == false && mStatusReceived == false) {
                        mPrefs.setPrefString("Registration", "Registering...");
                        mPrefs.setPrefBoolean("settingslogin", false);

                        Intent i = new Intent(SplashActivity.this, Home.class);
                        startActivity(i);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (mStatus == DataParser.UNKNOWN_HOST_EXCEPTION) { // Added by Ramesh
                errorDialog("Something went wrong, Please check your network connection.");
            } else {
                showAlert(mStatus);
                return;
            }
        }
    }

    protected void showAlert(final int OPXMLStatus) {
        String msg = "Something went wrong. Please try again.";

        if (OPXMLStatus == DataParser.OPXML_WRONG_BRAND_PIN) {
            msg = "Something went wrong, Please try again.";
        } else if (OPXMLStatus == DataParser.OPXML_INACTIVE_BRAND_PIN) {
            msg = "Inactive Dialer";
        }
        mPrefs.setPrefString("Registration", msg);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent home_intent = new Intent(getApplicationContext(),
                        LoginActivity.class);
                mPrefs.setPrefBoolean("settingslogin", false);
                startActivity(home_intent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }

   /* public int getFailOverStatus(String fip) {
        SharedPreferences.Editor save_link = sp.edit();
        save_link.putString("opxmllink", fip);
        save_link.commit();

        SplashActivity.userSettings_httpLink = fip;
        mStatus = DataParser.setOpxmlUDP(fip,
                "", mPrefs.getPrefString("login_brandpin"),
                getApplicationContext());
        return mStatus;
    }*/

    /**
     * This method shows error popup and redirects to login screen.
     *
     * @param msg text message
     */
    private void errorDialog(final String msg) { // Added by Ramesh for UnknownHost Error as guided by Raghava
        try {
            final Dialog error_dialog = new Dialog(SplashActivity.this);

            error_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            error_dialog.setContentView(R.layout.dialog);
            error_dialog.setCancelable(false);
            error_dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));

            TextView tv_message = (TextView) error_dialog
                    .findViewById(R.id.tv_alert_title);
            TextView tv_title = (TextView) error_dialog
                    .findViewById(R.id.tv_alert_title1);
            Button yes = (Button) error_dialog
                    .findViewById(R.id.btn_alert_ok);
            Button no = (Button) error_dialog
                    .findViewById(R.id.btn_alert_cancel);

            tv_title.setText("Network Error");
            tv_message.setText("" + msg);
            no.setVisibility(View.GONE);

            yes.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    error_dialog.dismiss();

                    Intent home_intent = new Intent(getApplicationContext(),
                            LoginActivity.class);
                    mPrefs.setPrefBoolean("settingslogin", false);
                    startActivity(home_intent);
                    finish();
                }
            });

            if (error_dialog != null)
                error_dialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void invokeCloseDialog(final String msg) {
        if (mPopupDialog == null) {
            try {
                mPopupDialog = new Dialog(SplashActivity.this);
                mPopupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mPopupDialog.setContentView(R.layout.dialog);
                mPopupDialog.setCancelable(false);
                mPopupDialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));

                TextView tv_title = (TextView) mPopupDialog
                        .findViewById(R.id.tv_alert_title);
                TextView tv_alert_title1 = (TextView) mPopupDialog
                        .findViewById(R.id.tv_alert_title1);
                Button yes = (Button) mPopupDialog
                        .findViewById(R.id.btn_alert_ok);
                Button no = (Button) mPopupDialog
                        .findViewById(R.id.btn_alert_cancel);

                tv_title.setText("" + msg);
                no.setText("Settings");
                no.setVisibility(View.VISIBLE);

                yes.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPopupDialog.dismiss();
                        finish();
                    }
                });
                no.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPopupDialog.dismiss();
                        mPopupDialog = null;

                        startActivityForResult(new Intent(
                                android.provider.Settings.ACTION_SETTINGS), 0);
                    }
                });

                if (mPopupDialog != null)
                    mPopupDialog.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0: {
                startActivity(new Intent(this, SplashActivity.class));
                this.finish();
            }
            break;

            default:
                break;
        }
    }

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isNetwork = intent.getBooleanExtra("NetworkStatus", false);

            if (isNetwork) {
                if (mPopupDialog != null)
                    mPopupDialog.dismiss();

                Intent i = null;
                boolean isLogin = mPrefs.getPrefBoolean("islogin");
                Log.i("SplashScreen", "is login or not" + isLogin);

                if (isLogin) {
                    try {
                        if (mStatusReceived == true) {
                            mStatusReceived = false;
                            new GetOPXMLData().execute();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (mStatusReceived == true) {
                        mStatusReceived = false;
                        i = new Intent(SplashActivity.this, LoginActivity.class);
                        if (i != null) {
                            mPrefs.setPrefBoolean("settingslogin", false);
                            startActivity(i);
                            finish();
                        }
                    }
                }
            }
        }
    };
}