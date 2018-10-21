package com.bitvault.mediavault.application;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.bitvault.mediavault.authentication.AuthActivity;
import com.bitvault.mediavault.authentication.BackgroundAuthActivity;
import com.bitvault.mediavault.authentication.SplashActivity;

/**
 * Created by vvdn on 10/6/2017.
 * This class used for maintain the life cycle of activity and get the event when the app is
 * in foreground/background
 */

public class ApplicationLifecycleHandler implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    private static final String TAG = ApplicationLifecycleHandler.class.getSimpleName();
    public static boolean isInBackground = false;

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        isInBackground = false;
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (isInBackground) {
            isInBackground = false;
            if ((activity instanceof AuthActivity) || (activity instanceof SplashActivity)) {
            } else {
                if ((activity instanceof BackgroundAuthActivity)) {
                } else {
                    Intent intent = new Intent(activity, BackgroundAuthActivity.class);
                    activity.startActivity(intent);
                }
            }
        }
    }


    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        isInBackground = false;
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
    }

    @Override
    public void onLowMemory() {
    }

    @Override
    public void onTrimMemory(int i) {
        if (i == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            isInBackground = true;
        }
    }
}