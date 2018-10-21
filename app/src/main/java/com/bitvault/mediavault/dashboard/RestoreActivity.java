package com.bitvault.mediavault.dashboard;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.bitvault.mediavault.R;

public class RestoreActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);
        init();
    }

    /**
     * Initialised the preferences and other view.
     */
    private void init() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }
}
