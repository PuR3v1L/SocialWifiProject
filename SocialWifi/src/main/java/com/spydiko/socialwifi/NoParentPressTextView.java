package com.spydiko.socialwifi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by PuR3v1L on 30/8/2013.
 */
public class NoParentPressTextView extends TextView {

    public NoParentPressTextView(Context context) {
        this(context, null);
    }

    public NoParentPressTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) setPressed(true);
        return super.onTouchEvent(event);
    }
}