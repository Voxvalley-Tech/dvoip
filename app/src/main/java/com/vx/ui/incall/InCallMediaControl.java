
/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.ui.incall;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.app.dvoip.R;
import com.vx.utils.PreferenceProvider;

import java.util.Timer;
import java.util.TimerTask;

import vx.plt.VoxEngine;

public class InCallMediaControl extends Activity implements OnSeekBarChangeListener, OnCheckedChangeListener, OnClickListener {
    protected static final String THIS_FILE = "inCallMediaCtrl";
    // Below variable will be used while closing InCallCardActivity
    public static Context mediaControllerActivityContext = null;
    private SeekBar speakerAmplification;
    private SeekBar microAmplification;
    private Button saveButton;
    private CheckBox echoCancellation;
//	private Button recordButton;

    private boolean isAutoClose = false;

    private final static int AUTO_QUIT_DELAY = 3000;
    private Timer quitTimer;
    private ProgressBar txProgress;
    private ProgressBar rxProgress;
    private LinearLayout okBar;
    Button buttonTitleBar;
    private PreferenceProvider preferenceProvider;
    private String TAG = "InCallMediaControl";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_volume_control);
        mediaControllerActivityContext = InCallMediaControl.this;

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.toolbar_media_control);
        // Then like usual, locate the widgets and use 'em!
        buttonTitleBar = (Button) findViewById(R.id.titlebarButton);

        buttonTitleBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        registerReceiver(broadcast_reciever, new IntentFilter("finish_activity"));
        speakerAmplification = (SeekBar) findViewById(R.id.speaker_level);
        microAmplification = (SeekBar) findViewById(R.id.micro_level);
        saveButton = (Button) findViewById(R.id.save_bt);
        echoCancellation = (CheckBox) findViewById(R.id.echo_cancellation);
        okBar = (LinearLayout) findViewById(R.id.ok_bar);

        speakerAmplification.setMax((int) (max * subdivision * 2));
        microAmplification.setMax((int) (max * subdivision * 2));
        preferenceProvider = PreferenceProvider.getPrefInstance(this);
        int spekerlevel = 75;
        int miclevel = 75;
        try {
            spekerlevel = preferenceProvider.getPrefInt("speakerlevel", 75);
            miclevel = preferenceProvider.getPrefInt("miclevel", 75);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Voluem speaker ::" + spekerlevel);
        speakerAmplification.setProgress(spekerlevel);
        speakerAmplification.setOnSeekBarChangeListener(this);
        microAmplification.setOnSeekBarChangeListener(this);

        Log.i(TAG, "Voluem speaker ::" + miclevel);
        microAmplification.setProgress(miclevel);
        saveButton.setOnClickListener(this);

        echoCancellation.setOnCheckedChangeListener(this);

        rxProgress = (ProgressBar) findViewById(R.id.rx_bar);
        txProgress = (ProgressBar) findViewById(R.id.tx_bar);

        IntentFilter intentFilter = new IntentFilter(getPackageName() + ".media_CLOSERECEIVER");

        registerReceiver(mediaClosetReceiver, intentFilter);
    }


    //@Override
    protected void onResume() {
        super.onResume();

		/* prefsWrapper = new PreferencesProviderWrapper(this);
            if(prefsWrapper!=null)
	    	
		  if( prefsWrapper.getPreferenceBooleanValue("mute", false)==true)
		  {
				microAmplification.setMax(0);
		  }*/

		
		/*Intent sipServiceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
		bindService(sipServiceIntent , sipConnection, BIND_AUTO_CREATE);
		*/

        int direction = getIntent().getIntExtra(Intent.EXTRA_KEY_EVENT, 0);
        if (direction == AudioManager.ADJUST_LOWER || direction == AudioManager.ADJUST_RAISE) {
            isAutoClose = true;
            okBar.setVisibility(View.GONE);
            delayedQuit(AUTO_QUIT_DELAY);
        } else {
            okBar.setVisibility(View.VISIBLE);
            isAutoClose = false;
        }

        //registerReceiver(callStateReceiver, new IntentFilter(SipManager.ACTION_SIP_CALL_CHANGED));
       /* if (monitorThread == null) {
            monitorThread = new MonitorThread();
            monitorThread.start();
        }*/

    }


    private class LockTimerTask extends TimerTask {
        @Override
        public void run() {
            finish();
        }
    }

    ;


    public void delayedQuit(int time) {
        if (quitTimer != null) {
            quitTimer.cancel();
            quitTimer.purge();
            quitTimer = null;
        }

        quitTimer = new Timer("Quit-timer-media");

        quitTimer.schedule(new LockTimerTask(), time);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:

                if (speakerAmplification != null) {
                    int step = (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) ? -1 : +1;
                    int newValue = speakerAmplification.getProgress() + step;
                    if (newValue >= 0 && newValue < speakerAmplification.getMax()) {
                        speakerAmplification.setProgress(newValue);
                    }
                }


                return true;
            case KeyEvent.KEYCODE_CALL:
            case KeyEvent.KEYCODE_ENDCALL:
            case KeyEvent.KEYCODE_SEARCH:
                //Prevent search
                return true;
            default:
                //Nothing to do
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_CALL:
            case KeyEvent.KEYCODE_ENDCALL:
            case KeyEvent.KEYCODE_SEARCH:
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private double subdivision = 5;
    private double max = 15;

    private int valueToProgressUnit(float val) {
        Log.d(THIS_FILE, "Value is " + val);
        double dB = (10.0f * Math.log10(val));
        return (int) ((dB + max) * subdivision);
    }

    private float progressUnitToValue(int pVal) {
        Log.d(THIS_FILE, "Progress is " + pVal);
        double dB = pVal / subdivision - max;
        return (float) Math.pow(10, dB / 10.0f);
    }

    @Override
    public void onProgressChanged(SeekBar arg0, int value, boolean arg2) {
        Log.d(THIS_FILE, "Progress has changed");
        try {
            float newValue = progressUnitToValue(value);
            String key;
            //boolean useBT = sipService.getCurrentMediaState().isBluetoothScoOn;

            int sId = arg0.getId();
            if (sId == R.id.speaker_level) {
                Log.i(TAG, "Speaker volume onProgressChanged  " + value+" , converted float value: "+newValue);
                preferenceProvider.setPrefInt("speakerlevel", value);
                VoxEngine.JNI_VX_Adjust_tx_level(0, newValue);

                //	key =  useBT ? SipConfigManager.SND_BT_SPEAKER_LEVEL : SipConfigManager.SND_SPEAKER_LEVEL;
                //SipConfigManager.setPreferenceFloatValue(this, key, newValue);
            } else if (sId == R.id.micro_level) {
                Log.i(TAG, "MicroPhone volume onProgressChanged  " + value+" , converted float value: "+newValue);
                preferenceProvider.setPrefInt("miclevel", value);
                VoxEngine.JNI_VX_Adjust_rx_level(0, newValue);
                //key =  useBT ? SipConfigManager.SND_BT_MIC_LEVEL : SipConfigManager.SND_MIC_LEVEL;
                //SipConfigManager.setPreferenceFloatValue(this, key, newValue);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        //Update quit timer
		/*if(isAutoClose) {
			delayedQuit(AUTO_QUIT_DELAY);
		}*/
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        // Nothing to do
    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
        // Nothing to do
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.save_bt) {
            Toast.makeText(getApplicationContext(), "cancel button", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // private MonitorThread monitorThread;

    private class MonitorThread extends Thread {
        private boolean finished = false;

        public synchronized void markFinished() {
            finished = true;
        }

        @Override
        public void run() {
            super.run();
         /*   while (true) {
                if (sipService != null) {
                    try {
                        long value = sipService.confGetRxTxLevel(0);
                        runOnUiThread(new UpdateConfLevelRunnable((int) ((value >> 8) & 0xff), (int) (value & 0xff)));
                    } catch (RemoteException e) {
                        Log.e(THIS_FILE, "Problem with remote service", e);
                        break;
                    }
                }

                // End of loop, sleep for a while and exit if necessary
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    Log.e(THIS_FILE, "Interupted monitor thread", e);
                }
                synchronized (this) {
                    if (finished) {
                        break;
                    }
                }
            }*/
        }
    }

    private class UpdateConfLevelRunnable implements Runnable {
        private final int mRx, mTx;

        UpdateConfLevelRunnable(int rx, int tx) {
            mRx = rx;
            mTx = tx;
        }

        @Override
        public void run() {
            txProgress.setProgress(mTx);
            rxProgress.setProgress(mRx);
        }
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub

    }

    BroadcastReceiver mediaClosetReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            finish();


        }


    };

    BroadcastReceiver broadcast_reciever = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals("finish_activity")) {
                finish();
                // DO WHATEVER YOU WANT.
            }
        }
    };

    protected void onDestroy() {

        mediaControllerActivityContext = null;
        if (mediaClosetReceiver != null) {
            unregisterReceiver(mediaClosetReceiver);
        }
        if (broadcast_reciever != null) {
            unregisterReceiver(broadcast_reciever);
        }
        //InCallCardActivity.media_task=false;
        super.onDestroy();
    }

}
