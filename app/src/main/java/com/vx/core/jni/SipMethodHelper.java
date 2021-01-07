/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.jni;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.vx.core.android.model.CallInfo;
import com.vx.core.android.utils.MethodHelper;
import com.vx.utils.Constants;
import com.vx.utils.PermissionUtils;
import com.vx.utils.PreferenceProvider;

import vx.plt.SWIGTYPE_p__VX_ERROR;
import vx.plt.VX_MediaType;
import vx.plt.VoxEngine;

/**
 * This method provide helper methods which we use for JNI calls.
 */

public class SipMethodHelper {

    private static final String TAG = "SipMethodHelper";

    public static int makeCall(Context context, PreferenceProvider prefProvider, int accID, String phoneNumber) {

        // If OS version is more than Lollipop we ask all below permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionUtils.hasPermissions(context, PermissionUtils.PERMISSIONS)) {
                PermissionUtils.requestForAllPermission((Activity) context);
                return Constants.MAKE_CALL_ERROR_CODE;
            }
        }

        int callId = Constants.PJSIP_RETURN_STATUS_DEFAULT;

        try {
            Log.i(TAG, "Make call called, accID: " + accID + " ,PhoneNumber: " + phoneNumber);
            if (!MethodHelper.isNetworkAvailable(context)) {
                Toast.makeText(context, "Please check your internet connection.", Toast.LENGTH_SHORT).show();

                return Constants.MAKE_CALL_ERROR_CODE;
            }

            String registrationStatus = prefProvider.getPrefString("Registration").toString();
            if (!registrationStatus.equals("Registered")) {
                Toast.makeText(context, "Please Register", Toast.LENGTH_SHORT).show();
                return Constants.MAKE_CALL_ERROR_CODE;
            }

           /* if (prefProvider.getPrefBoolean("isGSMCall")) {
                Toast.makeText(context,
                        "Not permitted!",
                        Toast.LENGTH_SHORT).show();
                return Constants.MAKE_CALL_ERROR_CODE;
            }*/

            SWIGTYPE_p__VX_ERROR p__VX_ERROR = SipManager.getSwigPointerInstance();

            if (phoneNumber.equals(prefProvider.getPrefString("sipusername"))) {
                Toast.makeText(context, "Call is not allowed to same registered user", Toast.LENGTH_SHORT).show();
                return Constants.MAKE_CALL_ERROR_CODE;

            }

            String number = phoneNumber;
            number = number.replace(" ", "").replace("\u00A0", "").replace("+", "").replace("#",
                    "").replace("$", "").replace("#", "").replace("*", "").replace("(", "").replace(")", "").replace("-", "").replace("/", "");
            int len = number.length();

            if (len == 0) {
                Toast.makeText(context, "Please Enter Valid Number", Toast.LENGTH_SHORT).show();
                return Constants.MAKE_CALL_ERROR_CODE;
            }
            boolean isvalidNum = false;
            for (int i = 0; i < len; ++i) {
                if (!(number.charAt(i) == '#' || number.charAt(i) == '*' || (number.charAt(i) >= '0' && number.charAt(i) <= '9'))) {
                    Log.i(TAG, "Number contains special character: " + number.charAt(i) + " , Character ASCII Code: " + (int) number.charAt(i));
                    Toast.makeText(context, "Please Enter Valid Number", Toast.LENGTH_SHORT).show();
                    isvalidNum = true;
                    break;
                }
            }

            if (isvalidNum) {
                return Constants.MAKE_CALL_ERROR_CODE;
            }

            prefProvider.setPrefBoolean("incallhold", false);
            prefProvider.setPrefBoolean("incallspeaker", false);
            prefProvider.setPrefBoolean("incallmute", false);
            String phnum = "sip:" + number + "@" + prefProvider.getPrefString("switchip");

            callId = VoxEngine.JNI_VX_MakeCall(accID, phnum, VX_MediaType.AUDIO,
                    p__VX_ERROR);

            // As Raghava suggested added below logic for end call
            if (callId == -1) {
                Toast.makeText(context, "Resource temporarily busy, Please try after 30 seconds.", Toast.LENGTH_SHORT).show();
                return Constants.MAKE_CALL_ERROR_CODE;
            }
            prefProvider.setPrefString("lastcallnumber", "" + phoneNumber);
            SipManager.setCurrentCallInfo(null);
            CallInfo callInfo = new CallInfo();
            callInfo.setCallType(SipConstants.CALL_OUTGOING);
            callInfo.setCallId(callId);
            callInfo.setCallContactNumber(number);
            SipManager.setCurrentCallInfo(callInfo);
            SipManager.addCallInfo(callInfo);

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return callId;
    }

}
