/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.asynctask;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.app.dvoip.R;
import com.vx.utils.Constants;
import com.vx.utils.PreferenceProvider;

import org.jsoup.Jsoup;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This class checks version from play store based on that show popup for update the application.
 */

public class AppVersionCheckAsyncTask extends AsyncTask<Void, String, String> {

    private Context mContext;
    private String mCurrentVersion;

    public AppVersionCheckAsyncTask(Context context, String currentVersion) {
        mContext = context;
        mCurrentVersion = currentVersion;
    }

    @Override
    protected String doInBackground(Void... voids) {
        String newVersion = null;
        try {
            newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + mContext.getPackageName() + "&hl=it")
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
                    .select("div[itemprop=softwareVersion]")
                    .first()
                    .ownText();
            return newVersion;
        } catch (Throwable e) {
            e.printStackTrace();
            return newVersion;
        }
    }

    @Override
    protected void onPostExecute(String onlineVersion) {
        super.onPostExecute(onlineVersion);

        try {
            if (onlineVersion != null && onlineVersion.length() > 0 && mCurrentVersion != null && mCurrentVersion.length() > 0) {
                //mCurrentVersion = "1.7.0"; //For testing
                Log.i("AppVersionCheck", "currentVersion: " + mCurrentVersion + " , Play store version: " + onlineVersion);
                //if (Float.valueOf(mCurrentVersion) < Float.valueOf(onlineVersion)) {
                //if (!mCurrentVersion.trim().equalsIgnoreCase(onlineVersion.trim())) {
                if (mCurrentVersion.compareTo(onlineVersion) < 0) {
                    showUpdateAppPopup(mContext.getResources().getString(R.string.play_store_new_version_alert));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method shows popup for auto update.
     *
     * @param message information message
     */
    private void showUpdateAppPopup(String message) {
        try {
            final Dialog popupDialog = new Dialog(mContext);
            popupDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            popupDialog.setContentView(R.layout.dialog);
            popupDialog.setCanceledOnTouchOutside(false);
            popupDialog.setCancelable(false);
            popupDialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));

            TextView tvAlertMessage = (TextView) popupDialog
                    .findViewById(R.id.tv_alert_title);

            Button yes = (Button) popupDialog
                    .findViewById(R.id.btn_alert_ok);
            Button no = (Button) popupDialog
                    .findViewById(R.id.btn_alert_cancel);

            tvAlertMessage.setText("" + message);
            yes.setText("Yes");
            no.setText("Remind Later");

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (popupDialog != null)
                        popupDialog.dismiss();

                    try {
                        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GOOGLE_PLAY_URL1 + mContext.getPackageName())));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.GOOGLE_PLAY_URL2 + mContext.getPackageName())));
                    }
                    ((Activity) mContext).finish();

                }
            });
            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (popupDialog != null)
                        popupDialog.dismiss();

                    PreferenceProvider preferenceProvider = PreferenceProvider.getPrefInstance(mContext);
                    String previousTimeStamp = preferenceProvider.getPrefString("appUpdateTimestamp");
                    Log.i("AppVersionCheck", "Previous Time: " + previousTimeStamp);

                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String currentTime = format.format(calendar.getTime());
                    Log.i("AppVersionCheck", "Current Time: " + currentTime);

                    preferenceProvider.setPrefString("appUpdateTimestamp", currentTime);
                }
            });

            popupDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
