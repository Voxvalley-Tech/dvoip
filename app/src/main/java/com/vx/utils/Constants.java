
/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.utils;

import android.os.Environment;

import java.io.File;

/**
 * This class contains all constant values of MOSIP
 *
 * @author rameshreddy
 */
public class Constants {

    public static final String SHARED_PREFERENCE_MOSIP = "MoSIP";

    public static final String extStorageDirectory = Environment
            .getExternalStorageDirectory().toString() + File.separator + "Mosip";
    public static final String CALL_RECORDING_FILE_PATH = extStorageDirectory
            + File.separator + "CallRecordings";
    public static final String SD_CARD_LOGS_LOCATION = "/sdcard/vx_log_msip.txt";

    // Play sore URLs
    public static String GOOGLE_PLAY_URL1 = "market://details?id=";
    public static String GOOGLE_PLAY_URL2 = "https://play.google.com/store/apps/details?id=";

    public static boolean IS_MAKECALL_CALLED = false;

    // Call States
    public static final String CALL_STATE_IN_COMING = "1";
    public static final String CALL_STATE_OUTGOING = "2";
    public static final String CALL_STATE_MISSED_CALL = "3";
    public static final String CALL_STATE_REJECTED = "5";

    /*************** SIP Constants ***************************/

    // Volume control Constants
    public static final float SPEAKER_DEFAULT_VOLUME = 1.0f;
    public static final float MIC_DEFAULT_VOLUME = 4.0f;
    public static final float MIC_DEFAULT_VOLUME_NEXUS_4 = 1.0f;
    public static final float SPEAKER_MAX_VOLUME = 20.0f;
    public static final float SPEAKER_MIN_VOLUME = 1.0f;
    public static final float SPEAKER_CHANGE_LEVEL_UP = 0.5f;
    public static final float SPEAKER_CHANGE_LEVEL_DOWN = 0.25f;

    // Volume control constants
    public static final int DEFAULT_OR_EXISTING_VALUE = 0;
    public static final int EVENT_UP = 1;
    public static final int EVENT_DOWN = 2;

    public static final int PJSIP_RETURN_STATUS_DEFAULT = -1;
    public static final int PJSIP_RETURN_STATUS_SUCCESS = 0;

    public static final int ANDROID_VERSION_2X = 1;
    public static final int ANDROID_VERSION_ABOVE_2X = 0;

    public static final int INTERFACE_TYPE_OPEN_SL = 0; /* 0 for OpenSL & 1 for JNI */
    public static final int INTERFACE_TYPE_JNI = 1;

    public static final String AUDIO_CODEC = "G729/8000/1";
    public static final int AUDIO_CODEC_PRIORITY = 255;

    // 0 --> Adaptive Jitter Buffer, 1 --> Static Jitter Buffer
    public static final int JB_TYPE = 0;
    // transport 0 -> TCP, 1 -> UDP, 2 -> TLS
    public static final int PORT_CONFIG = 1;
    //public static final int STUN_SERVERS_COUNT = 2;
    public static final int DISABLE_STUN = 0;
    public static final int DEFAULT_SAMPLE_RATE = 16000;
    public static boolean ISNETWORKDISCONNECTED = false;

    public static int MAKE_CALL_ERROR_CODE = 999;
    public static String PREVIOUS_NETWORK_TYPE = "";
    public static String CURRENT_NETWORK_TYPE = "";
    public static String PREVIOUS_NETWORK_NAME = "";
    public static String CURRENT_NETWORK_NAME = "";
    public static boolean IS_NETWORK_SWITCHED = false;

}
