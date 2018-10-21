package com.bitvault.mediavault.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by linchpin on 1/8/17.
 */

public class RobotoBold extends android.support.v7.widget.AppCompatTextView {

    public RobotoBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RobotoBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoBold(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/Roboto-Bold.ttf");
        setTypeface(tf);
    }

}

