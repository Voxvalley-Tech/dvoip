/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.jni;

import android.util.Log;

import java.io.File;
import java.io.IOException;

import vx.plt.SWIGTYPE_p__VX_ERROR;
import vx.plt.VX_CallInfo;
import vx.plt.VoxEngine;

public class SimpleWavRecorderHandler {
    private static final String TAG = "SimpleWavRecorder";
    final int way;
    final VX_CallInfo callInfo;
    // private final int recorderId;
    public final String recordingPath;

    public SimpleWavRecorderHandler(VX_CallInfo callInfo, File recordFolder, int way)
            throws Exception, IOException {
        this.way = way;
        this.callInfo = callInfo;

        File targetFile = getRecordFile(recordFolder, callInfo.getRemote_contact(), way);
        if (targetFile == null) {
            throw new IOException("No target file possible");
        }
        recordingPath = targetFile.getAbsolutePath();
        // pj_str_t file = pjsua.pj_str_copy(recordingPath);
        int rcId = -1;// = new int[1];
        int status = VoxEngine.JNI_VX_recorder_create(recordingPath, 0, 0, 0, rcId);
        if (status == 0) {
            //   recorderId = rcId;
        } else {
            throw new IOException("Pjsip not able to write the file");
        }
    }

    /**
     * Get the file to record to for a given remote contact. This will
     * implicitly get the current date in file name.
     *
     * @param remoteContact The remote contact name
     * @return The file to store conversation
     */
    private File getRecordFile(File dir, String remoteContact, int way) {
        if (dir != null) {
            // The file name is only to have an unique identifier.
            // It should never be used to store datas as may change.
            // The app using the recording files should rely on the broadcast
            // and on callInfo instead that is reliable.
            //String datePart = (String) DateFormat.format("dd-MM-yy kk:mm:ss", new Date());
            String remotePart = sanitizeForFile(remoteContact);
            String fileName = "";
            try {
                String[] remotePart_new = remotePart.split("@");
                String remotePart_ = remotePart_new[0].trim().replace("sip_", "");
                //fileName = datePart + "_" + remotePart_.trim();
                fileName = remotePart_.trim();
            } catch (Exception e) {
                //fileName = datePart + "_" + remotePart;
                fileName = remotePart;
            }
            if (way != (SipConstants.BITMASK_ALL)) {
                fileName += ((way & SipConstants.BITMASK_IN) == 0) ? "_out" : "_in";
            }
            fileName = fileName.replace("_", "");
            fileName = fileName + "_" + System.currentTimeMillis();
            File file = new File(dir.getAbsoluteFile() + File.separator + fileName + ".wav");
            return file;
        }
        return null;
    }

    private String sanitizeForFile(String remoteContact) {
        String fileName = remoteContact;
        fileName = fileName.replaceAll("[\\.\\\\<>:; \"\'\\*]", "_");
        return fileName;
    }

    //   @Override
    public void startRecording(int callID) {
        SWIGTYPE_p__VX_ERROR p__VX_ERROR = new SWIGTYPE_p__VX_ERROR();
        int wavPort = VoxEngine.JNI_VX_recorder_get_conf_port();
        System.out.println("Recorder JNI_VX_recorder_get_conf_port or wavport= " + wavPort);
        if ((way & SipConstants.BITMASK_IN) == SipConstants.BITMASK_IN) {
            Log.i(TAG, "SipManager.BITMASK_IN");
            int wavConfPort = VoxEngine.JNI_VXGetConfPort(callID, p__VX_ERROR);//.getConfPort();
            System.out.println("Recorder JNI_VXGetConfPort or wavConfPort= " + wavConfPort);
            VoxEngine.JNI_VX_ConferenceConnect(wavConfPort, wavPort, p__VX_ERROR);
        }
        if ((way & SipConstants.BITMASK_OUT) == SipConstants.BITMASK_OUT) {
            Log.i(TAG, "SipManager.BITMASK_OUT");
            VoxEngine.JNI_VX_ConferenceConnect(0, wavPort, p__VX_ERROR);
        }
    }

    public void pauseRecording(int callId) {
        SWIGTYPE_p__VX_ERROR p__VX_ERROR = new SWIGTYPE_p__VX_ERROR();
        int wavPort = VoxEngine.JNI_VX_recorder_get_conf_port();
        System.out.println("Recorder JNI_VX_recorder_get_conf_port or wavport= " + wavPort);
        if ((way & SipConstants.BITMASK_IN) == SipConstants.BITMASK_IN) {
            Log.i(TAG, "SipManager.BITMASK_IN: " + SipConstants.BITMASK_IN);
            int wavConfPort = VoxEngine.JNI_VXGetConfPort(callId, p__VX_ERROR);//.getConfPort();
            System.out.println("Recorder JNI_VXGetConfPort or wavConfPort= " + wavConfPort);
            VoxEngine.JNI_VX_ConferenceDisconnect(wavConfPort, wavPort, p__VX_ERROR);
        }
        if ((way & SipConstants.BITMASK_OUT) == SipConstants.BITMASK_OUT) {
            Log.i(TAG, "SipManager.BITMASK_OUT: " + SipConstants.BITMASK_OUT);
            VoxEngine.JNI_VX_ConferenceDisconnect(0, wavPort, p__VX_ERROR);
        }
    }

    //  @Override
    public void stopRecording() {
        VoxEngine.JNI_VX_recorder_destroy();
    }


}
