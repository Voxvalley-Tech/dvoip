package com.vx.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.dvoip.R;


public class Progressdialog_custom extends Dialog {

	@SuppressWarnings("unused")
	private ProgressBar mProgress;
	
	TextView tv;

	public Progressdialog_custom(Context context) {
		 super(context, R.style.Theme_Dialog1);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.prograss_main);
		mProgress = (ProgressBar) findViewById(R.id.progress_bar);
		
		//mProgress=(ProgressBar) findViewById(R.id.progress_bar);
	
		
		// TODO Auto-generated constructor stub
	}
	/** Called when the activity is first created. */

}