package com.bitvault.mediavault.authentication;

import com.embedded.wallet.BitVaultActivity;

import iclasses.UserAuthenticationCallback;

public class BackgroundAuthActivity extends BitVaultActivity implements UserAuthenticationCallback {
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
        finish();
    }

    @Override
    public void onBackPressed() {

    }
}
