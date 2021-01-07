/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.app.dvoip.R;
import com.vx.core.android.receivers.ForegroundNotificationReceiver;
import com.vx.ui.Home;
import com.vx.ui.incall.ConferenceActivity;
import com.vx.ui.incall.InCallCardActivity;
import com.vx.utils.PreferenceProvider;

public class NotificationService extends Service {

    final static String ACTION = "NotifyServiceAction";
    final static String STOP_SERVICE = "";
    final static int RQS_STOP_SERVICE = 1;
    public static final String CHANNEL_ID = "vibeplus_channel";


    private NotificationManager notificationManager;
    private Notification myNotification;
    public static NotificationService notificationService;

    private static boolean isInit = false;
    public static final int REGISTER_NOTIF_ID = 1;
    public static final int CALL_NOTIF_ID = REGISTER_NOTIF_ID + 1;
    public static final int CALLLOG_NOTIF_ID = REGISTER_NOTIF_ID + 2;
    public static final int CONFERENCE_NOTIF_ID = REGISTER_NOTIF_ID + 5;
    PreferenceProvider prefprovider;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationService = this;
        Log.i("NotificationService", "service oncreate called");
        prefprovider = PreferenceProvider.getPrefInstance(this);
        //notifyServiceReceiver = new NotifyServiceReceiver();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        cancelAll();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public static NotificationService getInstance() {
        return notificationService;
    }

    public void notificationsforRegister() {

        Context context = getApplicationContext();
        if (notificationManager == null) {
            notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name1);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        int icon = R.mipmap.ic_launcher;
        String title = context.getString(R.string.app_name1);
        //long when = System.currentTimeMillis();

       // Intent notificationIntent = new Intent(context, Home.class);
        // set intent so it does not start a new activity
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // notificationIntent.putExtra("NotificationsMsg", "home");
       // notificationIntent.putExtra("IsNotification", "Register");
       // PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        PendingIntent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent receiverIntent = new Intent(context, ForegroundNotificationReceiver.class);
            receiverIntent.putExtra("IsNotification", "Register");
            intent = PendingIntent.getBroadcast(context, 0, receiverIntent, 0);
        }else{
            Intent notificationIntent = new Intent(context, Home.class);
            // set intent so it does not start a new activity
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            // notificationIntent.putExtra("NotificationsMsg", "home");
            notificationIntent.putExtra("IsNotification", "Register");
            intent= PendingIntent.getActivity(context, 0, notificationIntent, 0);
        }
        Notification notification = null;
        // Notification not working in API level 27 so if API level more than 26 this will work
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(context)
                    .setContentTitle(title)
                    .setContentText(context.getString(R.string.notification_register))
                    .setSmallIcon(R.drawable.status_bar_icon)
                    .setColor(Color.parseColor("#aa4a9b"))
                    .setChannelId(CHANNEL_ID)
                    .setContentIntent(intent)
                    .build();

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification = new Notification.Builder(context)
                    .setContentTitle(title)
                    .setContentText(context.getString(R.string.notification_register))
                    .setSmallIcon(R.drawable.status_bar_icon)
                    .setColor(Color.parseColor("#aa4a9b"))
                    .setContentIntent(intent).build();
        } else {
            notification = new Notification.Builder(context)
                    .setContentTitle(title)
                    .setContentText(context.getString(R.string.notification_register))
                    .setSmallIcon(icon)
                    .setContentIntent(intent).build();
        }



        notification.flags |= Notification.FLAG_NO_CLEAR;
        notificationManager.notify(REGISTER_NOTIF_ID, notification);

        prefprovider.setPrefBoolean("NotificationMsgHome", true);
    }

    public final void cancelRegisters() {
        if (notificationManager == null) {
            notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        notificationManager.cancel(REGISTER_NOTIF_ID);
    }

    // Calls
    public void showNotificationForCall(String msg, String contactName) {

        String callType = null;
        Context context = getApplicationContext();
        @SuppressWarnings("deprecation")
        int icon = android.R.drawable.stat_sys_phone_call;

        if (notificationManager == null) {
            notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name1);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        CharSequence tickerText = "";

        if (msg.contains("Incoming")) {
            tickerText = "Incoming Call";
        } else if (msg.contains("Reconnecting")) {
            tickerText = context.getText(R.string.reconnecting);
        } else {
            tickerText = context.getText(R.string.ongoing_call);
        }

        long when = System.currentTimeMillis();

        Intent notificationIntent = new Intent(context, InCallCardActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        if (msg.contains("Incoming")) {
            notificationIntent.putExtra("ISCall", "income");
        } else {

            notificationIntent.putExtra("ISCall", "notification");
        }
        notificationIntent.putExtra("ContactNum", "" + contactName);
        notificationIntent.putExtra("CallID", "0");
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (msg.contains("Incoming")) {
            callType = "Incoming Call";
        } else {
            callType = (String) context.getText(R.string.ongoing_call);
        }

        Notification notification = null;
        // Notification not working in API level 27 so if API level more than 26 this will work
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(context)
                    .setContentTitle(callType)
                    .setTicker(tickerText)
                    .setWhen(when)
                    .setOngoing(true)
                    .setContentText(contactName)
                    .setSmallIcon(icon)
                    .setChannelId(CHANNEL_ID)
                    .setContentIntent(contentIntent)
                    .build();

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification = new Notification.Builder(context)
                    .setContentTitle(callType)
                    .setTicker(tickerText)
                    .setWhen(when)
                    .setOngoing(true)
                    .setContentText(contactName)
                    .setSmallIcon(icon)
                    .setContentIntent(contentIntent).build();
        } else {
            notification = new Notification.Builder(context)
                    .setContentTitle(callType)
                    .setTicker(tickerText)
                    .setWhen(when)
                    .setOngoing(true)
                    .setContentText(contactName)
                    .setSmallIcon(icon)
                    .setContentIntent(contentIntent).build();
        }


        notification.flags |= Notification.FLAG_NO_CLEAR;
        notificationManager.notify(CALL_NOTIF_ID, notification);
    }

    //Conference call notifications

    public void showNotificationForConferenceCall(String msg, String contactName) {
        // This is the pending call
        Context context = getApplicationContext();
        @SuppressWarnings("deprecation")
        int icon = android.R.drawable.stat_sys_phone_call;

        if (notificationManager == null) {
            notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name1);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        long when = System.currentTimeMillis();

        Intent notificationIntent = new Intent(context, ConferenceActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = null;
        // Notification not working in API level 27 so if API level more than 26 this will work
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(context)
                    .setContentTitle(msg)
                    .setTicker(msg)
                    .setWhen(when)
                    .setOngoing(true)
                    .setContentText(contactName)
                    .setSmallIcon(icon)
                    .setChannelId(CHANNEL_ID)
                    .setContentIntent(contentIntent)
                    .build();

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification = new Notification.Builder(context)
                    .setContentTitle(msg)
                    .setTicker(msg)
                    .setWhen(when)
                    .setOngoing(true)
                    .setContentText(contactName)
                    .setSmallIcon(icon)
                    .setContentIntent(contentIntent).build();
        } else {
            notification = new Notification.Builder(context)
                    .setContentTitle(msg)
                    .setTicker(msg)
                    .setWhen(when)
                    .setOngoing(true)
                    .setContentText(contactName)
                    .setSmallIcon(icon)
                    .setContentIntent(contentIntent).build();
        }

        notification.flags |= Notification.FLAG_NO_CLEAR;
        notificationManager.notify(CALL_NOTIF_ID, notification);
    }


    public void showNotificationForMissedCall(String contactNumber) {

        Context context = getApplicationContext();
        int icon = android.R.drawable.stat_notify_missed_call;

        if (notificationManager == null) {
            notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name1);// The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        CharSequence tickerText = context.getText(R.string.app_name1)+"- " + context.getText(R.string.missed_call);
        long when = System.currentTimeMillis();

        Intent notificationIntent = new Intent(context, Home.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationIntent.putExtra("IsNotification", "Missed");
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        prefprovider.setPrefBoolean("NotificationMsgHome", false);
        prefprovider.setPrefBoolean("NotificationMsgMissed", true);

        Notification notification = null;
        // Notification not working in API level 27 so if API level more than 26 this will work
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notification = new Notification.Builder(context)
                    .setContentTitle(context.getText(R.string.app_name1)+" " + context.getText(R.string.missed_call))
                    .setTicker(tickerText)
                    .setWhen(when)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentText(contactNumber)
                    .setSmallIcon(icon)
                    .setChannelId(CHANNEL_ID)
                    .setContentIntent(contentIntent)
                    .build();

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification = new Notification.Builder(context)
                    .setContentTitle(context.getText(R.string.app_name1)+" " + context.getText(R.string.missed_call))
                    .setTicker(tickerText)
                    .setWhen(when)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentText(contactNumber)
                    .setSmallIcon(icon)
                    .setContentIntent(contentIntent).build();
        } else {
            notification = new Notification.Builder(context)
                    .setContentTitle(context.getText(R.string.app_name1)+" " + context.getText(R.string.missed_call))
                    .setTicker(tickerText)
                    .setWhen(when)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentText(contactNumber)
                    .setSmallIcon(icon)
                    .setContentIntent(contentIntent).build();
        }


        notificationManager.notify(CALLLOG_NOTIF_ID, notification);
    }


    public final void cancelConference() {
        notificationManager.cancel(CONFERENCE_NOTIF_ID);
    }

    public final void cancelCalls() {

        if (notificationManager != null) {
            notificationManager.cancel(CALL_NOTIF_ID);
            notificationManager.cancel(CONFERENCE_NOTIF_ID);
        }

    }

    public final void cancelMissedCalls() {

        if (notificationManager != null)
            notificationManager.cancel(CALLLOG_NOTIF_ID);
    }

    public final void cancelAll() {
        // Do not cancel calls notification since it's possible that there is
        // still an ongoing call.

        cancelRegisters();
        cancelCalls();
        //cancelConference();
        //cancelMessages();
        cancelMissedCalls();
        //cancelVoicemails();
    }


}
