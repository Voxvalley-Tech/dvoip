/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.vx.core.jni.SipManager;

import vx.plt.SWIGTYPE_p__VX_ERROR;
import vx.plt.VoxEngine;

public class RecentCloseService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.i("RecentCloseService", "Recent onTaskRemoved invoked");

        closeAppActivities();

        stopSelf();
    }

    /*@Override
    public void onDestroy() {
        super.onDestroy();

        closeAppActivities();
    }*/

    /**
     * This method cancel all notifications and shutdowns stack.
     */
    private void closeAppActivities() {
        NotificationManager notifManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
        //Intent intent=new Intent(Home.packageName+".RecentClose");
        //sendBroadcast(intent);

        try {
            SWIGTYPE_p__VX_ERROR p__VX_ERROR = SipManager.getSwigPointerInstance();
            //if (true == VoxEngine.JNI_VX_IsAppInitialized(p__VX_ERROR)) {
            Log.i("RecentCloseService", "RecentCloseService.java. Invoking shutdownApp");
            VoxEngine.JNI_VX_ShutdownApp(p__VX_ERROR);
            //}
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
