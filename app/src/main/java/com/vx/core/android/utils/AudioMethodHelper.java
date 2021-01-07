/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.util.Log;

import com.vx.utils.Constants;
import com.vx.utils.PreferenceProvider;

import java.util.Arrays;
import java.util.List;

/**
 * This class contains all audio related helper methods.
 *
 * @author rameshreddy
 */

public class AudioMethodHelper {

    private static String TAG = "AudioMethodHelper";
    private static Ringer ringer;

    // List of devices where it has been verified that the built-in effect
    // bad and where it makes sense to avoid using it and instead rely on the
    // native WebRTC version instead. The device name is given by Build.MODEL.
    private static final String[] BLACKLISTED_AEC_MODELS = new String[]{
            "D6503",      // Sony Xperia Z2 D6503
            "ONE A2005",  // OnePlus 2
            "MotoG3",     // Moto G (3rd Generation)
            "Lenovo A6000",
            "GT-S7262",
            "Nexus 4"
    };
    private static final String[] BLACKLISTED_AGC_MODELS = new String[]{
            "Nexus 10",
            "Nexus 9",
            "Lenovo A6000" //Lenovo A6000
    };
    private static final String[] BLACKLISTED_NS_MODELS = new String[]{
            "Nexus 10",
            "Nexus 9",
            "ONE A2005",  // OnePlus 2
            "Lenovo A6000", //Lenovo A6000
            "GT-S7262"
    };

    /**
     * This method gets ringer instance which is singleton instance can be resued in whole ap;.
     * @param context
     * @return
     */
    public static Ringer getRingerInstance(Context context) {
        if (ringer == null)
            ringer = new Ringer(context);

        return ringer;
    }

    /**
     * This methods change the audio focus
     *
     * @param setFocus boolean variable
     */
    public static void setAudioFocus(AudioManager audioManager, PreferenceProvider prefProvider, boolean setFocus) {

        boolean isGSMCall = prefProvider.getPrefBoolean("isGSMCall");
        Log.i(TAG, "isGSM Call: " + isGSMCall + " , audioFocus: " + setFocus);
        if (isGSMCall) {
            if (audioManager != null) {
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.abandonAudioFocus(null);
            }
            return;
        }

        if (audioManager != null) {
            // This variable saves the last audio mode
            int savedAudioMode = audioManager.getMode();
            if (setFocus) {

                // Request audio focus before making any device switch.
                audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

                /*
                 * Start by setting MODE_IN_COMMUNICATION as default audio mode. It is
                 * required to be in this mode when playout and/or recording starts for
                 * best possible VoIP performance. Some devices have difficulties with speaker mode
                 * if this is not set.
                 */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                } else {
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                }
            } else {
                audioManager.setMode(savedAudioMode);
                audioManager.abandonAudioFocus(null);
            }
        }
    }

    /**
     * This method sets Max device volume and stores previous volume level.
     *
     * @param audioManager       audio manager instance
     * @param preferenceProvider shared preference instance
     */
    public static void setAppVolume(AudioManager audioManager, PreferenceProvider preferenceProvider) {
        int actual_volume = audioManager
                .getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        //int max_volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        preferenceProvider.setPrefInt("device_actual_volume", actual_volume);

        int app_phone_volume = preferenceProvider.getPrefInt("app_phone_volume", actual_volume);

        Log.i("Constants", "device volume actual" + actual_volume);

        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                app_phone_volume, AudioManager.ADJUST_SAME);
    }

    /**
     * This method restores to previous volume level.
     *
     * @param audioManager       audio manager instance
     * @param preferenceProvider shared preference instance
     */
    public static void resetAppVolume(AudioManager audioManager, PreferenceProvider preferenceProvider) {
        // Here we are maintaining original system volume, app system volume and stack volume
        int actual_volume = audioManager
                .getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        preferenceProvider.setPrefInt("app_phone_volume",actual_volume);

        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                preferenceProvider.getPrefInt("device_actual_volume"),
                AudioManager.ADJUST_SAME);

    }

    /**
     * This method gets sample rate
     *
     * @param context application context
     * @return sample rate
     */
    public static String getSampleRate(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        String rate = String.valueOf(Constants.DEFAULT_SAMPLE_RATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            rate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        }
        return rate;
    }

    /**
     * This method gets sample rate
     *
     * @param context application context
     * @return sample rate
     */
    public static String getBufferSize(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        String size = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            size = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        }
        return size;
    }

    /**
     * This method get device support low latency or not
     *
     * @param context application context
     * @return boolean variable, if returns true it indicates a continuous output latency of 45 ms or less.
     */
    public static boolean hasLowLatencyFeature(Context context) {
        boolean hasLowLatencyFeature = false;
        // It supports from API level 9
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            hasLowLatencyFeature =
                    context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY);
        }

        return hasLowLatencyFeature;
    }

    /**
     * This method gets does device supports Acoustic Echo Canceler.
     *
     * @return boolean variable is available or not.
     */
    public static boolean isAvailableAcousticEchoCanceler() {
        boolean isAvailable = false;

        // If device model exists in black listed models return false
        String model = Build.MODEL;
        Log.i(TAG, "Device Model: " + model);
        if (Arrays.asList(BLACKLISTED_AEC_MODELS).contains(model)) {
            return isAvailable;
        }

        // It supports from API level 16
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            isAvailable = AcousticEchoCanceler.isAvailable();
        }
        return isAvailable;
    }

    /**
     * This method gets does device supports NoiseSuppressor.
     *
     * @return boolean variable is available or not.
     */
    public static boolean isAvailableNoiseSuppressor() {
        boolean isAvailable = false;

        // If device model exists in black listed models return false
        String model = Build.MODEL;
        if (Arrays.asList(BLACKLISTED_NS_MODELS).contains(model)) {
            return isAvailable;
        }

        // It supports from API level 16
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            isAvailable = NoiseSuppressor.isAvailable();
        }
        return isAvailable;
    }

    /**
     * This method gets does device supports AutomaticGainControl.
     *
     * @return boolean variable is available or not.
     */
    public static boolean isAvailableAutomaticGainControl() {
        boolean isAvailable = false;

        // If device model exists in black listed models return false
        String model = Build.MODEL;
        if (Arrays.asList(BLACKLISTED_AGC_MODELS).contains(model)) {
            return isAvailable;
        }

        // It supports from API level 16
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            AutomaticGainControl.isAvailable();
        }
        return isAvailable;
    }


}
