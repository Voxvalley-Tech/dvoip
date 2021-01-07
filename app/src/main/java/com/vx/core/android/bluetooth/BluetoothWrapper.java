
/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import java.util.Set;

/**
 * This class provides the helper methods of bluetooth.
 *
 * @author Ramesh Reddy
 */
public class BluetoothWrapper {

    private Context mContext;

    private static final String TAG = "BluetoothWrapper";
    public AudioManager audioManager;

    protected BluetoothAdapter bluetoothAdapter;
    public boolean isBluetoothConnected = false;
    public boolean targetBt = false;

    public BluetoothWrapper(Context context) {
        mContext = context;

        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (bluetoothAdapter == null) {
            try {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean canBluetooth() {
        // Detect if any bluetooth a device is available for call
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            return false;
        }
        boolean hasConnectedDevice = false;
        //If bluetooth is on
        if (bluetoothAdapter.isEnabled()) {

            //We get all bounded bluetooth devices
            // bounded is not enough, should search for connected devices....
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                BluetoothClass bluetoothClass = device.getBluetoothClass();
                if (bluetoothClass != null) {
                    int deviceClass = bluetoothClass.getDeviceClass();
                    if (bluetoothClass.hasService(BluetoothClass.Service.RENDER) ||
                            deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET ||
                            deviceClass == BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO ||
                            deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE) {
                        //And if any can be used as a audio handset
                        hasConnectedDevice = true;
                        break;
                    }
                }
            }
        }
        boolean retVal = hasConnectedDevice && audioManager.isBluetoothScoAvailableOffCall();
        Log.d(TAG, "Can I do BT ? " + retVal);
        return retVal;
    }

    public void setBluetoothOn(boolean on) {
        Log.d(TAG, "setBluetoothOn, " + on + " , Audio manager isBluetoothScoOn " + audioManager.isBluetoothScoOn() +" , isBluetoothConnected: "+isBluetoothConnected);

        targetBt = on;
        if (on != isBluetoothConnected) {
            // BT SCO connection state is different from required activation
            if (on) {
                // First we try to connect
                audioManager.startBluetoothSco();
            } else {
                // We stop to use BT SCO
                audioManager.setBluetoothScoOn(false);
                // And we stop BT SCO connection
                audioManager.stopBluetoothSco();
            }
        } else if (on != audioManager.isBluetoothScoOn()) {
            // BT SCO is already in desired connection state
            // we only have to use it
            audioManager.setBluetoothScoOn(on);
        }
    }

    /**
     * This method checks is bluetooth connected or not
     * @return true or false
     */
    public boolean isBTHeadsetConnected() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            if (bluetoothAdapter != null) {
                return (bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothAdapter.STATE_CONNECTED);
            }
        } else {
            return canBluetooth();
        }
        return false;
    }
}
