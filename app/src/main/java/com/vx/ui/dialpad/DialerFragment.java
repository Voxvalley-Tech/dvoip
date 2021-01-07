/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.ui.dialpad;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.dvoip.R;
import com.vx.core.android.db.AccountsDB;
import com.vx.core.android.db.DataBaseHelper;
import com.vx.core.android.db.OPXMLDAO;
import com.vx.core.android.getaccounts.ProfileData;
import com.vx.core.android.model.CallInfo;
import com.vx.core.android.service.NotificationService;
import com.vx.core.android.utils.MethodHelper;
import com.vx.core.jni.Inv_states;
import com.vx.core.jni.SipManager;
import com.vx.core.jni.SipMethodHelper;
import com.vx.ui.Home;
import com.vx.ui.incall.InCallCardActivity;
import com.vx.utils.Constants;
import com.vx.utils.DataParser;
import com.vx.utils.DialerUtils;
import com.vx.utils.DialingFeedback;
import com.vx.utils.PreferenceProvider;

import java.util.ArrayList;
import java.util.HashMap;

import vx.plt.SWIGTYPE_p__VX_ERROR;

public class DialerFragment extends Fragment implements OnClickListener {


    private static final String TAG = "DialerFragment";

    public static EditText dialerPhoneNumberEditText;
    public static TextView registrationStatusTextView;
    public static TextView balanceTextView;

    private ImageView mDialerFeedbackImageView;
    private TextView mDialerNameHeaderTextView;
    private TextView mDialerNameFooterTextView;
    private View mDialerView;

    private static final int ADD_DATA = 2;
    private static String balValue = "";

    public boolean isNetworkSwitched = false; // It will avoid multiple re-invite
    private PreferenceProvider mPrefProvider;
    private AccountsDB mAccountsDB;
    private ProfileData mProfileData;
    private int mLoginAccId = 0;
    private int mAccID;
    private int mDeviceWidth;
    private int mDeviceHeight;
    private long mLastClickTime = 0;
    DialingFeedback mDialingfeedback;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.i(TAG, "dialer:OnCreateView");

        /* Below code need to comment to avoid crash in 2.x devices */
        if (mDialerView == null) {

            mDialerView = inflater.inflate(R.layout.fragment_dialer, container, false);
            dialerPhoneNumberEditText = (EditText) mDialerView.findViewById(R.id.screen_tab_dialer_editText_number);

            dialerPhoneNumberEditText.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    v.onTouchEvent(event);
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    return true;
                }
            });

            mDialerFeedbackImageView = (ImageView) mDialerView.findViewById(R.id.dialpad_feedback_img);
            registrationStatusTextView = (TextView) mDialerView.findViewById(R.id.registration_status_textview);
            balanceTextView = (TextView) mDialerView.findViewById(R.id.balace_textview);
            mDialerFeedbackImageView.setOnClickListener(this);
            registrationStatusTextView.setSelected(true);
            mDialerNameHeaderTextView = (TextView) mDialerView.findViewById(R.id.dialpad_header);
            mDialerNameFooterTextView = (TextView) mDialerView.findViewById(R.id.dialpad_footer);
            mDialerNameHeaderTextView.setSelected(true);
            mDialerNameFooterTextView.setSelected(true);

            try {

                WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics(metrics);
                mDeviceWidth = metrics.widthPixels;
                mDeviceHeight = metrics.heightPixels;

            } catch (Exception e) {
                e.printStackTrace();
            }


            dialerPhoneNumberEditText.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    if (s != null && s.length() > 0) {


                        if (mDeviceWidth > 480 && mDeviceHeight > 800) {

                            if (s.length() >= 12 && s.length() <= 0) {

                                dialerPhoneNumberEditText.setTextSize(30);

                            } else if (s.length() >= 13 && s.length() <= 17) {

                                dialerPhoneNumberEditText.setTextSize(23);
                            } else if (s.length() >= 17) {

                                dialerPhoneNumberEditText.setTextSize(16);
                            } else {

                                dialerPhoneNumberEditText.setTextSize(32);
                            }
                        } else {

                            if (s.length() >= 12 && s.length() <= 0) {

                                dialerPhoneNumberEditText.setTextSize(28);

                            } else if (s.length() >= 13 && s.length() <= 17) {

                                dialerPhoneNumberEditText.setTextSize(23);
                            } else if (s.length() >= 17) {

                                dialerPhoneNumberEditText.setTextSize(18);
                            } else {

                                dialerPhoneNumberEditText.setTextSize(28);
                            }
                        }
                    }

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void afterTextChanged(Editable s) {
                    // TODO Auto-generated method stub

                }
            });


            mPrefProvider = PreferenceProvider.getPrefInstance(activity.getApplicationContext());

            float size = 28, size_star = 45;

            try {
                DisplayMetrics mDisplay = activity.getApplicationContext().getResources().getDisplayMetrics();
                int width = mDisplay.widthPixels;
                if (width == 480) {
                    size = 28;
                    size_star = 45;
                } else if (width == 320) {
                    size = 26;
                    size_star = 30;
                } else if (width == 240) {
                    size = 18;
                    size_star = 30;
                } else if (width == 540) {
                    size = 25;
                    size_star = 40;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            float size1 = 13;

            try {
                DisplayMetrics mDisplay = getActivity().getApplicationContext().getResources()
                        .getDisplayMetrics();
                int width = mDisplay.widthPixels;
                if (width == 480) {
                    size1 = 13;
                } else if (width == 320) {
                    size1 = 10;
                } else if (width == 240) {
                    size1 = 9;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            mAccID = mPrefProvider.getPrefInt("AccID");

            mAccountsDB = new AccountsDB(activity.getApplicationContext());

            mLoginAccId = mPrefProvider.getPrefInt("AccountID");

            mAccountsDB.open();

            mProfileData = mAccountsDB.getAccount(mLoginAccId);

            mAccountsDB.close();


            DialerUtils.setDialerTextButton(mDialerView, R.id.screen_tab_dialer_button_0,
                    "0", "", DialerUtils.TAG_0, mOnDialerClick, mOnDialerLongClick, size, size1);
            DialerUtils.setDialerTextButton(mDialerView, R.id.screen_tab_dialer_button_1,
                    "1", "", DialerUtils.TAG_1, mOnDialerClick, null, size, size1);
            DialerUtils.setDialerTextButton(mDialerView, R.id.screen_tab_dialer_button_2,
                    "2", "ABC", DialerUtils.TAG_2, mOnDialerClick, null, size, size1);
            DialerUtils.setDialerTextButton(mDialerView, R.id.screen_tab_dialer_button_3,
                    "3", "DEF", DialerUtils.TAG_3, mOnDialerClick, null, size, size1);
            DialerUtils.setDialerTextButton(mDialerView, R.id.screen_tab_dialer_button_4,
                    "4", "GHI", DialerUtils.TAG_4, mOnDialerClick, null, size, size1);
            DialerUtils.setDialerTextButton(mDialerView, R.id.screen_tab_dialer_button_5,
                    "5", "JKL", DialerUtils.TAG_5, mOnDialerClick, null, size, size1);
            DialerUtils.setDialerTextButton(mDialerView, R.id.screen_tab_dialer_button_6,
                    "6", "MNO", DialerUtils.TAG_6, mOnDialerClick, null, size, size1);
            DialerUtils.setDialerTextButton(mDialerView, R.id.screen_tab_dialer_button_7,
                    "7", "PQRS", DialerUtils.TAG_7, mOnDialerClick, null, size, size1);
            DialerUtils.setDialerTextButton(mDialerView, R.id.screen_tab_dialer_button_8,
                    "8", "TUV", DialerUtils.TAG_8, mOnDialerClick, null, size, size1);
            DialerUtils.setDialerTextButton(mDialerView, R.id.screen_tab_dialer_button_9,
                    "9", "WXYZ", DialerUtils.TAG_9, mOnDialerClick, null, size, size1);
            DialerUtils.setDialerTextButton(mDialerView,
                    R.id.screen_tab_dialer_button_star, "*", "",
                    DialerUtils.TAG_STAR, mOnDialerClick, null, size, size1);
            DialerUtils.setDialerTextButton(mDialerView,
                    R.id.screen_tab_dialer_button_sharp, "#", "",
                    DialerUtils.TAG_SHARP, mOnDialerClick, null, size, size1);

            DialerUtils.setDialerImageButton(mDialerView,
                    R.id.screen_tab_dialer_button_audio, R.drawable.dialpad_call_selector,
                    DialerUtils.TAG_AUDIO_CALL, mOnDialerClick);
            DialerUtils.setDialerImageButton(mDialerView,
                    R.id.screen_tab_dialer_button_video,
                    R.drawable.dialpad_addcontact_selector, DialerUtils.TAG_CONTACT,
                    mOnDialerClick);
            DialerUtils.setLongDialerImageButton(mDialerView,
                    R.id.screen_tab_dialer_button_del, R.drawable.dialpad_clear_selector,
                    DialerUtils.TAG_DELETE, mOnDialerClick, mOnDialerLongClick);
            //register the broadcast receiver for error calls
            IntentFilter registrationStatusIntentFilter = new IntentFilter();
            registrationStatusIntentFilter.addAction(Home.packageName + ".RegistrationStatus");
            activity.registerReceiver(registrationStatusReceiver, registrationStatusIntentFilter);


        }
        /* else {
            ((ViewGroup) mDialerView.getParent()).removeView(mDialerView);
        }*/


        return mDialerView;
    }

    private final View.OnLongClickListener mOnDialerLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            int tag = Integer.parseInt(v.getTag().toString());
            if (tag == DialerUtils.TAG_DELETE) {
                dialerPhoneNumberEditText.getText().clear();
            }

            return true;
        }
    };

    private final View.OnClickListener mOnDialerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            /*
             * int tag = Integer.parseInt(v.getTag().toString()); final String
             * textToAppend = tag == DialerUtils.TAG_STAR ? "*" : (tag ==
             * DialerUtils.TAG_SHARP ? "#" : Integer.toString(tag));
             * appendText(textToAppend);
             */

            int tag = Integer.parseInt(v.getTag().toString());
            // final String number = mEtNumber.getText().toString();
            if (tag == DialerUtils.TAG_DELETE) {
                final int selStart = dialerPhoneNumberEditText.getSelectionStart();
                final String number = dialerPhoneNumberEditText.getText().toString();

                if (selStart > 0) {
                    final StringBuffer sb = new StringBuffer(number);
                    sb.delete(selStart - 1, selStart);
                    dialerPhoneNumberEditText.setText(sb.toString());
                    dialerPhoneNumberEditText.setSelection(selStart - 1);
                }

            } else if (tag == DialerUtils.TAG_AUDIO_CALL) {

                if (dialerPhoneNumberEditText.getText().toString().trim().length() == 0) {

                    if (mPrefProvider.getPrefString("lastcallnumber").toString().trim().length() > 0 && mPrefProvider.getPrefString("lastcallnumber").toString().trim().length() < 25) {
                        dialerPhoneNumberEditText.setText("" + mPrefProvider.getPrefString("lastcallnumber"));
                        dialerPhoneNumberEditText.setSelection(mPrefProvider.getPrefString("lastcallnumber").trim().length());
                        return;
                    } else {
                        Toast.makeText(activity.getApplicationContext(), "Please enter number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                }

                if (dialerPhoneNumberEditText.getText().toString().trim().length() == 0) {
                    Toast.makeText(activity.getApplicationContext(), "Please enter number", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (dialerPhoneNumberEditText.getText().toString().trim().length() > 0) {

                    Log.i(TAG, "Avoid multiple clicks, elapsed Time: " + SystemClock.elapsedRealtime() + " ,mLastClickTime: " + mLastClickTime);
                    // Below code avoids multiple clicks, using threshold of 1000 ms
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    // New changes to make reusable code.
                    int result = SipMethodHelper.makeCall(getActivity(), mPrefProvider, mAccID, dialerPhoneNumberEditText.getText().toString().trim());

                    if (result == Constants.MAKE_CALL_ERROR_CODE) {
                        return;
                    }

                    startCallingActivity(dialerPhoneNumberEditText.getText().toString().trim());

                } else {

                    Toast.makeText(activity.getApplicationContext(), "Please enter number", Toast.LENGTH_SHORT).show();
                }
            } else if (tag == DialerUtils.TAG_CONTACT) {

                if (dialerPhoneNumberEditText.getText().length() != 0) {
                    Intent in = new Intent(Intent.ACTION_INSERT);
                    in.setType(ContactsContract.Contacts.CONTENT_TYPE);
                    in.putExtra(ContactsContract.Intents.Insert.PHONE, dialerPhoneNumberEditText.getText().toString());
                    startActivityForResult(in, ADD_DATA);

                } else {

                    Toast.makeText(activity.getApplicationContext(), "Please Enter Number", Toast.LENGTH_SHORT).show();
                }


            } else {
                final String textToAppend = tag == DialerUtils.TAG_STAR ? "*"
                        : (tag == DialerUtils.TAG_SHARP ? "#" : Integer
                        .toString(tag));
                appendText(textToAppend);

                try {
                    if (textToAppend.equals("0"))
                        mDialingfeedback
                                .giveFeedback(ToneGenerator.TONE_DTMF_0);
                    else if (textToAppend.equals("1"))
                        mDialingfeedback
                                .giveFeedback(ToneGenerator.TONE_DTMF_1);
                    else if (textToAppend.equals("2"))
                        mDialingfeedback
                                .giveFeedback(ToneGenerator.TONE_DTMF_2);
                    else if (textToAppend.equals("3"))
                        mDialingfeedback
                                .giveFeedback(ToneGenerator.TONE_DTMF_3);
                    else if (textToAppend.equals("4"))
                        mDialingfeedback
                                .giveFeedback(ToneGenerator.TONE_DTMF_4);
                    else if (textToAppend.equals("5"))
                        mDialingfeedback
                                .giveFeedback(ToneGenerator.TONE_DTMF_5);
                    else if (textToAppend.equals("6"))
                        mDialingfeedback
                                .giveFeedback(ToneGenerator.TONE_DTMF_6);
                    else if (textToAppend.equals("7"))
                        mDialingfeedback
                                .giveFeedback(ToneGenerator.TONE_DTMF_7);
                    else if (textToAppend.equals("8"))
                        mDialingfeedback
                                .giveFeedback(ToneGenerator.TONE_DTMF_8);
                    else if (textToAppend.equals("9"))
                        mDialingfeedback
                                .giveFeedback(ToneGenerator.TONE_DTMF_9);
                    else if (textToAppend.equals("#"))
                        mDialingfeedback
                                .giveFeedback(ToneGenerator.TONE_DTMF_A);
                    else
                        mDialingfeedback
                                .giveFeedback(ToneGenerator.TONE_DTMF_B);

                } catch (Exception e) {
                    e.printStackTrace();
                    mDialingfeedback.giveFeedback(ToneGenerator.TONE_DTMF_0);
                }
            }

        }
        // }
    };


    private final View.OnLongClickListener mOnDialerTextLongClick = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            int tag = Integer.parseInt(v.getTag().toString());
            if (tag == DialerUtils.TAG_0) {
                appendText("+");
            }
            return true;
        }
    };


    private void appendText(String textToAppend) {
        if (dialerPhoneNumberEditText.getText().toString().length() <= 24) {
            final int selStart = dialerPhoneNumberEditText.getSelectionStart();
            final StringBuffer sb = new StringBuffer(dialerPhoneNumberEditText.getText().toString());
            sb.insert(selStart, textToAppend);
            dialerPhoneNumberEditText.setText(sb.toString());
            dialerPhoneNumberEditText.setSelection(selStart + 1);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {


            case R.id.dialpad_feedback_img:
                //Feedback button click listener

                try {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri
                            .fromParts("mailTo", "android@voxvalley.com", null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                            "Android MoSIP FeedBack from User ID: "
                                    + mProfileData.getUSERNAME());
                    startActivity(Intent.createChooser(emailIntent,
                            "Send email..."));

                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;


            default:
                break;
        }


    }

    @Override
    public void onResume() {

        Log.i(TAG, "dialer:onResume");
        try {
            mDialingfeedback = new DialingFeedback(getActivity(), false);
            mDialingfeedback.resume();
if(mPrefProvider.getPrefString(PreferenceProvider.APP_HEADER).toLowerCase().equalsIgnoreCase("dvoip"))
{
    mDialerNameHeaderTextView.setText("DVoIP");
}

           /* if (DataParser.footer != null) {
                mDialerNameFooterTextView.setText(mPrefProvider.getPrefString(PreferenceProvider.APP_FOOTER));
            }*/


            /*if (DataParser.header != null) {
                mDialerNameHeaderTextView.setText("" + DataParser.header);
            }

            */
           /* if (DataParser.footer != null) {
                mDialerNameFooterTextView.setText("" + DataParser.footer);
            }*/
            if (mPrefProvider.getPrefString("Registration").toString().trim().equalsIgnoreCase("Registered")) {
                if (DialerFragment.balanceTextView != null) {
                    Log.i(TAG, "dialer:onResume Bal" + mPrefProvider.getPrefString(PreferenceProvider.BALANCE_VALUE));
                    final String balanceValue = mPrefProvider.getPrefString(PreferenceProvider.BALANCE_VALUE);
                    DialerFragment.balanceTextView.post(new Runnable() {

                        @Override
                        public void run() {
                            if (balanceValue.length() > 0) {
                                balanceTextView.setVisibility(View.VISIBLE);
                                balanceTextView.setText("Bal: $" + balanceValue);

                                Log.i(TAG, "dialer:onresume post Bal" + balanceValue);
                            } else {
                                balanceTextView.setText("");
                                balanceTextView.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            } else {

                if (balanceTextView != null) {
                    balanceTextView.setVisibility(View.INVISIBLE);
                    DialerFragment.balanceTextView.setText("");
                }

                registrationStatusTextView.setText(mPrefProvider.getPrefString("Registration").toString());
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        if (MethodHelper.isNetworkAvailable(activity.getApplicationContext())) {
            if (mPrefProvider.getPrefBoolean("isWrongOrInactiveBrandPin") == true) {
                Log.i(TAG, "entered into if statement");
                DialerFragment.registrationStatusTextView.setText(mPrefProvider.getPrefString("WrongOrInactiveBrandPin"));
            } else {
                if (mPrefProvider.getPrefString("Registration").toString().trim().length() > 0) {
                    registrationStatusTextView.setText("" + mPrefProvider.getPrefString("Registration"));
                } else {
                    registrationStatusTextView.setText("Registering...");
                }
            }

        } else {
            mPrefProvider.setPrefString("Registration", "Please check your internet connection");
            registrationStatusTextView.setText("Please check your internet connection");
            DataParser.balance = "";
            balanceTextView.setVisibility(View.INVISIBLE);
            balanceTextView.setText("");
        }

        if (registrationStatusTextView.getText().toString().equalsIgnoreCase("Registered")) {

            if (MethodHelper.isNetworkAvailable(activity.getApplicationContext())) {
                balanceTextView.setVisibility(View.VISIBLE);

                balanceTextView.setText("Bal: $ " + mPrefProvider.getPrefString(PreferenceProvider.BALANCE_VALUE));
            } else {
                balanceTextView.setText("");
            }

        }

        super.onResume();


    }

    //for registration status update
    private BroadcastReceiver registrationStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final SWIGTYPE_p__VX_ERROR p__VX_ERROR = SipManager.getSwigPointerInstance();
            final int reststatuscode = intent.getIntExtra("RegStatus", 0);

            Log.i(TAG, "OnRegStateCb=" + reststatuscode);

            if (action.equals(Home.packageName + ".RegistrationStatus")) {

                //mAccID = intent.getIntExtra("AccountID", -1);
                //mPrefProvider.setPrefInt("AccID", mAccID);
                mAccID = mPrefProvider.getPrefInt("AccID");


                if (reststatuscode != 200) {
                    balanceTextView.setVisibility(View.INVISIBLE);
                    balanceTextView.setText("");

                    NotificationService obj = NotificationService.getInstance();
                    if (Constants.IS_MAKECALL_CALLED) {
                        if (obj != null) {
                            obj.cancelRegisters();
                        }
                    } else {
                        if (obj != null)
                            obj.cancelAll();
                    }

                }

                switch (reststatuscode) {

                    case 200:
                        Log.i(TAG, "notification callled" + reststatuscode);
                        Log.i(TAG, "isnetwork switched " + Constants.IS_NETWORK_SWITCHED);
                        // Sending re-invite to resume call
                        if (Constants.IS_MAKECALL_CALLED && Constants.IS_NETWORK_SWITCHED) {
                            Log.i(TAG, " isCallLive is true hence invoking reInvite");
                            Constants.IS_NETWORK_SWITCHED = false;
                            ArrayList<CallInfo> mCallList = SipManager.getCallListInfo();
                            // need to check any entries available before iterate Callinfo
                            if (mCallList != null && mCallList.size() > 0) {
                                CallInfo callInfo = null;
                                for (int i = 0; i < mCallList.size(); i++) {
                                    callInfo = mCallList.get(i);
                                    Log.i(TAG, "Call status " + callInfo.getCallState() + "call number " + callInfo.getCallContactNumber() + "Hold Status " + callInfo.isCallOnHold());
                                    // if call not in conference need to check Confirmed state
                                    // if call not confirmed state of call is initiated for transfer request need to send release call request
                                    // in else case if call is confirmed need to send hold/resume based on hold state
                                    if (callInfo != null && callInfo.getCallState() < Inv_states.VX_INV_STATE_CONFIRMED) {
                                        SipManager.releaseTheRunningCall(callInfo);
                                    } else if (callInfo != null && callInfo.getCallState() == Inv_states.VX_INV_STATE_CONFIRMED) {
                                        netWorkChangeCallHandle(callInfo);
                                    } else {
                                        /* we might have released the current call and whould have updated the status DISCONNECTING*/
                                        /*no need to handle this case*/
                                    }
                                }
                            }
                        } else if (Constants.IS_NETWORK_SWITCHED) {
                            Constants.IS_NETWORK_SWITCHED = false;
                            if (Constants.IS_MAKECALL_CALLED) {
                                Log.i(TAG, "Disconnecting the call");
                                Intent intent1 = new Intent("finish_Call");
                                context.sendBroadcast(intent1);
                            }
                        }
                        mPrefProvider.setPrefString("Registration", "Registered");
                        registrationStatusTextView.setText("Registered");

                        mLoginAccId = mPrefProvider.getPrefInt("AccountID");

                        mAccountsDB.open();

                        mProfileData = mAccountsDB.getAccount(mLoginAccId);

                        mAccountsDB.close();

                        boolean isbalancehit = mPrefProvider.getPrefBoolean("isbalancehit");
                        Log.i(TAG, "Bal  hit isbalancehit" + isbalancehit);
                        if (isbalancehit) {

                            mPrefProvider.setPrefBoolean("isbalancehit", false);
                            if (!Constants.IS_MAKECALL_CALLED) {
                                NotificationService obj = NotificationService.getInstance();
                                if (obj != null)
                                    obj.notificationsforRegister();
                            }


                            new Thread() {
                                public void run() {

                                    String usr = mPrefProvider.getPrefString("login_username");
                                    String pwd = mPrefProvider.getPrefString("login_password");
                                    balValue = DataParser.getBalance(mPrefProvider.getPrefString(PreferenceProvider.BALANCE_URL), usr, pwd,activity
                                    );
                                    mPrefProvider.setPrefString(PreferenceProvider.BALANCE_VALUE, balValue);
                                    balanceTextView.post(new Runnable() {

                                        @Override
                                        public void run() {
                                            Log.i(TAG, "Bal:registrationStatusReceiver" + balValue);

                                            if (balValue != null && balValue.length() > 0) {
                                                balanceTextView.setVisibility(View.VISIBLE);
                                                balanceTextView.setText("Bal: $ " + balValue);
                                            } else {
                                                balanceTextView.setVisibility(View.INVISIBLE);
                                                balanceTextView.setText("");
                                            }

                                        }
                                    });
                                }
                            }.start();

                        } else {
                            Log.i(TAG, "Bal not hit isbalancehit" + isbalancehit);
                        }
                        break;
                    case 403:
                    case 404:

                        mPrefProvider.setPrefString("Registration", "Wrong User Name or Password");
                        registrationStatusTextView.setText("Wrong user name or password");

                        break;
                    case 408:

                        mPrefProvider.setPrefString("Registration", "Not Registered.");
                        registrationStatusTextView.setText("Not Registered.");
                        Log.i(TAG, "Ismakecall called " + Constants.IS_MAKECALL_CALLED);
                        if (Constants.IS_MAKECALL_CALLED && !(mPrefProvider.getPrefBoolean("isCallLive"))) {
                            Log.i(TAG, "Brodcast sent");
                            Intent intent1 = new Intent("finish_Call");
                            context.sendBroadcast(intent1);
                        }
                        OPXMLDAO opxmldao = new OPXMLDAO(activity.getApplicationContext());

                        ArrayList<HashMap<String, String>> records = opxmldao
                                .getProvisionRecords(DataBaseHelper.PROVISION_BASE_TABLE);
                        if (records.size() != 0) {
                            opxmldao.delete(DataBaseHelper.PROVISION_BASE_TABLE, null,
                                    null);
                        }
                        opxmldao.close();

                        break;

                    case 503:

                        //break;
                    case -2:
                        boolean isNW = MethodHelper.isNetworkAvailable(context);
                        if (isNW) {
                            mPrefProvider.setPrefString("Registration", "Not Registered.");
                            registrationStatusTextView.setText("Not Registered.");
                        } else {
                            mPrefProvider.setPrefString("Registration", "Please check your internet connection");
                            registrationStatusTextView.setText("Please check your internet connection");
                            DataParser.balance = "";
                            balanceTextView.setVisibility(View.INVISIBLE);
                            balanceTextView.setText("");
                        }
                        break;

                    case 0:
                    case -1:


                        mPrefProvider.setPrefString("Registration", "Registering...");
                        registrationStatusTextView.setText("Registering...");

                        break;

                    default:
                        mPrefProvider.setPrefString("Registration", "Registration Failed");
                        registrationStatusTextView.setText("Registration Failed");
                        break;
                }

            }

        }

    };

    private Activity activity;


    /**
     * This method calls calling activity
     *
     * @param phoneNumber mobile number
     */
    private void startCallingActivity(final String phoneNumber) {

        activity.runOnUiThread(new Runnable() {
            public void run() {

                mPrefProvider.setPrefBoolean("incallspeaker", false);
                mPrefProvider.setPrefBoolean("speakerEnabled", false);
                mPrefProvider.setPrefBoolean("incallmute", false);

                Intent incall_intent = new Intent(activity.getApplicationContext(), InCallCardActivity.class);
                incall_intent.putExtra("ISCall", "outgoing");
                incall_intent.putExtra("ContactNum", "" + phoneNumber);
                incall_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(incall_intent);
                dialerPhoneNumberEditText.setText("");

            }
        });


    }

    @Override
    public void onDestroy() {

        if (registrationStatusReceiver != null) {

            activity.unregisterReceiver(registrationStatusReceiver);

        }
        super.onDestroy();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    private void netWorkChangeCallHandle(CallInfo callInfo) {
        if (callInfo.isCallOnHold()) {
            SipManager.holdTheCall(callInfo.getCallId());
        } else {
            SipManager.resumeTheCall(callInfo.getCallId());
        }
    }


}
