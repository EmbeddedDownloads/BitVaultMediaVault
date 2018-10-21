package com.bitvault.mediavault.utils;

/**
 * Font file used for displaying the text in RobotoLight font.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class RobotoLight extends android.support.v7.widget.AppCompatTextView {

    public RobotoLight(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RobotoLight(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoLight(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/Roboto-Light.ttf");
        setTypeface(tf);
    }

}

