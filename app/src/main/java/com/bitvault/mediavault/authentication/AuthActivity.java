package com.bitvault.mediavault.authentication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.bitvault.mediavault.common.Constant;
import com.embedded.wallet.BitVaultActivity;

import iclasses.UserAuthenticationCallback;

/**
 * Created by vvdn on 10/3/2017.
 */

public class AuthActivity extends BitVaultActivity implements UserAuthenticationCallback {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        /**
         * Moving to main page
         */
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Constant.result, true);
        setResult(Activity.RESULT_OK, returnIntent);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // Handling back press on Splash screen
    @Override
    public void onBackPressed() {
        finish();
    }

}
