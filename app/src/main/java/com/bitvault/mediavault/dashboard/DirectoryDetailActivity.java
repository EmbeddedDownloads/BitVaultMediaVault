package com.bitvault.mediavault.dashboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.adapter.DirectoryDetailAdapter;
import com.bitvault.mediavault.application.MediaVaultController;
import com.bitvault.mediavault.baseclass.BaseActivity;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.common.TaskFragment;
import com.bitvault.mediavault.croplibrary.CropMainActivity;
import com.bitvault.mediavault.database.MediaFileDuration;
import com.bitvault.mediavault.database.MediaVaultLocalDb;
import com.bitvault.mediavault.dialogfragment.FolderDialogFragment;
import com.bitvault.mediavault.eotwallet.SelectWalletType;
import com.bitvault.mediavault.helper.AudioViewData;
import com.bitvault.mediavault.helper.AudioViewDataOnIdBasis;
import com.bitvault.mediavault.helper.DialogData;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.helper.GalleryHelper;
import com.bitvault.mediavault.helper.GalleryHelperBaseOnId;
import com.bitvault.mediavault.helper.GlobalBus;
import com.bitvault.mediavault.helper.PhotoViewData;
import com.bitvault.mediavault.helper.PhotoViewDataOnIdBasis;
import com.bitvault.mediavault.helper.SharedPref;
import com.bitvault.mediavault.helper.SimpleDividerItemDecoration;
import com.bitvault.mediavault.helper.VideoViewData;
import com.bitvault.mediavault.helper.VideoViewDataOnIdBasis;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.ottonotification.DialogResponse;
import com.bitvault.mediavault.ottonotification.LandingFragmentNotification;
import com.bitvault.mediavault.utils.Utils;
import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import commons.SDKConstants;
import utils.SDKUtils;

/**
 * This class is used for show the media files in Grid view
 */
public class DirectoryDetailActivity extends BaseActivity implements DirectoryDetailAdapter.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, DirectoryDetailAdapter.OnLongItemClickListener
        , TaskFragment.TaskCallbacks {


    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.directories_grid_detail)
    RecyclerView directoriesGridDetail;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.activity_directory_detail)
    LinearLayout activityDirectoryDetail;
    @BindView(R.id.progressbarCount)
    TextView progressbarCount;
    @BindView(R.id.parentLayout)
    RelativeLayout parentLayout;
    @BindView(R.id.progressBarLayout)
    RelativeLayout progressBarLayout;
    @BindView(R.id.moveToSecureVault)
    TextView moveToSecureVault;
    private DirectoryDetailAdapter directoryDetailAdapter;
    private GridLayoutManager lLayout;
    private ArrayList<ImageDataModel> data = new ArrayList<>();
    private String nameKey;
    private Handler handler = new Handler();
    private static final String SAVED_SELECTION = "save_selected_items";
    private static final String VISIBILITY = "visibility_status";
    private SharedPreferences sharedPreferences;
    private FragmentManager fm;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private static final String DELETE_DIALOG_VISIBLE = "delete_dialog_visible";
    private static final String RENAME_DIALOG_VISIBLE = "rename_dialog_visible";
    private static final String PROPERTIES_DIALOG_VISIBLE = "properties_dialog_visible";

    public TaskFragment mTaskFragment;
    private AlertDialog renameDialog, deleteDialog, propertiesDialog;
    private Boolean isShowingDeleteDialog = false;
    private Boolean isShowingRenameDialog = false;
    private Boolean isPropertiesDialog = false;
    private MediaFileDuration mediaFileDuration;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private MediaVaultLocalDb secureMediaFileDb;
    private LoadFileDuration loadFileDuration;
    private FetchDialogDataInBackground fetchDialogDataInBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_detail);
        /**
         * Registering the ButterKnife view Injection
         */
        ButterKnife.bind(this);
        intiView();
        getIntentData();
        initClasses();
        initializeAdapter();
        updateToolbar();

    }

    /**
     * Method to get intent data from other activity
     */
    private void getIntentData() {
        if (getIntent() != null)
            nameKey = getIntent().getStringExtra(Constant.KEY_NAME);
        if (nameKey != null && nameKey.equalsIgnoreCase(Constant.AUDIO_FOLDER_NAME)) {
            toolbarTitle.setText(Constant.AUDIO_TITLE);
        } else {
            toolbarTitle.setText(nameKey);
        }
        toolbar.setTitle("");
    }

    /**
     * Method to initialise the view of current activity
     */
    private void intiView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbarTitle);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        /**
         * Enable the back key on navigation bar in toolbar
         */
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_back);

    }

    /**
     * Method to initialise the other class instance  of current activity
     */
    private void initClasses() {
        // Database instance
        /**
         * Initialising the shared preference class instance.
         */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        /**
         * Registering the Otto bus event for Handling the notification
         * call back when user swipe to refresh the view.
         */
        GlobalBus.getBus().register(this);
        mediaFileDuration = MediaFileDuration.getDatabaseInstance(this);
        secureMediaFileDb = MediaVaultLocalDb.getMediaVaultDatabaseInstance(this);
        fm = getSupportFragmentManager();
        mTaskFragment = (TaskFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mTaskFragment == null) {
            mTaskFragment = new TaskFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }
    }

    /***
     * Initialise the adapter class and set adapter on recycler view.
     */
    private void initializeAdapter() {
        final int columns = getResources().getInteger(R.integer.list_view);
        lLayout = new GridLayoutManager(this, columns);
        directoriesGridDetail.setHasFixedSize(true);
        directoriesGridDetail.setLayoutManager(lLayout);
        data.clear();
        /**
         * Getting all media files on basis of folderName from GalleryHelper class
         */
        if (SharedPref.getAlbumType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.ALBUM_VIEW)) {
            if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                if (GalleryHelper.imageFolderMap != null && nameKey != null && GalleryHelper.imageFolderMap.size() > 0 && GalleryHelper.imageFolderMap.get(nameKey) != null)
                    data.addAll(GalleryHelper.imageFolderMap.get(nameKey));
            } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                if (VideoViewData.imageFolderMap != null && nameKey != null && VideoViewData.imageFolderMap.size() > 0 && VideoViewData.imageFolderMap.get(nameKey) != null)
                    data.addAll(VideoViewData.imageFolderMap.get(nameKey));
            } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                if (AudioViewData.imageFolderMap != null && nameKey != null && AudioViewData.imageFolderMap.size() > 0 && AudioViewData.imageFolderMap.get(nameKey) != null)
                    data.addAll(AudioViewData.imageFolderMap.get(nameKey));
            } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                if (PhotoViewData.imageFolderMap != null && nameKey != null && PhotoViewData.imageFolderMap.size() > 0 && PhotoViewData.imageFolderMap.get(nameKey) != null)
                    data.addAll(PhotoViewData.imageFolderMap.get(nameKey));
            }
        }
        if (data != null && data.size() > 0)
            Collections.sort(data);
        directoryDetailAdapter = new DirectoryDetailAdapter(this, data, mediaFileDuration);
        /**
         * Registering call back of item click
         */
        directoryDetailAdapter.setOnItemClickListener(this);
        directoryDetailAdapter.setOnLongItemClickListener(this);
        directoriesGridDetail.setAdapter(directoryDetailAdapter);
        /**
         * Add item Decorator for Grid view in recycler view
         */
        directoriesGridDetail.addItemDecoration(new SimpleDividerItemDecoration(this));
        /**
         * Register swipe to refresh listener
         */
        swipeRefreshLayout.setOnRefreshListener(this);
        /**
         * Set color on swipe to refresh bar
         */
        swipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);

    }

    /**
     * Click event to move file on secure tab
     */
    @OnClick(R.id.moveToSecureVault)
    public void onViewClicked() {
        if (directoryDetailAdapter.getSelectedItems().size() <= Constant.FILE_LIMIT) {
            if (calculateSizeInLong(directoryDetailAdapter.getSelectedItems()) <= SDKConstants.MEDIA_LIMIT) {
                Intent intent = new Intent(this, SelectWalletType.class);
                intent.putParcelableArrayListExtra(Constant.DATA, directoryDetailAdapter.getSelectedItems());
                startActivity(intent);
            } else {
                Utils.showSnakbar(parentLayout, getResources().getString(R.string.selected_file_size), Snackbar.LENGTH_SHORT);
            }
        } else {
            Utils.showSnakbar(parentLayout, getResources().getString(R.string.selected_file_count), Snackbar.LENGTH_SHORT);

        }
    }

    /**
     * Method to return file size in KB
     *
     * @param selectedItems
     * @return
     */
    private long calculateSizeInLong(ArrayList<ImageDataModel> selectedItems) {
        long mSize = 0;
        if (selectedItems != null && selectedItems.size() > 0) {
            for (int fileSize = 0; fileSize < selectedItems.size(); fileSize++) {
                mSize += selectedItems.get(fileSize).getFile().length();
            }
        }
        if (mSize != 0) {
            mSize = mSize / 1024;
        } else {
            mSize = 0;
        }

        return mSize;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.action, menu);

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
                actionBackPress();
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

            case R.id.action_sort:
                actionSort();
                return true;

            case R.id.action_edit:
                actionCrop();
                return true;
            case R.id.action_info:
                if (directoryDetailAdapter != null && directoryDetailAdapter.getSelectedItems() != null
                        && directoryDetailAdapter.getSelectedItems().size() > 0) {
                    Intent intent = new Intent(this, InfoActivity.class);
                    intent.putExtra(Constant.DATA, directoryDetailAdapter.getSelectedItems().get(0));
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
        int itemCountOne = 1, itemCountZero = 0;

        if (directoryDetailAdapter != null) {

            int count = directoryDetailAdapter.getSelectedItemCount();

            menu.findItem(R.id.action_delete).setVisible(count >= itemCountOne);

            menu.findItem(R.id.action_rename).setVisible(count == itemCountOne);
            menu.findItem(R.id.action_info).setVisible(count == itemCountOne);
            if (directoryDetailAdapter.getSelectedItems() != null)//directoryDetailAdapter.getSelectedItems().get(0).isAudio()
                menu.findItem(R.id.action_copy).setVisible(count >= itemCountOne && !FileUtils.isAudioFile(directoryDetailAdapter.getSelectedItems().get(0).getFile().getAbsolutePath()));
            if (directoryDetailAdapter.getSelectedItems() != null)
                menu.findItem(R.id.action_move).setVisible(count >= itemCountOne && !FileUtils.isAudioFile(directoryDetailAdapter.getSelectedItems().get(0).getFile().getAbsolutePath()));

            menu.findItem(R.id.action_sort).setVisible(count == itemCountZero);
            // Hide  the crop option  in menu if file is other than image
            if (directoryDetailAdapter.getSelectedItems() != null)
                menu.findItem(R.id.action_edit).setVisible(count == itemCountOne && FileUtils.isImageFile(directoryDetailAdapter.getSelectedItems().get(0).getFile().getAbsolutePath()));
        }

        return super.onPrepareOptionsMenu(menu);
    }


    /**
     * Method to crop the media files (image type only)
     */
    private void actionCrop() {
        ArrayList<ImageDataModel> imageDataModels = directoryDetailAdapter.getSelectedItems();
        Intent intent = new Intent(this, CropMainActivity.class);
        intent.putExtra(Constant.DATA, imageDataModels.get(0));
        intent.putExtra(Constant.INTENT_STATUS, false);
        startActivity(intent);
        if (directoryDetailAdapter.anySelected())
            directoryDetailAdapter.clearSelection();
        updateToolbar();
    }

    /**
     * Method to open dialog fragment
     *
     * @param ActionType type of action perform like copy/move
     */
    private void actionCopyMove(boolean ActionType) {
        FolderDialogFragment dFragment = new FolderDialogFragment();
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

    @Override
    public void onBackPressed() {
        actionBackPress();
    }

    /**
     * Check that on back press from Navigation bar or back key, Then finish the activity if
     * there were <=1 fragment in stack else back the fragment.
     */
    private void actionBackPress() {
        if (directoryDetailAdapter.anySelected()) {
            directoryDetailAdapter.clearSelection();
            updateToolbar();
            return;
        }
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() <= 1) {
            finish();
        } else {
            manager.popBackStack();
        }
    }

    /**
     * Handling the call back of item click on grid view and pass data to PhotoViewActivity/MediaPlayerFragment class
     * class with keyName of folder and its position
     *
     * @param model
     * @param position
     */
    @Override
    public void itemClick(final ImageDataModel model, final int position) {
        DirectoryDetailActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (directoryDetailAdapter != null)
                    if (directoryDetailAdapter.anySelected()) {
                        directoryDetailAdapter.toggle(position);
                        updateToolbar();
                        return;
                    }
                Intent intent = new Intent(DirectoryDetailActivity.this, PhotoViewActivity.class);
                intent.putExtra(Constant.TYPE, Constant.ALBUM_TYPE);
                intent.putExtra(Constant.POSITION, position);
                intent.putExtra(Constant.KEY_NAME, nameKey);
                startActivity(intent);
            }
        });
    }

    @Override
    public void itemLongClick(ImageDataModel model, final int position) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                directoryDetailAdapter.toggle(position);
                updateToolbar();
            }
        });
    }

    /**
     * Update toolbar and action bar
     */
    private void updateToolbar() {

        invalidateOptionsMenu();

        invalidateTitle();

        invalidateToolbar();
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
    protected void onDestroy() {
        /**
         * Unregister the Otto bus event listener and dismiss dialog if visible
         */
        if (renameDialog != null && renameDialog.isShowing()) {
            renameDialog.dismiss();
        }
        if (deleteDialog != null && deleteDialog.isShowing()) {
            deleteDialog.dismiss();
        }
        GlobalBus.getBus().unregister(this);
        if (directoryDetailAdapter != null) {
            directoryDetailAdapter.stopAsyncTask();
        }
        super.onDestroy();

    }

    /**
     * Update the UI on Main Thread
     */
    private void updateUI() {
        if (DirectoryDetailActivity.this != null)
            DirectoryDetailActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    data.clear();
                    if (SharedPref.getAlbumType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.ALBUM_VIEW)) {
                        if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                            data.addAll(GalleryHelperBaseOnId.dataModelArrayList);
                        } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                            data.addAll(VideoViewDataOnIdBasis.dataModelArrayList);
                        } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                            data.addAll(AudioViewDataOnIdBasis.dataModelArrayList);
                        } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                            data.addAll(PhotoViewDataOnIdBasis.dataModelArrayList);
                        }
                    }
                    //Sort the data according to sorting value
                    if (data != null && data.size() > 0) {
                        Collections.sort(data);
                    }
                    directoryDetailAdapter.notifyDataSetChanged();
                    updateFileDurationInBackground();
                }
            });
    }

    /**
     * Method to update time duration of file which is audio/video type
     */
    public void updateFileDurationInBackground() {
        if (loadFileDuration != null && loadFileDuration.getStatus() != AsyncTask.Status.FINISHED)
            loadFileDuration.cancel(true);
        loadFileDuration = new LoadFileDuration();
        loadFileDuration.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Async task for loading time duration of each file in background and update the local database
     */
    private class LoadFileDuration extends AsyncTask<ArrayList<ImageDataModel>, ImageDataModel, ArrayList<ImageDataModel>> {
        @Override
        protected ArrayList<ImageDataModel> doInBackground(ArrayList<ImageDataModel>... params) {
            for (int i = 0; i < data.size(); i++) {
                ImageDataModel model = data.get(i);
                if (model != null && model.getFile() != null) {
                    if (!FileUtils.isImageFile(model.getFile().getAbsolutePath()))
                        if (FileUtils.getDuration(model.getFile()) != null) {
                            String path = model.getFile().getAbsolutePath();
                            path = path.replaceAll("'", "''");
                            // Check the file is already exists into database or not
                            if (!mediaFileDuration.checkFileStatus(path)) {
                                model.setTimeDuration(FileUtils.getDuration(model.getFile()));
                                // Method to insert file path and time duration into local database
                                mediaFileDuration.insertMediaTime(model);
                            }
                        }
                }
            }
            return data;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<ImageDataModel> videoModels) {
            super.onPostExecute(videoModels);
            directoryDetailAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        /**
         * Get the bucketId on basis of folder name and pass it  to GalleryHelperBaseOnId class
         */
        fetchFilesOnActionPerform();
        updateToolbar();
      //  fetchData();

    }

    //Method to fetch data in background
    private void fetchData() {
        SDKUtils.showErrorLog("fetch data..","called..");
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
            if (DirectoryDetailActivity.this!=null)
            DialogData.getImageFolderMap(DirectoryDetailActivity.this);
            return null;
        }
    }

    /**
     * Handling swipe to refresh method an update the UI
     */
    @Override
    public void onRefresh() {
        /**
         * Get the bucketId on basis of folder name and pass it  to GalleryHelperBaseOnId class
         */

        fetchFilesOnActionPerform();

    }

    /**
     * Fetch files from content provider
     */
    private void fetchFilesOnActionPerform() {
        swipeRefreshLayout.setRefreshing(false);
        // update media list

        if (SharedPref.getAlbumType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.ALBUM_VIEW)) {
            if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                if (GalleryHelper.imageFolderMap != null && nameKey != null && GalleryHelper.imageFolderMap.size() > 0 && GalleryHelper.imageFolderMap.get(nameKey) != null) {
                    GalleryHelperBaseOnId.getMediaFilesOnIdBasis(this, GalleryHelper.imageFolderMap.get(nameKey).get(0).getBucketId());
                }
            } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                if (VideoViewData.imageFolderMap != null && nameKey != null && VideoViewData.imageFolderMap.size() > 0 && VideoViewData.imageFolderMap.get(nameKey) != null) {
                    VideoViewDataOnIdBasis.getMediaFilesOnIdBasis(this, VideoViewData.imageFolderMap.get(nameKey).get(0).getBucketId());
                }
            } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                if (AudioViewData.imageFolderMap != null && nameKey != null && AudioViewData.imageFolderMap.size() > 0 && AudioViewData.imageFolderMap.get(nameKey) != null) {
                    AudioViewDataOnIdBasis.getMediaFilesOnIdBasis(this, AudioViewData.imageFolderMap.get(nameKey).get(0).getBucketId());
                }
            } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                if (PhotoViewData.imageFolderMap != null && nameKey != null && PhotoViewData.imageFolderMap.size() > 0 && PhotoViewData.imageFolderMap.get(nameKey) != null) {
                    PhotoViewDataOnIdBasis.getMediaFilesOnIdBasis(this, PhotoViewData.imageFolderMap.get(nameKey).get(0).getBucketId());
                }
            } else {
                finishActivity();
            }
        }
    }


    /**
     * Update  UI view when get notification from GalleryHelper class
     *
     * @param notification message of notification
     */
    @Subscribe
    public void getMessage(LandingFragmentNotification notification) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                    updateUI();
                }
            }
        }, 500);
    }

    /**
     * Update  UI view when get notification from Dialog fragment class.
     *
     * @param response Dialog response
     */
    @Subscribe
    public void getMessage(final DialogResponse response) {
        if (DirectoryDetailActivity.this != null)
            DirectoryDetailActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (response != null && response.getData().equals(Constant.STATUS_OK)) {
                        updateToolbar();
                    }
                }
            });
    }

    // update the UI after screen rotation
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Update the progress bar status
        if (savedInstanceState.getBoolean(VISIBILITY)) {
            progressBarLayout.setVisibility(View.VISIBLE);
        }
        // update  the list of selected item position when device state changes and update toolbar
        directoryDetailAdapter.select(savedInstanceState.getIntegerArrayList(SAVED_SELECTION));
        if (savedInstanceState != null) {
            isShowingDeleteDialog = savedInstanceState.getBoolean(DELETE_DIALOG_VISIBLE, false);
            isShowingRenameDialog = savedInstanceState.getBoolean(RENAME_DIALOG_VISIBLE, false);
            isPropertiesDialog = savedInstanceState.getBoolean(PROPERTIES_DIALOG_VISIBLE, false);
            if (isShowingDeleteDialog) {
                showDeleteDialog();
            }
            if (isShowingRenameDialog) {
                renameFile();
            }
        }
        updateToolbar();
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * Get the state before changing the orientation
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // get the list of selected item position when device state changes and progress bar status
        outState.putIntegerArrayList(SAVED_SELECTION, directoryDetailAdapter.getSelectedPositions());
        outState.putBoolean(VISIBILITY, progressBarLayout.getVisibility() == View.VISIBLE);
        outState.putBoolean(DELETE_DIALOG_VISIBLE, isShowingDeleteDialog);
        outState.putBoolean(RENAME_DIALOG_VISIBLE, isShowingRenameDialog);
        outState.putBoolean(PROPERTIES_DIALOG_VISIBLE, isPropertiesDialog);
        super.onSaveInstanceState(outState);
    }

    /**
     * Update toolbar icon as the item selected/deselected
     */
    private void invalidateToolbar() {
        if (directoryDetailAdapter.anySelected()) {
            toolbar.setNavigationIcon(R.drawable.ic_clear);
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_back);
        }
    }

    /**
     * Update toolbar title as item selected/deselected
     */
    private void invalidateTitle() {
        if (directoryDetailAdapter.anySelected()) {
            int selectedItemCount = directoryDetailAdapter.getSelectedItemCount();
            toolbar.setTitle(String.format("%s selected", selectedItemCount));
            toolbarTitle.setText("");
            moveToSecureVault.setVisibility(View.VISIBLE);
        } else {
            if (nameKey != null && nameKey.equalsIgnoreCase(Constant.AUDIO_FOLDER_NAME)) {
                toolbarTitle.setText(Constant.AUDIO_TITLE);
            } else {
                toolbarTitle.setText(nameKey);
            }
            toolbar.setTitle("");
            moveToSecureVault.setVisibility(View.GONE);
        }

    }

    /**
     * Open dialogue with single choice listener and update  UI after selecting the choice.
     */
    private void actionSort() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int checkedItem = Integer.parseInt(SharedPref.getSortType(sharedPreferences));
        String sorting[] = getResources().getStringArray(R.array.sorting_list);
        builder.setSingleChoiceItems(sorting, checkedItem, null)
                .setPositiveButton(R.string.ok_button_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        // Storing the value of sorting order into shared preference class
                        SharedPref.setSortType(sharedPreferences, String.valueOf(selectedPosition));
                        // Update the UI and notify adapter
                        updateUI();
                        //  updateUIOnSort();
                    }
                });

        builder.setTitle(R.string.sort_alert_dialog_title);

        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * this method for performing the delete operation and update toolbar with option menu.
     */
    private void actionDelete() {
        showDeleteDialog();
    }

    /**
     * Show the delete dialog for deleting the file(s)
     */
    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.delete_alert_dialog, null);
        builder.setView(view);
        TextView okBtn = (TextView) view.findViewById(R.id.alert_yes_button);
        TextView cancelBtn = (TextView) view.findViewById(R.id.alert_no_button);
        deleteDialog = builder.create();
        deleteDialog.show();
        isShowingDeleteDialog = true;
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // method to delete file after click on OK button
                deleteFile();
                deleteDialog.dismiss();
                isShowingDeleteDialog = false;
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialog.dismiss();
                isShowingDeleteDialog = false;
            }
        });
    }

    /**
     * Method to call action delete method for deleting the file
     */
    private void deleteFile() {
        actionDelete(directoryDetailAdapter.getSelectedItems());
        updateToolbar();
    }

    /**
     * Get the selected files list and delete/undo with snackbar and then  scan the file entry by
     * FileUtils.scanFile(Context,file)method.
     *
     * @param modelData
     */
    private void actionDelete(final ArrayList<ImageDataModel> modelData) {
        //removing  the item from mail list
        for (ImageDataModel file : modelData) {
            if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                // Remove the item  from Current  List of GalleryHelperBaseOnId.class
                GalleryHelperBaseOnId.dataModelArrayList.remove(file);
            } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                VideoViewDataOnIdBasis.dataModelArrayList.remove(file);
            } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                AudioViewDataOnIdBasis.dataModelArrayList.remove(file);
            } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                PhotoViewDataOnIdBasis.dataModelArrayList.remove(file);
            }
        }
        //removing the item from local list and notify the adapter
        directoryDetailAdapter.removeFilesNotifyList(modelData);

        final String message = String.format(getString(R.string.delete_file_message), modelData.size());
        for (ImageDataModel data : modelData) {
            try {
                // method to deleting the selected file
                FileUtils.deleteFile(data.getFile());
                FileUtils.deleteDirectory(data.getFile().getParentFile());
            } catch (Exception e) {
                e.printStackTrace();
            }
            // method to scan the file and update its position
            FileUtils.scanFile(DirectoryDetailActivity.this, data.getFile());

        }
        SDKUtils.showToast(DirectoryDetailActivity.this, message);
        //check that if folder is empty then close it and show toast message
        directoryDetailAdapter.itemCheckInList();
    }


    /**
     * Method to show the dialogue for  rename a file which is selected inside into a folder.
     */
    private void renameFile() {
        // Get modeldata of the selected file to rename
        final List<ImageDataModel> selectedItems = directoryDetailAdapter.getSelectedItems();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input;
        builder.setTitle(R.string.rename_dialog_title);
        View view = View.inflate(this, R.layout.dialog_edit_text, null);

        input = (EditText) view.findViewById(R.id.dialog_edit_text);

        builder.setView(view);
        builder.setPositiveButton(R.string.rename_dialog_ok_button, null);
        builder.setNegativeButton(R.string.rename_dialog_cancel_button, null);
        if (selectedItems.size() == 1) {
            // setting the file name into renameDialog text after removing the extension name of a file
            input.setText(FileUtils.removeExtension(selectedItems.get(0).getFile().getName()));
            // set the cursor at the end of edit text
            input.setSelection(input.getText().length());
        }
        renameDialog = builder.create();
        // for showing the keyboard when renameDialog is open,must call before renameDialog.show to get desired result
        renameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        renameDialog.show();
        isShowingRenameDialog = true;
        renameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = input.getText().toString().trim();
                if (fileName.length() != 0) {
                    if (fileName.length() <= Constant.FILE_NAME_LENGTH) {
                        if (FileUtils.getSpecialCharacterValidation(fileName)) {
                            // Method to update the name of file
                            final File newFile = new File(selectedItems.get(0).getFile().getParent(), input.getText().toString() + "." +
                                    FileUtils.getExtension(selectedItems.get(0).getFile().getName()));
                            if (newFile.exists()) {
                                SDKUtils.showToast(DirectoryDetailActivity.this, getString(R.string.file_already_exists_error));
                            } else {
                                updateFileName(selectedItems, input.getText().toString());
                                renameDialog.dismiss();
                                isShowingRenameDialog = false;
                            }

                            //Hide the keyboard after dismiss the renameDialog.
                            renameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        } else {
                            SDKUtils.showToast(DirectoryDetailActivity.this, getString(R.string.file_name_invalid_character));
                        }
                    } else {
                        SDKUtils.showToast(DirectoryDetailActivity.this, getString(R.string.rename_file_name_length));
                    }
                } else {
                    SDKUtils.showToast(DirectoryDetailActivity.this, getString(R.string.empty_dialog_message));
                }
            }
        });
        renameDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameDialog.dismiss();
                isShowingRenameDialog = false;
                //Hide the keyboard after dismiss the renameDialog.
                renameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            }
        });
    }

    /**
     * Method to update the name of selected file on dialog click event
     *
     * @param selectedItems
     * @param rename
     */
    private void updateFileName(List<ImageDataModel> selectedItems, String rename) {
        //  performing the action of rename clear selected item and update toolbar.
        directoryDetailAdapter.clearSelection();
        // updating the toolbar with action bar  menus items
        updateToolbar();
        try {
            if (selectedItems != null && selectedItems.size() == 1) {
                ImageDataModel dataModel = selectedItems.get(0);
                File file = dataModel.getFile();
                // method to rename the file and then set to model data for update the index of file in main list
                dataModel.setFile(FileUtils.renameFile(dataModel.getFile(), rename));
                // Update the name of Main List of GalleryHelper.class
                if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                    if (GalleryHelper.imageFolderMap != null && nameKey != null && GalleryHelper.imageFolderMap.size() > 0 && GalleryHelper.imageFolderMap.get(nameKey).size() > 0)
                        GalleryHelper.imageFolderMap.get(nameKey).get(0).setFile(dataModel.getFile());
                } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                    if (VideoViewData.imageFolderMap != null && nameKey != null && VideoViewData.imageFolderMap.size() > 0 && VideoViewData.imageFolderMap.get(nameKey).size() > 0)
                        VideoViewData.imageFolderMap.get(nameKey).get(0).setFile(dataModel.getFile());
                } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                    if (AudioViewData.imageFolderMap != null && nameKey != null && AudioViewData.imageFolderMap.size() > 0 && AudioViewData.imageFolderMap.get(nameKey).size() > 0)
                        AudioViewData.imageFolderMap.get(nameKey).get(0).setFile(dataModel.getFile());
                } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                    if (PhotoViewData.imageFolderMap != null && nameKey != null && PhotoViewData.imageFolderMap.size() > 0 && PhotoViewData.imageFolderMap.get(nameKey).size() > 0)
                        PhotoViewData.imageFolderMap.get(nameKey).get(0).setFile(dataModel.getFile());
                }
                        /*
                        * Scan old and new file to update entry into content provider.
                        * */
                FileUtils.scanFile(DirectoryDetailActivity.this, dataModel.getFile());
                FileUtils.scanFile(DirectoryDetailActivity.this, file);
                // update local database for time
                mediaFileDuration.updateFileName(dataModel.getFile().getAbsolutePath(), file.getAbsolutePath());
                // Method to update UI
                updateUI();
            }
        } catch (Exception e) {
            SDKUtils.showToast(DirectoryDetailActivity.this, e.toString());
        }
    }

    /**
     * method call from FolderDialogFragment class to copy/move the items
     *
     * @param currentDirectory current directory name
     * @param actionType       type of action
     */
    public void actionCopyOnDialogResponse(String currentDirectory, boolean actionType) {
        // Get selected file list form adapter
        ArrayList<ImageDataModel> selectedItems = directoryDetailAdapter.getSelectedItems();
        //Remove selected file from adapter
        directoryDetailAdapter.clearSelection();
        if (actionType) {
            //removing  the item from mail list
            for (ImageDataModel file : selectedItems) {
                File newFile = new File(new File(currentDirectory), file.getFile().getName());
                if (!newFile.exists()) {
                    if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                        // Remove the item  from Current  List of GalleryHelperBaseOnId.class
                        GalleryHelperBaseOnId.dataModelArrayList.remove(file);
                    } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                        VideoViewDataOnIdBasis.dataModelArrayList.remove(file);
                    } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                        AudioViewDataOnIdBasis.dataModelArrayList.remove(file);
                    } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                        PhotoViewDataOnIdBasis.dataModelArrayList.remove(file);
                    }
                    //removing the item from local list and notify the adapter
                    directoryDetailAdapter.removeFileNotifyList(file);
                }
            }
        }
        updateToolbar();
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
        progressBarLayout.setVisibility(View.VISIBLE);
        String message = null;
        ArrayList<File> src = new ArrayList<>();
        ArrayList<File> desc = new ArrayList<>();
        for (ImageDataModel file : dataModels) {
            src.add(file.getFile());
            desc.add(new File(currentDirectory));
        }
        if (mTaskFragment != null) {
            mTaskFragment.StartCopyTask(DirectoryDetailActivity.this, src, desc, actionType);
        }
    }


    // If no item in folder then close the activity.
    public void finishActivity() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);

    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onProgressUpdate(final int percent) {
        if (DirectoryDetailActivity.this != null)
            DirectoryDetailActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //  progressbarCount.setText("copying ..." + Integer.toString(percent) + "%");
                    // progressBar.setProgress(percent);
                }
            });
    }

    @Override
    public void onCancelled() {

    }

    @Override
    public void onPostExecute(final String message) {
        progressBarLayout.setVisibility(View.GONE);
        if (DirectoryDetailActivity.this != null)
            DirectoryDetailActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Show the message of action perform like copy/move
                    SDKUtils.showToast(MediaVaultController.getInstance(), message);
                    // Check that any item in the folder or not.
                    if (directoryDetailAdapter != null)
                        directoryDetailAdapter.itemCheckInList();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fetchData();
                        }
                    },200);

                }
            });

    }


}
