package com.spydiko.socialwifi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by PuR3v1L on 30/8/2013.
 */
public class NoParentPressImageView extends ImageView {

	public NoParentPressImageView(Context context) {
		this(context, null);
	}

	public NoParentPressImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//        if (event.getAction() == MotionEvent.ACTION_DOWN) setPressed(true);
		return super.onTouchEvent(event);
	}
}