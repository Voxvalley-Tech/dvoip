/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.dvoip.BuildConfig;
import com.app.dvoip.R;
import com.vx.core.android.db.AccountsDB;
import com.vx.core.android.service.NotificationService;
import com.vx.core.android.utils.MethodHelper;
import com.vx.ui.dialpad.DialerFragment;
import com.vx.utils.Config;
import com.vx.utils.DataParser;
import com.vx.utils.PermissionUtils;
import com.vx.utils.PreferenceProvider;

/**
 * LoginActivity uses for both registration and settings screens,
 * It helps to get server configuration for voice calling.
 */
public class LoginActivity extends Activity implements OnClickListener {

    // Variable declaration
    private Button mLoginSubmitButton;
    private Button mLoginCancelButton;
    private EditText mLoginUserNameEdt;
    private EditText mLogInPasswordEdt;
    private EditText mLogInBrandPinEdt;
    private EditText mLogInUserPhoneNumberEdt;
    private CheckBox mShowPasswordCheckBox;
    private TextView mLoginTitleTextView;
    private TextView mAppVersionTextView;

    private AccountsDB mAccountsDB;
    private PreferenceProvider mPreferenceProvider;
    private CustomProgressDialog mCustomProgressDialog;
    private Dialog mPopupDialog;
    private SharedPreferences mSharedPreference;
    AccountsDB sipdb;
    private Typeface mPasswordTextFontStyle;
    private static final String TAG = "LoginActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        // If OS version is more than Lollipop we ask all below permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "hasPermissions: " + PermissionUtils.hasPermissions(LoginActivity.this, PermissionUtils.PERMISSIONS));
            PermissionUtils.requestForAllPermission(LoginActivity.this);
        }

        mPreferenceProvider = PreferenceProvider.getPrefInstance(getApplicationContext());
        mSharedPreference = getApplicationContext().getSharedPreferences("opxmllink", MODE_PRIVATE);
        // Initialization
        mLoginUserNameEdt = (EditText) findViewById(R.id.login_username_edt);
        mLogInPasswordEdt = (EditText) findViewById(R.id.login_password_edt);
        mLogInBrandPinEdt = (EditText) findViewById(R.id.login_brandpin_edt);
        mLoginSubmitButton = (Button) findViewById(R.id.login_submit_button);
        mLoginCancelButton = (Button) findViewById(R.id.login_cancel_button);
        mLogInUserPhoneNumberEdt = (EditText) findViewById(R.id.login_phonenumber_edt);
        mAppVersionTextView = (TextView) findViewById(R.id.version);

        mLoginTitleTextView = (TextView) findViewById(R.id.login_title);
        mShowPasswordCheckBox = (CheckBox) findViewById(R.id.showpwd_check);
        sipdb = new AccountsDB(this);
        mLoginSubmitButton.setOnClickListener(this);

        mPasswordTextFontStyle = Typeface.createFromAsset(getAssets(), "Roboto-Regular_5.ttf");
        mShowPasswordCheckBox.setTypeface(mPasswordTextFontStyle);

        String username = mPreferenceProvider.getPrefString("login_username");
        String password = mPreferenceProvider.getPrefString("login_password");
        String brandpin = mPreferenceProvider.getPrefString("login_brandpin");
        String phnum = mPreferenceProvider.getPrefString("login_phone");
        //login_phoneNumber

        try {
            String appVersion = BuildConfig.VERSION_NAME;

            mAppVersionTextView.setVisibility(View.VISIBLE);
            mAppVersionTextView.setText("Version: " + appVersion);



        } catch (Exception e) {
            e.printStackTrace();
        }

       /* if (mPreferenceProvider.getPrefBoolean("settingslogin")) {
            mLoginTitleTextView.setText("Account Settings");

            try {
                String appVersion = BuildConfig.VERSION_NAME;
                mAppVersionTextView.setVisibility(View.VISIBLE);
                mAppVersionTextView.setText("New Version: " + appVersion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mAppVersionTextView.setVisibility(View.GONE);
        }*/

        if (username.toString().trim().length() > 0) {
            mLoginUserNameEdt.setText(mPreferenceProvider.getPrefString("login_username"));
        }

        if (password.toString().trim().length() > 0) {
            mLogInPasswordEdt.setText(mPreferenceProvider.getPrefString("login_password"));
        }

        if (brandpin.toString().trim().length() > 0) {
            mLogInBrandPinEdt.setText(mPreferenceProvider.getPrefString("login_brandpin"));
        }

        if (phnum.toString().trim().length() > 0) {

            mLogInUserPhoneNumberEdt.setText(phnum);
        }

        mAccountsDB = new AccountsDB(this);

        mLoginCancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        mShowPasswordCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                int position = mLogInPasswordEdt.getSelectionStart();

                if (isChecked) {
                    mShowPasswordCheckBox.setButtonDrawable(R.drawable.ic_password_hide);
                    mLogInPasswordEdt
                            .setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    // m_login_password_edt.setTypeface(fontboldTypeFace);
                    mLogInBrandPinEdt.setTypeface(mPasswordTextFontStyle);
                } else {
                    mShowPasswordCheckBox.setButtonDrawable(R.drawable.ic_password_show);
                    mLogInPasswordEdt.setInputType(129);
                    mLogInBrandPinEdt.setTypeface(mPasswordTextFontStyle);
                    // m_login_password_edt.setTypeface(fontboldTypeFace);
                }

                mLogInPasswordEdt.setSelection(position);
            }
        });


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.login_submit_button:

                ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                boolean is3g = manager.getNetworkInfo(
                        ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
                boolean isWifi = manager.getNetworkInfo(
                        ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

                if (!is3g && !isWifi) {
                    invokeCloseDialog("Please check your internet connection.");
                    mLoginSubmitButton.setEnabled(true);
                } else {
                    registrationValidation();
                }

                break;

            default:
                break;
        }
    }

    /**
     * This method do basic validation before hitting API call.
     */
    private void registrationValidation() {

        String username = mLoginUserNameEdt.getText().toString().trim()
                .replace(" ", "");
        String password = mLogInPasswordEdt.getText().toString().trim();
        String brandpin = mLogInBrandPinEdt.getText().toString().trim();
        String phNum = mLogInUserPhoneNumberEdt.getText().toString().trim();

        if (username.length() == 0) {
            Toast.makeText(getApplicationContext(), "Please enter User Name",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() == 0) {
            Toast.makeText(getApplicationContext(), "Please enter Password",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        username = username.replace("@", "");
        if (username.length() > 0 && password.length() > 0) {

            mPreferenceProvider.setPrefString("login_username", "" + username);
            mPreferenceProvider.setPrefString("login_password", "" + password);
            mPreferenceProvider.setPrefString("login_brandpin", "" + brandpin);
            mPreferenceProvider.setPrefString("login_phone", "" + phNum);
            mPreferenceProvider.setPrefBoolean("isbalancehit", true);

            mPreferenceProvider.setPrefString("sipusername", "" + username);// "108";
            mPreferenceProvider.setPrefString("sippassword", "" + password); // "v0x9cY10";
            mPreferenceProvider.setPrefString("sipbrandpin", "" + brandpin);// 11115
            mPreferenceProvider.setPrefString("xmppusername", username);
            mPreferenceProvider.setPrefString("xmppassword", password);

            new OPXMLOP().execute();

        }

    }

    private class OPXMLOP extends AsyncTask<Void, Void, Void> {

        String username;
        String password;

        String ip;
        String portRange;
        String brandpin;
        public int status;

        @Override
        protected void onPreExecute() {

            mCustomProgressDialog = new CustomProgressDialog(LoginActivity.this);
            mCustomProgressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {

                username = mPreferenceProvider.getPrefString("login_username");
                password = mPreferenceProvider.getPrefString("login_password");
                brandpin = mPreferenceProvider.getPrefString("login_brandpin");

                // PRIMARY URL
                /*status = DataParser.setOpxmlUDP(Config.PRIMARY_OPXML_URL, "", brandpin, getApplicationContext());

                // Don't go to fail over conditions to avoid delay, This logic suggested by Raghava
                if (status == DataParser.UNKNOWN_HOST_EXCEPTION) {
                    return null;
                }
                if (status == DataParser.OPXML_SERVER_ERROR) {
                    status = DataParser.setOpxmlUDP(Config.SECONDARY_OPXML_URL, "", brandpin, getApplicationContext());
                    // Don't go to fail over conditions to avoid delay, This logic suggested by Raghava
                    if (status == DataParser.UNKNOWN_HOST_EXCEPTION) { // Added by Ramesh
                        return null;
                    }

                    if (status == DataParser.OPXML_SERVER_ERROR) {
                        status = DataParser.setOpxmlUDP(DataParser.fip, "", brandpin, getApplicationContext());
                    }
                }

                if (status == DataParser.OPXML_SUCCESS) {
                    assignUrl();
                }*/

                out:
                for (int i = 0; i < 3; ++i) {

                        this.brandpin = brandpin;
                        // String[]
                        // ipPorts=Splash.iplist.get(j).toString().split(":");
                        ip = Config.PRIMARY_OPXML_URL;// ipPorts[0];
                        portRange = "";// ipPorts[1];

                        status = DataParser.setOpxmlUDPNew(ip, portRange,
                                brandpin,LoginActivity.this);
                        if (status == 5) {
                            DataParser.setstaticData(username, password, sipdb,
                                    mPreferenceProvider);
                        }
                        if (status != 3 && status != 0) {
                            break out;
                        }


                }

            } catch (Exception e) {
                status = DataParser.OPXML_SERVER_ERROR;
                e.printStackTrace();
            }

            return null;
        }



        protected void onPostExecute(Void unused) {

            try {
                if (mCustomProgressDialog != null)
                    mCustomProgressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (status == DataParser.OPXML_SUCCESS) {
                try {
                    if (mPreferenceProvider.getPrefBoolean("settingslogin")) {
                        mPreferenceProvider.setPrefString("Registration", "Registering...");
                        MethodHelper.stopAndStartSIPService(getApplicationContext());
                        finish();
                    } else {
                        closeActivity();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (status == DataParser.UNKNOWN_HOST_EXCEPTION) { // Added by Ramesh
                errorDialog("Something went wrong, Please check your network connection.");
            } else {
                String msg = "Something went wrong. Please try again.";
                if (status == DataParser.OPXML_WRONG_BRAND_PIN)
                    msg = "Trouble fetching dialer configuration... Please Try again";

                else if (status == DataParser.OPXML_INACTIVE_BRAND_PIN)
                    msg = "Inactive Dialer";
                try {
                    mPreferenceProvider.setPrefString("Registration", msg);
                    if (DialerFragment.registrationStatusTextView != null) {
                        DialerFragment.registrationStatusTextView
                                .setText(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                NotificationService obj = NotificationService.getInstance();
                if (obj != null)
                    obj.cancelRegisters();
                invokeCloseDialog(msg);
            }
        }

    }

    @Override
    public void onBackPressed() {
        try {
            mPreferenceProvider.setPrefBoolean("settingslogin", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onBackPressed();
    }

    void closeActivity() {

        try {
            Intent login_intent = new Intent(LoginActivity.this, Home.class);
            login_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(login_intent);
            finish();
        } catch (Throwable e) {
            e.printStackTrace();

        }
    }

    /**
     * This method shows error popup and redirects to login screen.
     *
     * @param msg text message
     */
    private void errorDialog(final String msg) { // Added by Ramesh for UnknownHost Error as guided by Raghava

        try {

            final Dialog error_dialog = new Dialog(LoginActivity.this);

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

                mPopupDialog = new Dialog(LoginActivity.this);
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

                        mPopupDialog.dismiss();
                        mPopupDialog = null;
                    }
                });

                no.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mPopupDialog.dismiss();
                        mPopupDialog = null;

                    }
                });

                if (mPopupDialog != null)
                    mPopupDialog.show();

            } catch (Throwable e) {
                e.printStackTrace();

            }

        }

    }

    public int getFailOverStatus(String fip) {

        SharedPreferences.Editor save_link = mSharedPreference.edit();
        save_link.putString("opxmllink", fip);
        save_link.commit();

        SplashActivity.userSettings_httpLink = fip;
        int status = DataParser.setOpxmlUDPNew(fip,
                "", mPreferenceProvider.getPrefString("login_brandpin"),LoginActivity.this);
        return status;

    }

}
