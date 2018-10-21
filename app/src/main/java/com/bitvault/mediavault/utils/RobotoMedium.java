package com.bitvault.mediavault.utils;

/**
 * Font file used for displaying the text in RobotoMedium font.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class RobotoMedium extends android.support.v7.widget.AppCompatTextView {

    public RobotoMedium(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RobotoMedium(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoMedium(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/Roboto-Medium.ttf");
        setTypeface(tf);
    }

}

