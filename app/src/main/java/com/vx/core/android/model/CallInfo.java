/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.model;

import com.vx.core.jni.Inv_states;
import com.vx.core.jni.SipConstants;
import com.vx.utils.Constants;

/**
 * This is model class stores call information.
 *
 * @author rameshreddy
 */

public class CallInfo {
    String callContactNumber = "";
    int callId = Constants.PJSIP_RETURN_STATUS_DEFAULT, callState = Inv_states.VX_INV_STATE_CALLING, callType = SipConstants.CALL_NONE;
    boolean isCallOnHold = false;

    public int getCallId() {
        return callId;
    }

    public void setCallId(int callId) {
        this.callId = callId;
    }

    public String getCallContactNumber() {
        return callContactNumber;
    }

    public void setCallContactNumber(String callContactNumber) {
        this.callContactNumber = callContactNumber;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public int getCallState() {
        return callState;
    }

    public void setCallState(int callState) {
        this.callState = callState;
    }

    public boolean isCallOnHold() {
        return isCallOnHold;
    }

    public void setCallOnHold(boolean callOnHold) {
        isCallOnHold = callOnHold;
    }
}
