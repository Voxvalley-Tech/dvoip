/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.jni;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.app.dvoip.BuildConfig;
import com.app.dvoip.R;
import com.vx.core.android.db.AccountsDB;
import com.vx.core.android.getaccounts.ProfileData;
import com.vx.core.android.model.CallInfo;
import com.vx.core.android.utils.AudioMethodHelper;
import com.vx.core.android.utils.MethodHelper;
import com.vx.utils.Constants;
import com.vx.utils.DataParser;
import com.vx.utils.PreferenceProvider;

import java.util.ArrayList;

import vx.plt.SWIGTYPE_p__VX_ERROR;
import vx.plt.VX_AccountInfo;
import vx.plt.VX_AppConfig;
import vx.plt.VX_CallInfo;
import vx.plt.VX_ENCRYPT_TYPE;
import vx.plt.VX_VoxAppSSL;
import vx.plt.VoxEngine;

/**
 * This class is model class for SIP related API calls.
 */
public class SipManager {

    private static String TAG = "SipManager";
    private static SWIGTYPE_p__VX_ERROR p__VX_ERROR;
    public static int mJNIInitStatus = Constants.PJSIP_RETURN_STATUS_DEFAULT;

    /**
     * callListInfo stores all calls information in ArrayList.
     * <p>
     * ArrayList has taken instead of HashSet because array list values can access faster then Set implemented classes
     * Use ful links to covert ArrayList to HashSet:
     * https://stackoverflow.com/questions/14192532/how-to-prevent-the-adding-of-duplicate-objects-to-an-arraylist
     * https://stackoverflow.com/questions/20126470/java-how-to-keep-unique-values-in-a-list-of-objects
     **/
    private static ArrayList<CallInfo> callListInfo = new ArrayList<>();
    private static CallInfo currentCallInfo;
    private static SIPCallBacks mSIPCallBacks;

    /**
     * This method gets singleton instance for Swig pointer.
     *
     * @return swig pointer instance
     */
    public static SWIGTYPE_p__VX_ERROR getSwigPointerInstance() {
        if (p__VX_ERROR == null)
            p__VX_ERROR = new SWIGTYPE_p__VX_ERROR();

        return p__VX_ERROR;
    }

    /**
     * This method returns all calls info
     *
     * @return array list
     */
    public static ArrayList<CallInfo> getCallListInfo() {

        return callListInfo;
    }

    /**
     * This method clears all calls info
     */
    public static void clearCallListInfo() {
        callListInfo.clear();
    }

    /**
     * This method adds call info object to array list
     *
     * @param callInfo call info object
     * @return returns updated all calls info array list
     */
    public static ArrayList<CallInfo> addCallInfo(CallInfo callInfo) {

        boolean isCallIdExists = false;
        for (int i = 0; i < callListInfo.size(); i++) {
            if (callInfo.getCallId() == callListInfo.get(i).getCallId()) {
                isCallIdExists = true;
            }
        }

        if (!isCallIdExists) {
            callListInfo.add(callInfo);
        } else {
            Log.e(TAG, "Trying to insert duplicate call info");
        }

        return callListInfo;
    }

    /**
     * This method returns particular call information.
     *
     * @param callerId call id
     * @return returns call info on given call id
     */
    public static CallInfo getCallInfoFromCallList(int callerId) {

        CallInfo callInfo = null;
        for (int i = 0; i < callListInfo.size(); i++) {
            if (callerId == callListInfo.get(i).getCallId()) {
                callInfo = callListInfo.get(i);
            }
        }

        return callInfo;
    }

    /**
     * This method updates Call info in callList Array
     *
     * @param callInfo call info object
     * @return updated call list array
     */
    public static ArrayList<CallInfo> updateCallListInfo(CallInfo callInfo) {

        for (int i = 0; i < callListInfo.size(); i++) {
            if (callInfo.getCallId() == callListInfo.get(i).getCallId()) {
                callListInfo.set(i, callInfo);
            }
        }

        return callListInfo;
    }

    /**
     * This method removes single call info from array list
     *
     * @param callID call id
     * @return returns updated all calls info array list
     */
    public static ArrayList<CallInfo> removeCallInfo(int callID) {
        for (int i = 0; i < callListInfo.size(); i++) {
            if (callID == callListInfo.get(i).getCallId()) {
                callListInfo.remove(i);
            }
        }
        return callListInfo;
    }

    /**
     * This method gets current call info
     *
     * @return current call info object
     */
    public static CallInfo getCurrentCallInfo() {
        if (currentCallInfo == null)
            currentCallInfo = new CallInfo();
        return currentCallInfo;
    }

    /**
     * This method updates current call info
     *
     * @param currentCallInformation call info object
     */
    public static void setCurrentCallInfo(CallInfo currentCallInformation) {
        currentCallInfo = currentCallInformation;
    }

    /**
     * This method loads the JNI libraries.
     *
     * @return status
     */
    public static int initLib() {
        // Load pjsua for audio calling
        try {
            System.loadLibrary(SipConstants.LIB_FILENAME);

        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
            return SipConstants.PJSIP_RETURN_STATUS_DEFAULT;
        }

        return SipConstants.PJSIP_RETURN_STATUS_SUCCESS;
    }

    /**
     * This method initializes PJSIP stack.
     *
     * @return initialize status
     */
    public static int initializeJNI(Context context) {
        try {

            if (mJNIInitStatus == 0) {
                Log.i(TAG, "Exiting from initializeJNI() as App is already initialized");
                return mJNIInitStatus;
            }
            Log.i(TAG, "initializeJNI called");

            mSIPCallBacks = new SIPCallBacks(context);
            Log.i(TAG, "Invoke initializeJNI method");
            VoxEngine.setCallbackObject(mSIPCallBacks);
            //int sampleRate=Constants.DEFAULT_SAMPLE_RATE;
            int isAECAvailable = 0, isAGCAvailable = 0, isNSAvailable = 0;
            try {
                //sampleRate = Integer.parseInt(AudioMethodHelper.getSampleRate(Home.this));
                //Log.i(TAG,"Sample Rate: "+sampleRate);
                if (AudioMethodHelper.isAvailableAcousticEchoCanceler())
                    isAECAvailable = 1;
                if (AudioMethodHelper.isAvailableAutomaticGainControl())
                    isAGCAvailable = 1;
                if (AudioMethodHelper.isAvailableNoiseSuppressor())
                    isNSAvailable = 1;

                Log.i(TAG, "isAECAvailable: " + isAECAvailable + " ,isAGCAvailable:" + isAGCAvailable + " ,isNSAvailable: " + isNSAvailable);

            } catch (Exception e) {
                e.printStackTrace();
            }

            //Debug logs
            Log.i(TAG, "PTime: " + DataParser.size);
            Log.i(TAG, "SipPort: " + DataParser.sprt);
            Log.i(TAG, "Port Config: " + Constants.PORT_CONFIG);
            Log.i(TAG, "User Agent: MoSIP v" + BuildConfig.VERSION_NAME);

            VX_AppConfig appConfig = new VX_AppConfig();
            appConfig.setPTime(Integer.parseInt(DataParser.size));
            appConfig.setSipPort(Integer.parseInt(DataParser.sprt));
            appConfig.setProtCfg(Constants.PORT_CONFIG);
            appConfig.setUser_agent(context.getResources().getString(R.string.app_name) + " v" + BuildConfig.VERSION_NAME);

            Log.i(TAG," App name "+context.getString(R.string.app_name));
            appConfig.setApp_name(context.getString(R.string.app_name));
            //appConfig.setClockRate(sampleRate);
            appConfig.setIsAECAvailable(0);
            //appConfig.setIsAECAvailable(isAECAvailable); // This code commented as suggested by Raghava
            appConfig.setIsAGCAvailable(isAGCAvailable);
            appConfig.setIsNSAvailable(isNSAvailable);

            appConfig.setJb_type(Constants.JB_TYPE); // Jitter Buffer

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                Log.i(TAG, "Current version is above 4.X , setIsAndroid_2_x_Version: " + Constants.ANDROID_VERSION_ABOVE_2X);
                appConfig.setIsAndroid_2_x_Version(Constants.ANDROID_VERSION_ABOVE_2X);
            } else {
                Log.i(TAG, "Current version is below 4.X , setIsAndroid_2_x_Version: " + Constants.ANDROID_VERSION_2X);
                appConfig.setIsAndroid_2_x_Version(Constants.ANDROID_VERSION_2X);
            }

            //appConfig.setStun_srv_count(Constants.DISABLE_STUN);

            // If RAM is greater than 1000MB enable OpenSL other wise enable JNI
            long RAM_VALUE = MethodHelper.getRAMInfo(context);
            Log.i(TAG, "RAM VALUE " + RAM_VALUE);
            if (RAM_VALUE > 1000) {
                Log.i(TAG, "INTERFACE_TYPE_OPEN_SL");
                appConfig.setInterface_type(Constants.INTERFACE_TYPE_OPEN_SL);
            } else {
                Log.i(TAG, "INTERFACE_TYPE_JNI");
                appConfig.setInterface_type(Constants.INTERFACE_TYPE_JNI);
            }

            //if (DataParser.send_logs.equalsIgnoreCase("on")) {
            appConfig.setLogfileName(Constants.SD_CARD_LOGS_LOCATION);
            appConfig.setLogLevel(5);
            //}

            Log.i(TAG, "Invoke before InitializeApp");
            mJNIInitStatus = VoxEngine.JNI_VX_InitializeApp(appConfig, p__VX_ERROR);
            Log.i(TAG, "Invoke After InitializeApp");

            Log.i(TAG, "app return val mJNIInitStatus=" + mJNIInitStatus);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return mJNIInitStatus;
    }

    /**
     * This method register with PJSIP stack.
     *
     * @param context application context
     */
    public static void register(Context context) {
        try {
            if (mJNIInitStatus == Constants.PJSIP_RETURN_STATUS_DEFAULT) {
                Log.i(TAG, "Exiting from register as App is not initialized");
                return;
            }

            PreferenceProvider preferenceProvider = PreferenceProvider.getPrefInstance(context);
            int mDBAccId = preferenceProvider.getPrefInt("AccountID");

            VX_VoxAppSSL appSSL = new VX_VoxAppSSL();

            if (DataParser.encryption_type.equalsIgnoreCase("latest")) {

                appSSL.setEType(VX_ENCRYPT_TYPE.LATEST);
            } else if (DataParser.encryption_type.equalsIgnoreCase("new")) {

                appSSL.setEType(VX_ENCRYPT_TYPE.NEW);
            } else if (DataParser.encryption_type.equalsIgnoreCase("old")) {

                appSSL.setEType(VX_ENCRYPT_TYPE.OLD);
            }

            // appSSL.setEType(VX_ENCRYPT_TYPE.LATEST);

            appSSL.setOldkey(DataParser.oldkey);

            if (DataParser.vpn.equalsIgnoreCase("on")) {

                if (!DataParser.newkey.equalsIgnoreCase("off")) {

                    String keys[] = DataParser.newkey.split("-");
                    appSSL.setIVpnTrigger(1);
                    appSSL.setILength(Integer.parseInt(keys[0]));
                    appSSL.setIStarting(Integer.parseInt(keys[1]));

                    if (keys.length == 3)
                        appSSL.setIDiff(Integer.parseInt(keys[2]));
                }

                Log.i(TAG, "Encryption preferences: " + DataParser.en_pref);
                if (DataParser.en_pref.equalsIgnoreCase("on")) {

                    String preKeys[] = DataParser.prefix.split("-");
                    appSSL.setINoOfPref(Integer.parseInt(preKeys[0]));
                    appSSL.setIPrefValue(Integer.parseInt(preKeys[1]));
                    appSSL.setINoOfInnerPref(Integer.parseInt(preKeys[2]));

                } else {
                    appSSL.setIPrefValue(0);
                    appSSL.setINoOfPref(0);
                    appSSL.setINoOfInnerPref(0);
                }

            } else {
                Log.e(TAG, "VPN is OFF");
                appSSL.setIPrefValue(0);
                appSSL.setINoOfPref(0);
                appSSL.setINoOfInnerPref(0);
                appSSL.setIVpnTrigger(0);
                appSSL.setILength(0);
                appSSL.setIStarting(0);
                appSSL.setIDiff(0);
            }
            VoxEngine.JNI_VX_SetSIPEncryptionConfiguration(appSSL, p__VX_ERROR);
            VoxEngine.JNI_VX_SetRTPEncryptionConfiguration(appSSL, p__VX_ERROR);

            AccountsDB accountsDB = new AccountsDB(context);

            accountsDB.open();
            ProfileData profiledata = accountsDB.getAccount(mDBAccId);
            accountsDB.close();

            VX_AccountInfo accountInfo = new VX_AccountInfo();

            if (profiledata != null && profiledata.getUSERNAME() != null
                    && profiledata.getUSERNAME().length() > 0) {
                String uri = "sip:" + profiledata.getUSERNAME() + "@"
                        + profiledata.getSIPDOMAIN();

                Log.i(TAG, "SIP uri: " + uri);

                // Bridge proxy
                String proxy;// "sip:206.222.29.145:1026";//"96.31.69.52:5050";
                if (DataParser.vpn.equalsIgnoreCase("on")) {
                    proxy = "sip:" + DataParser.ip;
                } else {
                    proxy = "";
                }
                accountInfo.setName("*");
                accountInfo.setPassword(profiledata.getPASSWORD());
                accountInfo.setUserName(profiledata.getUSERNAME());
                accountInfo.setProxy(proxy);
                accountInfo.setRegUri("sip:" + profiledata.getSIPDOMAIN());
                accountInfo.setExpires(Integer.parseInt(DataParser.rereg));
                accountInfo.setKeepAlive(Integer.parseInt(DataParser.keep));
                accountInfo.setIsDefault(0);

                // DEBUG Logs
                Log.i(TAG, "registration URI: " + profiledata.getSIPDOMAIN());
                Log.i(TAG, "username: " + profiledata.getUSERNAME());
                Log.i(TAG, "password: " + profiledata.getPASSWORD());
                Log.i(TAG, "Expires: " + DataParser.rereg);
                Log.i(TAG, "KeepAlive: " + DataParser.keep);
                Log.i(TAG, "RTP Port: " + DataParser.rtrp);
                Log.i(TAG, "URI: " + uri);


                if (DataParser.rtrp != null && DataParser.rtrp.length() > 0)
                    accountInfo.setRtpPort(Integer.parseInt(DataParser.rtrp));
                else
                    accountInfo.setRtpPort(4000);
                accountInfo.setPc(1);
                accountInfo.setUri(uri);

                if (DataParser.cid != null && !DataParser.cid.equalsIgnoreCase("no")) {
                    accountInfo.setCallerId("\"" + preferenceProvider.getPrefString("login_phone") + "\""
                            + "<" + uri + ">");
                } else {
                    accountInfo.setCallerId("" + "<" + uri + ">");
                }
                // accountInfo.setCallerId(""+"<"+uri+">");
                Log.i(TAG, "Invoke before JNI_VX_RegisterAccount");

                int accId = 0;
                try {
                    //Log.i(TAG, "Before AccountID: " + id + " ,accID" + accId);
                    accId = VoxEngine.JNI_VX_RegisterAccount(accountInfo, p__VX_ERROR);
                    Log.i(TAG, "After DBAccountID: " + mDBAccId + " , accID: " + accId);
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "Invoke After JNI_VX_RegisterAccount");
                Log.i(TAG, "Register accId: " + accId);
                preferenceProvider.setPrefInt("AccID", accId);

                VoxEngine.JNI_VX_SetCodecPriority("*", 0, p__VX_ERROR);
                //VoxEngine.JNI_VX_SetCodecPriority("AMR/8000/1", 128, p__VX_ERROR);
                VoxEngine.JNI_VX_SetCodecPriority(Constants.AUDIO_CODEC, Constants.AUDIO_CODEC_PRIORITY, p__VX_ERROR);
                //VoxEngine.JNI_VX_SetCodecPriority("PCMU/8000/1", 250, mSwigPointerInstance);
                //VoxEngine.JNI_VX_SetCodecPriority("PCMA/8000/1", 240, mSwigPointerInstance);

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will hold the running call
     *
     * @param callID
     */
    public static void holdTheCall(int callID) {
        try {
            VoxEngine.JNI_VX_HoldCall(callID, getSwigPointerInstance());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * This Methos will resume the call
     *
     * @param callID
     */
    public static void resumeTheCall(int callID) {
        try {
            VoxEngine.JNI_VX_ResumeCall(callID, getSwigPointerInstance());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * This Method will mute the runnung call
     */
    public static void muteTheCall() {
        try {
            VoxEngine.JNI_VX_MuteCall(getSwigPointerInstance());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * This Methos will UnMute the Muted call
     */
    /*public static void unMuteTheCall() {
        try {
            VoxEngine.JNI_VX_UnMuteCall(SipConstants.MIC_DEFAULT_VOLUME,getSwigPointerInstance());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }*/
    public static void releaseTheRunningCall(CallInfo callInfo) {
        try {
            VoxEngine.JNI_VX_ReleaseCall(callInfo.getCallId(), getSwigPointerInstance());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * This Method will transfer the Running call to another destination(PSTN/APP)
     *
     * @param callInfo    is for Running call CallInfo
     * @param transferURI Destination SIP URI
     */
    public static int transferTheCall(CallInfo callInfo, String transferURI) {
        int status = 0;
        try {
            status = VoxEngine.JNI_VX_TransferCall(callInfo.getCallId(), transferURI, getSwigPointerInstance());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return status;
    }

    /**
     * This method will used for to call att.Transfer JIN API
     *
     * @param callId1
     * @param callId2
     */
    public static void attTransferCall(int callId1, int callId2) {
        try {
            VoxEngine.JNI_VX_TransferCallWithReplaces(callId1, callId2, getSwigPointerInstance());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * This Methos will call DTMF events
     *
     * @param callInfo
     * @param val
     */
    public static void dialDTMF(CallInfo callInfo, String val) {
        try {
            VoxEngine.JNI_VX_DialDtmf(callInfo.getCallId(), val, 0, getSwigPointerInstance());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * This Method will connect conference between all runng calls
     *
     * @param callInfo
     */
    public static void connectConference(CallInfo callInfo) {
        try {
            VoxEngine.JNI_VX_MakeConference(callInfo.getCallId(), getSwigPointerInstance());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will get the conference port for given callerID
     *
     * @param callID
     * @return
     */
    public static int getConferencePort(int callID) {
        int confport = 0;
        try {
            confport = VoxEngine.JNI_VX_GetConfPort(callID, getSwigPointerInstance());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return confport;
    }

    /**
     * This Method will disconnect the conference between connected calls
     *
     * @param confport1
     * @param confport2
     */
    public static void disconnectConference(int confport1, int confport2) {
        try {
            VoxEngine.JNI_VX_ConferenceDisconnect(confport1, confport2, getSwigPointerInstance());

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will send answer request to incommingcall based on response code
     *
     * @param callID
     * @param code
     */
    public static void answerIncommingCall(int callID, int code) {
        try {
            VoxEngine.JNI_VX_AnswerCall(callID, code, getSwigPointerInstance());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static int getCallDuration(int callID) {
        VX_CallInfo JNICallInfo = new VX_CallInfo();
        VoxEngine.JNI_VX_GetCallInfo(callID, JNICallInfo, getSwigPointerInstance());
        int callDuration = JNICallInfo.getTotal_duration();
        return callDuration;
    }

    /**
     * This method will unregister the SIP account
     *
     * @param regAccountID
     */
    public static void unRegisterAccount(int regAccountID) {
        VoxEngine.JNI_VX_UnRegisterAccount(regAccountID, getSwigPointerInstance());
    }

    /**
     * This method will give the CallInfo from JNI
     *
     * @param callId
     * @param jniCallInfo
     * @return
     */
    public static VX_CallInfo getJNICallInfo(int callId, VX_CallInfo jniCallInfo) {

        try {
            VoxEngine.JNI_VX_GetCallInfo(callId, jniCallInfo, getSwigPointerInstance());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return jniCallInfo;
    }

    /**
     * This method will give the status of call either active or not if active it will give non zero value
     *
     * @param mCallerID
     * @return
     */
    public static int getCallActiveStatus(int mCallerID) {
        int callStatus = SipConstants.PJSIP_RETURN_STATUS_DEFAULT;
        try {
            callStatus = VoxEngine.JNI_VX_IsCallActive(mCallerID, getSwigPointerInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return callStatus;
    }

}
