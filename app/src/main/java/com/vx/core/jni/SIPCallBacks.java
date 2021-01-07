package com.vx.core.jni;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.vx.core.android.db.CallLogsDB;
import com.vx.core.android.model.CallInfo;
import com.vx.core.android.service.NotificationService;
import com.vx.core.android.utils.MethodHelper;
import com.vx.ui.Home;
import com.vx.ui.dialpad.DialerFragment;
import com.vx.ui.incall.InCallCardActivity;
import com.vx.utils.Constants;
import com.vx.utils.DataParser;
import com.vx.utils.PreferenceProvider;

import java.util.Calendar;

import vx.plt.VX_AppCallback;
import vx.plt.VX_CallInfo;

/**
 *
 */

public class SIPCallBacks extends VX_AppCallback {

    private String TAG = "SIPCallBacks";
    Context mContext;
    PreferenceProvider preferenceProvider;
    private VX_CallInfo mJNICallInfo;
    private int mRemoteContactDuration = 0;
    private String mRemoteContactNumber = "";
    private String RTPDumpStatus = "";
    private int mCallState;
    // Below code to resume calls while switching network.
    private final int mIntervalToSwitchNetwork = 30000; // 30 Seconds
    private Handler mNetworkSwitchHandler = new Handler();
    private Runnable mNetworkSwitchRunnable = new Runnable() {
        public void run() {
            if (!MethodHelper.isNetworkAvailable(mContext)) {
                Log.e(TAG, "Network lost disconnecting call");
                shutDownNetwork();
            }
        }
    };

    public SIPCallBacks(Context context) {
        mContext = context;
        preferenceProvider = PreferenceProvider.getPrefInstance(mContext);

        // register the broadcast receiver for error calls
        IntentFilter networkCheckIntentFilter = new IntentFilter();
        networkCheckIntentFilter.addAction(Home.packageName + ".NetworkStatus");
        mContext.registerReceiver(networkStatusReceiver, networkCheckIntentFilter);

        // Screen ON and OFF receiver register
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mScreenStateReceiver, screenStateFilter);
    }

    @Override
    public int onRegStateCb(int accountID, final int arg1) {
        Log.i(TAG, "OnRegStateCb: AccountId:" + accountID + "arg1:" + arg1);
        //accId = accountID;

        try {
            Intent intent = new Intent(Home.packageName + ".RegistrationStatus");
            intent.putExtra("RegStatus", arg1);
            //intent.putExtra("AccountID", accId);
            mContext.sendBroadcast(intent);

            //preferenceProvider.setPrefInt("AccID", accId);
            preferenceProvider.setPrefBoolean("islogin", true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.onRegStateCb(accountID, arg1);
    }

    @Override
    public void onCallMediaStateCb(int callid) {
        Log.i(TAG, "onMediaStateCd" + callid);
        super.onCallMediaStateCb(callid);
    }

    @Override
    public int onCallStateCb(final int callerId, final int arg1,
                             VX_CallInfo JNICallInfo) {

        mJNICallInfo = JNICallInfo;
        mRemoteContactDuration = mJNICallInfo.getConnect_duration();
        String lastCallStatus = JNICallInfo.getLast_status_text();
        RTPDumpStatus = RTPDumpStatus + JNICallInfo.getCall_dump();
        int statusCode = JNICallInfo.getStatus_code();
        Log.i(TAG,"response code "+JNICallInfo.getStatus_code());
        new CallStateChangeAsyncTask(callerId, arg1, lastCallStatus,statusCode).execute();

        if (Inv_states.VX_INV_STATE_DISCONNECTED == arg1 && mRemoteContactDuration > 0) {
            new GetBalanceAsyncTask().execute();
        }

        return super.onCallStateCb(callerId, arg1, JNICallInfo);
    }

    @Override
    public int onIncomingCallCb(final int arg0, String arg1) {
        //Incoming call callback
        Log.i(TAG, "arg0: " + arg0 + ", arg1: " + arg1);

        CallInfo currentCallInfo = SipManager.getCurrentCallInfo();
        if (currentCallInfo.getCallType() == SipConstants.CALL_NONE) {

            SipManager.answerIncommingCall(arg0, 180);

            if (arg1 != null && arg1.length() > 0) {
                // "2000" <sip:2000@96.31.69.52>
                int start = arg1.indexOf(":");
                int end = arg1.indexOf("@");
                mRemoteContactNumber = arg1.substring(start + 1, end);
            }

            CallInfo callInfo = new CallInfo();
            callInfo.setCallId(arg0);
            callInfo.setCallType(SipConstants.CALL_INCOMING);
            callInfo.setCallContactNumber(mRemoteContactNumber);

            SipManager.setCurrentCallInfo(callInfo);
            SipManager.addCallInfo(callInfo);

            preferenceProvider.setPrefString("lastcallnumber", "" + mRemoteContactNumber);

            try {
                Intent incall_intent = new Intent(
                        mContext,
                        InCallCardActivity.class);
                incall_intent.putExtra("ISCall", "income");
                incall_intent.putExtra("ContactNum", ""
                        + mRemoteContactNumber);
                incall_intent.putExtra("callID", arg0);
                incall_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(incall_intent);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            if (arg1 != null && arg1.length() > 0) {
                int start = arg1.indexOf(":");
                int end = arg1.indexOf("@");
                mRemoteContactNumber = arg1.substring(start + 1, end);
            }

            boolean isdnd = preferenceProvider.getPrefBoolean("dnd");
            if (!isdnd) {
                NotificationService obj = NotificationService.getInstance();
                if (obj != null)
                    obj.showNotificationForMissedCall(
                            mRemoteContactNumber);
            }

            SipManager.answerIncommingCall(arg0, 486);

            return -1;
        }

        return super.onIncomingCallCb(arg0, arg1);
    }

    /**
     * This class performs call stage change actions.
     *
     * @author Ramesh Reddy
     */
    class CallStateChangeAsyncTask extends AsyncTask<Void, Void, Void> {

        int callerIdValue, callStateValue, statusCode;
        String lastCallStatus;

        CallStateChangeAsyncTask(int callerId, int callState,String lastCallStatus,int statusCode) {
            this.callerIdValue = callerId;
            this.callStateValue = callState;
            this.lastCallStatus = lastCallStatus;
            this.statusCode = statusCode;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                mCallState = callStateValue;
                Log.i(TAG, "onCallState Callback is called callId: " + callerIdValue);

                NotificationService regobj = NotificationService.getInstance();
                if (regobj != null) {
                    regobj.cancelRegisters();
                }

                // Updating current CallInfo
                CallInfo callInfo = SipManager.getCallInfoFromCallList(callerIdValue);
                if (callInfo != null) {
                    callInfo.setCallState(callStateValue);
                    SipManager.updateCallListInfo(callInfo);
                }

                if (Inv_states.VX_INV_STATE_DISCONNECTED == mCallState) {

                    Log.i(TAG, "RTPDumpStatus: " + RTPDumpStatus);
                    Intent feedbackBrodcast = new Intent(Home.packageName + ".SendFeedback");
                    feedbackBrodcast.putExtra("RTPDumpStatus",RTPDumpStatus);
                   mContext.sendBroadcast(feedbackBrodcast);
                    RTPDumpStatus = "";
                    Log.i(TAG, "RTP Statistics: " + RTPDumpStatus);
                    SipManager.removeCallInfo(callerIdValue);
                }

                try {
                    Log.i(TAG, "makeCall status " + Constants.IS_MAKECALL_CALLED + " lascallSttus " + lastCallStatus);
                    // giving delay for sending call backs if call screen not open even after disconnect call back
                    if (!Constants.IS_MAKECALL_CALLED && (statusCode >= 400 && statusCode <= 700)) {
                        Log.i(TAG, "Sleep called for sending broadcast");
                        Thread.sleep(2000);
                    }


                    Log.i(TAG, "Sending broad cast for call Status changed");
                    Intent intent = new Intent(Home.packageName + ".CallStatus");
                    intent.putExtra("callId", callerIdValue);
                    intent.putExtra("callStatus", mCallState);
                    intent.putExtra("remortduration", "" + mRemoteContactDuration);
                    if (Inv_states.VX_INV_STATE_DISCONNECTED == mCallState) {
                        intent.putExtra("Statusmsg", "" + lastCallStatus);
                    }
                    mContext.sendBroadcast(intent);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (Inv_states.VX_INV_STATE_DISCONNECTED == mCallState) {
                    Log.i(TAG, "onCallState VX_INV_STATE_DISCONNECTED ");

                    preferenceProvider.setPrefBoolean("incallhold", false);
                    int callListSize = SipManager.getCallListInfo().size();
                    if (callListSize == 0) {
                        NotificationService obj = NotificationService.getInstance();
                        if (obj != null) {
                            obj.cancelCalls();
                            String reg_status = preferenceProvider.getPrefString("Registration");
                            if (reg_status.equals("Registered"))
                                obj.notificationsforRegister();
                        }
                    }
                    //mRemoteContactDuration = "" + mJNICallInfo.getConnect_duration();

                    long timeInMillis = System.currentTimeMillis();
                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTimeInMillis(timeInMillis);
                    Log.i(TAG, "duration " + mRemoteContactDuration);
                    String totalduration = MethodHelper.convertSecondsToHMmSs(mRemoteContactDuration);

                    CallLogsDB callLogs_db = new CallLogsDB(mContext);
                    callLogs_db.open();

                    mRemoteContactNumber = preferenceProvider.getPrefString("lastcallnumber");
                    // CallLog
                    ContentValues values = new ContentValues();

                    values.put(CallLogsDB.TABLE_ROW_NUMBER, ""
                            + mRemoteContactNumber);
                    values.put(CallLogsDB.TABLE_ROW_TIME, "" + "" + InCallCardActivity.rowTime);
                    values.put(CallLogsDB.TABLE_ROW_DURATION, "" + totalduration);
                    values.put(CallLogsDB.TABLE_ROW_TYPE, "" + Constants.CALL_STATE_OUTGOING);
                    values.put(CallLogsDB.TABLE_ROW_USER_ID, ""
                            + mRemoteContactNumber);
                    callLogs_db.addRow(CallLogsDB.TABLE_NAME, values);


                    callLogs_db.close();

                    Intent intent = new Intent(Home.packageName + ".RECENTUPDATE");
                    mContext.sendBroadcast(intent);

                    PreferenceProvider preference = new PreferenceProvider(mContext);
                    preference.setPrefBoolean(PreferenceProvider.ISCALLLOGSUPDATED, true);

                    Log.i(TAG, "Log call duration of last call: "
                            + totalduration);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    class GetBalanceAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            String usr = preferenceProvider
                    .getPrefString("login_username");
            String pwd = preferenceProvider
                    .getPrefString("login_password");
           /* try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
            final String balValue = DataParser.getBalance(
                    DataParser.bal_url, usr, pwd,mContext);
            Log.i(TAG, "Bal:Call disconnected balance="
                    + balValue);
            if (DialerFragment.balanceTextView != null) {
                DialerFragment.balanceTextView
                        .post(new Runnable() {

                            @Override
                            public void run() {
                                if (balValue != null && balValue.length() > 0) {
                                    DialerFragment.balanceTextView
                                            .setVisibility(View.VISIBLE);
                                    DialerFragment.balanceTextView
                                            .setText("Bal: $ "
                                                    + balValue);
                                } else {
                                    DialerFragment.balanceTextView
                                            .setVisibility(View.INVISIBLE);
                                    DialerFragment.balanceTextView
                                            .setText("");
                                }

                            }
                        });
            }

            return null;
        }
    }

    private BroadcastReceiver networkStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean networkStatus = intent.getBooleanExtra("NetworkStatus", false);
            Log.i(TAG, "Home OnNetworkCB=" + networkStatus);

            /*if (mJNIInitStatus == Constants.PJSIP_RETURN_STATUS_DEFAULT)
                return;*/

            int mDBAccId = preferenceProvider.getPrefInt("AccountID");

            if (networkStatus) {
                /** removing call when network comes **/
                if (mNetworkSwitchHandler.hasMessages(0))
                    mNetworkSwitchHandler.removeCallbacks(mNetworkSwitchRunnable);

                Log.i(TAG, "Network Available");
                preferenceProvider.setPrefBoolean("isbalancehit", true);


                // Informs networks status to calling screen
                Intent callStatusIntent = new Intent(Home.packageName + ".CallStatus");
                callStatusIntent.putExtra("callStatus", 0);
                callStatusIntent.putExtra("remortduration", "" + mRemoteContactDuration);
                mContext.sendBroadcast(callStatusIntent);

                try {

                    if (preferenceProvider.getPrefBoolean("isWrongOrInactiveBrandPin") == true) {
                        Log.i(TAG, "entered into if statement");
                        DialerFragment.registrationStatusTextView.setText(preferenceProvider.getPrefString("WrongOrInactiveBrandPin"));
                    } else {
                        Log.i(TAG, "entered into else statement");
                        // Informs network status to dialer screen
                        Intent reg_intent = new Intent(Home.packageName + ".RegistrationStatus");
                        reg_intent.putExtra("RegStatus", 0);
                        //reg_intent.putExtra("AccountID", 0);
                        mContext.sendBroadcast(reg_intent);
                        SipManager.unRegisterAccount(mDBAccId);
                        SipManager.register(mContext);
                    }

                    //VoxEngine.JNI_VX_UnRegisterAccount(mDBAccId, mSwigPointerInstance);
                    //register(mDBAccId);

                } catch (Throwable e) {
                    e.printStackTrace();
                }

            } else {
                try {

                    Constants.ISNETWORKDISCONNECTED = true;

                    if (!mNetworkSwitchHandler.hasMessages(0)) {
                        /** Below Code to resume the call while switching network **/
                        mNetworkSwitchHandler.postDelayed(mNetworkSwitchRunnable, mIntervalToSwitchNetwork);
                    }

                    // Informs to DialerActivity to update UI
                    Intent registrationIntent = new Intent(Home.packageName
                            + ".RegistrationStatus");
                    registrationIntent.putExtra("RegStatus", -2);
                    //registrationIntent.putExtra("AccountID", 0);
                    mContext.sendBroadcast(registrationIntent);

                    // Informs networks status to calling screen
                    Intent callStatusIntent = new Intent(Home.packageName + ".CallStatus");
                    callStatusIntent.putExtra("callStatus", -2);
                    callStatusIntent.putExtra("remortduration", "" + mRemoteContactDuration);
                    mContext.sendBroadcast(callStatusIntent);

                    /*NotificationService obj = NotificationService.getInstance();
                    if (obj != null)
                        obj.cancelAll();*/

                    //shutDownNetwork();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    };

    protected void shutDownNetwork() {
        try {
            NotificationService obj = NotificationService.getInstance();
            if (obj != null)
                obj.cancelAll();

            preferenceProvider.setPrefString("Registration", "Registering...");

            Intent intent = new Intent("finish_Call");
            mContext.sendBroadcast(intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * This BroadCast Receiver is used for knowing the screen lock and unlock events
     */
    BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i("Check", "Screen went OFF");

            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                try {
                    Log.i("Check", "Screen went ON");
                    String registrationStatus = preferenceProvider.getPrefString("Registration").toString();
                    boolean isNetWorkAvailable = MethodHelper.isNetworkAvailable(mContext);
                    Log.i(TAG, " Registration status " + registrationStatus + " network status " + isNetWorkAvailable);
                    if (!registrationStatus.equals("Registered") && isNetWorkAvailable) {
                        preferenceProvider.setPrefBoolean("isbalancehit", true);
                       // MethodHelper.stopAndStartSIPService(mContext);
                        SipManager.unRegisterAccount(preferenceProvider.getPrefInt("AccID"));
                        SipManager.register(context);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }
        }
    };
}