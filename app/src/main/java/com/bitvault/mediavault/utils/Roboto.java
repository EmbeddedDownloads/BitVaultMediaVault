package com.bitvault.mediavault.utils;

/**
 * Font file used for displaying the text in Roboto-Regular font.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class Roboto extends android.support.v7.widget.AppCompatTextView {

    public Roboto(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public Roboto(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Roboto(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/Roboto-Regular.ttf");
        setTypeface(tf);
    }

}

