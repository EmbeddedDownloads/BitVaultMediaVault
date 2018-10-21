package com.bitvault.mediavault.dashboard;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.application.MediaVaultController;
import com.bitvault.mediavault.authentication.AuthActivity;
import com.bitvault.mediavault.baseclass.BaseActivity;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.common.TaskFragment;
import com.bitvault.mediavault.helper.AudioViewData;
import com.bitvault.mediavault.helper.DialogData;
import com.bitvault.mediavault.helper.GalleryHelper;
import com.bitvault.mediavault.helper.PhotoViewData;
import com.bitvault.mediavault.helper.SharedPref;
import com.bitvault.mediavault.helper.VideoViewData;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.tabs.MediaVaultAllTab;
import com.bitvault.mediavault.tabs.MediaVaultSecureTab;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import commons.SDKHelper;
import controller.Preferences;
import utils.SDKUtils;

public class LandingActivity extends BaseActivity implements TaskFragment.TaskCallbacks {

    @BindView(R.id.dot)
    ImageView dot;
    @BindView(R.id.restore)
    ImageView restore;
    @BindView(R.id.frame_container)
    FrameLayout frameContainer;
    private static final int REQUEST_PERMISSIONS = 100;
    @BindView(R.id.tabs)
    TabLayout tabs;
    private MediaVaultAllTab listViewFragment;
    private AlbumViewFragment albumViewFragment;
    private MediaVaultSecureTab mediaVaultSecureTab;
    public boolean checkPermission = false;
    public boolean authSuccess = false;
    private SharedPreferences sharedPreferences;
    private String TAG1 = MediaVaultAllTab.class.getSimpleName();
    private String TAG2 = AlbumViewFragment.class.getSimpleName();
    private String TAG = LandingActivity.class.getSimpleName();
    private PopupWindow popupWindow;
    private ActionBar ab;
    private Toolbar toolbar;
    private TextView toolbarTitle, toolbarCount;
    private FetchDataInBackground fetchDataInBackground;

    public TaskFragment mTaskFragment;
    private FragmentManager fm;
    private static final String TAG_TASK_FRAGMENT = "task_fragment_new";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_activity);
        initView();
        checkPermission();
        updateToolbar(0);

    }

    /**
     * Initialised view and other data
     */
    private void initView() {
        new Preferences().saveData(this, "", SDKHelper.PREFERENCE_RECEIVE_NAME, SDKHelper.PREFERENCE_RECEIVE_KEY);
        /**
         * Registering the ButterKnife view Injection
         */
        ButterKnife.bind(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbarTitle);
        toolbarCount = (TextView) toolbar.findViewById(R.id.toolbarCount);
        /**
         * Initialising the shared preference class instance.
         */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        toolbarTitle.setText(R.string.app_name);
        toolbar.setTitle("");
        fm = getSupportFragmentManager();
        mTaskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mTaskFragment == null) {
            mTaskFragment = new TaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }
        ab = getSupportActionBar();
        /**
         * Setting toolbar
         */
        setSupportActionBar(toolbar);
        /**
         * Enable the back key on navigation bar in toolbar
         */
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
        }
    }

    /**
     * Retrun the instance of taskfragment
     *
     * @return
     */
    public TaskFragment getFragment() {
        return mTaskFragment;
    }

    /**
     * Method to update the toolbar view
     *
     * @param count
     */
    public void updateToolbar(int count) {
        if (count != 0) {
            dot.setVisibility(View.GONE);
            toolbarCount.setText(String.format(getString(R.string.selected_string), count));
            toolbarTitle.setText("");
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationIcon(R.drawable.ic_clear);
        } else {
            if (Constant.SECURE_TAB.equalsIgnoreCase(SharedPref.getSecureTab(sharedPreferences))) {
                dot.setVisibility(View.GONE);
                toolbarTitle.setText(R.string.app_name);
                toolbarCount.setText("");
                if (ab != null) {
                    ab.setDisplayHomeAsUpEnabled(false);
                }
                toolbar.setNavigationIcon(null);
            } else {
                dot.setVisibility(View.VISIBLE);
                toolbarTitle.setText(R.string.app_name);
                toolbarCount.setText("");
                if (ab != null) {
                    ab.setDisplayHomeAsUpEnabled(false);
                }
                toolbar.setNavigationIcon(null);
            }
        }

        invalidateOptionsMenu();
    }

    /**
     * This method is used for updating the toolbar from Secure tab class
     *
     * @param count
     */
    public void updateToolbarFromSecureTab(int count) {
        if (count != 0) {
            dot.setVisibility(View.GONE);
            toolbarCount.setText(String.format("%s selected", count));
            toolbarTitle.setText("");
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationIcon(R.drawable.ic_clear);

        } else {
            if (Constant.SECURE_TAB.equalsIgnoreCase(SharedPref.getSecureTab(sharedPreferences))) {
                dot.setVisibility(View.GONE);
                toolbarTitle.setText(R.string.app_name);
                toolbarCount.setText("");
                if (ab != null) {
                    ab.setDisplayHomeAsUpEnabled(false);
                }
                toolbar.setNavigationIcon(null);
            }
        }

        invalidateOptionsMenu();
    }

    /**
     * Show the restore button when user first time entering into application to back their
     * old file list form server if any.
     *
     * @param v
     */
    public void restore(View v) {

    }

    /**
     * Show the popup window on main screen for filtering the media files according to their preferences.
     *
     * @param v
     */
    public void showPopup(View v) {
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View popupView = layoutInflater.inflate(R.layout.custom_menu_layout, null);
            popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            popupWindow.setBackgroundDrawable(new BitmapDrawable());
            popupWindow.setOutsideTouchable(true);
            TextView albumView = (TextView) popupView.findViewById(R.id.albumView);
            TextView ListView = (TextView) popupView.findViewById(R.id.ListView);
            TextView allView = (TextView) popupView.findViewById(R.id.allView);
            TextView PhotoView = (TextView) popupView.findViewById(R.id.PhotoView);
            TextView videoView = (TextView) popupView.findViewById(R.id.videoView);
            TextView audioView = (TextView) popupView.findViewById(R.id.audioView);
            if (Constant.ALBUM_VIEW.equalsIgnoreCase(SharedPref.getAlbumType(sharedPreferences))) {
                if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                    allView.setTextColor(getResources().getColor(R.color.colorAccent));
                    audioView.setTextColor(getResources().getColor(R.color.light_black));
                    PhotoView.setTextColor(getResources().getColor(R.color.light_black));
                    videoView.setTextColor(getResources().getColor(R.color.light_black));
                } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                    audioView.setTextColor(getResources().getColor(R.color.colorAccent));
                    allView.setTextColor(getResources().getColor(R.color.light_black));
                    PhotoView.setTextColor(getResources().getColor(R.color.light_black));
                    videoView.setTextColor(getResources().getColor(R.color.light_black));
                } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                    videoView.setTextColor(getResources().getColor(R.color.colorAccent));
                    allView.setTextColor(getResources().getColor(R.color.light_black));
                    PhotoView.setTextColor(getResources().getColor(R.color.light_black));
                    audioView.setTextColor(getResources().getColor(R.color.light_black));
                } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                    PhotoView.setTextColor(getResources().getColor(R.color.colorAccent));
                    allView.setTextColor(getResources().getColor(R.color.light_black));
                    audioView.setTextColor(getResources().getColor(R.color.light_black));
                    videoView.setTextColor(getResources().getColor(R.color.light_black));
                }

                albumView.setTextColor(getResources().getColor(R.color.colorAccent));
                ListView.setTextColor(getResources().getColor(R.color.light_black));
            }
            if (Constant.LIST_VIEW.equalsIgnoreCase(SharedPref.getListType(sharedPreferences))) {
                if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                    allView.setTextColor(getResources().getColor(R.color.colorAccent));
                    audioView.setTextColor(getResources().getColor(R.color.light_black));
                    PhotoView.setTextColor(getResources().getColor(R.color.light_black));
                    videoView.setTextColor(getResources().getColor(R.color.light_black));
                } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                    audioView.setTextColor(getResources().getColor(R.color.colorAccent));
                    allView.setTextColor(getResources().getColor(R.color.light_black));
                    PhotoView.setTextColor(getResources().getColor(R.color.light_black));
                    videoView.setTextColor(getResources().getColor(R.color.light_black));
                } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                    videoView.setTextColor(getResources().getColor(R.color.colorAccent));
                    allView.setTextColor(getResources().getColor(R.color.light_black));
                    PhotoView.setTextColor(getResources().getColor(R.color.light_black));
                    audioView.setTextColor(getResources().getColor(R.color.light_black));
                } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                    PhotoView.setTextColor(getResources().getColor(R.color.colorAccent));
                    allView.setTextColor(getResources().getColor(R.color.light_black));
                    audioView.setTextColor(getResources().getColor(R.color.light_black));
                    videoView.setTextColor(getResources().getColor(R.color.light_black));
                }
                ListView.setTextColor(getResources().getColor(R.color.colorAccent));
                albumView.setTextColor(getResources().getColor(R.color.light_black));
            }
            albumView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPref.setAlbumType(sharedPreferences, Constant.ALBUM_VIEW);
                    SharedPref.setListType(sharedPreferences, "0");
                    setupTabLayout();
                    popupWindow.dismiss();
                }
            });
            ListView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPref.setListType(sharedPreferences, Constant.LIST_VIEW);
                    SharedPref.setAlbumType(sharedPreferences, "0");
                    setupTabLayout();
                    popupWindow.dismiss();
                }
            });
            allView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPref.setAllType(sharedPreferences, Constant.ALL_VIEW);
                    SharedPref.setPhotoType(sharedPreferences, "0");
                    SharedPref.setAudioType(sharedPreferences, "0");
                    SharedPref.setVideoType(sharedPreferences, "0");
                    setupTabLayout();
                    popupWindow.dismiss();
                }
            });
            PhotoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPref.setPhotoType(sharedPreferences, Constant.PHOTO_VIEW);
                    SharedPref.setAllType(sharedPreferences, "0");
                    SharedPref.setAudioType(sharedPreferences, "0");
                    SharedPref.setVideoType(sharedPreferences, "0");
                    setupTabLayout();
                    popupWindow.dismiss();
                }
            });
            videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPref.setVideoType(sharedPreferences, Constant.VIDEO_VIEW);
                    SharedPref.setAllType(sharedPreferences, "0");
                    SharedPref.setAudioType(sharedPreferences, "0");
                    SharedPref.setPhotoType(sharedPreferences, "0");
                    setupTabLayout();
                    popupWindow.dismiss();
                }
            });
            audioView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SharedPref.setAudioType(sharedPreferences, Constant.AUDIO_VIEW);
                    SharedPref.setAllType(sharedPreferences, "0");
                    SharedPref.setVideoType(sharedPreferences, "0");
                    SharedPref.setPhotoType(sharedPreferences, "0");
                    setupTabLayout();
                    popupWindow.dismiss();
                }
            });
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    popupWindow = null;
                }
            });
            //   popupWindow.setAnimationStyle(R.style.Animations_popup);
            popupWindow.showAtLocation(v, Gravity.TOP | Gravity.RIGHT, 20, 120);
        }
    }

    /**
     * add fragment  in Tab  layout
     */
    private void setupTabLayout() {
        tabs.removeAllTabs();
        clearBackStack();
        if (Constant.ALBUM_VIEW.equalsIgnoreCase(SharedPref.getAlbumType(sharedPreferences)))
            albumViewFragment = new AlbumViewFragment();
        else if (Constant.LIST_VIEW.equalsIgnoreCase(SharedPref.getListType(sharedPreferences)))
            listViewFragment = new MediaVaultAllTab();
        mediaVaultSecureTab = new MediaVaultSecureTab();
        if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
            tabs.addTab(tabs.newTab().setText(R.string.tab_all), true);
        } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
            tabs.addTab(tabs.newTab().setText(R.string.audio), true);
        } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
            tabs.addTab(tabs.newTab().setText(R.string.video), true);
        } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
            tabs.addTab(tabs.newTab().setText(R.string.photo), true);
        }
        tabs.addTab(tabs.newTab().setText(R.string.tab_secure));

    }

    /**
     * Removing all back stack fragment
     */
    private void clearBackStack() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = manager.getBackStackEntryAt(0);
            manager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    /**
     * Bind tab with tab change listener.
     */
    private void bindWidgetsWithAnEvent() {
        tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setCurrentTabFragment(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    /**
     * Set tab according to their position which is selected.
     *
     * @param tabPosition
     */
    private void setCurrentTabFragment(int tabPosition) {
        switch (tabPosition) {
            case 0:
                SharedPref.setSecureTab(sharedPreferences, "0");
                updateToolbar(0);
                if (Constant.ALBUM_VIEW.equalsIgnoreCase(SharedPref.getAlbumType(sharedPreferences))) {
                    replaceFragmentWithoutAnimation(albumViewFragment);
                } else if (Constant.LIST_VIEW.equalsIgnoreCase(SharedPref.getListType(sharedPreferences))) {
                    replaceFragmentWithoutAnimation(listViewFragment);
                }
                authSuccess = false;
                break;
            case 1:
                Intent i = new Intent(this, AuthActivity.class);
                startActivityForResult(i, Constant.AUTH_ACTIVITY_RESULT_CODE);
             /*   SharedPref.setSecureTab(sharedPreferences, Constant.SECURE_TAB);
                updateToolbar(0);
                replaceFragmentWithoutAnimation(mediaVaultSecureTab);*/

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.AUTH_ACTIVITY_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                authSuccess = true;
            }
        } else {
            authSuccess = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    /**
     * Checking  run time Permission For Android 6.O and Above
     */
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasWriteExternalStoragePermission = 0;
            int hasReadExternalStorageermission = 1;
            hasWriteExternalStoragePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            hasReadExternalStorageermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasWriteExternalStoragePermission != PackageManager.PERMISSION_GRANTED ||
                    hasReadExternalStorageermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
                return;
            } else {
                // after permission granted load  and add tab change listener
                bindWidgetsWithAnEvent();
                setupTabLayout();
                checkPermission = true;
            }
        } else {
            // after permission granted load  and add tab change listener
            bindWidgetsWithAnEvent();
            setupTabLayout();
            checkPermission = true;
        }
    }

    /**
     * method to replace fragment
     *
     * @param fragment
     */
    protected void replaceFragmentWithoutAnimation(Fragment fragment) {
        if (fragment != null) {
            String backStateName = fragment.getClass().getName();

            android.support.v4.app.FragmentManager manager = this.getSupportFragmentManager();
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

    @Override
    public void onBackPressed() {
        backPressHandling();
    }

    /**
     * Check that on back press from Navigation bar or back key, Then finish the activity if
     * there were <=1 fragment in stack else back the fragment.
     */
    private void backPressHandling() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
        // Check that popup window is visible or not ,if visible then close that popup window
        // then close the application
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        } else if (fragment instanceof MediaVaultAllTab) {
            if (((MediaVaultAllTab) fragment).getSelectedItemCount()) {
                ((MediaVaultAllTab) fragment).actionCrossKey();
            } else {
                FragmentManager manager = getSupportFragmentManager();
                if (manager.getBackStackEntryCount() <= 1) {
                    finish();
                } else {
                    manager.popBackStack();
                    // Selected tab highlight on back press
                    tabs.getTabAt(0).select();
                    authSuccess = false;
                }
            }
        } else if (fragment instanceof MediaVaultSecureTab) {
            if (((MediaVaultSecureTab) fragment).getSelectedItemCount()) {
                ((MediaVaultSecureTab) fragment).actionCrossKey();
            } else if (!((MediaVaultSecureTab) fragment).progressBarVisibilityStatus()) {
                showAlertDialog();
            } else {
                FragmentManager manager = getSupportFragmentManager();
                if (manager.getBackStackEntryCount() <= 1) {
                    finish();
                } else {
                    manager.popBackStack();
                    // Selected tab highlight on back press
                    tabs.getTabAt(0).select();
                    authSuccess = false;
                }
            }
            // progressBarVisibilityStatus
        } else {
            FragmentManager manager = getSupportFragmentManager();
            if (manager.getBackStackEntryCount() <= 1) {
                finish();
            } else {
                manager.popBackStack();
                // Selected tab highlight on back press
                tabs.getTabAt(0).select();
                authSuccess = false;
            }
        }
    }

    /**
     * show alert dialog when user backpress from secure tab while file downloading in progress
     */
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.delete_alert_dialog, null);
        builder.setView(view);
        TextView okBtn = (TextView) view.findViewById(R.id.alert_yes_button);
        TextView alertMessage = (TextView) view.findViewById(R.id.alert_message);
        TextView cancelBtn = (TextView) view.findViewById(R.id.alert_no_button);
        TextView alertTitle = (TextView) view.findViewById(R.id.alertTitle);
        alertTitle.setText(R.string.cancel_download);
        alertMessage.setText(R.string.are_you_sure_want_to_cancel_download);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // method to back press
                backPressCall();
                alertDialog.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    /***
     * Method to call back press when user click on cancel download button
     */
    private void backPressCall() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() <= 1) {
            finish();
        } else {
            manager.popBackStack();
            // Selected tab highlight on back press
            tabs.getTabAt(0).select();
            authSuccess = false;
        }
    }

    /**
     * Get call back of Storage permissions while using the application
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                //If permission is granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // after permission granted load tabs
                    bindWidgetsWithAnEvent();
                    setupTabLayout();
                    checkPermission = true;
                } else {
                    SDKUtils.showToast(LandingActivity.this, getString(R.string.storage_permission_message));
                }
                // }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Check restore file status...
        if (SharedPref.getVisibilityRestoreButton(sharedPreferences)) {
            //  restore.setVisibility(View.VISIBLE);
        } else {
            //restore.setVisibility(View.GONE);
        }
        // Move to secure tab
        fetchData();
        if (authSuccess)
            moveToSecureTab();
        else {
            authSuccess = false;
            setCurrentTabFragment(0);
            // Selected tab highlight on back press
            if (tabs != null && tabs.getTabAt(0) != null)
                tabs.getTabAt(0).select();
        }

    }

    // method to move in secure tab after auth success...
    private void moveToSecureTab() {
        SharedPref.setSecureTab(sharedPreferences, Constant.SECURE_TAB);
        updateToolbar(0);
        replaceFragmentWithoutAnimation(mediaVaultSecureTab);
    }

    /**
     * Avoid low memory exception,Clear the Glide cache
     * memory and run System.gc to remove the garbage the data .
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(getApplicationContext()).clearMemory();
        Glide.get(getApplicationContext()).trimMemory(TRIM_MEMORY_COMPLETE);
        System.gc();
    }

    public void actionCopyOnDialogResponse(String currentDirectory, boolean actionType) {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
        if (fragment instanceof MediaVaultAllTab) {
            // Get selected file list form adapter
            ArrayList<ImageDataModel> selectedItems = ((MediaVaultAllTab) fragment).listViewAdapter.getSelectedItems();
            //Remove selected file from adapter
            ((MediaVaultAllTab) fragment).listViewAdapter.clearSelection();
            if (actionType) {
                //removing  the item from mail list
                for (ImageDataModel file : selectedItems) {
                    File newFile = new File(new File(currentDirectory), file.getFile().getName());
                    if (!newFile.exists()) {
                        // Remove the item  from Current  List
                        if (SharedPref.getListType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.LIST_VIEW)) {
                            if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                                GalleryHelper.imageDataModelList.remove(file);
                            } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                                VideoViewData.imageDataModelList.remove(file);
                            } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                                AudioViewData.imageDataModelList.remove(file);
                            } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                                PhotoViewData.imageDataModelList.remove(file);
                            }
                            ((MediaVaultAllTab) fragment).listViewAdapter.removeFileNotifyView(file);
                        }
                    }

                }
                //removing the item from local list and notify the adapter


            }
            transferFiles(selectedItems, actionType, currentDirectory);
        }
    }

    /**
     * Method to transfer the file like copy/move
     *
     * @param dataModels       selected modelList item from adapter
     * @param actionType       type of action perform like copy/move
     * @param currentDirectory name of new file directory
     */
    public void transferFiles(final List<ImageDataModel> dataModels, final Boolean actionType, String currentDirectory) {
        ArrayList<File> src = new ArrayList<>();
        ArrayList<File> desc = new ArrayList<>();
        for (ImageDataModel file : dataModels) {
            src.add(file.getFile());
            desc.add(new File(currentDirectory));
        }
        if (mTaskFragment != null) {
            mTaskFragment.StartCopyTask(LandingActivity.this, src, desc, actionType);
        }
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onProgressUpdate(int percent) {

    }

    @Override
    public void onCancelled() {

    }

    @Override
    public void onPostExecute(final String message) {
        if (LandingActivity.this != null)
            LandingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Show the message of action perform like copy/move
                    SDKUtils.showToast(MediaVaultController.getInstance(), message);
                    // Check that any item in the folder or not.
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_container);
                    if (fragment instanceof MediaVaultAllTab) {
                        ((MediaVaultAllTab) fragment).updateToolbarView(((MediaVaultAllTab) fragment).listViewAdapter.getSelectedItemCount());
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fetchData();
                        }
                    },200);
                }
            });
    }

    //Method to fetch data in background
    private void fetchData() {
        if (fetchDataInBackground != null && fetchDataInBackground.getStatus() != AsyncTask.Status.FINISHED)
            fetchDataInBackground.cancel(true);
        fetchDataInBackground = new FetchDataInBackground();
        fetchDataInBackground.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Fetch data in background
     */
    class FetchDataInBackground extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            if (LandingActivity.this!=null)
            DialogData.getImageFolderMap(LandingActivity.this);
            return null;
        }
    }
}
