/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.jni;

public class Inv_states {
	
	public static final int VX_INV_STATE_CALLING=1;
	
	public static final int VX_INV_STATE_INCOMING =VX_INV_STATE_CALLING+1;
	public static final int VX_INV_STATE_EARLY  =VX_INV_STATE_CALLING+2;
	public static final int VX_INV_STATE_CONNECTING =VX_INV_STATE_CALLING+3;
	public static final int VX_INV_STATE_CONFIRMED =VX_INV_STATE_CALLING+4;
	public static final int VX_INV_STATE_DISCONNECTED =VX_INV_STATE_CALLING+5;
	public static final int VX_INV_STATE_DISCONNECTING=550;
	

}
