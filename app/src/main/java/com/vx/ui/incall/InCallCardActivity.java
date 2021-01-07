/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.ui.incall;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.dvoip.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.vx.core.android.bluetooth.BluetoothWrapper;
import com.vx.core.android.contacts.ContactMethodHelper;
import com.vx.core.android.db.RecordingsDB;
import com.vx.core.android.model.CallInfo;
import com.vx.core.android.service.NotificationService;
import com.vx.core.android.utils.AudioMethodHelper;
import com.vx.core.android.utils.MethodHelper;
import com.vx.core.android.utils.Ringer;
import com.vx.core.jni.Inv_states;
import com.vx.core.jni.SimpleWavRecorderHandler;
import com.vx.core.jni.SipConstants;
import com.vx.core.jni.SipManager;
import com.vx.core.jni.SipMethodHelper;
import com.vx.ui.Home;
import com.vx.utils.Constants;
import com.vx.utils.PreferenceProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import vx.plt.SWIGTYPE_p__VX_ERROR;
import vx.plt.VX_CallInfo;
import vx.plt.VoxEngine;

/**
 * This class shows the calling screen along with all call features.
 */
public class InCallCardActivity extends Activity implements OnClickListener,
        SensorEventListener {

    private final String TAG = "InCallCardActivity";


    private ImageView mInCallSpeakerImg;
    private ImageView mInCallKeypadImg;
    private ImageView mInCallHoldImg;
    private ImageView mInCallBluetoothImg;
    private ImageView mInCallMuteImg;
    private ImageView mInCallEndCallImg;
    private Button mInCallAnswerCallImg;
    private ImageView mContactPhoto;

    private TextView mInCallContactName;
    private TextView mInCallContactNumber;
    private TextView mInCallCallStatusCode;
    private EditText mNumberPadEditTextView;

    private VX_CallInfo JNICallInfo;
    private CallInfo mCurrentCallInfo;
    private PreferenceProvider mPrefProvider;
    private AudioManager mAudioManager;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Ringer mRinger;
    private Dialog mAddCallDialog;

    private Chronometer mInCallElapsedTime;
    private AdView mAdView;
    static int callStatus;
    static int callDuration = 0;
    public static long rowTime;
    public static long confirmedTime = 0;

    private float mStackVolumeLevel = Constants.SPEAKER_DEFAULT_VOLUME;
    private String mIsCall = "";
    private String mContactNumber;
    private StringBuffer mSelectedText;
    private String mCallStatusMessage = "";
    private boolean mSpeakerClicked = false;
    private HeadSetIntentReceiver mHeadsetReceiver;

    // Bluetooth Variables
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothWrapper mBluetoothWrapper;

    private final int ADD_CALL = 1;
    private final int CALL_TRANSFER = 2;

    private SWIGTYPE_p__VX_ERROR mSwigPointerInstance;
    private boolean isRecordEnabled;
    private SimpleWavRecorderHandler recorder;
    private long recordingStartingTime;
    private Handler handler = new Handler();

    // Below timer added to resolve end call issue
    private final int mInterval = 5000; // 5 Seconds
    private Handler mEndCallHandler = new Handler();
    private LinearLayout mSensorView;
    private ServicePhoneStateReceiver mPhoneConnectivityReceiver;
    private TelephonyManager mTelephonyManager;
    private boolean mIsEndCallBeingProcessed = false; // It will avoid multiple clicks on end call.
    private Runnable mEndCallRunnable = new Runnable() {
        public void run() {
            Log.e(TAG, "closing activity from End call handler");
            closeActivity();
        }
    };

    private Runnable recordingRunnable = new Runnable() {
        @Override
        public void run() {
            long diff = System.currentTimeMillis() - recordingStartingTime;
            long diffMinutes = diff / (60 * 1000) % 60;
            if (diffMinutes >= 30) {
                stopRecording();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_inacllcard_two);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("32D2BC149FACD71D3338CE50819FF503")
                .build();

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.i(TAG, "onAdLoaded");
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.e(TAG, "FailedToLoad ad: " + i);
            }
        });


        mAdView.loadAd(adRequest);

        try {
            mIsCall = getIntent().getExtras().getString("ISCall");
            mContactNumber = getIntent().getExtras().getString("ContactNum");
            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            mHeadsetReceiver = new HeadSetIntentReceiver();
            registerReceiver(mHeadsetReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        rowTime = System.currentTimeMillis();
        Constants.IS_MAKECALL_CALLED = true;

        // register the broadcast reciever for error calls
        IntentFilter callcountintentfilter = new IntentFilter();
        callcountintentfilter.addAction(Home.packageName + ".CallStatus");
        registerReceiver(callStatuserviceReceiver, callcountintentfilter);


        try {
            Constants.PREVIOUS_NETWORK_TYPE = MethodHelper.getNetworkType(InCallCardActivity.this);
            if (Constants.PREVIOUS_NETWORK_TYPE.equals("Wifi"))
                Constants.PREVIOUS_NETWORK_NAME = MethodHelper.getCurrentSsid(InCallCardActivity.this);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (Constants.PREVIOUS_NETWORK_TYPE.equals("Wifi"))
                Constants.CURRENT_NETWORK_NAME = Constants.PREVIOUS_NETWORK_NAME;
            Constants.CURRENT_NETWORK_TYPE = Constants.PREVIOUS_NETWORK_TYPE;
        }

        mInCallEndCallImg = (ImageView) findViewById(R.id.incall_endcall_img);
        mInCallAnswerCallImg = (Button) findViewById(R.id.incall_answercall_img);

        mSensorView = (LinearLayout) findViewById(R.id.sensor_layout);
        mInCallSpeakerImg = (ImageView) findViewById(R.id.incallspeaker_img);
        mInCallHoldImg = (ImageView) findViewById(R.id.incallhold_img);
        mInCallMuteImg = (ImageView) findViewById(R.id.incall_mute_img);
        mInCallBluetoothImg = (ImageView) findViewById(R.id.incallbluetooth_img);
        mInCallKeypadImg = (ImageView) findViewById(R.id.incall_keypad_img);

        mInCallCallStatusCode = (TextView) findViewById(R.id.incall_callstatuscode);
        mInCallElapsedTime = (Chronometer) findViewById(R.id.incall_elapsedTime);// (Chronometer)

        mInCallContactName = (TextView) findViewById(R.id.incall_contact_name);
        mInCallContactNumber = (TextView) findViewById(R.id.incall_contact_number);
        mContactPhoto = (ImageView) findViewById(R.id.contact_photo);

        mInCallEndCallImg.setOnClickListener(this);
        mInCallAnswerCallImg.setOnClickListener(this);
        mInCallSpeakerImg.setOnClickListener(this);
        mInCallHoldImg.setOnClickListener(this);
        mInCallMuteImg.setOnClickListener(this);
        mInCallBluetoothImg.setOnClickListener(this);
        mInCallKeypadImg.setOnClickListener(this);

        mPrefProvider = PreferenceProvider.getPrefInstance(getApplicationContext());
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mRinger = AudioMethodHelper.getRingerInstance(InCallCardActivity.this);
        mSwigPointerInstance = SipManager.getSwigPointerInstance();

        // This method gains audio focus
        AudioMethodHelper.setAudioFocus(mAudioManager, mPrefProvider, true);

        // Wake up the screen
        try {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        mInCallHoldImg.setEnabled(false);
        mInCallMuteImg.setEnabled(false);
        mInCallKeypadImg.setEnabled(false);


        mInCallElapsedTime.setVisibility(View.INVISIBLE);
        NotificationService obj1 = NotificationService.getInstance();
        if (obj1 != null) {
            obj1.cancelAll();
            obj1.showNotificationForCall("Calling",
                    mContactNumber);
        }
        Log.i("InCallCardActivity", "notification mIsCall: " + mIsCall);

        mCurrentCallInfo = SipManager.getCurrentCallInfo();
        if (mIsCall != null && mIsCall.equalsIgnoreCase("income")) {
            mInCallCallStatusCode.setText("Incoming Call");

            NotificationService obj = NotificationService.getInstance();
            if (obj != null) {
                if (mCurrentCallInfo.getCallType() == SipConstants.CALL_INCOMING) {
                    obj.showNotificationForCall("Incoming Call", mContactNumber);
                } else {
                    obj.showNotificationForCall("Call in Progress",
                            mContactNumber);
                }
            }

            mInCallAnswerCallImg.setVisibility(View.VISIBLE);
            if (SipManager.getCallListInfo().size() >= 2) {

                mInCallCallStatusCode.setText("Call Waiting...");

                mRinger.playInCallTone(Ringer.TONE_CALL_WAITING);

                if (mPrefProvider.getPrefBoolean("incallspeaker") == true) {
                    // Default speaker
                    mAudioManager.setSpeakerphoneOn(true);
                    mInCallSpeakerImg.setSelected(true);

                  /*  mInCallSpeakerImg
                            .setBackgroundResource(R.drawable.incall_pressed);*/


                  /* mInCallSpeakerImg.setCompoundDrawablesWithIntrinsicBounds(mute,
                            0, 0, 0);

                    mInCallSpeakerImg.setTextColor(getResources().getColor(
                            R.color.incall_button_text_hover_color));*/

                }

            } else {
                if (!mRinger.isRinging()) {
                    mRinger.ring("12345",
                            Settings.System.DEFAULT_RINGTONE_URI.toString());

                    mRinger.updateRingerMode();
                }
            }

        } else if (mIsCall.equalsIgnoreCase("notification")) {
            updateUIFromNotification(mCurrentCallInfo.getCallId());
        } else {
            mInCallAnswerCallImg.setVisibility(View.GONE);

            mCurrentCallInfo.setCallType(SipConstants.CALL_OUTGOING);
            SipManager.setCurrentCallInfo(mCurrentCallInfo);
        }

        if (mContactNumber != null && mContactNumber.length() > 0) {
            String contact_name=null;
             contact_name = ContactMethodHelper.getContactNameForCallLogs(mContactNumber,
                    getApplicationContext());
             if(contact_name!=null){
                 contact_name = ContactMethodHelper.getContactName(mContactNumber,
                         getApplicationContext());
             }

           /* String[] webser_details = contact_name.split("%24%");
            contact_name = webser_details[0];*/
            try {
                if (contact_name != null) {
                    mInCallContactName.setText("" + contact_name);
                    if (contact_name.matches("[0-9]+")) {
                        mInCallContactNumber.setVisibility(View.GONE);
                    } else {
                        mInCallContactNumber.setVisibility(View.VISIBLE);
                        mInCallContactNumber.setText("" + mContactNumber);
                    }

                } else {
                    mInCallContactNumber.setVisibility(View.GONE);
                    mInCallContactName.setText("" + mContactNumber);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Bitmap contactbitmap = ContactMethodHelper.getContactImage(getApplicationContext(),
                    mContactNumber);

            if (contactbitmap != null) {
                mContactPhoto.setImageBitmap(MethodHelper.getRoundedCornerBitmap(
                        contactbitmap, 15));
            } else {
                mContactPhoto.setImageResource(R.drawable.ic_calling_avatar);
            }
        }

        if (mPrefProvider.getPrefBoolean("incallspeaker") == true) {
            mAudioManager.setSpeakerphoneOn(true);
            mPrefProvider.setPrefBoolean("incallspeaker", true);
            mInCallSpeakerImg.setSelected(true);



        } else {
            mAudioManager.setSpeakerphoneOn(false);
            mPrefProvider.setPrefBoolean("incallspeaker", false);
            mInCallSpeakerImg.setSelected(false);


        }


        registerReceiver(endcall_reciever, new IntentFilter("finish_Call"));
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        // Telephony listner for GSM call state
        if (mPhoneConnectivityReceiver == null) {
            mPhoneConnectivityReceiver = new ServicePhoneStateReceiver();
            mTelephonyManager.listen(mPhoneConnectivityReceiver,
                    PhoneStateListener.LISTEN_CALL_STATE);
        }

        boolean mute = mPrefProvider.getPrefBoolean("incallmute");
        try {
            if (!mute) {
                VoxEngine.JNI_VX_UnMuteCall(mSwigPointerInstance, Constants.MIC_DEFAULT_VOLUME);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        // Bluetooth objects initialisation
        if (mBluetoothWrapper == null) {
            mBluetoothWrapper = new BluetoothWrapper(this);
        }

        // This method scans paired bluetooth devices
        scanNearestDevices();

        AudioMethodHelper.setAppVolume(mAudioManager, mPrefProvider);
        setVolumeLevel(Constants.DEFAULT_OR_EXISTING_VALUE);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);

        if (mAdView != null) {
            mAdView.pause();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }

        Log.i("InCallCardActivity", "InCallCardActivity.onResume()");
        try {
            mSensorManager.registerListener(this, mSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
            boolean speaker_status = mPrefProvider.getPrefBoolean("incallspeaker");

            if (mSpeakerClicked || speaker_status) {
                mAudioManager.setSpeakerphoneOn(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {

        if (mAdView != null) {
            mAdView.destroy();
        }

        Log.i("InCallCardActivity", "onDestroy called");
        try {
            NotificationService obj2 = NotificationService
                    .getInstance();
            if (obj2 != null)
                obj2.cancelCalls();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Constants.IS_MAKECALL_CALLED = false;
        mPrefProvider.setPrefBoolean("isCallLive", false); // Using to resume call while switching network
        mPrefProvider.setPrefBoolean("isGSMCall", false);
        //  SipManager.setCurrentCallInfo(null);

        if (callStatuserviceReceiver != null) {
            unregisterReceiver(callStatuserviceReceiver);
        }

        if (endcall_reciever != null) {
            unregisterReceiver(endcall_reciever);
        }
        if (mHeadsetReceiver != null) {
            unregisterReceiver(mHeadsetReceiver);
        }

        if (mBluetoothReceiver != null)
            unregisterReceiver(mBluetoothReceiver);
        if (mPhoneConnectivityReceiver != null) {
            mTelephonyManager.listen(mPhoneConnectivityReceiver,
                    PhoneStateListener.LISTEN_NONE);
            mPhoneConnectivityReceiver = null;
        }
        try {
            if (isRecordEnabled) {
                stopRecording();
            }

            if (mRinger.isRinging())
                mRinger.stopRing();

            if (mAddCallDialog != null && mAddCallDialog.isShowing())
                mAddCallDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            AudioMethodHelper.resetAppVolume(mAudioManager, mPrefProvider);
            if (mAudioManager == null)
                mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            // Resets to previous audio focus state
            AudioMethodHelper.setAudioFocus(mAudioManager, mPrefProvider, false);
            mEndCallHandler.removeCallbacks(mEndCallRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    BroadcastReceiver endcall_reciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals("finish_Call")) {

                runOnUiThread(new Runnable() {
                    public void run() {
                        try {


                            // Network crash fix below code added
                            ArrayList<CallInfo> mCallList = SipManager.getCallListInfo();
                            // need to check any entries available before iterate Callinfo
                            if (mCallList != null && mCallList.size() > 0) {
                                CallInfo callInfo = null;
                                for (int i = 0; i < mCallList.size(); i++) {
                                    callInfo = mCallList.get(i);
                                    Log.i(TAG, "Call status " + callInfo.getCallState() + "call number " + callInfo.getCallContactNumber() + "Hold Status " + callInfo.isCallOnHold());
                                    if (callInfo != null && callInfo.getCallState() <= Inv_states.VX_INV_STATE_CONFIRMED) {
                                        SipManager.releaseTheRunningCall(callInfo);
                                        callInfo.setCallState(Inv_states.VX_INV_STATE_DISCONNECTING);
                                    }
                                }
                            }

                            if (MethodHelper.isNetworkAvailable(getApplicationContext())) {
                                Toast.makeText(InCallCardActivity.this, "Network switched, Disconnecting the call", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(InCallCardActivity.this, "Internet is not available, Disconnecting the call", Toast.LENGTH_SHORT).show();
                            }
                            Log.e("InCallCardActivity", "Closing activity from endcall_reciever");
                            closeActivity();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    };

    private class HeadSetIntentReceiver extends BroadcastReceiver {
        boolean isSpeaker;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        // Log.d(TAG, "Headset is unplugged");
                        Log.i("InCallCardActivity", "Headset is unplugged");
                        speakerEnableOrDisable(isSpeaker);
                        break;
                    case 1:
                        Log.i("InCallCardActivity", "Headset is plugged");
                        isSpeaker = mPrefProvider.getPrefBoolean("incallspeaker");
                        speakerEnableOrDisable(false);
                        break;
                    default:
                        Log.i("InCallCardActivity", "I have no idea what the headset state is");
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int status = 0;
        switch (v.getId()) {

            case R.id.incall_answercall_img:

                // Answers the incoming app to app call
                VoxEngine.JNI_VX_AnswerCall(mCurrentCallInfo.getCallId(), 200, mSwigPointerInstance);

                break;

            case R.id.incall_endcall_img:
                // End call click listener
                if (mIsEndCallBeingProcessed) {
                    return;
                }
                mIsEndCallBeingProcessed = true;

                try {

                    Log.i("InCallCardActivity", "hang up calling");
                    //if (true == VoxEngine.JNI_VX_IsAppInitialized(p__VX_ERROR)) {
                    if (mCurrentCallInfo != null)
                        VoxEngine.JNI_VX_ReleaseCall(mCurrentCallInfo.getCallId(), mSwigPointerInstance);
                    mCurrentCallInfo.setCallState(Inv_states.VX_INV_STATE_DISCONNECTING);
                    Intent intent=new Intent(this,Home.class);
                    startActivity(intent);
                    finish();
                    //}

                } catch (Throwable e) {
                    e.printStackTrace();
                }

                NotificationService obj = NotificationService.getInstance();
                if (obj != null) {
                    obj.cancelCalls();

                    String reg_status = mPrefProvider.getPrefString("Registration");
                    if (reg_status.equals("Registered"))
                        obj.notificationsforRegister();
                }

                mEndCallHandler.postDelayed(mEndCallRunnable, mInterval);
                Log.i("InCallCardActivity", "hangup status=" + status);
                break;

            case R.id.incallspeaker_img:

                // Below method handles speaker ON/OFF functionality
                boolean speaker_status = mPrefProvider.getPrefBoolean("incallspeaker");
                speakerEnableOrDisable(!speaker_status);

                break;

            case R.id.incallhold_img:

                // Below method handles hold and resume functionality
                holdOrResume();

                break;

            case R.id.incall_mute_img:

                boolean mute_statuss = mPrefProvider.getPrefBoolean("incallmute");

                if (mute_statuss == false) {
                    mPrefProvider.setPrefBoolean("incallmute", true);
                    mInCallMuteImg.setSelected(true);



                    VoxEngine.JNI_VX_MuteCall(mSwigPointerInstance);

                } else {
                    mPrefProvider.setPrefBoolean("incallmute", false);
                    mInCallMuteImg.setSelected(false);

                    VoxEngine.JNI_VX_UnMuteCall(mSwigPointerInstance, Constants.MIC_DEFAULT_VOLUME);
                }

                break;

            case R.id.incall_keypad_img:

                try {
                    DTMFDialog();
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                break;

            case R.id.incallbluetooth_img:
                showBluetooth();
                break;


            default:
                break;
        }
    }

    /**
     * This method adds another call and redirects to conference screen.
     */
    private void addCallDialog(final int type) {
        if (mAddCallDialog == null) {
            mAddCallDialog = new Dialog(InCallCardActivity.this);
            mAddCallDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mAddCallDialog.setContentView(R.layout.dialog_conference_add_call);
            mAddCallDialog.setCancelable(false);
            mAddCallDialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));

            TextView titleTextView = (TextView) mAddCallDialog
                    .findViewById(R.id.add_call_heading_tv);
            final EditText phoneNumberTextView = (EditText) mAddCallDialog.findViewById(R.id.phone_number_et);
            RelativeLayout closePopup = (RelativeLayout) mAddCallDialog.findViewById(R.id.close_add_call_layout);
            Button okButton = (Button) mAddCallDialog.findViewById(R.id.add_call_ok_button);

            switch (type) {
                case ADD_CALL:
                    titleTextView.setText("Add Call");
                    break;
                case CALL_TRANSFER:
                    titleTextView.setText("Transfer Call");
                    break;
            }

            closePopup.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mAddCallDialog != null)
                        mAddCallDialog.dismiss();
                    mAddCallDialog = null;
                }
            });

            okButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    String phoneNumber = phoneNumberTextView.getText().toString().trim();

                    if (phoneNumber.length() > 0) {

                        switch (type) {
                            case ADD_CALL:
                                // Holds current call
                                int status = VoxEngine.JNI_VX_HoldCall(mCurrentCallInfo.getCallId(),
                                        mSwigPointerInstance);

                                mCurrentCallInfo = SipManager.getCallInfoFromCallList(mCurrentCallInfo.getCallId());
                                mCurrentCallInfo.setCallOnHold(true);
                                SipManager.updateCallListInfo(mCurrentCallInfo);

                                int accID = mPrefProvider.getPrefInt("AccID");

                                // New changes to make reusable code.
                                int result = SipMethodHelper.makeCall(InCallCardActivity.this, mPrefProvider, accID, phoneNumber);

                                if (result == Constants.MAKE_CALL_ERROR_CODE) {
                                    return;
                                }

                                Intent intent = new Intent(InCallCardActivity.this, ConferenceActivity.class);
                                intent.putExtra("callId", result);
                                intent.putExtra("contactNumber", phoneNumber);
                                startActivityForResult(intent, 1);
                                break;

                            case CALL_TRANSFER:

                                String transferURI = "sip:" + phoneNumber + "@" + mPrefProvider.getPrefString("switchip");
                                Log.i(TAG, "call Transfer URI: " + transferURI);
                                VoxEngine.JNI_VX_TransferCall(mCurrentCallInfo.getCallId(), transferURI, mSwigPointerInstance);

                                break;
                        }

                        if (mAddCallDialog != null)
                            mAddCallDialog.dismiss();
                        mAddCallDialog = null;

                    } else {
                        Toast.makeText(InCallCardActivity.this, "Please enter number", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            if (mAddCallDialog != null)
                mAddCallDialog.show();
        }

    }

    private void speakerEnableOrDisable(boolean isEnable) {
        Log.i(TAG, "speakerEnableOrDisable: " + isEnable);
        try {
            mAudioManager.setSpeakerphoneOn(isEnable);
            mPrefProvider.setPrefBoolean("incallspeaker", isEnable);
            mSpeakerClicked = isEnable;
            mInCallSpeakerImg.setSelected(isEnable);
            if (!isEnable) {


            } else {

                mInCallSpeakerImg.setSelected(true);
                mInCallBluetoothImg.setSelected(false);
                mBluetoothWrapper.setBluetoothOn(false);
                mBluetoothWrapper.isBluetoothConnected = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hold Resume
    protected void holdOrResume() {
        boolean hold_status = mPrefProvider.getPrefBoolean("incallhold");
        if (hold_status == false) {
            int status = VoxEngine.JNI_VX_HoldCall(mCurrentCallInfo.getCallId(),
                    mSwigPointerInstance);
            Log.i("InCallCardActivity", "holdOrResume status=" + status);
            mPrefProvider.setPrefBoolean("incallhold", true);
            mInCallHoldImg.setSelected(true);


        } else {
            mPrefProvider.setPrefBoolean("incallhold", false);
            mInCallHoldImg.setSelected(false);
            int status = VoxEngine.JNI_VX_ResumeCall(mCurrentCallInfo.getCallId(),
                    mSwigPointerInstance);
            Log.i("InCallCardActivity", "JNI_VX_IsRemoteHold status=" + status);
            /*   mInCallHoldImg.setBackgroundResource(R.drawable.incall_normal);*/



        }

        mCurrentCallInfo = SipManager.getCallInfoFromCallList(mCurrentCallInfo.getCallId());
        mCurrentCallInfo.setCallOnHold(!hold_status);
        SipManager.updateCallListInfo(mCurrentCallInfo);

    }

    // for registration status update
    private BroadcastReceiver callStatuserviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int callId = intent.getIntExtra("callId", Constants.PJSIP_RETURN_STATUS_DEFAULT);
            callStatus = intent.getIntExtra("callStatus", Constants.PJSIP_RETURN_STATUS_DEFAULT);


            String callduration = intent.getStringExtra("remortduration");

            if (callduration != null && callduration.length() > 0) {
                callDuration = Integer.parseInt(callduration);
                Log.i("InCallCardActivity", "log duration conformed:" + callDuration);
            }

            switch (callStatus) {
                case Inv_states.VX_INV_STATE_CALLING:
                    mInCallCallStatusCode.setText("Calling...");
                    break;

                case Inv_states.VX_INV_STATE_EARLY:
                    mInCallCallStatusCode.setText("Calling...");
                    break;

                case Inv_states.VX_INV_STATE_CONNECTING:
                    mInCallCallStatusCode.setText("Connecting..");
                    break;
                case Inv_states.VX_INV_STATE_CONFIRMED:
                    if (callId == mCurrentCallInfo.getCallId()) {
                        if (mCurrentCallInfo.getCallType() == SipConstants.CALL_INCOMING && mRinger.isRinging()) {
                            mRinger.stopRing();
                        }
                        confirmedTime = System.currentTimeMillis();
                        mInCallAnswerCallImg.setVisibility(View.GONE);

                        mPrefProvider.setPrefBoolean("isCallLive", true); // Using to resume call while switching network
                        mInCallElapsedTime.setVisibility(View.VISIBLE);

                        mInCallElapsedTime.setBase(SystemClock.elapsedRealtime());
                        mInCallElapsedTime.start();

                        mPrefProvider.setPrefBoolean("mChronometerStart", true);
                        mInCallCallStatusCode.setText("In Call");
                        mInCallHoldImg.setEnabled(true);
                        mInCallMuteImg.setEnabled(true);
                        mInCallKeypadImg.setEnabled(true);
                    }
                    break;

                case Inv_states.VX_INV_STATE_DISCONNECTED:

                    ArrayList<CallInfo> callList = SipManager.getCallListInfo();

                    // It will aovid conference calls
                    if (callList.size() == 0 && callId == mCurrentCallInfo.getCallId()) {
                        mPrefProvider.setPrefBoolean("iscalldisconnected", true);
                        mCallStatusMessage = intent.getExtras().getString("Statusmsg");
                        mAudioManager.setMode(AudioManager.MODE_NORMAL);
                        Log.i("InCallCardActivity", "Log call status : " + mCallStatusMessage);

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                NotificationService obj2 = NotificationService
                                        .getInstance();
                                if (obj2 != null)
                                    obj2.cancelCalls();
                            }
                        });

                        mInCallElapsedTime.stop();
                        mPrefProvider.setPrefBoolean("add_call", false);

                        if (mCallStatusMessage != null && mCallStatusMessage.length() > 0) {
                            Log.i(TAG, "statusChangeReceiver, callStatusMessage: " + mCallStatusMessage);
                            if (mCallStatusMessage.startsWith("Session")) {
                                mInCallCallStatusCode.setText("Calling...");
                            } else if (mCallStatusMessage.equalsIgnoreCase("Ringing")) {
                                mInCallCallStatusCode.setText("Ringing...");
                            } else
                                mInCallCallStatusCode.setText("" + mCallStatusMessage);
                        }

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "Handler count zero and call finish");
                                mPrefProvider.setPrefBoolean("add_call", false);
                                //mPrefProvider.setPrefBoolean("att_transfer", false);
                                mPrefProvider.setPrefBoolean("incallspeaker", false);
                                mPrefProvider.setPrefBoolean("incallmute", false);
                                NotificationService obj2 = NotificationService
                                        .getInstance();
                                if (obj2 != null)
                                    obj2.cancelCalls();
                                Log.e("InCallCardActivity", "Closing activity from Inv_states.VX_INV_STATE_DISCONNECTED");
                                closeActivity();
                            }
                        });
                    }
                    break;


                case Inv_states.VX_INV_STATE_INCOMING:
                    mInCallAnswerCallImg.setVisibility(View.VISIBLE);
                    mInCallCallStatusCode.setText("Incoming Call");

                    break;
                case 0:
                    Log.i(TAG, "Network came call Status: " + mCallStatusMessage);
                    if (mCallStatusMessage != null && mCallStatusMessage.length() > 0) {
                        Log.i(TAG, "statusChangeReceiver, callStatusMessage: " + mCallStatusMessage);
                        if (mCallStatusMessage.equalsIgnoreCase("OK")) {
                            mInCallCallStatusCode.setText("In Call");
                        } else if (mCallStatusMessage.startsWith("Session")) {
                            mInCallCallStatusCode.setText("Calling...");
                        } else if (mCallStatusMessage.equalsIgnoreCase("Ringing")) {
                            mInCallCallStatusCode.setText("Ringing...");
                        } else
                            mInCallCallStatusCode.setText("" + mCallStatusMessage);
                    } else {
                        if (mPrefProvider.getPrefBoolean("isCallLive")) {
                            mInCallCallStatusCode.setText("In Call");
                        } else {
                            mInCallCallStatusCode.setText("Calling...");
                        }
                    }

                    break;

                case -2:
                   /* if (!mInCallCallStatusCode.getText().toString().equals("Reconnecting")) {
                        Log.e(TAG, "Network lost call Status: " + mInCallCallStatusCode.getText().toString());
                        mCallStatusMessage = mInCallCallStatusCode.getText().toString();
                    }*/

                    mInCallCallStatusCode.setText("Reconnecting");
                    break;

                default:
                    break;
            }

            // Updating call Notification.
            if (callStatus != Inv_states.VX_INV_STATE_DISCONNECTED) {
                NotificationService obj = NotificationService.getInstance();
                Log.i(TAG, "callStatusReceiver contactNumber: " + mContactNumber + " , Status message: " + mInCallCallStatusCode.getText()
                        .toString());
                if (obj != null)
                    obj.showNotificationForCall(mInCallCallStatusCode.getText()
                            .toString(), mContactNumber);
            }

        }

    };


    /**
     * This method closes current activity
     */
    private void closeActivity() {
        finish();
    }

    void updateUIFromNotification(final int callid) {
        runOnUiThread(new Runnable() {

            public void run() {
                try {

                    Log.i("InCallCardActivity", "incallcard updateuiswap Call id: "
                            + callid);

                    JNICallInfo = new VX_CallInfo();

                    VoxEngine.JNI_VX_GetCallInfo(callid, JNICallInfo, mSwigPointerInstance);
                    Log.i(TAG, "incallcard updateuiswap Log Call status: "
                            + JNICallInfo.getLast_status_text());

                    // For remote contact
                    if (JNICallInfo.getRemote_info() != null
                            && JNICallInfo.getRemote_info().contains("@")) {
                        String remortcontact = JNICallInfo.getRemote_info();
                        if (remortcontact.length() > 0) {
                            int start = remortcontact.indexOf(":");
                            int end = remortcontact.indexOf("@");

                            mContactNumber = remortcontact.substring(start + 1,
                                    end);
                        }
                    }

                    String callduration = "" + JNICallInfo.getConnect_duration();
                    if (callduration != null && callduration.length() > 0) {
                        callDuration = Integer.parseInt(callduration);
                        Log.i("InCallCardActivity", "log duration conformed:" + callDuration);
                    }

                    mCallStatusMessage = JNICallInfo.getLast_status_text();
                    Log.i("InCallCardActivity", "Log VX_CallID update ui callDuration"
                            + callDuration);

                    if (JNICallInfo.getCallState() == Inv_states.VX_INV_STATE_CONFIRMED) {
                        mInCallAnswerCallImg.setVisibility(View.GONE);
                        mInCallElapsedTime.setVisibility(View.VISIBLE);
                        mInCallElapsedTime.setBase(SystemClock
                                .elapsedRealtime() - (callDuration * 1000));
                        mInCallElapsedTime.start();

                        mPrefProvider.setPrefBoolean("mChronometerStart", true);
                        mInCallHoldImg.setEnabled(true);
                        mInCallMuteImg.setEnabled(true);
                        mInCallKeypadImg.setEnabled(true);
                    }

                    if (mCallStatusMessage.equalsIgnoreCase("OK")) {
                        mInCallCallStatusCode.setText("In Call");
                    } else if (mCallStatusMessage.equalsIgnoreCase("Ringing")) {
                        mInCallCallStatusCode.setText("Ringing...");
                    } else {
                        if (mCallStatusMessage.startsWith("Session")) {
                            mInCallCallStatusCode.setText("Calling...");
                        } else
                            mInCallCallStatusCode.setText("" + mCallStatusMessage);
                    }

                    NotificationService obj = NotificationService.getInstance();

                    Log.i(TAG, "updateUIFromNotification contactNumber: " + mContactNumber);
                    if (obj != null)
                        obj.showNotificationForCall(mInCallCallStatusCode
                                .getText().toString(), mContactNumber);

                    if (mContactNumber != null && mContactNumber.length() > 0) {
                        String contact_name = ContactMethodHelper.getContactNameForCallLogs(
                                mContactNumber, getApplicationContext());
                       /* String[] webser_details = contact_name.split("%24%");
                        contact_name = webser_details[0];*/
                        if(contact_name!=null){
                            contact_name = ContactMethodHelper.getContactName(mContactNumber,
                                    getApplicationContext());
                        }
                        try {
                            if (contact_name != null) {
                                mInCallContactName.setText("" + contact_name);
                                if (contact_name.matches("[0-9]+")) {
                                    mInCallContactNumber.setVisibility(View.GONE);
                                } else {
                                    mInCallContactNumber.setVisibility(View.VISIBLE);
                                    mInCallContactNumber.setText("" + mContactNumber);
                                }

                            } else {
                                mInCallContactNumber.setVisibility(View.GONE);
                                mInCallContactName.setText("" + mContactNumber);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * This method shows the keypad to play DTMF.
     */
    void DTMFDialog() {
        final Dialog dtmf_dialog = new Dialog(InCallCardActivity.this);

        dtmf_dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {

                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dtmf_dialog.dismiss();
                }

                return true;
            }
        });

        dtmf_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dtmf_dialog.setContentView(R.layout.dialog_dtmf);
        dtmf_dialog.setCancelable(false);
        dtmf_dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Window window = dtmf_dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        wlp.windowAnimations = R.style.DialogAnimation;
        window.setAttributes(wlp);
        dtmf_dialog.getWindow().setLayout(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);

        mNumberPadEditTextView = (EditText) dtmf_dialog
                .findViewById(R.id.screen_tab_dialer_editText_number);
        LinearLayout view_dialpad_text_textView_num1 = (LinearLayout) dtmf_dialog
                .findViewById(R.id.view_dialpad_text_textView_num1);
        LinearLayout view_dialpad_text_textView_num2 = (LinearLayout) dtmf_dialog
                .findViewById(R.id.view_dialpad_text_textView_num2);
        LinearLayout view_dialpad_text_textView_num3 = (LinearLayout) dtmf_dialog
                .findViewById(R.id.view_dialpad_text_textView_num3);
        LinearLayout view_dialpad_text_textView_num4 = (LinearLayout) dtmf_dialog
                .findViewById(R.id.view_dialpad_text_textView_num4);
        LinearLayout view_dialpad_text_textView_num5 = (LinearLayout) dtmf_dialog
                .findViewById(R.id.view_dialpad_text_textView_num5);
        LinearLayout view_dialpad_text_textView_num6 = (LinearLayout) dtmf_dialog
                .findViewById(R.id.view_dialpad_text_textView_num6);
        LinearLayout view_dialpad_text_textView_num7 = (LinearLayout) dtmf_dialog
                .findViewById(R.id.view_dialpad_text_textView_num7);
        LinearLayout view_dialpad_text_textView_num8 = (LinearLayout) dtmf_dialog
                .findViewById(R.id.view_dialpad_text_textView_num8);
        LinearLayout view_dialpad_text_textView_num9 = (LinearLayout) dtmf_dialog
                .findViewById(R.id.view_dialpad_text_textView_num9);
        LinearLayout view_dialpad_text_textView_numstar = (LinearLayout) dtmf_dialog
                .findViewById(R.id.view_dialpad_text_textView_numstar);
        LinearLayout view_dialpad_text_textView_num0 = (LinearLayout) dtmf_dialog
                .findViewById(R.id.view_dialpad_text_textView_num0);
        LinearLayout view_dialpad_text_textView_numhash = (LinearLayout) dtmf_dialog
                .findViewById(R.id.view_dialpad_text_textView_numhash);
        ImageView dtmf_dialog_hide = (ImageView) dtmf_dialog
                .findViewById(R.id.btn_dtmf_hide);
        ImageView dtmf_backspace_img = (ImageView) dtmf_dialog
                .findViewById(R.id.dtmf_backspace_img);

        mNumberPadEditTextView.setInputType(InputType.TYPE_NULL);
        dtmf_dialog_hide.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dtmf_dialog.dismiss();
            }
        });

        mNumberPadEditTextView
                .addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String dtmf_text = mNumberPadEditTextView
                                .getText().toString().trim();

                        if (dtmf_text.length() > 0) {
                            String val = dtmf_text.substring(dtmf_text.length() - 1);
                            Log.i("InCallCardActivity", "DTMF is called" + val);
                            invokeDtmf(val);
                        }
                    }
                });

        dtmf_backspace_img.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mNumberPadEditTextView.setText("");
                return false;
            }
        });

        dtmf_backspace_img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final int selStart = mNumberPadEditTextView
                        .getSelectionStart();
                final String number = mNumberPadEditTextView
                        .getText().toString();

                if (selStart > 0) {
                    final StringBuffer sb = new StringBuffer(number);
                    sb.delete(selStart - 1, selStart);
                    mNumberPadEditTextView.setText(sb.toString());
                    mNumberPadEditTextView
                            .setSelection(selStart - 1);
                }
            }
        });


        view_dialpad_text_textView_num1
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("1");
                    }
                });

        view_dialpad_text_textView_num2
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("2");
                    }
                });

        view_dialpad_text_textView_num3
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("3");
                    }
                });

        view_dialpad_text_textView_num4
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("4");
                    }
                });

        view_dialpad_text_textView_num5
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("5");
                    }
                });

        view_dialpad_text_textView_num6
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("6");
                    }
                });

        view_dialpad_text_textView_num7
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("7");
                    }
                });

        view_dialpad_text_textView_num8
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("8");
                    }
                });

        view_dialpad_text_textView_num9
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("9");
                    }
                });

        view_dialpad_text_textView_numstar
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("*");
                    }
                });

        view_dialpad_text_textView_num0
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("0");
                    }
                });

        view_dialpad_text_textView_numhash
                .setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("#");
                    }
                });

        if (dtmf_dialog != null)
            dtmf_dialog.show();
    }

    private void StringAppend(String appendtext) {
        if (mNumberPadEditTextView.getText().toString().length() < 25) {
            mSelectedText = new StringBuffer(mNumberPadEditTextView
                    .getText().toString());
            final int selStart = mNumberPadEditTextView
                    .getSelectionStart();
            mSelectedText.insert(selStart, appendtext);
            mNumberPadEditTextView.setText(mSelectedText.toString());
            mNumberPadEditTextView.setSelection(selStart + 1);
        }
    }

    protected void invokeDtmf(String val) {
        try {
            //if (true == VoxEngine.JNI_VX_IsAppInitialized(p__VX_ERROR)) {
            VoxEngine.JNI_VX_DialDtmf(mCurrentCallInfo.getCallId(), val, 0, mSwigPointerInstance);
            //}
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.i(TAG, "Sensor Name: " + event.sensor.getName() + " , event value: " + event.values[0]+" , maximum Range: "+mSensor.getMaximumRange());
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] < mSensor.getMaximumRange()) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mSensorView.setVisibility(View.VISIBLE);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mSensorView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i("InCallCardActivity", "onNewIntent on new intent called");

        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("CallID")) {
                updateUIFromNotification(mCurrentCallInfo.getCallId());
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disabled back button
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                setVolumeLevel(Constants.EVENT_DOWN);
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                setVolumeLevel(Constants.EVENT_UP);
                break;
            case KeyEvent.KEYCODE_ENDCALL:
                Log.i(TAG, "OnKeyDown: End Call");
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * This method set the adjust the volume for call
     *
     * @param event volume up or down
     */
    private void setVolumeLevel(int event) {
        try {

            Log.i(TAG, "setVolumeLevel, volume level: " + mStackVolumeLevel);
            switch (event) {
                case Constants.EVENT_UP:
                    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);

                    int actual_volume= audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);

                    /** Along with app volume increasing signaling volume from stack **/
                    mStackVolumeLevel = mStackVolumeLevel + Constants.SPEAKER_CHANGE_LEVEL_UP;
                    Log.i(TAG, "Constants.EVENT_UP: level: " + mStackVolumeLevel);
                    if (mStackVolumeLevel > Constants.SPEAKER_MAX_VOLUME ) {
                        mStackVolumeLevel = Constants.SPEAKER_MAX_VOLUME;
                    } else {
                        if(actual_volume <= maxVolume){
                            VoxEngine.JNI_VX_Adjust_tx_level(0, mStackVolumeLevel);
                            Log.i("InCallCardActivity", "Current volume is after increasing"
                                    + mStackVolumeLevel);
                            mPrefProvider.setPrefFloat("speakerlevel", mStackVolumeLevel);
                        }

                    }
                    break;
                case Constants.EVENT_DOWN:
                    /** Along with app volume decreasing signaling volume from stack **/
                    mStackVolumeLevel = mStackVolumeLevel - Constants.SPEAKER_CHANGE_LEVEL_DOWN;
                    Log.i(TAG, "Constants.EVENT_DOWN, level: " + mStackVolumeLevel);
                    if (mStackVolumeLevel < Constants.SPEAKER_MIN_VOLUME) {
                        mStackVolumeLevel = Constants.SPEAKER_MIN_VOLUME;
                    } else {
                        Log.i("InCallCardActivity", "Current volume is after decreasing"
                                + mStackVolumeLevel);
                        VoxEngine.JNI_VX_Adjust_tx_level(0, mStackVolumeLevel);
                        mPrefProvider.setPrefFloat("speakerlevel", mStackVolumeLevel);
                    }
                    break;
                case Constants.DEFAULT_OR_EXISTING_VALUE:

                    SharedPreferences sharedPref = getSharedPreferences(Constants.SHARED_PREFERENCE_MOSIP,
                            Context.MODE_PRIVATE);
                    mStackVolumeLevel = sharedPref.getFloat("speakerlevel", Constants.SPEAKER_DEFAULT_VOLUME);

                    Log.i(TAG, "Current volume is in first case" + mStackVolumeLevel);
                    VoxEngine.JNI_VX_Adjust_tx_level(0, mStackVolumeLevel);

                    if (Build.MODEL.equalsIgnoreCase("Nexus 4")) {
                        VoxEngine.JNI_VX_Adjust_rx_level(0, Constants.MIC_DEFAULT_VOLUME_NEXUS_4);
                    } else {
                        VoxEngine.JNI_VX_Adjust_rx_level(0, Constants.MIC_DEFAULT_VOLUME);
                    }

                    break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    /*********************** Call Recording *************/

    /**
     * This method starts audio recording.
     *
     * @param callId caller id of call
     * @param way    call is in incoming call more or outgoing
     */
    private void startRecording(int callId, int way) throws Exception {
        if (way == 0) {
            way = SipConstants.BITMASK_ALL;
        }
        try {
            // File recFolder = getRecordsFolder(getApplicationContext());
            File recFolder = MethodHelper.getFolder(Constants.CALL_RECORDING_FILE_PATH);
            VX_CallInfo callInfo = new VX_CallInfo();
            VoxEngine.JNI_VX_GetCallInfo(callId, callInfo, mSwigPointerInstance);
            recorder = new SimpleWavRecorderHandler(callInfo, recFolder, way);
            recorder.startRecording(mCurrentCallInfo.getCallId());
            recordingStartingTime = System.currentTimeMillis();
            handler.postDelayed(recordingRunnable, 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method pause the call recording.
     */
    private void pauseRecording(int callId) {
        try {
            recorder.pauseRecording(callId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method resumes call recording.
     */
    private void resumeRecording(int callId) {
        try {
            recorder.startRecording(callId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method stops call recording and stores file information in DB.
     */
    private void stopRecording() {
        try {
            isRecordEnabled = false;

            //incall_record_indicator_linear.setVisibility(View.INVISIBLE);
            if (recorder != null) {
                RecordingsDB recordingsDB = new RecordingsDB(getApplicationContext()).open();
                ContentValues values = new ContentValues();
                values.put("table_row_number", mContactNumber);
                values.put("table_row_time", "" + recordingStartingTime);
                Date startDate = new Date(recordingStartingTime);
                Date endDate = new Date(System.currentTimeMillis());
                long dur = endDate.getTime() - startDate.getTime();
                values.put("table_row_duration", MethodHelper.convertSecondsToHMmSs(dur / 1000));
                values.put("table_row_path", recorder.recordingPath);
                recordingsDB.addRow(values);
                recordingsDB.close();
                recordingStartingTime = 0;
                handler.removeCallbacks(recordingRunnable);
                if (MethodHelper.isNetworkAvailable(InCallCardActivity.this)) {
                    recorder.stopRecording();
                }
                recorder = null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    /*************************
     * Bluetooth Code
     *****************************/

    /**
     * This scans bluetooth devices if any device connected enable voice in bluetooth by default
     */
    public void scanNearestDevices() {
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null)
            return;

        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            int headset_state;

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    headset_state = BluetoothAdapter.getDefaultAdapter()
                            .getProfileConnectionState(BluetoothProfile.HEADSET);

                    Log.i(TAG, "scanNearestDevices, headset state: " + headset_state);

                    if (headset_state == BluetoothProfile.STATE_CONNECTED) {
                        //speakerEnableOrDisable(false);
                        mBluetoothWrapper.setBluetoothOn(true);
                        mInCallBluetoothImg.setSelected(true);


                        mBluetoothWrapper.isBluetoothConnected = true;
                    } else if (headset_state == BluetoothProfile.STATE_DISCONNECTED) {
                        //speakerEnableOrDisable(false);
                        mBluetoothWrapper.setBluetoothOn(false);
                        mInCallBluetoothImg.setSelected(false);

                        mBluetoothWrapper.isBluetoothConnected = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Monitor profile events
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED);
        registerReceiver(mBluetoothReceiver, filter);
    }

    /**
     * This method connects voice to bluetooth device.
     */
    protected void showBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();

        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                if (mBluetoothWrapper.isBTHeadsetConnected()) {
                    if (mBluetoothWrapper != null) {
                        try {
                            if (mBluetoothWrapper.isBluetoothConnected) {
                                speakerEnableOrDisable(false);
                                mBluetoothWrapper.setBluetoothOn(false);
                                mInCallBluetoothImg.setSelected(false);
                                mBluetoothWrapper.isBluetoothConnected = false;
                            } else {
                                speakerEnableOrDisable(false);
                                mBluetoothWrapper.setBluetoothOn(true);
                                mInCallBluetoothImg.setSelected(true);
                                mBluetoothWrapper.isBluetoothConnected = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Intent settingsIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                    startActivity(settingsIntent);
                }
            } else {
                Intent settingsIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(settingsIntent);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth not supported",
                    Toast.LENGTH_LONG).show();
        }
    }

    // The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.i("InCallCardActivity", "Bluetooth Receiver action: " + action);
            if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) { // when bluetooth connected
                try {
                    Thread t = new Thread() {
                        public void run() {
                            try {
                                Thread.sleep(5000);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showBluetooth();
                                    }
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    t.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) { // when bluetooth
                // disconnected
                try {
                    speakerEnableOrDisable(false);
                    mBluetoothWrapper.setBluetoothOn(false);
                    mInCallBluetoothImg
                            .setSelected(false);
                    mBluetoothWrapper.isBluetoothConnected = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }  else if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
                Log.i("InCallCardActivity", "Bluetooth Receiver, Audio connected state: " + state);

                mBluetoothWrapper.audioManager.setBluetoothScoOn(mBluetoothWrapper.targetBt);

                if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                    Log.i("InCallCardActivity", "Head set audio connected");
                    mInCallBluetoothImg.setSelected(true);

                    mBluetoothWrapper.isBluetoothConnected = true;
                } else if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                    Log.i("InCallCardActivity", "Audio disconnected");
                    mInCallBluetoothImg.setSelected(false);

                    mBluetoothWrapper.isBluetoothConnected = false;
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            int callId = data.getIntExtra("callId", Constants.PJSIP_RETURN_STATUS_DEFAULT);
            Log.i(TAG, "CallerID in onActivityResult " + callId);

            if (callId != Constants.PJSIP_RETURN_STATUS_DEFAULT) {
                mCurrentCallInfo = SipManager.getCallInfoFromCallList(callId);
                if (mCurrentCallInfo != null) {
                    int status = VoxEngine.JNI_VX_ResumeCall(mCurrentCallInfo.getCallId(), mSwigPointerInstance);
                    mInCallCallStatusCode.setText("In Call");
                    mContactNumber = mCurrentCallInfo.getCallContactNumber();
                    NotificationService notificationService = NotificationService.getInstance();
                    if (notificationService != null) {
                        notificationService.showNotificationForCall(mInCallCallStatusCode.getText().toString().trim(), mContactNumber);
                    }

                    if (mContactNumber != null && mContactNumber.length() > 0) {
                        String contact_name = ContactMethodHelper.getContactNameForCallLogs(
                                mContactNumber, getApplicationContext());
                       /* String[] webser_details = contact_name.split("%24%");
                        contact_name = webser_details[0];*/
                        if(contact_name!=null){
                            contact_name = ContactMethodHelper.getContactName(mContactNumber,
                                    getApplicationContext());
                        }
                        try {
                            if (contact_name != null) {
                                mInCallContactName.setText("" + contact_name);
                                if (contact_name.matches("[0-9]+")) {
                                    mInCallContactNumber.setVisibility(View.GONE);
                                } else {
                                    mInCallContactNumber.setVisibility(View.VISIBLE);
                                    mInCallContactNumber.setText("" + mContactNumber);
                                }

                            } else {
                                mInCallContactNumber.setVisibility(View.GONE);
                                mInCallContactName.setText("" + mContactNumber);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                closeActivity();
            }
        }
    }

    private class ServicePhoneStateReceiver extends PhoneStateListener {
        @Override
        public void onCallStateChanged(final int state,
                                       final String incomingNumber) {
            mPrefProvider.setPrefInt("GSM state: ", state);
            try {
                /*Log.i(TAG, "Invoke onCallStateChanged in ServicePhoneStateReceiver initializeJNI status="
                        + mJNIInitStatus);

                if (mJNIInitStatus == Constants.PJSIP_RETURN_STATUS_DEFAULT)
                    return;*/

                //int callcount = VoxEngine.JNI_VX_GetCallCount(p__VX_ERROR);
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                if (state == TelephonyManager.CALL_STATE_RINGING || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    mPrefProvider.setPrefBoolean("isGSMCall", true);

                    /** If we get GSM call disconnecting our call if call is not connected **/
                    if (mCurrentCallInfo != null && mCurrentCallInfo.getCallState() < Inv_states.VX_INV_STATE_CONFIRMED) {
                        VoxEngine.JNI_VX_ReleaseCall(mCurrentCallInfo.getCallId(), mSwigPointerInstance);
                    } else if (mCurrentCallInfo != null && mCurrentCallInfo.getCallState() == Inv_states.VX_INV_STATE_CONFIRMED) {
                        AudioMethodHelper.setAudioFocus(audioManager, mPrefProvider, false);
                        VoxEngine.JNI_VX_HoldCall(mCurrentCallInfo.getCallId(), mSwigPointerInstance);
                        mCurrentCallInfo.setCallOnHold(true);
                    }
                } else {
                    if (mPrefProvider.getPrefBoolean("isGSMCall") == true) {
                        mPrefProvider.setPrefBoolean("isGSMCall", false);
                        boolean speaker_status = mPrefProvider.getPrefBoolean("incallspeaker");

                        if (speaker_status) {
                            audioManager.setSpeakerphoneOn(true);
                        }

                        //Log.i(TAG, "Resume is going to call: call state: " + mJNICallInfo.getCallType());
                        if (mCurrentCallInfo != null && mCurrentCallInfo.getCallState() == Inv_states.VX_INV_STATE_CONFIRMED) {
                            // This method changes the audio focus
                            AudioMethodHelper.setAudioFocus(audioManager, mPrefProvider, true);
                            VoxEngine.JNI_VX_ResumeCall(SipManager.getCurrentCallInfo().getCallId(), mSwigPointerInstance);
                        } else {
                            audioManager.setMode(AudioManager.MODE_NORMAL);
                        }
                    }

                }
                super.onCallStateChanged(state, incomingNumber);

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
