package com.vx.core.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.vx.ui.Home;
import com.vx.ui.incall.InCallCardActivity;
import com.vx.utils.Constants;

public class ForegroundNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Constants.IS_MAKECALL_CALLED) {
            Intent notificationIntent = new Intent(context, Home.class);
            /* in android version Q we mush shouls use this flag otherwise app will be crashed*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(notificationIntent);
            }else {
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(notificationIntent);
            }
     /*      notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(notificationIntent);*/
        }else{
            Intent callIntent = new Intent(context, InCallCardActivity.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(callIntent);
            }else {
                callIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(callIntent);
            }
            // callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // context.startActivity(callIntent);
           /* callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(callIntent);*/
        }
    }
}
