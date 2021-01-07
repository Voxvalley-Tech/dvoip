/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.callback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.vx.core.android.utils.MethodHelper;
import com.vx.ui.Home;
import com.vx.utils.Constants;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final int NO_CONNECTION_TYPE = -1;
    private static int sLastType = NO_CONNECTION_TYPE;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
            Log.i("NetworkChangeReceiver", "isNetwork Available: " + MethodHelper.isNetworkAvailable(context));

            if (Constants.IS_MAKECALL_CALLED) {
                Constants.CURRENT_NETWORK_TYPE = MethodHelper.getNetworkType(context);
                if (!Constants.CURRENT_NETWORK_TYPE.equals(Constants.PREVIOUS_NETWORK_TYPE)) {
                    Log.i("NetworkChangeReceiver","WIFI TO DATA"+Constants.CURRENT_NETWORK_TYPE+Constants.PREVIOUS_NETWORK_TYPE);
                    Constants.PREVIOUS_NETWORK_TYPE=Constants.CURRENT_NETWORK_TYPE;
                    Constants.IS_NETWORK_SWITCHED = true;
                } else {
                    Constants.CURRENT_NETWORK_NAME = MethodHelper.getCurrentSsid(context);
                    if (Constants.CURRENT_NETWORK_NAME != null && !Constants.CURRENT_NETWORK_NAME.equals(Constants.PREVIOUS_NETWORK_NAME)) {
                        Log.i("NetworkChangeReceiver","WIFI TO WIFI"+Constants.CURRENT_NETWORK_NAME+Constants.PREVIOUS_NETWORK_NAME);
                        Constants.PREVIOUS_NETWORK_NAME=Constants.CURRENT_NETWORK_NAME;
                        Constants.IS_NETWORK_SWITCHED = true;
                    }
                }
            }
            final int currentType = activeNetworkInfo != null
                    ? activeNetworkInfo.getType() : NO_CONNECTION_TYPE;
          Log.i("NetworkChangeReceiver","sLastType "+sLastType+"currentType "+currentType);
            // Avoid handling multiple broadcasts for the same connection type
            if ((sLastType != currentType)|| (Constants.IS_NETWORK_SWITCHED)) {

                sLastType = currentType;
                if (activeNetworkInfo!=null) {
                    if (activeNetworkInfo.isConnected()) {
                        Intent networkIntent = new Intent(Home.packageName + ".NetworkStatus");
                        networkIntent.putExtra("NetworkStatus", true);
                        networkIntent.putExtra("NetworkChangeStatus", true);
                        context.sendBroadcast(networkIntent);
                    } else {
                        Intent networkIntent = new Intent(Home.packageName + ".NetworkStatus");
                        networkIntent.putExtra("NetworkStatus", false);
                        context.sendBroadcast(networkIntent);
                    }
                } else {
                    Intent networkIntent = new Intent(Home.packageName + ".NetworkStatus");
                    networkIntent.putExtra("NetworkStatus", false);
                    context.sendBroadcast(networkIntent);
                }

            } else {
                Log.i("NetworkChangeReceiver","activeNetworkInfo "+activeNetworkInfo);
                if (activeNetworkInfo == null) {
                    Intent networkintent = new Intent(Home.packageName + ".NetworkStatus");
                    networkintent.putExtra("NetworkStatus", false);
                    context.sendBroadcast(networkintent);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
