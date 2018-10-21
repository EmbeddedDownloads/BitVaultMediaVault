package com.bitvault.mediavault.tabs;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.adapter.ListViewAdapterNew;
import com.bitvault.mediavault.application.MediaVaultController;
import com.bitvault.mediavault.baseclass.BaseSupportFragment;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.croplibrary.CropMainActivity;
import com.bitvault.mediavault.dashboard.InfoActivity;
import com.bitvault.mediavault.dashboard.LandingActivity;
import com.bitvault.mediavault.dashboard.PhotoViewActivity;
import com.bitvault.mediavault.database.MediaFileDuration;
import com.bitvault.mediavault.database.MediaVaultLocalDb;
import com.bitvault.mediavault.dialogfragment.ListViewItemCopyMoveDialog;
import com.bitvault.mediavault.eotwallet.SelectWalletType;
import com.bitvault.mediavault.helper.AudioViewData;
import com.bitvault.mediavault.helper.DialogData;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.helper.GalleryHelper;
import com.bitvault.mediavault.helper.GlobalBus;
import com.bitvault.mediavault.helper.PhotoViewData;
import com.bitvault.mediavault.helper.SharedPref;
import com.bitvault.mediavault.helper.SimpleDividerItemDecoration;
import com.bitvault.mediavault.helper.VideoViewData;
import com.bitvault.mediavault.model.ImageDataModel;
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
import butterknife.Unbinder;
import commons.SDKConstants;
import utils.SDKUtils;

import static android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE;

/**
 * Created by vvdn on 7/31/2017.
 */

public class MediaVaultAllTab extends BaseSupportFragment implements ListViewAdapterNew.OnItemClickListener,
        ListViewAdapterNew.OnItemLongClickListener {
    public ListViewAdapterNew listViewAdapter;
    @BindView(R.id.directories_grid)
    RecyclerView recyclerView;
    @BindView(R.id.parentLayout)
    RelativeLayout parentLayout;
    @BindView(R.id.moveToSecureVault)
    TextView moveToSecureVault;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private ArrayList<ImageDataModel> data = new ArrayList<>();
    private Unbinder unbinder;
    private Handler handler;
    private LoadInBackground loadInBackground;
    private FetchDataInBackground fetchDataInBackground;
    private SortDataInBackground sortDataInBackground;
    private MediaFileDuration mediaFileDuration;
    private AlertDialog renameDialog, deleteDialog;
    private FragmentManager fm;
    private SharedPreferences sharedPreferences;
    private MediaVaultLocalDb secureMediaFileDb;
    private FetchDialogDataInBackground fetchDialogDataInBackground;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.media_vault_all_tab_layout, container, false);
        unbinder = ButterKnife.bind(this, view);
        GlobalBus.getBus().register(this);
        initialised();
        return view;
    }

    //Method to initialise  the database and set recycler view
    private void initialised() {
        handler = new Handler();
        /**
         * Initialising the shared preference class instance.
         */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mediaFileDuration = MediaFileDuration.getDatabaseInstance(getActivity());
        secureMediaFileDb = MediaVaultLocalDb.getMediaVaultDatabaseInstance(getActivity());
        progressBar.setVisibility(View.VISIBLE);
        setupRecycler();

    }

    //Method to set recycler view
    protected void setupRecycler() {
        data.clear();
        listViewAdapter = new ListViewAdapterNew(getActivity(), data, mediaFileDuration, secureMediaFileDb);
        /**
         * Add item Decorator for Grid view in recycler view
         */
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
// set grid layout manager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.list_view));
        recyclerView.setLayoutManager(gridLayoutManager);
        listViewAdapter.setGridLayoutManager(gridLayoutManager);
        /**
         * Registering call back of item click
         */
        listViewAdapter.setOnItemClickListener(this);
        listViewAdapter.setOnItemLongClickListener(this);
        recyclerView.setAdapter(listViewAdapter);
    }

    /**
     * Update  UI view when get notification from GalleryHelper class
     *
     * @param s
     */
    @Subscribe
    public void getMessage(LandingFragmentNotification s) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /**
                 * sorting the data in background
                 */
                if (sortDataInBackground != null && sortDataInBackground.getStatus() != AsyncTask.Status.FINISHED)
                    sortDataInBackground.cancel(true);
                sortDataInBackground = new SortDataInBackground();
                sortDataInBackground.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


            }
        });
        //Update the time duration in background
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loadInBackground != null && loadInBackground.getStatus() != AsyncTask.Status.FINISHED)
                    loadInBackground.cancel(true);
                loadInBackground = new LoadInBackground();
                loadInBackground.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }, 500);

    }

    @Override
    public void onResume() {
        super.onResume();
        /**
         * Fetching all media files while use resume the application
         */
        updateUI();
        fetchData();

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
            DialogData.getImageFolderMap(getActivity());
            return null;
        }
    }

    //Method to update UI
    private void updateUI() {
        if (fetchDataInBackground != null && fetchDataInBackground.getStatus() != AsyncTask.Status.FINISHED)
            fetchDataInBackground.cancel(true);
        fetchDataInBackground = new FetchDataInBackground();
        fetchDataInBackground.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // Move to secure file click
    @OnClick(R.id.moveToSecureVault)
    public void onViewClicked() {
        if (listViewAdapter.getSelectedItems().size() <= Constant.FILE_LIMIT) {
            if (calculateSizeInLong(listViewAdapter.getSelectedItems()) <= SDKConstants.MEDIA_LIMIT) {
                Intent intent = new Intent(getActivity(), SelectWalletType.class);
                intent.putParcelableArrayListExtra(Constant.DATA, listViewAdapter.getSelectedItems());
                startActivity(intent);
            } else {
                Utils.showSnakbar(parentLayout, getResources().getString(R.string.selected_file_size), Snackbar.LENGTH_SHORT);
            }
        } else {
            Utils.showSnakbar(parentLayout, getResources().getString(R.string.selected_file_count), Snackbar.LENGTH_SHORT);

        }

    }

    //Method to return file size in KB
    private long calculateSizeInLong(ArrayList<ImageDataModel> selectedItems) {
        long mSize = 0;
        if (selectedItems != null) {
            int fileSize = selectedItems.size();
            if (fileSize > 0) {
                for (int i = 0; i < selectedItems.size(); i++) {
                    mSize += selectedItems.get(i).getFile().length();
                }
            }
        }
        if (mSize != 0) {
            mSize = mSize / 1024;
        } else {
            mSize = 0;
        }

        return mSize;
    }

    /**
     * Fetch data in background
     */
    class FetchDataInBackground extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            if (getActivity() != null)
                if (((LandingActivity) getActivity()).checkPermission) {
                    if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                        GalleryHelper.getImageFolderMap(getActivity());
                    } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                        VideoViewData.getImageFolderMap(getActivity());
                    } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                        AudioViewData.getImageFolderMap(getActivity());
                    } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                        PhotoViewData.getImageFolderMap(getActivity());
                    }
                }
            return null;
        }
    }

    /**
     * Sorting data in background
     */
    class SortDataInBackground extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (getActivity() != null) {
                data.clear();
                if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                    data.addAll(GalleryHelper.imageDataModelList);
                } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                    data.addAll(VideoViewData.imageDataModelList);
                } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                    data.addAll(AudioViewData.imageDataModelList);
                } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                    data.addAll(PhotoViewData.imageDataModelList);
                }
                if (data != null && data.size() > 0)
                    Collections.sort(data);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (getActivity() != null) {
                listViewAdapter.notifyDataChanged();
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (renameDialog != null && renameDialog.isShowing()) {
            renameDialog.dismiss();
        }
        if (deleteDialog != null && deleteDialog.isShowing()) {
            deleteDialog.dismiss();
        }
        //   SharedPref.setSecureTab(sharedPreferences, "0");
        unbinder.unbind();
        GlobalBus.getBus().unregister(this);
        super.onDestroyView();
    }

    @Override
    public void itemClickedPosition(final ImageDataModel model, final int position) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (listViewAdapter.anySelected()) {
                        listViewAdapter.toggle(position);
                        //  ((LandingActivity) getActivity()).updateToolbar(listViewAdapter.getSelectedItemCount());
                        updateToolbarView(listViewAdapter.getSelectedItemCount());
                        return;
                    }
                    Intent intent = new Intent(getActivity(), PhotoViewActivity.class);
                    intent.putExtra(Constant.TYPE, Constant.LIST_TYPE);
                    intent.putExtra(Constant.DATA, model);
                    startActivity(intent);
                    moveToSecureVault.setVisibility(View.GONE);
                }
            });
    }

    @Override
    public void itemLongClickedPosition(final ImageDataModel dataModel, final int position) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listViewAdapter.toggle(position);
                updateToolbarView(listViewAdapter.getSelectedItemCount());
            }
        });
    }

    public void updateToolbarView(int count) {
        ((LandingActivity) getActivity()).updateToolbar(count);
        if (count == 0) {
            moveToSecureVault.setVisibility(View.GONE);
        } else {
            moveToSecureVault.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Loading section view data into background
     */
    public class LoadInBackground extends AsyncTask<Void, ImageDataModel, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < data.size(); i++) {
                ImageDataModel model = data.get(i);
                if (!FileUtils.isImageFile(model.getFile().getAbsolutePath())) {
                    if (FileUtils.getDuration(model.getFile()) != null) {
                        String path = model.getFile().getAbsolutePath();
                        path = path.replaceAll("'", "''");
                        // Check the file is already exists into database or not
                        if (!mediaFileDuration.checkFileStatus(path)) {
                            model.setTimeDuration(FileUtils.getDuration(model.getFile()));
                            // Method to insert file path and time duration into local dadabase
                            mediaFileDuration.insertMediaTime(model);
                        }
                    }

                }
                publishProgress(model);
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            // progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(ImageDataModel... values) {
            super.onProgressUpdate(values);
            // listViewAdapter.notifyItemInsertedAtPosition(index);

        }

        /**
         * Note that we do NOT call the callback object's methods
         * directly from the background thread, as this could result
         * in a race condition.
         */


        @Override
        protected void onPostExecute(Void ignore) {
            if (getActivity() != null) {
                // progressBar.setVisibility(View.GONE);
                if (listViewAdapter != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listViewAdapter.notifyDataChanged();
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                        }
                    });

                }

            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // TODO Add your menu entries here
        inflater.inflate(R.menu.action, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
                actionCrossKey();
                return true;

            case R.id.action_delete:
                actionDelete();
                return true;

            case R.id.action_rename:
                renameFile();
                return true;


            case R.id.action_copy:
                if (((LandingActivity) getActivity()).getFragment() != null &&
                        !((LandingActivity) getActivity()).getFragment().isRunningAsync()) {
                    actionCopyMove(false);
                } else {
                    SDKUtils.showToast(MediaVaultController.getInstance(), getString(R.string.task_running_status));
                }

                return true;

            case R.id.action_move:
                if (((LandingActivity) getActivity()).getFragment() != null &&
                        !((LandingActivity) getActivity()).getFragment().isRunningAsync()) {
                    actionCopyMove(true);
                } else {
                    SDKUtils.showToast(MediaVaultController.getInstance(), getString(R.string.task_running_status));
                }
                return true;


            case R.id.action_edit:
                actionCrop();
                return true;
            case R.id.action_info:
                if (listViewAdapter != null && listViewAdapter.getSelectedItems() != null
                        && listViewAdapter.getSelectedItems().size() > 0) {
                    Intent intent = new Intent(getActivity(), InfoActivity.class);
                    intent.putExtra(Constant.DATA, listViewAdapter.getSelectedItems().get(0));
                    startActivity(intent);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // * Method to perform cross key on Toolbar and notify the list
    public void actionCrossKey() {
        listViewAdapter.clearSelection();
        updateToolbarView(listViewAdapter.getSelectedItemCount());
    }

    // Get selected item count
    public boolean getSelectedItemCount() {
        return listViewAdapter.getSelectedItemCount() > 0;
    }

    /**
     * @param menu show and hide menu items as item selected
     * @return
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        if (listViewAdapter != null) {
            if (!listViewAdapter.anySelected()) {
                menu.findItem(R.id.action_delete).setVisible(false);
                menu.findItem(R.id.action_rename).setVisible(false);
                menu.findItem(R.id.action_info).setVisible(false);
                menu.findItem(R.id.action_copy).setVisible(false);
                menu.findItem(R.id.action_move).setVisible(false);

                menu.findItem(R.id.action_sort).setVisible(false);
                // Hide  the crop option  in menu if file is other than image
                menu.findItem(R.id.action_edit).setVisible(false);
            } else {
                int count = listViewAdapter.getSelectedItemCount();

                menu.findItem(R.id.action_delete).setVisible(count >= 1);

                menu.findItem(R.id.action_rename).setVisible(count == 1);
                menu.findItem(R.id.action_info).setVisible(count == 1);
                if (listViewAdapter.getSelectedItems() != null) {

                    menu.findItem(R.id.action_copy).setVisible(count >= 1 && fileType());

                }
                if (listViewAdapter.getSelectedItems() != null)
                    menu.findItem(R.id.action_move).setVisible(count >= 1 && fileType());

                menu.findItem(R.id.action_sort).setVisible(false);
                // Hide  the crop option  in menu if file is other than image
                if (listViewAdapter.getSelectedItems() != null)
                    menu.findItem(R.id.action_edit).setVisible(count == 1 && listViewAdapter.getSelectedItems().get(0).isImage());
            }
        }
        super.onPrepareOptionsMenu(menu);
    }

    //Check the type of file for show/hide menu items
    private boolean fileType() {
        boolean checkFileType = true;
        for (int i = 0; i < listViewAdapter.getSelectedItems().size(); i++) {
            ImageDataModel dataModel = listViewAdapter.getSelectedItems().get(i);
            if (FileUtils.isAudioFile(dataModel.getFile().getAbsolutePath())) {
                checkFileType = false;
                return checkFileType;
            }
        }
        return checkFileType;
    }

    /**
     * this method for performing the delete operation and update toolbar with option menu.
     */
    private void actionDelete() {
        showDeleteDialog();
    }

    // Show the delete dialog for deleting the file(s)
    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = View.inflate(getActivity(), R.layout.delete_alert_dialog, null);
        builder.setView(view);
        TextView okBtn = (TextView) view.findViewById(R.id.alert_yes_button);
        TextView cancelBtn = (TextView) view.findViewById(R.id.alert_no_button);
        deleteDialog = builder.create();
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

    // Method to call action delete method for deleting the file
    private void deleteFile() {
        actionDelete(listViewAdapter.getSelectedItems());
        updateToolbarView(listViewAdapter.getSelectedItemCount());
    }

    /**
     * Get the selected files list and delete and then  scan the file entry by
     * FileUtils.scanFile(Context,file)method.
     *
     * @param modelData
     */
    private void actionDelete(final ArrayList<ImageDataModel> modelData) {
        //removing  the item from mail list
        for (ImageDataModel file : modelData) {
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
            }

        }


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
            FileUtils.scanFile(getActivity(), data.getFile());

        }
        //removing the item from local list and notify the adapter
        listViewAdapter.removeFilesNotifyList(modelData);
        SDKUtils.showToast(getActivity(), message);
    }

    /**
     * Method to show the dialogue for  rename a file which is selected inside into a folder.
     */
    private void renameFile() {
        // Get modeldata of the selected file to rename
        final List<ImageDataModel> selectedItems = listViewAdapter.getSelectedItems();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText input;
        builder.setTitle(R.string.rename_dialog_title);
        View view = View.inflate(getActivity(), R.layout.dialog_edit_text, null);

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
                                SDKUtils.showToast(getActivity(), getString(R.string.file_already_exists_error));
                            } else {
                                updateFileName(selectedItems, fileName);
                                renameDialog.dismiss();
                            }

                            //Hide the keyboard after dismiss the renameDialog.
                            renameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                        } else {
                            SDKUtils.showToast(getActivity(), getString(R.string.file_name_invalid_character));
                        }
                    } else {
                        SDKUtils.showToast(getActivity(), getString(R.string.rename_file_name_length));
                    }
                } else {
                    SDKUtils.showToast(getActivity(), getString(R.string.empty_dialog_message));
                }
            }
        });
        renameDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameDialog.dismiss();
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
        listViewAdapter.clearSelection();
        // updating the toolbar with action bar  menus items
        updateToolbarView(listViewAdapter.getSelectedItemCount());
        try {
            if (selectedItems != null && selectedItems.size() == 1) {
                ImageDataModel dataModel = selectedItems.get(0);
                File file = dataModel.getFile();
                // method to rename the file and then set to model data for update the index of file in main list
                dataModel.setFile(FileUtils.renameFile(dataModel.getFile(), rename));
                // Update the name of Main List of GalleryHelper.class
                if (SharedPref.getListType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.LIST_VIEW)) {
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
                FileUtils.scanFile(getActivity(), dataModel.getFile());
                FileUtils.scanFile(getActivity(), file);
                // update local database for time
                mediaFileDuration.updateFileName(dataModel.getFile().getAbsolutePath(), file.getAbsolutePath());

                // Method to update UI
                listViewAdapter.notifyItemAfterRename(dataModel);
            }
        } catch (Exception e) {
            SDKUtils.showToast(getActivity(), e.toString());
        }
    }

    //Method to crop the media files (image type only)
    private void actionCrop() {
        ArrayList<ImageDataModel> imageDataModels = listViewAdapter.getSelectedItems();
        Intent intent = new Intent(getActivity(), CropMainActivity.class);
        intent.putExtra(Constant.DATA, imageDataModels.get(0));
        intent.putExtra(Constant.INTENT_STATUS, false);
        startActivity(intent);
        if (listViewAdapter.anySelected())
            listViewAdapter.clearSelection();
        // updating the toolbar with action bar  menus items
        updateToolbarView(listViewAdapter.getSelectedItemCount());
    }

    /**
     * Avoid low memory exception,Clear the Glide cache
     * memory and run System.gc to remove the garbage the data .
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(getActivity()).clearMemory();
        Glide.get(getActivity()).trimMemory(TRIM_MEMORY_COMPLETE);
        System.gc();
    }

    /**
     * Method to open dialog fragment
     *
     * @param ActionType type of action perform like copy/move
     */
    private void actionCopyMove(boolean ActionType) {
        String name = "xyz";
        ListViewItemCopyMoveDialog dFragment = new ListViewItemCopyMoveDialog();
        Bundle args = new Bundle();
        args.putBoolean(Constant.ACTION_TYPE, ActionType);
        args.putString(Constant.KEY_NAME, name);
        dFragment.setArguments(args);
        // Show DialogFragment
        fm = getFragmentManager();
        // dFragment.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme);
        dFragment.setStyle(DialogFragment.STYLE_NO_FRAME, R.style.MyCustomThemeDialog);
        dFragment.show(fm, "");
    }

    /**
     * method call from FolderDialogFragment class to copy/move the items
     *
     * @param currentDirectory current directory name
     * @param actionType       type of action
     */

}
