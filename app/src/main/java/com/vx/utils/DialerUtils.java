
package com.vx.utils;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.dvoip.R;


public class DialerUtils {
	public static final int TAG_0 = 0;
	public static final int TAG_1 = 1;
	public static final int TAG_2 = 2;
	public static final int TAG_3 = 3;
	public static final int TAG_4 = 4;
	public static final int TAG_5 = 5;
	public static final int TAG_6 = 6;
	public static final int TAG_7 = 7;
	public static final int TAG_8 = 8;
	public static final int TAG_9 = 9;
	public static final int TAG_STAR = 10;
	public static final int TAG_SHARP = 11;
	public static final int TAG_CHAT = 12;
	public static final int TAG_AUDIO_CALL = 13;
	public static final int TAG_CONTACT = 14;
	public static final int TAG_DELETE = 15;

	public static final int DTMF_0 = 7;
	public static final int DTMF_1 = 8;
	public static final int DTMF_2 = 9;
	public static final int DTMF_3 = 10;
	public static final int DTMF_4 = 11;
	public static final int DTMF_5 = 12;
	public static final int DTMF_6 = 13;
	public static final int DTMF_7 = 14;
	public static final int DTMF_8 = 15;
	public static final int DTMF_9 = 16;
	public static final int DTMF_STAR = 17;
	public static final int DTMF_SHARP = 18;

	public static void setDialerTextButton(View view, String num, String text, int tag, View.OnClickListener listener,View.OnLongClickListener longlistener,float size,float size1){
		view.setTag(tag);
		view.setOnClickListener(listener);

		if(longlistener!=null){
			view.setOnLongClickListener(longlistener);
		}

		((TextView)view.findViewById(R.id.view_dialer_button_text_textView_num)).setText(num);
		((TextView)view.findViewById(R.id.view_dialer_button_text_textView_numm)).setText(text);
		((TextView)view.findViewById(R.id.view_dialer_button_text_textView_num)).setTextSize(size);
		((TextView)view.findViewById(R.id.view_dialer_button_text_textView_numm)).setTextSize(size1);

		//((TextView)view.findViewById(R.id.view_dialer_button_text_textView_num)).setTypeface(segoepr_0);
		//System.out.println("text"+text);
		//((TextView)view.findViewById(R.id.view_dialer_button_text_textView_numm)).setText(text);
		//((TextView)view.findViewById(R.id.view_dialer_button_text_textView_numm)).setTextSize(size1);
	}

	public static void setDialerTextButton(Activity parent, int viewId, String num, String text, int tag, View.OnClickListener listener, View.OnLongClickListener longlistener,float size,float size1){
		setDialerTextButton(parent.findViewById(viewId), num, text, tag, listener,longlistener,size,size1);
	}

	public static void setDialerTextButton(View parent, int viewId, String num, String text, int tag, View.OnClickListener listener, View.OnLongClickListener longlistener,float size,float size1){
		setDialerTextButton(parent.findViewById(viewId), num, text, tag, listener,longlistener,size,size1);
	}
	public static void setDialerImageButton(View parent, int viewId, int imageId, int tag, View.OnClickListener listener){
		View view = parent.findViewById(viewId);
		view.setTag(tag);
		view.setOnClickListener(listener);
		((ImageView)view.findViewById(R.id.view_dialer_button_image_imageView)).setImageResource(imageId);
	}


	public static void setLongDialerImageButton(View parent, int viewId, int imageId, int tag, View.OnClickListener listener,View.OnLongClickListener longlistener){
		View view = parent.findViewById(viewId);
		view.setTag(tag);
		view.setOnClickListener(listener);
		view.setOnLongClickListener(longlistener);
		((ImageView)view.findViewById(R.id.view_dialer_button_image_imageView)).setImageResource(imageId);
	}
}