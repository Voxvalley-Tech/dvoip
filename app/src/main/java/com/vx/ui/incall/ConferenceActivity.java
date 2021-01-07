/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.ui.incall;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.dvoip.R;
import com.vx.core.android.bluetooth.BluetoothWrapper;
import com.vx.core.android.contacts.ContactMethodHelper;
import com.vx.core.android.model.CallInfo;
import com.vx.core.android.service.NotificationService;
import com.vx.core.android.utils.AudioMethodHelper;
import com.vx.core.android.utils.MethodHelper;
import com.vx.core.jni.Inv_states;
import com.vx.core.jni.SipManager;
import com.vx.ui.Home;
import com.vx.utils.Constants;
import com.vx.utils.PreferenceProvider;

import java.util.ArrayList;

import vx.plt.SWIGTYPE_p__VX_ERROR;
import vx.plt.VoxEngine;

/**
 * This class holds two calls and here we can do conference call and allows features like split, swap etc.
 *
 * @author rameshreddy
 */
public class ConferenceActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private static final String TAG = "ConferenceActivity";

    private LinearLayout mConferenceHoldLinear, mConferenceSpeakerLinear, mConferenceMuteLinear, mConferenceKeypadLinear, mConferenceBluetoothLinear, mConferenceSwapLinear, mConferenceAttTransferLinear;
    private ImageView mConferenceHoldImg, mConferenceSpeakerImg, mConferenceMuteImg, mConferenceKeypadImg, mConferenceBluetoothImg, mConferenceMergeImg, mConferenceSplitImg, mConferenceSwapImg, mConferenceContactImg;
    private TextView mConferenceHoldTv, mConferenceSpeakerTv, mConferenceMuteTv, mConferenceKeypadTv, mConferenceBluetoothTv, mConferenceSwapTv, mConferenceContactTv, mConferenceCallStatusTv;
    private ImageView mEndCallButton;
    private Chronometer mCallTimerChronometer;

    private EditText mNumberPadEditTextView;
    private StringBuffer mSelectedText;

    private SWIGTYPE_p__VX_ERROR mSwigPointerInstance;
    private ArrayList<CallInfo> mCallList = new ArrayList<>();
    private CallInfo mCurrentCallInfo = new CallInfo();
    private PreferenceProvider mPrefProvider;
    private AudioManager mAudioManager;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Dialog mSplitCallDialog;
    // Bluetooth Variables
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothWrapper mBluetoothWrapper;

    private boolean mIsSpeakerEnabled = false, mIsMutedCall = false, mIsCallOnHold = false;
    private String mContactNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference);

        mConferenceHoldLinear = (LinearLayout) findViewById(R.id.conference_hold_linear);
        mConferenceBluetoothLinear = (LinearLayout) findViewById(R.id.conference_bluetooth_linear);
        mConferenceKeypadLinear = (LinearLayout) findViewById(R.id.conference_keypad_linear);
        mConferenceMuteLinear = (LinearLayout) findViewById(R.id.conference_mute_linear);
        mConferenceSpeakerLinear = (LinearLayout) findViewById(R.id.conference_speaker_linear);
        mConferenceSwapLinear = (LinearLayout) findViewById(R.id.conference_swap_linear);
        mConferenceAttTransferLinear = (LinearLayout) findViewById(R.id.incal_att_transfer_linear);

        mConferenceContactImg = (ImageView) findViewById(R.id.conference_contact_photo);
        mConferenceHoldImg = (ImageView) findViewById(R.id.conference_hold_img);
        mConferenceBluetoothImg = (ImageView) findViewById(R.id.conference_bluetooth_img);
        mConferenceKeypadImg = (ImageView) findViewById(R.id.conference_keypad_img);
        mConferenceMuteImg = (ImageView) findViewById(R.id.conference_mute_img);
        mConferenceSpeakerImg = (ImageView) findViewById(R.id.conference_speaker_img);
        mConferenceSwapImg = (ImageView) findViewById(R.id.conference_swap_img);
        mConferenceMergeImg = (ImageView) findViewById(R.id.conference_merge_img);
        mConferenceSplitImg = (ImageView) findViewById(R.id.conference_split_img);
        mEndCallButton = (ImageView) findViewById(R.id.conference_end_call_img);

        mConferenceContactTv = (TextView) findViewById(R.id.conference_contact_name);
        mConferenceHoldTv = (TextView) findViewById(R.id.conference_hold_tv);
        mConferenceBluetoothTv = (TextView) findViewById(R.id.conference_bluetooth_tv);
        mConferenceKeypadTv = (TextView) findViewById(R.id.conference_keypad_tv);
        mConferenceMuteTv = (TextView) findViewById(R.id.conference_mute_tv);
        mConferenceSpeakerTv = (TextView) findViewById(R.id.conference_speaker_tv);
        mConferenceSwapTv = (TextView) findViewById(R.id.conference_swap_tv);
        mConferenceCallStatusTv = (TextView) findViewById(R.id.call_status_tv);
        mCallTimerChronometer = (Chronometer) findViewById(R.id.conference_elapsedTime);

        // OnClickListeners
        mConferenceHoldLinear.setOnClickListener(this);
        mConferenceBluetoothLinear.setOnClickListener(this);
        mConferenceKeypadLinear.setOnClickListener(this);
        mConferenceMuteLinear.setOnClickListener(this);
        mConferenceSpeakerLinear.setOnClickListener(this);
        mConferenceMergeImg.setOnClickListener(this);
        mConferenceSplitImg.setOnClickListener(this);
        mConferenceSwapLinear.setOnClickListener(this);
        mEndCallButton.setOnClickListener(this);
        mConferenceAttTransferLinear.setOnClickListener(this);

        mConferenceHoldLinear.setEnabled(false);
        mConferenceMuteLinear.setEnabled(false);
        mConferenceKeypadLinear.setEnabled(false);

        mCallTimerChronometer.setVisibility(View.INVISIBLE);

        mCallList = SipManager.getCallListInfo();

        Intent intent = getIntent();
        if (intent != null) {
            int callId = intent.getIntExtra("callId", Constants.PJSIP_RETURN_STATUS_DEFAULT);
            mCurrentCallInfo = SipManager.getCallInfoFromCallList(callId);
            mContactNumber = intent.getStringExtra("contactNumber");
        }
        mSwigPointerInstance = SipManager.getSwigPointerInstance();
        mPrefProvider = PreferenceProvider.getPrefInstance(getApplicationContext());
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
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

        // This method updates current call UI.
        updateUI();

        // register the broadcast reciever for error calls
        IntentFilter callcountintentfilter = new IntentFilter();
        callcountintentfilter.addAction(Home.packageName + ".CallStatus");
        registerReceiver(callStatuserviceReceiver, callcountintentfilter);

        registerReceiver(endcall_reciever, new IntentFilter("finish_Call"));

        // Bluetooth objects initialisation
        if (mBluetoothWrapper == null) {
            mBluetoothWrapper = new BluetoothWrapper(this);
        }

        // This method scans paired bluetooth devices
        scanNearestDevices();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mSensorManager != null && mSensor != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (callStatuserviceReceiver != null) {
            unregisterReceiver(callStatuserviceReceiver);
        }

        if (endcall_reciever != null) {
            unregisterReceiver(endcall_reciever);
        }
        if (mBluetoothReceiver != null) {
            unregisterReceiver(mBluetoothReceiver);
        }
        if (mSplitCallDialog != null && mSplitCallDialog.isShowing())
            mSplitCallDialog.dismiss();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.conference_merge_img:
                mCallList = SipManager.getCallListInfo();
                if (mCallList.size() >= 2) {
                    int confPort1 = Constants.PJSIP_RETURN_STATUS_DEFAULT, confPort2 = Constants.PJSIP_RETURN_STATUS_DEFAULT;
                    CallInfo callInfo = null;
                    String phoneNumber1 = "", phoneNumber2 = "";
                    for (int i = 0; i < mCallList.size(); i++) {
                        callInfo = mCallList.get(i);
                        Log.i(TAG, "callInfo call ID: " + callInfo.getCallId() + " Number: " + callInfo.getCallContactNumber() + " , isOnHold: " + callInfo.isCallOnHold());

                        if (callInfo.isCallOnHold()) {
                            VoxEngine.JNI_VX_ResumeCall(callInfo.getCallId(), mSwigPointerInstance);
                        }

                        /*switch (i) {
                            case 0:
                                confPort1 = VoxEngine.JNI_VX_GetConfPort(callInfo.getCallId(), mSwigPointerInstance);
                                phoneNumber1 = callInfo.getCallContactNumber();
                                break;
                            case 1:
                                confPort2 = VoxEngine.JNI_VX_GetConfPort(callInfo.getCallId(), mSwigPointerInstance);
                                phoneNumber2 = callInfo.getCallContactNumber();
                                break;
                        }*/
                    }

                    Log.i(TAG, "confPor1: " + confPort1 + " ,confPort2: " + confPort2);
                    /*VoxEngine.JNI_VX_ConferenceConnect(confPort1, confPort2, mSwigPointerInstance);
                    VoxEngine.JNI_VX_ConferenceConnect(confPort2, confPort1, mSwigPointerInstance);*/
                    VoxEngine.JNI_VX_MakeConference(callInfo.getCallId(), mSwigPointerInstance);

                    NotificationService notificationService = NotificationService.getInstance();
                    if (notificationService != null) {
                        notificationService.cancelCalls();
                        notificationService.showNotificationForConferenceCall("ConferenceCall inProgress", phoneNumber1 + "," + phoneNumber2);
                    }
                    mConferenceContactTv.setText("Conference");
                    mConferenceContactImg.setBackgroundResource(R.drawable.ic_contacts_avatar);
                    mConferenceMergeImg.setVisibility(View.GONE);
                    mConferenceSplitImg.setVisibility(View.VISIBLE);
                    mConferenceHoldLinear.setVisibility(View.VISIBLE);
                    mConferenceSwapLinear.setVisibility(View.GONE);
                }


                break;
            case R.id.conference_split_img:

                splitCalls();

                break;

            case R.id.conference_end_call_img:

                try {
                    if (mConferenceContactTv.getText().toString().equals("Conference")) {
                        VoxEngine.JNI_VX_ReleaseAllCalls(mSwigPointerInstance);
                    } else {
                        VoxEngine.JNI_VX_ReleaseCall(mCurrentCallInfo.getCallId(), mSwigPointerInstance);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                //  closeActivity();

                break;

            case R.id.conference_speaker_linear:

                mIsSpeakerEnabled = !mIsSpeakerEnabled;
                speakerEnableOrDisable(mIsSpeakerEnabled);

                break;

            case R.id.conference_keypad_linear:

                try {
                    DTMFDialog();
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                break;

            case R.id.conference_mute_linear:

                if (mIsMutedCall) {
                    mIsMutedCall = false;
                    mConferenceMuteImg.setSelected(false);
                    mConferenceMuteTv.setSelected(false);
                    mConferenceMuteImg
                            .setBackgroundResource(R.drawable.rounded_rectangle_dialpad_textview_normal);
                    VoxEngine.JNI_VX_UnMuteCall(mSwigPointerInstance,Constants.MIC_DEFAULT_VOLUME);
                } else {
                    mIsMutedCall = true;
                    mConferenceMuteImg.setSelected(true);
                    mConferenceMuteTv.setSelected(true);
                    mConferenceMuteImg
                            .setBackgroundResource(R.drawable.rounded_rectangle_dialpad_textview_pressed);
                    VoxEngine.JNI_VX_MuteCall(mSwigPointerInstance);
                }

                break;

            case R.id.conference_hold_linear:

                Log.i(TAG, "Conference Hold clicked, mIsCallOnHold: " + mIsCallOnHold);
                if (mIsCallOnHold) {
                    mIsCallOnHold = false;
                    mConferenceHoldImg.setSelected(false);
                    mConferenceHoldTv.setSelected(false);
                    mConferenceHoldImg
                            .setBackgroundResource(R.drawable.rounded_rectangle_dialpad_textview_normal);
                    int status = VoxEngine.JNI_VX_ResumeCall(mCurrentCallInfo.getCallId(), mSwigPointerInstance);
                    Log.i(TAG, "JNI_VX_ResumeCall status=" + status);
                } else {
                    mIsCallOnHold = true;
                    mConferenceHoldImg.setSelected(true);
                    mConferenceHoldTv.setSelected(true);
                    mConferenceHoldImg.setBackgroundResource(R.drawable.rounded_rectangle_dialpad_textview_pressed);
                    int status = VoxEngine.JNI_VX_HoldCall(mCurrentCallInfo.getCallId(), mSwigPointerInstance);
                    Log.i(TAG, "JNI_VX_HoldCall status=" + status);
                }

                break;

            case R.id.conference_swap_linear:

                mCallList = SipManager.getCallListInfo();
                // Holds current call
                int status = VoxEngine.JNI_VX_HoldCall(mCurrentCallInfo.getCallId(),
                        mSwigPointerInstance);

                CallInfo newCallInfo = mCurrentCallInfo;
                for (int i = 0; i < mCallList.size(); i++) {
                    Log.i(TAG, "Current caller ID: " + mCurrentCallInfo.getCallId());
                    if (mCurrentCallInfo.getCallId() != mCallList.get(i).getCallId()) {
                        int resumeCallStatus = VoxEngine.JNI_VX_ResumeCall(mCallList.get(i).getCallId(), mSwigPointerInstance);
                        newCallInfo = mCallList.get(i);

                    }
                }
                NotificationService notificationService = NotificationService.getInstance();
                if (notificationService != null)
                    notificationService.showNotificationForConferenceCall("Call in Progress", newCallInfo.getCallContactNumber());
                mCurrentCallInfo = newCallInfo;

                updateUI();

                break;

            case R.id.conference_bluetooth_linear:

                showBluetooth();

                break;

            case R.id.incal_att_transfer_linear:

                if (mCallList.size() >= 2) {

                    int callId1 = mCallList.get(0).getCallId();
                    int CallId2 = mCallList.get(1).getCallId();

                    VoxEngine.JNI_VX_TransferCallWithReplaces(callId1, CallId2, mSwigPointerInstance);
                }

                break;

            default:

        }


    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (sensorEvent.values[0] < mSensor.getMaximumRange()) {
                //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                //mCallingControlsView.setVisibility(View.GONE);
                //mSensorView.setVisibility(View.VISIBLE);
            } else {
                //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                //mCallingControlsView.setVisibility(View.VISIBLE);
                //mSensorView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // for registration status update
    private BroadcastReceiver callStatuserviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int callId = intent.getIntExtra("callId", Constants.PJSIP_RETURN_STATUS_DEFAULT);
            int callStatus = intent.getIntExtra("callStatus", Constants.PJSIP_RETURN_STATUS_DEFAULT);
            String callduration = intent.getStringExtra("remortduration");
            int callDuration;

            if (callduration != null && callduration.length() > 0) {
                callDuration = Integer.parseInt(callduration);
                Log.i("InCallCardActivity", "log duration conformed:" + callDuration);
            }

            switch (callStatus) {
                case Inv_states.VX_INV_STATE_CALLING:
                    mConferenceCallStatusTv.setText("Calling...");
                    break;

                case Inv_states.VX_INV_STATE_EARLY:
                    mConferenceCallStatusTv.setText("Calling...");
                    break;

                case Inv_states.VX_INV_STATE_CONNECTING:
                    mConferenceCallStatusTv.setText("Connecting..");
                    break;

                case Inv_states.VX_INV_STATE_CONFIRMED:

                    //mInCallOptionsView.setVisibility(View.VISIBLE);

                    mPrefProvider.setPrefBoolean("isCallLive", true); // Using to resume call while switching network
                    mCallTimerChronometer.setVisibility(View.VISIBLE);
                    mCallTimerChronometer.setBase(SystemClock.elapsedRealtime());
                    mCallTimerChronometer.start();
                    mConferenceMergeImg.setVisibility(View.VISIBLE);
                    mPrefProvider.setPrefBoolean("mChronometerStart", true);
                    mConferenceCallStatusTv.setText("In Call");
                    mConferenceHoldLinear.setEnabled(true);
                    mConferenceMuteLinear.setEnabled(true);
                    mConferenceKeypadLinear.setEnabled(true);

                    break;

                case Inv_states.VX_INV_STATE_DISCONNECTED:

                    mCallList = SipManager.getCallListInfo();
                    if (mCallList.size() < 2) {
                        mPrefProvider.setPrefBoolean("iscalldisconnected", true);

                        mCallTimerChronometer.stop();


                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {

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

                default:
                    break;
            }

            // Updating call Notification.
            if (callStatus != Inv_states.VX_INV_STATE_DISCONNECTED) {
                NotificationService obj = NotificationService.getInstance();
                Log.i(TAG, "callStatusReceiver contactNumber: " + mContactNumber + " , Status message: " + mConferenceCallStatusTv.getText()
                        .toString());
                if (obj != null)
                    obj.showNotificationForCall(mConferenceCallStatusTv.getText()
                            .toString(), mContactNumber);
            }
        }

    };

    BroadcastReceiver endcall_reciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals("finish_Call")) {
                Log.i(TAG, "Closing Conference call screen");
                runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            closeActivity();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    };

    /**
     * This method closes current activity
     */
    private void closeActivity() {

        if (mCallList.size() > 0)
            mCurrentCallInfo = mCallList.get(0);

        Intent intent = new Intent();
        intent.putExtra("callId", mCurrentCallInfo.getCallId());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * This method enables and disable Speaker
     *
     * @param isEnable boolean variable
     */
    private void speakerEnableOrDisable(boolean isEnable) {
        Log.i(TAG, "speakerEnableOrDisable: " + isEnable);
        try {
            mAudioManager.setSpeakerphoneOn(isEnable);

            mConferenceSpeakerImg.setSelected(isEnable);
            mConferenceSpeakerTv.setSelected(isEnable);
            if (isEnable) {
                mConferenceSpeakerImg.setBackgroundResource(R.drawable.rounded_rectangle_dialpad_textview_pressed);
            } else {
                mConferenceSpeakerImg.setBackgroundResource(R.drawable.rounded_rectangle_dialpad_textview_normal);
            }
            /*if (!isEnable) {
                if (mBluetoothWrapper.isBTHeadsetConnected()) {
                    mInCallBluetoothImg.setSelected(true);
                    mInCallBluetoothTextView.setSelected(true);
                    mInCallBluetoothImg
                            .setBackgroundResource(R.drawable.rounded_rectangle_dialpad_textview_pressed);
                    mBluetoothWrapper.setBluetoothOn(true);
                    mBluetoothWrapper.isBluetoothConnected = true;
                }
                //mConferenceSpeakerImg.setSelected(false);
            } else {
                mInCallBluetoothImg.setSelected(false);
                mInCallBluetoothTextView.setSelected(false);
                mInCallBluetoothImg
                        .setBackgroundResource(R.drawable.rounded_rectangle_dialpad_textview_normal);
                mBluetoothWrapper.setBluetoothOn(false);
                mBluetoothWrapper.isBluetoothConnected = false;
                //mConferenceSpeakerImg.setSelected(true);
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method split the calls using popup window.
     */
    private void splitCalls() {
        if (mSplitCallDialog == null) {
            mSplitCallDialog = new Dialog(ConferenceActivity.this);
            mSplitCallDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mSplitCallDialog.setContentView(R.layout.dialog_conference_split);
            mSplitCallDialog.setCancelable(false);
            mSplitCallDialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));

            final TextView firstCallTextView = (TextView) mSplitCallDialog
                    .findViewById(R.id.split_first_call);
            final TextView secondCallTextView = (TextView) mSplitCallDialog.findViewById(R.id.split_second_call);
            final RelativeLayout closeLayout = (RelativeLayout) mSplitCallDialog.findViewById(R.id.split_close_layout);
            final NotificationService notificationService = NotificationService.getInstance();
            for (int i = 0; i < mCallList.size(); i++) {
                CallInfo callInfo = mCallList.get(i);
                switch (i) {
                    case 0:
                        firstCallTextView.setText(callInfo.getCallContactNumber());
                        firstCallTextView.setTag(callInfo.getCallId());
                        break;
                    case 1:
                        secondCallTextView.setText(callInfo.getCallContactNumber());
                        secondCallTextView.setTag(callInfo.getCallId());
                        break;
                }
            }

            closeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSplitCallDialog != null)
                        mSplitCallDialog.dismiss();
                    mSplitCallDialog = null;
                }
            });

            firstCallTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSplitCallDialog != null)
                        mSplitCallDialog.dismiss();
                    mSplitCallDialog = null;

                    firstCallTextView.setTextColor(getResources().getColor(R.color.contacts_bottom_tv_selected_color));
                    secondCallTextView.setTextColor(getResources().getColor(R.color.contacts_listitem_number_color));

                    VoxEngine.JNI_VX_HoldCall((int) firstCallTextView.getTag(), mSwigPointerInstance);

                    int confport1 = VoxEngine.JNI_VX_GetConfPort((int) firstCallTextView.getTag(), mSwigPointerInstance);
                    int confport2 = VoxEngine.JNI_VX_GetConfPort((int) secondCallTextView.getTag(), mSwigPointerInstance);

                    VoxEngine.JNI_VX_ConferenceDisconnect(confport1, confport2, mSwigPointerInstance);
                    int status = VoxEngine.JNI_VX_ResumeCall((int) secondCallTextView.getTag(), mSwigPointerInstance);

                    mCurrentCallInfo = SipManager.getCallInfoFromCallList((int) secondCallTextView.getTag());
                    if (notificationService != null) {
                        notificationService.showNotificationForConferenceCall("Call in Progress", mCurrentCallInfo.getCallContactNumber());
                    }
                    updateUI();

                    mConferenceMergeImg.setVisibility(View.VISIBLE);
                    mConferenceSplitImg.setVisibility(View.GONE);
                    mConferenceHoldLinear.setVisibility(View.GONE);
                    mConferenceSwapLinear.setVisibility(View.VISIBLE);

                }
            });

            secondCallTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mSplitCallDialog != null)
                        mSplitCallDialog.dismiss();
                    mSplitCallDialog = null;

                    secondCallTextView.setTextColor(getResources().getColor(R.color.contacts_bottom_tv_selected_color));
                    firstCallTextView.setTextColor(getResources().getColor(R.color.contacts_listitem_number_color));

                    VoxEngine.JNI_VX_HoldCall((int) secondCallTextView.getTag(), mSwigPointerInstance);

                    int confport1 = VoxEngine.JNI_VX_GetConfPort((int) firstCallTextView.getTag(), mSwigPointerInstance);
                    int confport2 = VoxEngine.JNI_VX_GetConfPort((int) secondCallTextView.getTag(), mSwigPointerInstance);

                    VoxEngine.JNI_VX_ConferenceDisconnect(confport2, confport1, mSwigPointerInstance);
                    int status = VoxEngine.JNI_VX_ResumeCall((int) firstCallTextView.getTag(), mSwigPointerInstance);

                    mCurrentCallInfo = SipManager.getCallInfoFromCallList((int) firstCallTextView.getTag());
                    if (notificationService != null) {
                        notificationService.showNotificationForConferenceCall("Call in Progress", mCurrentCallInfo.getCallContactNumber());
                    }
                    updateUI();

                    mConferenceMergeImg.setVisibility(View.VISIBLE);
                    mConferenceSplitImg.setVisibility(View.GONE);
                    mConferenceHoldLinear.setVisibility(View.GONE);
                    mConferenceSwapLinear.setVisibility(View.VISIBLE);

                }
            });

            if (mSplitCallDialog != null)
                mSplitCallDialog.show();
        }
    }

    /**
     * This method updates the current calling screen single call UI
     */
    private void updateUI() {
        if (mCurrentCallInfo.getCallContactNumber() != null && mCurrentCallInfo.getCallContactNumber().length() > 0) {
            String contact_name = ContactMethodHelper.getContactName(mCurrentCallInfo.getCallContactNumber(),
                    getApplicationContext());
            /*String[] webser_details = contact_name.split("%24%");
            contact_name = webser_details[0];*/
            mConferenceContactTv.setText("" + contact_name);

            Bitmap contactbitmap = ContactMethodHelper.getContactImage(getApplicationContext(),
                    mCurrentCallInfo.getCallContactNumber());

            if (contactbitmap != null) {
                mConferenceContactImg.setImageBitmap(MethodHelper.getRoundedCornerBitmap(
                        contactbitmap, 15));
            } else {
                mConferenceContactImg.setImageResource(R.drawable.ic_contacts_avatar);
            }
        }
    }

    /**
     * This method shows the keypad to play DTMF.
     */
    void DTMFDialog() {
        final Dialog dtmf_dialog = new Dialog(ConferenceActivity.this);

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
        dtmf_dialog.getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

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
        dtmf_dialog_hide.setOnClickListener(new View.OnClickListener() {

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

        dtmf_backspace_img.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mNumberPadEditTextView.setText("");
                return false;
            }
        });

        dtmf_backspace_img.setOnClickListener(new View.OnClickListener() {
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
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("1");
                    }
                });

        view_dialpad_text_textView_num2
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("2");
                    }
                });

        view_dialpad_text_textView_num3
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("3");
                    }
                });

        view_dialpad_text_textView_num4
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("4");
                    }
                });

        view_dialpad_text_textView_num5
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("5");
                    }
                });

        view_dialpad_text_textView_num6
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("6");
                    }
                });

        view_dialpad_text_textView_num7
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("7");
                    }
                });

        view_dialpad_text_textView_num8
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("8");
                    }
                });

        view_dialpad_text_textView_num9
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("9");
                    }
                });

        view_dialpad_text_textView_numstar
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("*");
                    }
                });

        view_dialpad_text_textView_num0
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        StringAppend("0");
                    }
                });

        view_dialpad_text_textView_numhash
                .setOnClickListener(new View.OnClickListener() {

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
                        mConferenceBluetoothImg.setSelected(true);
                        mConferenceBluetoothTv.setSelected(true);
                        mConferenceBluetoothImg
                                .setBackgroundResource(R.drawable.rounded_rectangle_dialpad_textview_pressed);
                        mBluetoothWrapper.isBluetoothConnected = true;
                    } else if (headset_state == BluetoothProfile.STATE_DISCONNECTED) {
                        //speakerEnableOrDisable(false);
                        mBluetoothWrapper.setBluetoothOn(false);
                        mConferenceBluetoothImg.setSelected(false);
                        mConferenceBluetoothTv.setSelected(false);
                        mConferenceBluetoothImg
                                .setBackgroundResource(R.drawable.rounded_rectangle_dialpad_textview_normal);
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
                                mConferenceBluetoothImg.setSelected(false);
                                mConferenceBluetoothTv.setSelected(false);
                                mConferenceBluetoothImg
                                        .setBackgroundResource(R.drawable.rounded_rectangle_dialpad_textview_normal);
                                mBluetoothWrapper.isBluetoothConnected = false;
                            } else {
                                speakerEnableOrDisable(false);
                                mBluetoothWrapper.setBluetoothOn(true);
                                mConferenceBluetoothImg.setSelected(true);
                                mConferenceBluetoothTv.setSelected(true);
                                mConferenceBluetoothImg
                                        .setBackgroundResource(R.drawable.rounded_rectangle_dialpad_textview_pressed);
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
                    mConferenceBluetoothImg
                            .setSelected(false);
                    mBluetoothWrapper.isBluetoothConnected = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (action.equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
                Log.i("InCallCardActivity", "Bluetooth Receiver, Audio connected state: " + state);

                mBluetoothWrapper.audioManager.setBluetoothScoOn(mBluetoothWrapper.targetBt);

                if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
                    Log.i("InCallCardActivity", "Head set audio connected");
                    mConferenceBluetoothImg.setSelected(true);
                    mConferenceBluetoothTv.setSelected(true);
                    mConferenceBluetoothImg
                            .setBackgroundResource(R.drawable.rounded_rectangle_dialpad_textview_pressed);
                    mBluetoothWrapper.isBluetoothConnected = true;
                } else if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
                    Log.i("InCallCardActivity", "Audio disconnected");
                    mConferenceBluetoothImg.setSelected(false);
                    mConferenceBluetoothTv.setSelected(false);
                    mConferenceBluetoothImg
                            .setBackgroundResource(R.drawable.rounded_rectangle_dialpad_textview_normal);
                    mBluetoothWrapper.isBluetoothConnected = false;
                }
            }
        }
    };

}
