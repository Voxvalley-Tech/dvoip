package com.vx.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;

/**
 * This class provide permission request in marshmallow device.
 *
 * @author Ramesh Reddy
 */

public class PermissionUtils {

    public static String[] PERMISSIONS = {Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE};

    public static String[] CONTACTSPERMISSION = {Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS};

    public static int PERMISSION_REQUEST_CODE = 99;

    /**
     * This method request all the permissions which are required for this application
     *
     * @param currentActivity running activity context
     */
    public static void requestForAllPermission(Activity currentActivity) {

        if (!hasPermissions(currentActivity, PERMISSIONS)) {
            ActivityCompat.requestPermissions(currentActivity, PERMISSIONS, PERMISSION_REQUEST_CODE);

        }

    }

    /**
     * This method checks whether this application has requested permissions or not.
     *
     * @param context     application context
     * @param permissions requested permissions
     * @return boolean variable
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.i("PermissionUtils", "Failed at Permission: " + permission);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This method requests for contacts permission to read the contacts.
     *
     * @param currentActivity
     * @param permission
     */
    public static void requestPermission(Activity currentActivity, String[] permission) {
        if (!hasPermissions(currentActivity, permission)) {
            ActivityCompat.requestPermissions(currentActivity, permission, PERMISSION_REQUEST_CODE);

        }
    }


    public static boolean checkCameraPermission(Activity currentActivity) {
        int result = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkLocationPermission(Activity currentActivity) {
        int result = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_FINE_LOCATION);
        int result1 = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_COARSE_LOCATION);


        if (result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkReadContactsPermission(Activity currentActivity) {
        int result = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.READ_CONTACTS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkWriteContactsPermission(Activity currentActivity) {
        int result = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.WRITE_CONTACTS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkAccessFineLocationPermission(Activity currentActivity) {
        int result = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkAccessCoarseLocationPermission(Activity currentActivity) {
        int result = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkRecordAudioPermission(Activity currentActivity) {
        int result = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.RECORD_AUDIO);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkReceiveSMSPermission(Activity currentActivity) {
        int result = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.RECEIVE_SMS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkReadSMSPermission(Activity currentActivity) {
        int result = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.READ_SMS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkReadExternalStoragePermission(Activity currentActivity) {
        int result = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkWriteExternalStoragePermission(Activity currentActivity) {
        int result = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkCallPhonePermission(Activity currentActivity) {
        int result = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.CALL_PHONE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

}
