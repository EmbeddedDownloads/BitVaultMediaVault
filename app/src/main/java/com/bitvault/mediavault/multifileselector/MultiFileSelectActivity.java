package com.bitvault.mediavault.multifileselector;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;
import android.widget.Toolbar;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.helper.SharedPref;
import com.embedded.wallet.BitVaultActivity;

import java.util.ArrayList;

import iclasses.UserAuthenticationCallback;

/**
 * Created by vvdn on 11-Dec-17.
 */

public class MultiFileSelectActivity extends BitVaultActivity implements UserAuthenticationCallback {
    TextView toolbarTitle;
    TextView toolbarCount;
    Toolbar toolbar;
    private boolean multipleAllow = false;
    private android.app.ActionBar ab;
    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Register the sdk for authentication the use to logging the application.
         */
        validateUser(this, this);
    }

    /**
     * Method to initialising the shared preference class instance.
     */
    private void init() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    /**
     * Initialise toolbar and set as action bar
     */
    private void initActionBar() {
        ab = getActionBar();
        /**
         * Setting toolbar
         */
        setActionBar(toolbar);
        /**
         * Enable the back key on navigation bar in toolbar
         */
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
        }
        toolbarTitle.setText(R.string.app_name);
        toolbar.setTitle("");
    }

    /**
     * Method to update the toolbar view
     *
     * @param count
     */
    public void updateToolbar(int count, String titleName) {
        if (count != 0) {
            toolbarCount.setText(String.format(getString(R.string.selected_string), count));
            toolbarTitle.setText("");
            enableClearKey();
        } else {
            enableBackKey();
            toolbarCount.setText("");
            if (titleName != null && !titleName.equalsIgnoreCase(""))
                toolbarTitle.setText(titleName);
        }
        invalidateOptionsMenu();
    }

    /**
     * Handle back press event
     */
    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
        if (fragment instanceof MultiFileSelectFragment) {
            ((MultiFileSelectFragment) fragment).actionBackPress();
        } else {
            FragmentManager manager = getSupportFragmentManager();
            if (manager.getBackStackEntryCount() <= 1) {
                finish();
            } else {
                updateToolbar(0, getResources().getString(R.string.app_name));
                manager.popBackStack();
            }
        }
    }

    /**
     * Method to disable back key on activity
     */
    public void disableBackKey() {
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
        }
        toolbar.setNavigationIcon(null);
    }

    /**
     * Method to enable back key on activity
     */
    public void enableBackKey() {
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_back);
    }

    /**
     * Method to enable clear key on activity
     */
    public void enableClearKey() {
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_clear);
    }

    /**
     * Method to get data from coming intent...
     */
    private void getIntentData() {
        if (getIntent() != null && getIntent().getAction() != null &&
                (getIntent().getAction().equalsIgnoreCase(Intent.ACTION_GET_CONTENT)
                        || getIntent().getAction().equalsIgnoreCase(Intent.ACTION_PICK))
                && getIntent().getType() != null) {
            multipleAllow = getIntent().getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
            if (multipleAllow) {
                SharedPref.setAnyContentIntent(sharedPreferences, Constant.ALL_VIEW);
            } else {
                SharedPref.setImageContentIntent(sharedPreferences, Constant.PHOTO_VIEW);
            }
        }
    }

    /**
     * initialisation of fragment and sending data to fragment
     */
    private void initFragment() {
        Bundle data = new Bundle();
        MultiViewFolderFragment fragment = new MultiViewFolderFragment();
        data.putBoolean(Constant.MULTIPLE_SELECT, multipleAllow);
        fragment.setArguments(data);
        replaceFragmentWithoutAnimation(fragment);
    }

    @Override
    public void onAuthenticationSuccess() {
        this.setContentView(R.layout.activity_multi_image_select);
        toolbarTitle = (TextView) findViewById(R.id.toolbarTitle);
        toolbarCount = (TextView) findViewById(R.id.toolbarCount);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        initActionBar();
        init();
        getIntentData();
        initFragment();
    }

    /**
     * method to replace fragment
     *
     * @param fragment
     */
    protected void replaceFragmentWithoutAnimation(Fragment fragment) {
        if (fragment != null) {
            String backStateName = fragment.getClass().getName();

            FragmentManager manager = this.getSupportFragmentManager();
            boolean fragmentPopped = manager
                    .popBackStackImmediate(backStateName, 0);
            // fragment not in back stack, create it.
            if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(R.id.frame_container, fragment, backStateName);
                ft.addToBackStack(backStateName);
                ft.commitAllowingStateLoss();
            }
        }
    }

    /**
     * Back result to other application which launch the media vault app
     *
     * @param uriArrayList
     */
    public void backResultToLaunchingApp(ArrayList<Uri> uriArrayList) {
        int size = uriArrayList.size();
        Intent intent = new Intent();
        String[] mimeType = new String[]{"*/*"};
        ClipData clipData = new ClipData(getString(R.string.image), mimeType, new ClipData.Item(null, null, (uriArrayList.get(0))));
        for (int i = 1; i < size; i++) {
            clipData.addItem(new ClipData.Item(null, null, uriArrayList.get(i)));
        }
        /**
         * Security Exception from attachment uri handled by below permission
         */
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (size == 1) {
            intent.setData(uriArrayList.get(0));
        } else
            intent.setClipData(clipData);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
