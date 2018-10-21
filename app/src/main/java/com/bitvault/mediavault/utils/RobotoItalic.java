package com.bitvault.mediavault.utils;

/**
 * Font file used for displaying the text in RobotoItalic font.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class RobotoItalic extends android.support.v7.widget.AppCompatTextView {

    public RobotoItalic(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RobotoItalic(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoItalic(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/Roboto-Regular.ttf");
        setTypeface(tf);
    }

}

