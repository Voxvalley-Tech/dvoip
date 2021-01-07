/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.jni;

/**
 * This class contains SIP configuration details.
 *
 * @author rameshreddy
 */

public class SipConstants {

    public static final String LIB_FILENAME = "pjsua";

    public static final int CALL_NONE = 0;
    public static final int CALL_OUTGOING = 1;
    public static final int CALL_INCOMING = 2;
    public static final int CALL_CONFERENCE = 3;
    public static final int CALL_GSM = 4;

    public static final int PJSIP_RETURN_STATUS_DEFAULT = -1;
    public static final int PJSIP_RETURN_STATUS_SUCCESS = 0;

    /**
     * Bitmask to keep media/call coming from outside
     */
    public final static int BITMASK_IN = 1 << 0;
    /**
     * Bitmask to keep only media/call coming from the app
     */
    public final static int BITMASK_OUT = 1 << 1;
    /**
     * Bitmask to keep all media/call whatever incoming/outgoing
     */
    public final static int BITMASK_ALL = BITMASK_IN | BITMASK_OUT;



}
