package com.vx.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.app.dvoip.BuildConfig;
import com.app.dvoip.R;
import com.vx.core.android.db.AccountsDB;
import com.vx.core.android.service.NotificationService;
import com.vx.core.android.service.SIPService;
import com.vx.core.android.utils.MethodHelper;
import com.vx.ui.dialpad.DialerFragment;
import com.vx.utils.Config;
import com.vx.utils.DataParser;
import com.vx.utils.PreferenceProvider;

/**
 * Created by ramesh.u on 2/27/2018.
 */

public class SettingsFragment extends Fragment implements View.OnClickListener{

    private View mMoreView;
    Button m_login_submit_button;
    EditText m_login_username_edt;
    public static EditText m_login_password_edt;
    EditText m_login_brandpin_edt;
    public static CheckBox showpwd_check;
    public static String versionNum = "";
    int loginaccid = -1;
    String TAG = "Settings_Activity";
    Progressdialog_custom dialog;
    Dialog popup_dialog;
    static PreferenceProvider prefereceprovider;
    TextView version;
    private String username;
    private String password;
    private String brandpin;
    private String phone_number;

    EditText login_phonenumber_edt;
    boolean check;
    private Activity activity;
    private AccountsDB mAccountsDB;
    private SharedPreferences mSharedPreference;
    AccountsDB sipdb;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mMoreView == null) {
            mMoreView = inflater.inflate(R.layout.fragment_settings, container, false);

            mAccountsDB = new AccountsDB(getActivity());
            mSharedPreference = getActivity().getApplicationContext().getSharedPreferences("opxmllink",
                    getActivity().MODE_PRIVATE);


            sipdb = new AccountsDB(getActivity());
            getActivity().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST);
            prefereceprovider = PreferenceProvider.getPrefInstance(activity.getApplicationContext());
            // Intilization
            m_login_username_edt = (EditText)mMoreView. findViewById(R.id.login_username_edt1);
            m_login_password_edt = (EditText)mMoreView. findViewById(R.id.login_password_edt1);
            m_login_brandpin_edt = (EditText)mMoreView. findViewById(R.id.login_brandpin_edt1);
            m_login_submit_button = (Button)mMoreView. findViewById(R.id.login_submit_button);
            version = (TextView) mMoreView.findViewById(R.id.version);
            showpwd_check = (CheckBox) mMoreView.findViewById(R.id.showpwd_check);


            login_phonenumber_edt = (EditText)mMoreView. findViewById(R.id.login_phonenumber_edt1);


            try {
                PackageInfo pinfo =getActivity(). getPackageManager().getPackageInfo(
                        getActivity().getPackageName(), 0);
                versionNum = pinfo.versionName;

            } catch(Exception e){
                e.printStackTrace();
            }
            String appVersion = BuildConfig.VERSION_NAME;
            version.setText("Version: " + appVersion);
            m_login_submit_button.setOnClickListener(this);

           updateData();

            loginaccid = prefereceprovider.getPrefInt("AccountID", -1);



            showpwd_check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    int position = m_login_password_edt.getSelectionStart();

                    if (isChecked) {
                        showpwd_check.setButtonDrawable(R.drawable.ic_password_hide);
                        m_login_password_edt
                                .setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

                        // m_login_password_edt.setTypeface(Typeface.DEFAULT);
                    } else {
                        showpwd_check.setButtonDrawable(R.drawable.ic_password_show);
                        m_login_password_edt.setInputType(129);
                        // m_login_password_edt.setTypeface(Typeface.DEFAULT);
                    }

                    m_login_password_edt.setSelection(position);
                }
            });

        }
        return mMoreView;
    }


    public void updateData(){

        username = prefereceprovider.getPrefString("login_username");
        password = prefereceprovider.getPrefString("login_password");
        brandpin = prefereceprovider.getPrefString("login_brandpin");
        phone_number = prefereceprovider.getPrefString("login_phone");

        if (username!=null && username.toString().trim().length() > 0) {
            m_login_username_edt.setText(prefereceprovider.getPrefString("login_username"));
        }else{
            m_login_username_edt.setText("");
        }

        if (password!=null && password.toString().trim().length() > 0) {
            m_login_password_edt.setText(prefereceprovider.getPrefString("login_password"));
        }else{
            m_login_password_edt.setText("");
        }

        if (brandpin!=null && brandpin.toString().trim().length() > 0) {
            m_login_brandpin_edt.setText(prefereceprovider.getPrefString("login_brandpin"));
        }else{
            m_login_brandpin_edt.setText("");
        }

        if (phone_number!=null && phone_number.toString().trim().length() > 0) {
            login_phonenumber_edt.setText(prefereceprovider.getPrefString("login_phone"));
        }else{
            login_phonenumber_edt.setText("");
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        check=false;
        showpwd_check.setButtonDrawable(R.drawable.ic_password_show);
        m_login_password_edt.setInputType(129);

        prefereceprovider.setPrefString("tab_num", "0");

        updateData();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.login_cancel_button:

                // Home.tabHost.setCurrentTab(1);

                String navigationScreen1 = prefereceprovider.getPrefString("navigationScreen");
                if (navigationScreen1 != null
                        && navigationScreen1.equalsIgnoreCase("0")) {
                    Home.mViewPager.setCurrentItem(0);
                } else if (navigationScreen1 != null
                        && navigationScreen1.equalsIgnoreCase("1")) {
                    Home.mViewPager.setCurrentItem(1);
                } else if (navigationScreen1 != null
                        && navigationScreen1.equalsIgnoreCase("2")) {
                    Home.mViewPager.setCurrentItem(2);
                }

                break;
            case R.id.login_submit_button:
                // Register button click listner

                try {
                    // ramesh add try catch block

                    // prefs.setPrefboolean("settingslogin", true);
                    ConnectivityManager manager = (ConnectivityManager)getActivity(). getSystemService(getActivity().CONNECTIVITY_SERVICE);
                    boolean is3g = manager.getNetworkInfo(
                            ConnectivityManager.TYPE_MOBILE)
                            .isConnectedOrConnecting();
                    boolean isWifi = manager.getNetworkInfo(
                            ConnectivityManager.TYPE_WIFI)
                            .isConnectedOrConnecting();
                    if (!is3g && !isWifi) {

                        invokeCloseDialog("Please make sure your Network Connection is ON");

                        m_login_submit_button.setEnabled(true);

                    } else {

                        RegistraionValidation();
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }


    private void RegistraionValidation() {

        String username = m_login_username_edt.getText().toString().trim();
        String password = m_login_password_edt.getText().toString().trim();
        String brandpin = m_login_brandpin_edt.getText().toString().trim();
        String phone = login_phonenumber_edt.getText().toString().trim();

        if (username.length() == 0) {
            Toast.makeText(getActivity(), "Please enter Username",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() == 0) {
            Toast.makeText(getActivity(), "Please enter Password", Toast.LENGTH_SHORT).show();
            return;
        }

        username = username.replace("@", "");

        if (username.length() > 0 && password.length() > 0) {

            prefereceprovider.setPrefString("login_username", "" + username);
            prefereceprovider.setPrefString("login_password", "" + password);
            prefereceprovider.setPrefString("login_brandpin", "" + brandpin);
            prefereceprovider.setPrefString("login_phone", "" + phone);
            prefereceprovider.setPrefBoolean("isbalancehit", true);

            prefereceprovider.setPrefString("sipusername", "" + username);// "108";
            prefereceprovider.setPrefString("sippassword", "" + password); // "v0x9cY10";
            prefereceprovider.setPrefString("sipbrandpin", "" + brandpin);// 11115
            prefereceprovider.setPrefString("xmppusername", username);
            prefereceprovider.setPrefString("xmppassword", password);

            new OPXMLOP().execute();

        }

    }


    private class OPXMLOP extends AsyncTask<Void, Void, Void> {

        String ip;
        String portRange;
        String brandpin;
        public int status;

        public OPXMLOP() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                dialog = new Progressdialog_custom(getActivity());
                dialog.show();
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            try {

                username = prefereceprovider.getPrefString("login_username");
                password = prefereceprovider.getPrefString("login_password");
                brandpin = prefereceprovider.getPrefString("login_brandpin");

               /* // PRIMARY URL
                status = DataParser.setOpxmlUDP(Config.PRIMARY_OPXML_URL, "", brandpin, getActivity());

                // Don't go to fail over conditions to avoid delay, This logic suggested by Raghava
                if (status == DataParser.UNKNOWN_HOST_EXCEPTION) {
                    return null;
                }
                if (status == DataParser.OPXML_SERVER_ERROR) {
                    status = DataParser.setOpxmlUDP(Config.SECONDARY_OPXML_URL, "", brandpin, getActivity());
                    // Don't go to fail over conditions to avoid delay, This logic suggested by Raghava
                    if (status == DataParser.UNKNOWN_HOST_EXCEPTION) { // Added by Ramesh
                        return null;
                    }

                    if (status == DataParser.OPXML_SERVER_ERROR) {
                        status = DataParser.setOpxmlUDP(DataParser.fip, "", brandpin, getActivity());
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
                                brandpin,getActivity());
                        if (status == 5) {
                            DataParser.setstaticData(username, password, sipdb,
                                    prefereceprovider);
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
            super.onPostExecute(unused);

            try {
                if (dialog != null)
                    dialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (status == DataParser.OPXML_SUCCESS) {
                prefereceprovider.setPrefBoolean("isWrongOrInactiveBrandPin", false);
                try {
                    prefereceprovider.setPrefBoolean("isbalancehit",true);
                    prefereceprovider.setPrefString("Registration", "Registering...");
                    DialerFragment.registrationStatusTextView.setText("Registering...");
                    DialerFragment.balanceTextView.setText("");
                    MethodHelper.stopAndStartSIPService(getActivity());
                    Home.mViewPager.setCurrentItem(1);
                    /*
                    if (prefereceprovider.getPrefBoolean("settingslogin")) {
                        prefereceprovider.setPrefString("Registration", "Registering...");
                        MethodHelper.stopAndStartSIPService(getActivity());
                        getActivity().finish();
                    } else {
                        closeActivity();
                    }*/
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (status == DataParser.UNKNOWN_HOST_EXCEPTION) { // Added by Ramesh

                errorDialog("Something went wrong, Please check your network connection.");
            } else {
                String msg = "Something went wrong. Please try again.";
                if (status == DataParser.OPXML_WRONG_BRAND_PIN){
                    msg = "Trouble fetching dialer configuration... Please Try again";
                    prefereceprovider.setPrefBoolean("isWrongOrInactiveBrandPin", false);
                }

                else if (status == DataParser.OPXML_INACTIVE_BRAND_PIN) {
                    msg = "Inactive Dialer";
                    prefereceprovider.setPrefBoolean("isWrongOrInactiveBrandPin", true);
                    prefereceprovider.setPrefString("WrongOrInactiveBrandPin", "Inactive Dialer");
                    if (MethodHelper.isGivenServiceRunning(getActivity(), SIPService.class)) {
                        getActivity().stopService(new Intent(getActivity(), SIPService.class));
                    }

                }

                try {
                    prefereceprovider.setPrefString("Registration", msg);
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
    void closeActivity() {

        try {
            Intent login_intent = new Intent(getActivity(), Home.class);
            login_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(login_intent);
            getActivity().finish();
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

            final Dialog error_dialog = new Dialog(getActivity());

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

            yes.setOnClickListener(new View.OnClickListener() {
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

        if (popup_dialog == null) {

            try {

                popup_dialog = new Dialog(getActivity());
                popup_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                popup_dialog.setContentView(R.layout.dialog);
                popup_dialog.setCancelable(false);
                popup_dialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));

                TextView tv_title = (TextView) popup_dialog
                        .findViewById(R.id.tv_alert_title);
                Button yes = (Button) popup_dialog
                        .findViewById(R.id.btn_alert_ok);
                Button no = (Button) popup_dialog
                        .findViewById(R.id.btn_alert_cancel);

                tv_title.setText("" + msg);

                Button server_yes = (Button) popup_dialog.findViewById(R.id.btn_alert_ok_server);
                no.setVisibility(View.GONE);
                yes.setVisibility(View.GONE);
                server_yes.setVisibility(View.VISIBLE);

                server_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        popup_dialog.dismiss();
                        popup_dialog = null;
                    }
                });

                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        popup_dialog.dismiss();
                        popup_dialog = null;

                    }
                });

                if (popup_dialog != null)
                    popup_dialog.show();

            } catch(Throwable e){
                e.printStackTrace();
            }

        }

    }


    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null) {
            try {
                InputMethodManager inputMethodManager = (InputMethodManager) activity
                        .getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    inputMethodManager.hideSoftInputFromWindow(activity
                            .getCurrentFocus().getWindowToken(), 0);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }


}
