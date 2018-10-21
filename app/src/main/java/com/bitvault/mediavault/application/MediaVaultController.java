package com.bitvault.mediavault.application;

import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;

import controller.SDKControl;
import io.fabric.sdk.android.Fabric;

/**
 * Created by vvdn on 6/28/2017.
 */

public class MediaVaultController extends SDKControl {
    public static final String TAG = MediaVaultController.class
            .getSimpleName();
    private static MediaVaultController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        ApplicationLifecycleHandler handler = new ApplicationLifecycleHandler();
        registerActivityLifecycleCallbacks(handler);
        registerComponentCallbacks(handler);
    }

    public static synchronized MediaVaultController getInstance() {
        return mInstance;
    }

    public static synchronized SharedPreferences getSharedPreferencesInstance() {
        return PreferenceManager.getDefaultSharedPreferences(mInstance);
    }

}
