package com.vx.core.android.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.app.dvoip.R;
import com.vx.core.android.callback.NetworkChangeReceiver;
import com.vx.core.android.db.DataBaseHelper;
import com.vx.core.android.db.OPXMLDAO;
import com.vx.core.android.model.CallInfo;
import com.vx.core.android.receivers.ForegroundNotificationReceiver;
import com.vx.core.android.utils.MethodHelper;
import com.vx.core.jni.Inv_states;
import com.vx.core.jni.SipManager;
import com.vx.utils.Constants;
import com.vx.utils.DataParser;
import com.vx.utils.PreferenceProvider;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rameshreddy on 2/8/2018.
 */

public class SIPService extends Service {

    private static String TAG = "SIPService";
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    PreferenceProvider preferenceProvider;
    private NetworkChangeReceiver networkStatusReceiver;

    public native void stringFromJNI(Context activity);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "onStartCommand SIP Service started");
        try {
            SipManager.initLib();


            preferenceProvider = PreferenceProvider.getPrefInstance(getApplicationContext());
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

            stringFromJNI(getApplicationContext());
            int initStatus = SipManager.initializeJNI(getApplicationContext());
            Log.i(TAG, "initialisation status: " + initStatus);
            SipManager.register(getApplicationContext());


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                networkStatusReceiver = new NetworkChangeReceiver();
                IntentFilter networkCheckIntentFilter = new IntentFilter();
                networkCheckIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                getApplicationContext().registerReceiver(networkStatusReceiver, networkCheckIntentFilter);
                Log.i(TAG,"Network status receiver registered");
            }



        } catch (Throwable e) {
            e.printStackTrace();
        }


        return START_NOT_STICKY;

    }
    @Override
    public void onCreate() {
        super.onCreate();

        // Notification for above oreo device according to new background service limitation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            CharSequence name = getApplicationContext().getString(R.string.app_name1);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(NotificationService.CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);

            int icon = R.mipmap.ic_launcher;
            String title = getApplicationContext().getString(R.string.app_name);
            String subTitle = "";
            preferenceProvider = PreferenceProvider.getPrefInstance(getApplicationContext());
            String reg_status = preferenceProvider.getPrefString("Registration");
            if (reg_status != null && reg_status.equals("Registered")) {
                subTitle = getApplicationContext().getString(R.string.notification_register);
            } else {
                subTitle = getApplicationContext().getString(R.string.notification_not_register);
            }
            //long when = System.currentTimeMillis();

            Intent notificationIntent = new Intent(getApplicationContext(), ForegroundNotificationReceiver.class);
            // set intent so it does not start a new activity
            //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // notificationIntent.putExtra("NotificationsMsg", "home");
            notificationIntent.putExtra("IsNotification", "Register");
            PendingIntent intent = PendingIntent.getBroadcast(getApplicationContext(), 0, notificationIntent, 0);
            Notification notification = null;
            // Notification not working in API level 27 so if API level more than 26 this will work
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle(title)
                        .setContentText(subTitle)
                        .setSmallIcon(R.drawable.status_bar_icon)
                        .setColor(Color.parseColor("#BF3D26"))
                        .setChannelId(NotificationService.CHANNEL_ID)
                        .setContentIntent(intent)
                        .build();

            }
            //notification.flags |= Notification.FLAG_NO_CLEAR;

            startForeground(NotificationService.REGISTER_NOTIF_ID, notification);
        }

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("SIPService", "SIPService onTaskRemoved Called");
        super.onTaskRemoved(rootIntent);
    }

    /**
     * Class used for the client Binder. Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SIPService getService() {
            // Return this instance of SIPService so clients can call public
            // methods
            return SIPService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            // if user close the application from recents while call in running state we need to store it in DB
            // below logic will store the running call history in DB
            ArrayList<CallInfo> mCallList = SipManager.getCallListInfo();
            if (mCallList != null && mCallList.size() > 0) {
                CallInfo callInfo = null;
                for (int i = 0; i < mCallList.size(); i++) {
                    callInfo = mCallList.get(i);
                    if (callInfo != null) {
                        Log.i(TAG, "Call status " + callInfo.getCallState() + "call number " + callInfo.getCallContactNumber() + "Hold Status " + callInfo.isCallOnHold());
                        if (callInfo.getCallState() <= Inv_states.VX_INV_STATE_CONFIRMED) {
                            SipManager.releaseTheRunningCall(callInfo);
                            Log.i("SIPService", "Released running Call: " + Constants.IS_MAKECALL_CALLED);
                            callInfo.setCallState(Inv_states.VX_INV_STATE_DISCONNECTING);
                        }

                        Log.i("SIPService", "mISCallLogsUpdateCalled: "+preferenceProvider.getPrefBoolean(PreferenceProvider.ISCALLLOGSUPDATED));

                        if (!preferenceProvider.getPrefBoolean(PreferenceProvider.ISCALLLOGSUPDATED)){
                            MethodHelper.updateCallLogHistory(callInfo, getApplicationContext());

                            Log.i(TAG, "updateCallLogHistory called in for loop");
                        }

                        preferenceProvider.setPrefBoolean(PreferenceProvider.ISCALLLOGSUPDATED, true);

                       /* MethodHelper.updateCallLogHistory(callInfo, getApplicationContext());
                        Log.i(TAG, "updateCallLogHistory called in for loop");*/


                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            //if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

            Log.i("SIPService", "SIPService onDestroy Called");
            PreferenceProvider preferenceProvider = PreferenceProvider.getPrefInstance(getApplicationContext());
            int accID = preferenceProvider.getPrefInt("AccID");
            preferenceProvider.setPrefString("Registration", "Registering...");
            SipManager.unRegisterAccount(accID);
            Intent intent = new Intent(getApplicationContext(), NotificationService.class);
            stopService(intent);

            //}
        } catch (Throwable e) {
            e.printStackTrace();
        }


        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (networkStatusReceiver != null)
                    unregisterReceiver(networkStatusReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
