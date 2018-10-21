package com.bitvault.mediavault.utils;

/**
 * Font file used for displaying the text in RobotoLightItalic font for Edittext.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class RobotoLightItalicEdittext extends android.support.v7.widget.AppCompatEditText {

    public RobotoLightItalicEdittext(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public RobotoLightItalicEdittext(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RobotoLightItalicEdittext(Context context) {
        super(context);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(),
                "fonts/Roboto-Regular.ttf");
        setTypeface(tf);
    }

}

