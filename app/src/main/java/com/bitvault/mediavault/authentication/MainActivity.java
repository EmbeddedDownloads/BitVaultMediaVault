package com.bitvault.mediavault.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.bitvault.mediavault.baseclass.BaseActivity;

public class MainActivity extends BaseActivity {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        getIntentData();
    }

    /**
     * Method to initialising the shared preference class instance.
     */
    private void init() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private void getIntentData() {
        Intent intent = new Intent(MainActivity.this, SplashActivity.class);
        startActivity(intent);
        finish();
    }

}