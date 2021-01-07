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
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;

/**
 * Ringer manager for the Phone app.
 */
public class Ringer {
    private static final String THIS_FILE = "Ringer";
   
    private static final int VIBRATE_LENGTH = 1000; // ms
    private static final int PAUSE_LENGTH = 1000; // ms

    // Uri for the ringtone.
    Uri customRingtoneUri;

    Ringtone ringtone = null;				// [sentinel]
    Vibrator vibrator;
    VibratorThread vibratorThread;
    RingerThread ringerThread;
    Context context;

 // The possible tones we can play.
    public static final int TONE_NONE = 0;
    public static final int TONE_CALL_WAITING = 1;
    public static final int TONE_BUSY = 2;
    public static final int TONE_CONGESTION = 3;
    public static final int TONE_BATTERY_LOW = 4;
    public static final int TONE_CALL_ENDED = 5;
    
    public Ringer(Context aContext) {
        context = aContext;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Starts the ringtone and/or vibrator. 
     * 
     */
    public void ring(String remoteContact, String defaultRingtone) {
        Log.d(THIS_FILE, "==> ring() called...");

        synchronized (this) {

        	AudioManager audioManager =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        	
        	//Save ringtone at the begining in case we raise vol
            //ringtone = getRingtone(remoteContact, defaultRingtone);
            
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);    		
    		ringtone = RingtoneManager.getRingtone(context, notification);
    		
        	//No ring no vibrate
            int ringerMode = audioManager.getRingerMode();
            if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
            	Log.d(THIS_FILE, "skipping ring and vibrate because profile is Silent");
            	return;
            }
            
            // Vibrate
            int vibrateSetting = audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
            Log.d(THIS_FILE, "v=" + vibrateSetting + " rm=" + ringerMode);
            if (vibratorThread == null &&
            		(vibrateSetting == AudioManager.VIBRATE_SETTING_ON || 
            				ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
                vibratorThread = new VibratorThread();
                Log.d(THIS_FILE, "Starting vibrator...");
                vibratorThread.start();
            }

            // Vibrate only
            if (ringerMode == AudioManager.RINGER_MODE_VIBRATE ||
            		audioManager.getStreamVolume(AudioManager.STREAM_RING) == 0 ) {
            	Log.d(THIS_FILE, "skipping ring because profile is Vibrate OR because volume is zero");
            	return;
            }

            // Ringer normal, audio set for ring, do it
            if(ringtone == null) {
            	Log.d(THIS_FILE, "No ringtone available - do not ring");
            	return;
            }

            Log.d(THIS_FILE, "Starting ring with " + ringtone.getTitle(context));
            
            if (ringerThread == null) {
            	ringerThread = new RingerThread();
            	Log.d(THIS_FILE, "Starting ringer...");
            	audioManager.setMode(AudioManager.MODE_RINGTONE);
            	ringerThread.start();
            }
        }
    }

    /**
     * @return true if we're playing a ringtone and/or vibrating
     *     to indicate that there's an incoming call.
     *     ("Ringing" here is used in the general sense.  If you literally
     *     need to know if we're playing a ringtone or vibrating, use
     *     isRingtonePlaying() or isVibrating() instead.)
     */
    public boolean isRinging() {
    	return (ringerThread != null || vibratorThread != null);
    }
    
    /**
     * Stops the ringtone and/or vibrator if any of these are actually
     * ringing/vibrating.
     */
	public void stopRing() {
		synchronized (this) {
			Log.d(THIS_FILE, "==> stopRing() called...");

			stopVibrator();
			stopRinger();
		}
	}
    
    
	private void stopRinger() {
		if (ringerThread != null) {
			ringerThread.interrupt();
			try {
				ringerThread.join(250);
			} catch (InterruptedException e) {
			}
			ringerThread = null;
		}
	}
    
	private void stopVibrator() {

		if (vibratorThread != null) {
			vibratorThread.interrupt();
			try {
				vibratorThread.join(250); // Should be plenty long (typ.)
			} catch (InterruptedException e) {
			} // Best efforts (typ.)
			vibratorThread = null;
		}
	}

	public void updateRingerMode() {

		AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		synchronized (this) {
			int ringerMode = audioManager.getRingerMode();
			// Silent : stop everything
			if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
				stopRing();
				return;
			}

			// Vibrate
			int vibrateSetting = audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
			// If not already started restart it
			if (vibratorThread == null && (vibrateSetting == AudioManager.VIBRATE_SETTING_ON || ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
				vibratorThread = new VibratorThread();
				vibratorThread.start();
			}

			// Vibrate only
			if (ringerMode == AudioManager.RINGER_MODE_VIBRATE || audioManager.getStreamVolume(AudioManager.STREAM_RING) == 0) {
				stopRinger();
				return;
			}
			
			//Ringer
			if (ringerThread == null) {
            	ringerThread = new RingerThread();
            	Log.d(THIS_FILE, "Starting ringer...");
            	audioManager.setMode(AudioManager.MODE_RINGTONE);
            	ringerThread.start();
            }

		}
	}

    private class VibratorThread extends Thread {
        public void run() {
        	try {
	            while (true) {
	                vibrator.vibrate(VIBRATE_LENGTH);
	                Thread.sleep(VIBRATE_LENGTH + PAUSE_LENGTH);
	            }
        	} catch (InterruptedException ex) {
        		Log.d(THIS_FILE, "Vibrator thread interrupt");
        	} finally {
        		vibrator.cancel();
        	}
    		Log.d(THIS_FILE, "Vibrator thread exiting");
        }
    }
    
    private class RingerThread extends Thread {
    	public void run() {
            try {
	    		while (true) {
	    			ringtone.play();
	    			while (ringtone.isPlaying()) {
	    				Thread.sleep(100);
	    			}
	    		}
            } catch (InterruptedException ex) {
        		Log.d(THIS_FILE, "Ringer thread interrupt");
            } finally {
            	if(ringtone != null) {
            		ringtone.stop();
            	}
            }
    		Log.d(THIS_FILE, "Ringer thread exiting");
    	}
    }

    private Ringtone getRingtone(String remoteContact, String defaultRingtone) {
    	Uri ringtoneUri = Uri.parse(defaultRingtone);
		
		/*// TODO - Should this be in a separate thread? We would still have to wait for
		// it to complete, so at present, no.
		CallerInfo callerInfo = CallerInfo.getCallerInfoFromSipUri(context, remoteContact);
		
		if(callerInfo != null && callerInfo.contactExists && callerInfo.contactRingtoneUri != null) {
			Log.d(THIS_FILE, "Found ringtone for " + callerInfo.name);
			ringtoneUri = callerInfo.contactRingtoneUri;
		}*/
		
    	
		
		return RingtoneManager.getRingtone(context, ringtoneUri);
    }
    
    public void playInCallTone(int toneId) {
	    (new InCallTonePlayer(toneId)).start();
	}
    
    
    private class InCallTonePlayer extends Thread {
        private int mToneId;


        // The tone volume relative to other sounds in the stream
        private static final int TONE_RELATIVE_VOLUME_HIPRI = 80;
        private static final int TONE_RELATIVE_VOLUME_LOPRI = 50;

        InCallTonePlayer(int toneId) {
            super();
            mToneId = toneId;
        }

        @Override
        public void run() {
            Log.d(THIS_FILE, "InCallTonePlayer.run(toneId = " + mToneId + ")...");

            int toneType; // passed to ToneGenerator.startTone()
            int toneVolume; // passed to the ToneGenerator constructor
            int toneLengthMillis;
            switch (mToneId) {
                case TONE_CALL_WAITING:
                    toneType = ToneGenerator.TONE_SUP_CALL_WAITING;
                    toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                    toneLengthMillis = 5000;
                    break;
                case TONE_BUSY:
                    toneType = ToneGenerator.TONE_SUP_BUSY;
                    toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                    toneLengthMillis = 4000;
                    break;
                case TONE_CONGESTION:
                    toneType = ToneGenerator.TONE_SUP_CONGESTION;
                    toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                    toneLengthMillis = 4000;
                    break;
                case TONE_BATTERY_LOW:
                    // For now, use ToneGenerator.TONE_PROP_ACK (two quick
                    // beeps). TODO: is there some other ToneGenerator
                    // tone that would be more appropriate here? Or
                    // should we consider adding a new custom tone?
                    toneType = ToneGenerator.TONE_PROP_ACK;
                    toneVolume = TONE_RELATIVE_VOLUME_HIPRI;
                    toneLengthMillis = 1000;
                    break;
                case TONE_CALL_ENDED:
                    toneType = ToneGenerator.TONE_PROP_PROMPT;
                    toneVolume = TONE_RELATIVE_VOLUME_LOPRI;
                    toneLengthMillis = 2000;
                    break;
                default:
                    throw new IllegalArgumentException("Bad toneId: " + mToneId);
            }

            // If the mToneGenerator creation fails, just continue without it. It is
            // a local audio signal, and is not as important.
            ToneGenerator toneGenerator;
            try {
                toneGenerator = new ToneGenerator(AudioManager.STREAM_VOICE_CALL, toneVolume);
                // if (DBG) log("- created toneGenerator: " + toneGenerator);
            } catch (RuntimeException e) {
                Log.w(THIS_FILE, "InCallTonePlayer: Exception caught while creating ToneGenerator: " + e);
                toneGenerator = null;
            }

            // Using the ToneGenerator (with the CALL_WAITING / BUSY /
            // CONGESTION tones at least), the ToneGenerator itself knows
            // the right pattern of tones to play; we do NOT need to
            // manually start/stop each individual tone, or manually
            // insert the correct delay between tones. (We just start it
            // and let it run for however long we want the tone pattern to
            // continue.)
            //
            // TODO: When we stop the ToneGenerator in the middle of a
            // "tone pattern", it sounds bad if we cut if off while the
            // tone is actually playing. Consider adding API to the
            // ToneGenerator to say "stop at the next silent part of the
            // pattern", or simply "play the pattern N times and then
            // stop."

            if (toneGenerator != null) {
                toneGenerator.startTone(toneType);
                SystemClock.sleep(toneLengthMillis);
                toneGenerator.stopTone();

                Log.v(THIS_FILE, "- InCallTonePlayer: done playing.");
                toneGenerator.release();
            }
        }
    }
}
