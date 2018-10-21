package com.bitvault.mediavault.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.adapter.CustomPagerAdapter;
import com.bitvault.mediavault.application.MediaVaultController;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.common.ViewPagerAsyncTask;
import com.bitvault.mediavault.croplibrary.CropMainActivity;
import com.bitvault.mediavault.database.MediaFileDuration;
import com.bitvault.mediavault.database.MediaVaultLocalDb;
import com.bitvault.mediavault.dialogfragment.ViewPagerDialogFragment;
import com.bitvault.mediavault.helper.AudioViewData;
import com.bitvault.mediavault.helper.AudioViewDataOnIdBasis;
import com.bitvault.mediavault.helper.DialogData;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.helper.GalleryHelper;
import com.bitvault.mediavault.helper.GalleryHelperBaseOnId;
import com.bitvault.mediavault.helper.GlobalBus;
import com.bitvault.mediavault.helper.Measure;
import com.bitvault.mediavault.helper.PhotoViewData;
import com.bitvault.mediavault.helper.PhotoViewDataOnIdBasis;
import com.bitvault.mediavault.helper.SharedPref;
import com.bitvault.mediavault.helper.VideoViewData;
import com.bitvault.mediavault.helper.VideoViewDataOnIdBasis;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.ottonotification.CropNotification;
import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import utils.SDKUtils;

public class PhotoViewActivity extends AppCompatActivity implements ViewPagerAsyncTask.TaskCallbacks {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.pager)
    ViewPager pager;
    @BindView(R.id.photo_view_activity)
    RelativeLayout photoViewActivity;
    private CustomPagerAdapter CustomPagerAdapter;
    private int position = -1;
    private ArrayList<ImageDataModel> dataModelArrayList = new ArrayList<>();
    // private String nameKey = null;
    private boolean fullScreenMode = false;
    private String nameKey;
    private FragmentManager fm;
    private static final String TAG_TASK_FRAGMENT = "task_fragment_view_pager";
    public ViewPagerAsyncTask mTaskFragment;
    private Handler handler = new Handler();
    private ImageDataModel imageDataModel;
    private SharedPreferences sharedPreferences;
    private MediaVaultLocalDb secureMediaFileDb;
    private MediaFileDuration mediaFileDuration;
    private FetchDialogDataInBackground fetchDialogDataInBackground;

    // get real path from URI
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {"*"};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_view_activity);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

       /* Registering the Otto bus event for Handling the notification
                * call back when user swipe to refresh the view.*/
        GlobalBus.getBus().register(this);
        initClass();
        setToolBar();
        setupSystemUI();
        setFragment();
        getIntentData();
        setDataOnList();
        setAdapterAndListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    //Method to fetch data in background
    private void fetchData() {
        if (fetchDialogDataInBackground != null && fetchDialogDataInBackground.getStatus() != AsyncTask.Status.FINISHED)
            fetchDialogDataInBackground.cancel(true);
        fetchDialogDataInBackground = new FetchDialogDataInBackground();
        fetchDialogDataInBackground.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Fetch data in background
     */
    class FetchDialogDataInBackground extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            if (PhotoViewActivity.this != null)
                DialogData.getImageFolderMap(PhotoViewActivity.this);
            return null;
        }
    }

    /**
     * Initialising  the view pager adapter class and set the adapter class on view pager and its change listener
     */
    private void setAdapterAndListener() {
        CustomPagerAdapter = new CustomPagerAdapter(getSupportFragmentManager(), PhotoViewActivity.this, dataModelArrayList);
        pager.setAdapter(CustomPagerAdapter);
        pager.setOffscreenPageLimit(3);
        pager.setCurrentItem(position);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int pos, float positionOffset, int positionOffsetPixels) {
                /*
                * Setting the title on toolbar
                * */
                position = pos;
                String name = dataModelArrayList.get(position).getFile().getName();
                String actualName = FileUtils.getSecureFileName(name);
                if (actualName != null && !actualName.equalsIgnoreCase("")) {
                    updateTileBar(actualName);
                } else {
                    updateTileBar(name);
                }


            }


            @Override
            public void onPageSelected(int num) {
                int pos = 0;
                //Update the toolbar menu item on page change
                invalidateOptionsMenu();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * Set data on list
     */
    private void setDataOnList() {
        if (SharedPref.getSecureTab(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.SECURE_TAB)) {
            dataModelArrayList.addAll(secureMediaFileDb.getSecureUnarchiveFileList());
            Collections.sort(dataModelArrayList);
            position = dataModelArrayList.indexOf(imageDataModel);
        } else if (SharedPref.getListType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.LIST_VIEW)) {
            if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                dataModelArrayList.addAll(GalleryHelper.imageDataModelList);
            } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                dataModelArrayList.addAll(VideoViewData.imageDataModelList);
            } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                dataModelArrayList.addAll(AudioViewData.imageDataModelList);
            } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                dataModelArrayList.addAll(PhotoViewData.imageDataModelList);
            }
            Collections.sort(dataModelArrayList);
            position = dataModelArrayList.indexOf(imageDataModel);
        } else if (SharedPref.getAlbumType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.ALBUM_VIEW)) {
            if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                dataModelArrayList.addAll(GalleryHelperBaseOnId.dataModelArrayList);
            } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                dataModelArrayList.addAll(VideoViewDataOnIdBasis.dataModelArrayList);
            } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                dataModelArrayList.addAll(AudioViewDataOnIdBasis.dataModelArrayList);
            } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                dataModelArrayList.addAll(PhotoViewDataOnIdBasis.dataModelArrayList);
            }
            Collections.sort(dataModelArrayList);
        }
    }

    /**
     * Get data from intent
     */
    private void getIntentData() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            //  Handling intent data from DirectoryDetailsActivity class
            if (Constant.ALBUM_TYPE.equalsIgnoreCase(getIntent().getExtras().getString(Constant.TYPE))) {
                position = getIntent().getIntExtra(Constant.POSITION, 0);
                nameKey = getIntent().getStringExtra(Constant.KEY_NAME);
            } else if (Constant.LIST_TYPE.equalsIgnoreCase(getIntent().getExtras().getString(Constant.TYPE))) {
                imageDataModel = getIntent().getParcelableExtra(Constant.DATA);
            } else if (Constant.SECURE_TAB.equalsIgnoreCase(getIntent().getExtras().getString(Constant.TYPE))) {
                imageDataModel = getIntent().getParcelableExtra(Constant.DATA);
            } else {
                // Handling other intent data like camera intent
                Uri uri = getIntent().getData();
                GalleryHelper.getImageFolderMap(this);
                File file = new File(getRealPathFromURI(uri));
                nameKey = FileUtils.getParentName(file.getParent());
                GalleryHelperBaseOnId.getMediaFilesOnIdBasis(this, GalleryHelper.imageFolderMap.get(nameKey).get(0).getBucketId());
            }
        }
    }

    /**
     * Set fragment while activity start
     */
    private void setFragment() {
        dataModelArrayList.clear();
        fm = getSupportFragmentManager();
        mTaskFragment = (ViewPagerAsyncTask) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mTaskFragment == null) {
            mTaskFragment = new ViewPagerAsyncTask();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }
    }

    /**
     * Enable the back key on navigation bar in toolbar
     */
    private void setToolBar() {
        toolbar.setTitle(R.string.photo_details);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_back);
    }

    /**
     * Initialising the classes like shared preferences and database
     */
    private void initClass() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        secureMediaFileDb = MediaVaultLocalDb.getMediaVaultDatabaseInstance(this);
        mediaFileDuration = MediaFileDuration.getDatabaseInstance(this);
    }

    private void notifyAdapter() {
        dataModelArrayList.clear();
        if (SharedPref.getSecureTab(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.SECURE_TAB)) {
            dataModelArrayList.addAll(secureMediaFileDb.getSecureUnarchiveFileList());
            Collections.sort(dataModelArrayList);
            position = dataModelArrayList.indexOf(imageDataModel);
        }
        if (SharedPref.getListType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.LIST_VIEW)) {
            if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                dataModelArrayList.addAll(GalleryHelper.imageDataModelList);
            } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                dataModelArrayList.addAll(VideoViewData.imageDataModelList);
            } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                dataModelArrayList.addAll(AudioViewData.imageDataModelList);
            } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                dataModelArrayList.addAll(PhotoViewData.imageDataModelList);
            }
        } else if (SharedPref.getAlbumType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.ALBUM_VIEW))
            if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                dataModelArrayList.addAll(GalleryHelperBaseOnId.dataModelArrayList);
            } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                dataModelArrayList.addAll(VideoViewDataOnIdBasis.dataModelArrayList);
            } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                dataModelArrayList.addAll(AudioViewDataOnIdBasis.dataModelArrayList);
            } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                dataModelArrayList.addAll(PhotoViewDataOnIdBasis.dataModelArrayList);
            }
        Collections.sort(dataModelArrayList);
        CustomPagerAdapter.notifyDataSetChanged();
    }

    /**
     * Method to update toolbar title
     *
     * @param title
     */
    private void updateTileBar(String title) {
        toolbar.setTitle(title);
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
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() <= 1) {
            finish();
        } else {
            manager.popBackStack();
        }
    }

    /**
     * Method to set Toolbar view with full screen
     */
    private void setupSystemUI() {
        toolbar.animate().translationY(Measure.getStatusBarHeight(getResources())).setInterpolator(new DecelerateInterpolator())
                .setDuration(0).start();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /**
     * this method to show and hide the toolbar with animation
     * when user touche on image in PhotoViewFragment class
     */
    public void toggleSystemUI() {
        if (fullScreenMode)
            showSystemUI();
        else hideSystemUI();
    }

    /**
     * This method for hide the toolbar with notification bar with animation
     */
    public void hideSystemUI() {
        if (PhotoViewActivity.this != null)
            PhotoViewActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator())
                            .setDuration(240).start();
                    photoViewActivity.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
                    fullScreenMode = true;
                }
            });
    }

    /**
     * This method for show the toolbar with notification bar with animation
     */
    public void showSystemUI() {
        if (PhotoViewActivity.this != null)
            PhotoViewActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    toolbar.animate().translationY(Measure.getStatusBarHeight(getResources())).setInterpolator(new DecelerateInterpolator())
                            .setDuration(240).start();
                    photoViewActivity.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    fullScreenMode = false;
                }
            });

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.action_menu_viewpager, menu);

        return true;
    }

    /**
     * Perform action as the menu items click
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                backPressHandling();
                return true;

            case R.id.action_delete:
                actionDelete();
                return true;

            case R.id.action_rename:
                renameFile();
                return true;


            case R.id.action_copy:
                if (mTaskFragment != null && !mTaskFragment.isRunningAsync()) {
                    actionCopyMove(false);
                } else {
                    SDKUtils.showToast(MediaVaultController.getInstance(), getString(R.string.task_running_status));
                }
                return true;

            case R.id.action_move:
                if (mTaskFragment != null && !mTaskFragment.isRunningAsync()) {
                    actionCopyMove(true);
                } else {
                    SDKUtils.showToast(MediaVaultController.getInstance(), getString(R.string.task_running_status));
                }
                return true;
            case R.id.action_edit:
                actionCrop();
                return true;
            case R.id.action_info:
                if (dataModelArrayList.get(position) != null) {
                    Intent intent = new Intent(this, InfoActivity.class);
                    intent.putExtra(Constant.DATA, dataModelArrayList.get(position));
                    startActivity(intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * @param menu show and hide menu items as item selected
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Hide  the crop option  in menu if file is other than image
        // For secure media files show/hide option menus
        if (SharedPref.getSecureTab(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.SECURE_TAB)) {
            menu.findItem(R.id.action_edit).setVisible(false);
            menu.findItem(R.id.action_move).setVisible(false);
            menu.findItem(R.id.action_copy).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(false);
            menu.findItem(R.id.action_rename).setVisible(false);
            menu.findItem(R.id.action_info).setVisible(false);
        } else if (dataModelArrayList != null && dataModelArrayList.size() > 0 && dataModelArrayList.get(position) != null) {
            // For other  media files show/hide option menus
            menu.findItem(R.id.action_delete).setVisible(true);
            menu.findItem(R.id.action_rename).setVisible(FileUtils.isImageFile(dataModelArrayList.get(position).getFile().getAbsolutePath()));
            menu.findItem(R.id.action_info).setVisible(true);
            menu.findItem(R.id.action_edit).setVisible(FileUtils.isImageFile(dataModelArrayList.get(position).getFile().getAbsolutePath()));
            menu.findItem(R.id.action_move).setVisible(!FileUtils.isAudioFile(dataModelArrayList.get(position).getFile().getAbsolutePath()));
            menu.findItem(R.id.action_copy).setVisible(!FileUtils.isAudioFile(dataModelArrayList.get(position).getFile().getAbsolutePath()));
        }
        return super.onPrepareOptionsMenu(menu);
    }


    // Crop the file
    private void actionCrop() {
        Intent intent = new Intent(this, CropMainActivity.class);
        intent.putExtra(Constant.DATA, dataModelArrayList.get(position));
        intent.putExtra(Constant.INTENT_STATUS, true);
        startActivity(intent);
    }

    /**
     * Method to show the dialogue for  rename a file which is selected inside into a folder.
     */
    private void renameFile() {
        // Get modeldata of the selected file to rename
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input;
        builder.setTitle(R.string.rename_dialog_title);
        View view = View.inflate(this, R.layout.dialog_edit_text, null);

        input = (EditText) view.findViewById(R.id.dialog_edit_text);

        builder.setView(view);
        builder.setPositiveButton(R.string.rename_dialog_ok_button, null);
        builder.setNegativeButton(R.string.rename_dialog_cancel_button, null);
        // setting the file name into dialog text after removing the extension name of a file
        input.setText(FileUtils.removeExtension(dataModelArrayList.get(position).getFile().getName()));
        // set the cursor at the end of edit text
        input.setSelection(input.getText().length());
        final AlertDialog dialog = builder.create();
        // for showing the keyboard when dialog is open,must call before dialog.show to get desired result
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = input.getText().toString().trim();
                if (fileName.length() != 0) {
                    if (fileName.length() <= 30) {
                        if (FileUtils.getSpecialCharacterValidation(fileName)) {

                            final File newFile = new File(dataModelArrayList.get(position).getFile().getParent(), input.getText().toString() + "." +
                                    FileUtils.getExtension(dataModelArrayList.get(position).getFile().getName()));
                            if (newFile.exists()) {
                                SDKUtils.showToast(PhotoViewActivity.this, getString(R.string.file_already_exists_error));
                            } else {
                                // Method to update the name of file
                                updateFileName(dataModelArrayList.get(position), input.getText().toString());
                                dialog.dismiss();
                            }


                            //Hide the keyboard after dismiss the dialog.
                            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        } else {
                            SDKUtils.showToast(PhotoViewActivity.this, getString(R.string.file_name_invalid_character));
                        }
                    } else {
                        SDKUtils.showToast(PhotoViewActivity.this, getString(R.string.rename_file_name_length));
                    }
                } else {
                    SDKUtils.showToast(PhotoViewActivity.this, getString(R.string.empty_dialog_message));
                }
            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //Hide the keyboard after dismiss the dialog.
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            }
        });
    }

    /**
     * Method to update the name of selected file on dialog click event
     *
     * @param dataModel
     * @param rename
     */
    private void updateFileName(ImageDataModel dataModel, String rename) {
        try {
            File file = dataModel.getFile();
            // method to rename the file and then set to model data for update the index of file in main list
            dataModel.setFile(FileUtils.renameFile(dataModel.getFile(), rename));
            if (SharedPref.getListType(MediaVaultController.getSharedPreferencesInstance())
                    .equals(Constant.LIST_VIEW)) {
                GalleryHelper.imageFolderMap.get(nameKey).get(0).setFile(dataModel.getFile());
                if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                    GalleryHelper.imageDataModelList.get(0).setFile(dataModel.getFile());
                } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                    VideoViewData.imageDataModelList.get(0).setFile(dataModel.getFile());
                } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                    AudioViewData.imageDataModelList.get(0).setFile(dataModel.getFile());
                } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                    PhotoViewData.imageDataModelList.get(0).setFile(dataModel.getFile());
                }
            }
            if (SharedPref.getAlbumType(MediaVaultController.getSharedPreferencesInstance()).
                    equals(Constant.ALBUM_VIEW)) {
                // Update the name of Main List of GalleryHelper.class
                GalleryHelper.imageFolderMap.get(nameKey).get(0).setFile(dataModel.getFile());
                if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                    GalleryHelper.imageDataModelList.get(0).setFile(dataModel.getFile());
                } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                    VideoViewData.imageDataModelList.get(0).setFile(dataModel.getFile());
                } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                    AudioViewData.imageDataModelList.get(0).setFile(dataModel.getFile());
                } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                    PhotoViewData.imageDataModelList.get(0).setFile(dataModel.getFile());
                }
            }

                        /*
                        * Scan old and new file to update entry into content provider.
                        * */
            FileUtils.scanFile(PhotoViewActivity.this, dataModel.getFile());
            FileUtils.scanFile(PhotoViewActivity.this, file);
            // update local database for time
            mediaFileDuration.updateFileName(dataModel.getFile().getAbsolutePath(), file.getAbsolutePath());
            //Update toolbar with new file name
            String name = dataModel.getFile().getName();
            String actualName = FileUtils.getSecureFileName(name);
            if (actualName != null && !actualName.equalsIgnoreCase("")) {
                updateTileBar(actualName);
            } else {
                updateTileBar(name);
            }
        } catch (Exception e) {
            SDKUtils.showToast(PhotoViewActivity.this, e.toString());
        }
    }

    /**
     * this method for performing the delete operation and update toolbar with option menu.
     */
    private void actionDelete() {
        showDeleteDialog();
    }

    // Show the delete dialog for deleting file(s)
    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.delete_alert_dialog, null);
        builder.setView(view);
        TextView okBtn = (TextView) view.findViewById(R.id.alert_yes_button);
        TextView cancelBtn = (TextView) view.findViewById(R.id.alert_no_button);
        final AlertDialog deleteDialog = builder.create();
        deleteDialog.show();
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // method to delete file after click on OK button
                deleteFile();
                deleteDialog.dismiss();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialog.dismiss();
            }
        });
    }

    /**
     * Method to call action delete method for deleting the file
     */
    private void deleteFile() {
        actionDelete(dataModelArrayList.get(position));
    }

    /**
     * Get the selected files list and delete , then  scan the file entry by
     * FileUtils.scanFile(Context,file)method.
     *
     * @param modelData
     */
    private void actionDelete(final ImageDataModel modelData) {
        // Remove the item  from Current  List of GalleryHelperBaseOnId.class
        if (SharedPref.getListType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.LIST_VIEW)) {
            if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                GalleryHelper.imageDataModelList.remove(modelData);
            } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                VideoViewData.imageDataModelList.remove(modelData);
            } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                AudioViewData.imageDataModelList.remove(modelData);
            } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                PhotoViewData.imageDataModelList.remove(modelData);
            }
        }

        if (SharedPref.getAlbumType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.ALBUM_VIEW)) {
            if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                GalleryHelperBaseOnId.dataModelArrayList.remove(modelData);
                // Remove the item  from Main List of GalleryHelper.class
                GalleryHelper.imageFolderMap.get(nameKey).remove(modelData);
            } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                VideoViewDataOnIdBasis.dataModelArrayList.remove(modelData);
                VideoViewData.imageFolderMap.get(nameKey).remove(modelData);
            } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                AudioViewData.imageFolderMap.get(nameKey).remove(modelData);
                AudioViewDataOnIdBasis.dataModelArrayList.remove(modelData);
            } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                PhotoViewData.imageFolderMap.get(nameKey).remove(modelData);
                PhotoViewDataOnIdBasis.dataModelArrayList.remove(modelData);
            }
        }


        final String message = String.format(getString(R.string.delete_success_message));
        try {
            // method to deleting the selected file
            FileUtils.deleteFile(modelData.getFile());
            FileUtils.deleteDirectory(modelData.getFile().getParentFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // method to scan the file and update its position
        FileUtils.scanFile(PhotoViewActivity.this, modelData.getFile());

        SDKUtils.showToast(PhotoViewActivity.this, message);
        // notify viewpager adapter
        notifyAdapter();
        if (CustomPagerAdapter != null)
            CustomPagerAdapter.itemCheckInList();
    }

    /**
     * Method to open dialog fragment
     *
     * @param ActionType type of action perform like copy/move
     */
    private void actionCopyMove(boolean ActionType) {
        ViewPagerDialogFragment dFragment = new ViewPagerDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(Constant.ACTION_TYPE, ActionType);
        args.putString(Constant.KEY_NAME, nameKey);
        dFragment.setArguments(args);
        // Show DialogFragment
        fm = getSupportFragmentManager();
        // dFragment.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme);
        dFragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.MyCustomThemeDialog);
        dFragment.show(fm, getString(R.string.dialog_fragment));
    }

    /**
     * method call from FolderDialogFragment class to copy/move the items
     *
     * @param currentDirectory current directory name
     * @param actionType       type of action
     */
    public void actionCopyOnDialogResponse(String currentDirectory, boolean actionType) {
        // Get selected file list form adapter
        ArrayList<ImageDataModel> selectedItems = new ArrayList<>();
        selectedItems.clear();
        selectedItems.add(dataModelArrayList.get(position));
        if (actionType) {
            //removing  the item from mail list
            for (ImageDataModel file : selectedItems) {
                if (SharedPref.getAlbumType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.ALBUM_VIEW)) {
                    if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                        // Remove the item  from Current  List of GalleryHelperBaseOnId.class
                        GalleryHelperBaseOnId.dataModelArrayList.remove(file);
                        // Remove the item  from Main List of GalleryHelper.class
                        GalleryHelper.imageFolderMap.get(nameKey).remove(file);
                    } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                        VideoViewDataOnIdBasis.dataModelArrayList.remove(file);
                        VideoViewData.imageFolderMap.get(nameKey).remove(file);
                    } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                        AudioViewData.imageFolderMap.get(nameKey).remove(file);
                        AudioViewDataOnIdBasis.dataModelArrayList.remove(file);
                    } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                        PhotoViewData.imageFolderMap.get(nameKey).remove(file);
                        PhotoViewDataOnIdBasis.dataModelArrayList.remove(file);
                    }
                }
                if (SharedPref.getListType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.LIST_VIEW))
                    if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                        GalleryHelper.imageDataModelList.remove(file);
                    } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                        VideoViewData.imageDataModelList.remove(file);
                    } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                        AudioViewData.imageDataModelList.remove(file);
                    } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                        PhotoViewData.imageDataModelList.remove(file);
                    }

            }

        }
        transferFiles(selectedItems, actionType, currentDirectory);
    }

    /**
     * Method to transfer the file like copy/move
     *
     * @param dataModels       selected modelList item from adapter
     * @param actionType       type of action perform like copy/move
     * @param currentDirectory name of new file directory
     */
    public void transferFiles(final List<ImageDataModel> dataModels, final Boolean actionType, String currentDirectory) {
        String message = null;
        ArrayList<File> src = new ArrayList<>();
        ArrayList<File> desc = new ArrayList<>();
        for (ImageDataModel file : dataModels) {
            src.add(file.getFile());
            desc.add(new File(currentDirectory));
        }
        if (mTaskFragment != null) {
            mTaskFragment.StartCopyTask(src, desc, actionType);
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
        if (PhotoViewActivity.this != null)
            PhotoViewActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Show the message of action perform like copy/move
                    SDKUtils.showToast(MediaVaultController.getInstance(), message);
                    // notify adapter to update view pager
                    notifyAdapter();
                    if (CustomPagerAdapter != null)
                        CustomPagerAdapter.itemCheckInList();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fetchData();
                        }
                    }, 200);
                }
            });
    }

    /**
     * If no item in folder then close the activity.
     */
    public void finishActivity() {
        if (handler != null)
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1000);

    }

    @Override
    protected void onDestroy() {
        /**
         * Unregister the Otto bus event listener
         */
        GlobalBus.getBus().unregister(this);
        super.onDestroy();
    }

    /**
     * Update  UI view when get notification from CropFragment fragment class.
     *
     * @param response
     */
    @Subscribe
    public void getMessage(final CropNotification response) {
        if (PhotoViewActivity.this != null)
            PhotoViewActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (response != null) {
                        if (response.getStatus()) {
                            ImageDataModel model = new ImageDataModel();
                            model.setFile(response.getData());
                            if (SharedPref.getAlbumType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.ALBUM_VIEW)) {
                                if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                                    GalleryHelperBaseOnId.dataModelArrayList.add(model);
                                } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                                    VideoViewDataOnIdBasis.dataModelArrayList.add(model);
                                } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                                    AudioViewDataOnIdBasis.dataModelArrayList.add(model);
                                } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                                    PhotoViewDataOnIdBasis.dataModelArrayList.add(model);
                                }
                            }
                            if (SharedPref.getListType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.LIST_VIEW))
                                if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                                    GalleryHelper.imageDataModelList.add(model);
                                } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                                    VideoViewData.imageDataModelList.add(model);
                                } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                                    AudioViewData.imageDataModelList.add(model);
                                } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                                    PhotoViewData.imageDataModelList.add(model);
                                }
                            notifyAdapter();
                        } else {
                            notifyAdapter();
                        }
                    }
                }
            });
    }
}
