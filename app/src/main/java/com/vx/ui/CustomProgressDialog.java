/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.ProgressBar;

import com.app.dvoip.R;

public class CustomProgressDialog extends Dialog {

    @SuppressWarnings("unused")
    private ProgressBar mProgress;

    public CustomProgressDialog(Context context) {
        super(context, R.style.Theme_Dialog1);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_prograss_bar);
        mProgress = (ProgressBar) findViewById(R.id.progress_bar);

    }

}