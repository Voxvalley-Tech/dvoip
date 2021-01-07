/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.utils;

import java.io.File;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import androidx.core.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.app.dvoip.BuildConfig;
import com.app.dvoip.R;
import com.vx.core.android.db.CallLogsDB;
import com.vx.core.android.model.CallInfo;
import com.vx.core.android.service.SIPService;
import com.vx.ui.incall.InCallCardActivity;
import com.vx.utils.Constants;
import com.vx.utils.PreferenceProvider;

/**
 * This class contains all helper methods which can be reusable in whole project.
 */
public class MethodHelper {
    private static String TAG = "MethodHelper";

    /**
     * To check network available or not
     *
     * @param context application context
     * @return the network connection is ON return true or it is OFF return
     * false
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager
                    .getActiveNetworkInfo();

            return activeNetworkInfo != null
                    && activeNetworkInfo.isConnectedOrConnecting();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * To check network Type weather WIFI of Data
     *
     * @param context Application Context
     * @return the connected type of network if not connected to any network it will return No Network
     */
    public static String getNetworkType(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnectedOrConnecting()) {
            return "Wifi";
        } else if (mobile.isConnectedOrConnecting()) {
            return "Data";
        }
        return "No Network";
    }

    /**
     * To check the connected wifi name
     *
     * @param context application context
     * @return connexted network name if conncted to data it will return null
     */

    public static String getCurrentSsid(Context context) {

        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !(connectionInfo.getSSID().equals(""))) {
                //if (connectionInfo != null && !StringUtil.isBlank(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
            // Get WiFi status MARAKANA
            WifiInfo info = wifiManager.getConnectionInfo();
            String textStatus = "";
            textStatus += "\n\nWiFi Status: " + info.toString();
            String BSSID = info.getBSSID();
            String MAC = info.getMacAddress();

            List<ScanResult> results = wifiManager.getScanResults();
            ScanResult bestSignal = null;
            int count = 1;
            String etWifiList = "";
            for (ScanResult result : results) {
                etWifiList += count++ + ". " + result.SSID + " : " + result.level + "\n" +
                        result.BSSID + "\n" + result.capabilities + "\n" +
                        "\n=======================\n";
            }
            //  Log.v("MethodHelper", "from SO: \n"+etWifiList);

            // List stored networks
            List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration config : configs) {
                textStatus += "\n\n" + config.toString();
            }
            //  Log.v("MethodHelper","from marakana: \n"+textStatus);
        }
        return ssid;
    }


    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * This method will give the required backend data while user sending feedback to panel
     * @param context
     * @return
     */
    public static String getTheDataForFeedback(Context context) {
        StringBuilder requiredData = new StringBuilder();
        requiredData.append(context.getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME + "|");
        requiredData.append(Build.MODEL + "|");
        requiredData.append(getWifiMacAddress(context) + "|");
        requiredData.append(Build.VERSION.RELEASE + "|");
        if (getNetworkType(context).equals("Data")) {
            requiredData.append(getNetworkClass(context) + "|");
        } else {
            requiredData.append(getNetworkType(context) + "|");
        }
        requiredData.append(getRAMInfo(context) + "|");
        requiredData.append(getAvilableRAMInfo(context) + "|");
        return requiredData.toString();
    }
    /**
     * This method creates folder if not exists
     *
     * @param folderPath folder path
     * @return returns file path
     */
    public static File getFolder(String folderPath) {
        File root = Environment.getExternalStorageDirectory();

        if (root.canWrite()) {
            File dir = new File(folderPath);
            if (!dir.exists()) {
                dir.mkdirs();
                //Log.d(THIS_FILE, "Create directory " + dir.getAbsolutePath());
            }
            return dir;
        }
        return null;
    }

    /**
     * This method converts seconds in to timestamps.
     *
     * @param seconds value
     * @return final timestamp
     */
    public static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        if (h > 0) {
            return String.format("%d:%02d:%02d", h, m, s);
        } else {
            return String.format("%02d:%02d", m, s);
        }
    }

    /**
     * This method gets RAM info.
     *
     * @param context application context
     * @return RAM info
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static long getRAMInfo(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;
        long totalRAM = mi.totalMem / 1048576L;
        Log.i("SipMethodHelper", "Total RAM: " + totalRAM + "MB");
        Log.i("SipMethodHelper", "Available RAM: " + availableMegs + "MB");
        return totalRAM;
    }

    /**
     * This method gets Available RAM info.
     *
     * @param context application context
     * @return RAM info
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static long getAvilableRAMInfo(Context context) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;
        long totalRAM = mi.totalMem / 1048576L;
        Log.i("SipMethodHelper", "Total RAM: " + totalRAM + "MB");
        Log.i("SipMethodHelper", "Available RAM: " + availableMegs + "MB");
        return availableMegs;
    }

    /**
     * This method starts the SIP service.
     *
     * @param context application context
     */
    public static void startSIPService(Context context) {
        try {
            Log.i(TAG, "startSIPService, Is Running service: " + MethodHelper.isGivenServiceRunning(context,
                    SIPService.class));
            if (!MethodHelper.isGivenServiceRunning(context,
                    SIPService.class)) {
                Log.i(TAG, "Starting SIP service freshly");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context,(new Intent(context, SIPService.class)));
                } else {
                    context.startService(new Intent(context, SIPService.class));
                }
            } else {
                context.stopService(new Intent(context, SIPService.class));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context,(new Intent(context, SIPService.class)));
                } else {
                    context.startService(new Intent(context, SIPService.class));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This methods stop and start new service.
     *
     * @param context application context.
     */
    public static void stopAndStartSIPService(Context context) {
        if (!MethodHelper.isGivenServiceRunning(context,
                SIPService.class)) {
            Log.i(TAG, "Starting SIP service");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context,(new Intent(context, SIPService.class)));
            } else {
                context.startService(new Intent(context, SIPService.class));
            }
        } else {
            context.stopService(new Intent(context, SIPService.class));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context,(new Intent(context, SIPService.class)));
            } else {
                context.startService(new Intent(context, SIPService.class));
            }
        }
    }

    /**
     * This method creates job scheduler to check XMPP status
     *
     * @param context application context
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    /*public static void createJobScheduler(Context context) {
        ComponentName mServiceComponent = new ComponentName(context, JobSchedulerService.class);
        JobInfo.Builder builder = new JobInfo.Builder(Constants.UNIQUE_ID_CODE, mServiceComponent);
        *//*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setMinimumLatency(1 * 60 * 1000);
        else*//*
        //builder.setPeriodic(1 * 60 * 1000);
        builder.setMinimumLatency(5 * 1000); // wait at least
        builder.setOverrideDeadline(1 * 30 * 1000); // maximum delay
        builder.setPersisted(true);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require un metered network
        builder.setRequiresDeviceIdle(true); // device should be idle
        builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());

        //Log.e("MethodHelper", "Job scheduler created");
    }*/

    /**
     * Checking Service status
     *
     * @return boolean value
     */
    public static boolean isGivenServiceRunning(Context context,
                                                Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method gets device ID using MAC Address
     *
     * @param context application context
     * @return device id
     */
    public static String getWifiMacAddress(Context context) {
        String wifiMacAddress = "";

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                String interfaceName = "wlan0";
                List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface intf : interfaces) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                        continue;
                    }

                    byte[] mac = intf.getHardwareAddress();
                    if (mac == null) {
                        wifiMacAddress = "";
                    }

                    StringBuilder buf = new StringBuilder();
                    for (byte aMac : mac) {
                        buf.append(String.format("%02X:", aMac));
                    }
                    if (buf.length() > 0) {
                        buf.deleteCharAt(buf.length() - 1);
                    }
                    wifiMacAddress = buf.toString();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            /**
             * Getting device identifier that is MAC identifier
             */
            WifiManager m_wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiMacAddress = m_wm.getConnectionInfo().getMacAddress();
        }


        return wifiMacAddress;
    }

    /**
     * This method will insert the call logs when we close the application from recants while in call
     *
     * @param callInfo
     * @param context
     */
    public static void updateCallLogHistory(CallInfo callInfo, Context context) {
        try {
            // VX_CallInfo vx_callInfo = null;
            int mRemoteContactDuration = 0;
            if (callInfo != null) {
                Log.i(TAG, "updateCallLogHistory called");
                //    vx_callInfo = SipManager.getJNICallInfo(callInfo.getCallId(), vx_callInfo);
                //   if (vx_callInfo != null) {
                // mRemoteContactDuration = vx_callInfo.getConnect_duration();
                long timeInMillis = System.currentTimeMillis();
                String mRemoteContactNumber = callInfo.getCallContactNumber();
                if (InCallCardActivity.confirmedTime > 0) {
                    mRemoteContactDuration = (int) (timeInMillis - InCallCardActivity.confirmedTime);
                } else {
                    mRemoteContactDuration = 0;
                }
                Log.i(TAG, "current time " + timeInMillis + " incall time " + InCallCardActivity.confirmedTime + " Difference is " + mRemoteContactDuration);
                String totalduration = MethodHelper.convertSecondsToHMmSs((mRemoteContactDuration / 1000) % 60);
                CallLogsDB callLogs_db = new CallLogsDB(context);
                callLogs_db.open();
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

                PreferenceProvider preference = new PreferenceProvider(context);
                preference.setPrefBoolean(PreferenceProvider.ISCALLLOGSUPDATED, true);

                //  }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getNetworkClass(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G";
            default:
                return "Unknown";
        }
    }

}
