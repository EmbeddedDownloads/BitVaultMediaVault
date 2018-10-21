package com.bitvault.mediavault.authentication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.dashboard.LandingActivity;
import com.bitvault.mediavault.helper.SharedPref;
import com.embedded.wallet.BitVaultActivity;

import iclasses.UserAuthenticationCallback;

/**
 * This class for showing the splash screen of application for a while and then mve to Landing fragment.
 */
public class SplashActivity extends BitVaultActivity implements UserAuthenticationCallback {
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();

    }

    /**
     * Initialising the shared preference class instance.
     */
    private void initData() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //Setting the type of view and retain the state of last view.
        if (!Constant.IS_FIRST_LAUNCH.equalsIgnoreCase(SharedPref.getIsFirstLaunch(sharedPreferences))) {
            SharedPref.setIsFirstLaunch(sharedPreferences, Constant.IS_FIRST_LAUNCH);
            SharedPref.setAlbumType(sharedPreferences, Constant.ALBUM_VIEW);
            SharedPref.setListType(sharedPreferences, "0");
            SharedPref.setSecureTab(sharedPreferences, "0");
            //Setting type of data on view
            SharedPref.setAllType(sharedPreferences, Constant.ALL_VIEW);
            SharedPref.setAudioType(sharedPreferences, "0");
            SharedPref.setVideoType(sharedPreferences, "0");
            SharedPref.setPhotoType(sharedPreferences, "0");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * Register the sdk for authentication the use to logging the application.
         */
        validateUser(this, this);
    }

    /**
     * On Authentication successful move to splash screen
     */
    @Override
    public void onAuthenticationSuccess() {
        startActivity(new Intent(SplashActivity.this, LandingActivity.class));
        finish();
    }

    // Handling back press on Splash screen
    @Override
    public void onBackPressed() {
        finish();
    }
}
