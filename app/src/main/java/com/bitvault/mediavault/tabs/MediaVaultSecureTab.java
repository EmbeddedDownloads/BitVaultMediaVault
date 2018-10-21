package com.bitvault.mediavault.tabs;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
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
import com.bitvault.mediavault.adapter.SecureMediaAdapter;
import com.bitvault.mediavault.baseclass.BaseSupportFragment;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.dashboard.InfoActivity;
import com.bitvault.mediavault.dashboard.LandingActivity;
import com.bitvault.mediavault.dashboard.PhotoViewActivity;
import com.bitvault.mediavault.database.MediaFileDuration;
import com.bitvault.mediavault.database.MediaVaultLocalDb;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.helper.GlobalBus;
import com.bitvault.mediavault.helper.SharedPref;
import com.bitvault.mediavault.helper.SimpleDividerItemDecoration;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.WeakHashMap;

import bitmanagers.BitVaultAppStoreManager;
import bitmanagers.BitVaultDataManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import commons.SDKHelper;
import iclasses.mediavaultcallback.DeleteMediaFromAppStore;
import iclasses.mediavaultcallback.DeleteMediaFromPbcCallBack;
import iclasses.mediavaultcallback.MediaFileDetailsOperationCallBack;
import iclasses.mediavaultcallback.MediaVaultReceiveCallBack;
import model.MediaVaultBlockModel;
import utils.SDKUtils;

/**
 * Created by vvdn on 7/31/2017.
 */

public class MediaVaultSecureTab extends BaseSupportFragment implements SecureMediaAdapter.OnItemClickListener,
        SecureMediaAdapter.OnItemLongClickListener, MediaVaultReceiveCallBack, MediaFileDetailsOperationCallBack, DeleteMediaFromPbcCallBack, DeleteMediaFromAppStore {
    @BindView(R.id.directories_grid)
    RecyclerView recyclerView;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.parentLayout)
    RelativeLayout parentLayout;
    Unbinder unbinder;
    private MediaFileDuration mediaFileDuration;
    private MediaVaultLocalDb secureMediaFileDb;
    public SecureMediaAdapter secureMediaAdapter;
    private SharedPreferences sharedPreferences;
    private ArrayList<ImageDataModel> data = new ArrayList<>();
    private ArrayList<ImageDataModel> downloadDataList = new ArrayList<>();
    private WeakHashMap<String, String> fileListMap = new WeakHashMap<>();
    private BitVaultDataManager mediaVaultDataManager;
    private SortDataInBackground sortDataInBackground;
    private LoadInBackground loadInBackground;
    private BitVaultAppStoreManager bitVaultAppStoreManager;
    private AlertDialog renameDialog, deleteDialog;
    private int mCounter = 0, mCallBackCounter = 0, mCounterSuccess = 0;
    private File renamedFile;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.media_vault_secure_tab, container, false);
        unbinder = ButterKnife.bind(this, view);
        initialised();
        return view;
    }

    //Method to initialise  the database and set recycler view
    private void initialised() {
        /**
         * Initialising the shared preference class instance.
         */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mediaFileDuration = MediaFileDuration.getDatabaseInstance(getActivity());
        secureMediaFileDb = MediaVaultLocalDb.getMediaVaultDatabaseInstance(getActivity());
        mediaVaultDataManager = BitVaultDataManager.getSecureMessangerInstance();
        bitVaultAppStoreManager = BitVaultAppStoreManager.getAppStoreManagerInstance();
        setupRecycler();
    }

    @Override
    public void onResume() {
        super.onResume();
        GlobalBus.getBus().register(this);
        updateUiInBackground();
        //Update the time duration in background
        new Handler().postDelayed(new Runnable() {
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
    public void onPause() {
        super.onPause();
        GlobalBus.getBus().unregister(this);
    }

    /**
     * Sorting data in background
     */
    class SortDataInBackground extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (getActivity() != null) {
                data.clear();
                if (Constant.SECURE_TAB.equalsIgnoreCase(SharedPref.getSecureTab(sharedPreferences))) {
                    data.addAll(secureMediaFileDb.getSecureFileList());
                }
                if (data != null && data.size() > 0)
                    Collections.sort(data);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (getActivity() != null && secureMediaAdapter != null) {
                secureMediaAdapter.notifyDataChanged();
                if (progressBar != null)
                    if (mCounter == mCallBackCounter)
                        progressBar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Method to set recycler view on adapter
     */
    protected void setupRecycler() {
        secureMediaAdapter = new SecureMediaAdapter(this, data, mediaFileDuration, secureMediaFileDb);
        /**
         * Add item Decorator for Grid view in recycler view
         */
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        // set grid layout manager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), getResources().getInteger(R.integer.list_view));
        recyclerView.setLayoutManager(gridLayoutManager);
        secureMediaAdapter.setGridLayoutManager(gridLayoutManager);
        /**
         * Registering call back of item click
         */
        secureMediaAdapter.setOnItemClickListener(this);
        secureMediaAdapter.setOnItemLongClickListener(this);
        recyclerView.setAdapter(secureMediaAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.secure_tab_action, menu);
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
            case R.id.action_rename:
                renameFile();
                return true;
            case R.id.action_delete:
                actionDelete();
                return true;
            case R.id.action_archive:
                showArchiveDialog();
                return true;
            case R.id.action_download:
                if (progressBar != null && progressBar.getVisibility() != View.VISIBLE) {
                    if (Utils.isNetworkConnected(getActivity())) {
                        if (secureMediaAdapter.getSelectedItems() != null)
                            downloadSecureFile(secureMediaAdapter.getSelectedItems());
                    } else {
                        Utils.showSnakbar(parentLayout, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
                    }
                } else {
                    SDKUtils.showToast(getActivity(), getString(R.string.file_downloading_in_progress));
                }

                return true;
            case R.id.action_info:
                if (secureMediaAdapter != null && secureMediaAdapter.getSelectedItems() != null
                        && secureMediaAdapter.getSelectedItems().size() > 0) {
                    Intent intent = new Intent(getActivity(), InfoActivity.class);
                    intent.putExtra(Constant.DATA, secureMediaAdapter.getSelectedItems().get(0));
                    startActivity(intent);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = View.inflate(getActivity(), R.layout.delete_alert_dialog, null);
        builder.setView(view);
        TextView okBtn = (TextView) view.findViewById(R.id.alert_yes_button);
        TextView alertMessage = (TextView) view.findViewById(R.id.alert_message);
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

    /**
     * Show the archive dialog for archive the file(s)
     */
    private void showArchiveDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = View.inflate(getActivity(), R.layout.delete_alert_dialog, null);
        builder.setView(view);
        TextView okBtn = (TextView) view.findViewById(R.id.alert_yes_button);
        TextView alertMessage = (TextView) view.findViewById(R.id.alert_message);
        TextView cancelBtn = (TextView) view.findViewById(R.id.alert_no_button);
        TextView alertTitle = (TextView) view.findViewById(R.id.alertTitle);
        alertTitle.setText(R.string.confirm_archive);
        alertMessage.setText(R.string.archive_file_alert_message);
        deleteDialog = builder.create();
        deleteDialog.show();
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // method to archive file after click on OK button
                archiveFile();
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
        if (progressBar != null && progressBar.getVisibility() != View.VISIBLE) {
            if (Utils.isNetworkConnected(getActivity())) {
                deleteSecureFile(secureMediaAdapter.getSelectedItems());
            } else {
                Utils.showSnakbar(parentLayout, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
            }
        } else {
            SDKUtils.showToast(getActivity(), getString(R.string.file_delete_in_progress));
        }
    }

    /**
     * Method to show the dialogue for  rename a file which is selected inside into a folder.
     */
    private void renameFile() {
        // Get model data of the selected file to rename
        final List<ImageDataModel> selectedItems = secureMediaAdapter.getSelectedItems();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText input;
        builder.setTitle(R.string.rename_dialog_title);
        View view = View.inflate(getActivity(), R.layout.dialog_edit_text, null);
        input = (EditText) view.findViewById(R.id.dialog_edit_text);
        builder.setView(view);
        builder.setPositiveButton(R.string.rename_dialog_ok_button, null);
        builder.setNegativeButton(R.string.rename_dialog_cancel_button, null);
        if (selectedItems != null && selectedItems.size() == 1) {
            // setting the file name into renameDialog text after removing the extension name of a file
            String actualName = FileUtils.getSecureFileName(selectedItems.get(0).getFile().getName());
            input.setText(FileUtils.removeExtension(actualName));
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
                            String suffixName = FileUtils.getSecureFileSuffixName(selectedItems.get(0).getFile().getName());
                            // Method to update the name of file
                            renamedFile = new File(selectedItems.get(0).getFile().getParent(), suffixName + fileName + "." +
                                    FileUtils.getExtension(selectedItems.get(0).getFile().getName()));
                            if (renamedFile.exists()) {
                                SDKUtils.showToast(getActivity(), getString(R.string.file_already_exists_error));
                            } else {
                                updateDataOnAppStore(selectedItems, fileName);
                                //  updateFileName(secureMediaAdapter.getSelectedItems(), renamedFile);
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
     * Update file name on app store
     *
     * @param selectedItems
     * @param fileName
     */
    private void updateDataOnAppStore(List<ImageDataModel> selectedItems, String fileName) {
        if (bitVaultAppStoreManager != null && progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
            bitVaultAppStoreManager.updateFileDetails(selectedItems.get(0).getFileUniqueId(), fileName, this);

        }

    }


    /**
     * This method to use the  archive secure file
     */
    private void archiveFile() {
        if (secureMediaAdapter != null && secureMediaAdapter.getSelectedItems() != null
                && secureMediaAdapter.getSelectedItems().size() > 0) {
            for (int i = 0; i < secureMediaAdapter.getSelectedItems().size(); i++) {
                final ImageDataModel dataModel = secureMediaAdapter.getSelectedItems().get(i);
                // Update local database for file status..
                secureMediaFileDb.updateFileStatus(dataModel.getFileUniqueId(), Constant.FILE_ARCHIVE);
            }
            //  performing the action of archiveFile, clear selected item and update toolbar.
            secureMediaAdapter.clearSelection();
            // updating the toolbar with action bar  menus items
            updateToolbarView(secureMediaAdapter.getSelectedItemCount());

            updateUiInBackground();

        }
    }

    /**
     * Get selected item count
     *
     * @return
     */
    public boolean getSelectedItemCount() {
        return secureMediaAdapter.getSelectedItemCount() > 0;
    }

    /**
     * Update UI
     */
    private void updateUiInBackground() {
        if (sortDataInBackground != null && sortDataInBackground.getStatus() != AsyncTask.Status.FINISHED)
            sortDataInBackground.cancel(true);
        sortDataInBackground = new SortDataInBackground();
        sortDataInBackground.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    /**
     * @param menu show and hide menu items as item selected
     * @return
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        int itemCountOne = 1;
        if (secureMediaAdapter != null) {
            if (!secureMediaAdapter.anySelected()) {
                menu.findItem(R.id.action_delete).setVisible(false);
                menu.findItem(R.id.action_rename).setVisible(false);
                menu.findItem(R.id.action_info).setVisible(false);
                menu.findItem(R.id.action_archive).setVisible(false);
                menu.findItem(R.id.action_download).setVisible(false);
            } else {
                int count = secureMediaAdapter.getSelectedItemCount();
                menu.findItem(R.id.action_delete).setVisible(count >= itemCountOne && fileTypeStatus(Constant.STATUS_SUCCESS) && !fileType());
                menu.findItem(R.id.action_archive).setVisible(count >= itemCountOne && !fileType());
                menu.findItem(R.id.action_rename).setVisible(count == itemCountOne && !fileType());
                menu.findItem(R.id.action_info).setVisible(count == itemCountOne && !fileType());
                menu.findItem(R.id.action_download).setVisible(count >= itemCountOne && fileType());
            }
        }
        super.onPrepareOptionsMenu(menu);
    }

    /**
     * Check the type of file for show/hide menu items
     *
     * @return
     */
    private boolean fileType() {
        for (int i = 0; i < secureMediaAdapter.getSelectedItems().size(); i++) {
            ImageDataModel dataModel = secureMediaAdapter.getSelectedItems().get(i);
            if (dataModel.getFile() != null) {
                if (dataModel.getFileStatus().equalsIgnoreCase(Constant.FILE_ARCHIVE))
                    return true;
            }
        }
        return false;
    }

    /**
     * Check the type of file for show/hide menu items
     *
     * @param status
     * @return
     */
    private boolean fileTypeStatus(String status) {
        for (int i = 0; i < secureMediaAdapter.getSelectedItems().size(); i++) {
            ImageDataModel dataModel = secureMediaAdapter.getSelectedItems().get(i);
            if (dataModel.getFile() != null) {
                if (dataModel.getFileStatus().equalsIgnoreCase(status))
                    return true;
            }
        }
        return false;
    }

    /**
     * Method to check progressbar visibility status
     *
     * @return
     */
    public boolean progressBarVisibilityStatus() {
        if (progressBar != null && progressBar.getVisibility() != View.VISIBLE) {
            return true;
        }
        return false;
    }

    @Override
    public void itemClickedPosition(final ImageDataModel model, final int position) {
        if (getActivity() != null && model != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressBar != null && progressBar.getVisibility() != View.VISIBLE) {
                        if (!secureMediaAdapter.anySelected()) {
                            actionOnItemClick(model, position);
                        } else {
                            if (!fileTypeStatus(model.getFileStatus())) {
                                SDKUtils.showToast(getActivity(), getString(R.string.select_same_file_type));
                            } else {
                                actionOnItemClick(model, position);
                            }
                        }
                    } else {
                        SDKUtils.showToast(getActivity(), getString(R.string.file_downloading_in_progress));
                    }

                }
            });

    }

    /**
     * method to perform action on item click
     *
     * @param model    item click data model
     * @param position which position is clicked
     */
    private void actionOnItemClick(ImageDataModel model, int position) {
        if (secureMediaAdapter.anySelected()) {
            secureMediaAdapter.toggle(position);
            updateToolbarView(secureMediaAdapter.getSelectedItemCount());
            return;
        }
        if (model.getFileStatus().equalsIgnoreCase(Constant.FILE_ARCHIVE)) {
            ArrayList<ImageDataModel> dataModels = new ArrayList<>();
            dataModels.add(model);
            if (Utils.isNetworkConnected(getActivity())) {
                if (dataModels != null)
                    downloadSecureFile(dataModels);
            } else {
                Utils.showSnakbar(parentLayout, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
            }
        } else {
            Intent intent = new Intent(getActivity(), PhotoViewActivity.class);
            intent.putExtra(Constant.TYPE, Constant.SECURE_TAB);
            intent.putExtra(Constant.DATA, model);
            startActivity(intent);
        }

    }

    @Override
    public void itemLongClickedPosition(final ImageDataModel dataModel, final int position) {
        if (getActivity() != null && dataModel != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressBar != null && progressBar.getVisibility() != View.VISIBLE) {
                        if (!secureMediaAdapter.anySelected()) {
                            secureMediaAdapter.toggle(position);
                            updateToolbarView(secureMediaAdapter.getSelectedItemCount());
                        } else {

                            if (!fileTypeStatus(dataModel.getFileStatus())) {
                                SDKUtils.showToast(getActivity(), getString(R.string.select_same_file_type));
                            } else {
                                secureMediaAdapter.toggle(position);
                                updateToolbarView(secureMediaAdapter.getSelectedItemCount());
                            }
                        }
                    } else {
                        SDKUtils.showToast(getActivity(), getString(R.string.file_downloading_in_progress));
                    }


                }
            });

    }

    /**
     * This method to use the  download multiple secure file
     *
     * @param data
     */

    private void downloadSecureFile(ArrayList<ImageDataModel> data) {
        fileListMap.clear();
        downloadDataList.clear();
        downloadDataList.addAll(data);
        mCounter = downloadDataList.size();
        for (int i = 0; i < downloadDataList.size(); i++) {
            fileListMap.put(downloadDataList.get(i).getFileUniqueId(), downloadDataList.get(i).getFile().getPath());
        }
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
        //  performing the action of archiveFile, clear selected item and update toolbar.
        secureMediaAdapter.clearSelection();
        // updating the toolbar with action bar  menus items
        updateToolbarView(secureMediaAdapter.getSelectedItemCount());
        receiveSecureFile(mCallBackCounter);

    }

    /**
     * Method to call receive file in sequential manner.
     *
     * @param count
     */
    private void receiveSecureFile(int count) {
        ImageDataModel model = downloadDataList.get(count);
        mediaVaultDataManager.receiveSecureFile(model.getFileUniqueId(), SDKHelper.MEDIA_VAULT_TAG,
                model.getWalletAddress(), model.getFileEncTxid(), model.getFileEncKey(),
                MediaVaultSecureTab.this);
    }

    /**
     * This method to use the  delete multiple secure file
     *
     * @param data
     */
    private void deleteSecureFile(ArrayList<ImageDataModel> data) {
        fileListMap.clear();
        downloadDataList.clear();
        downloadDataList.addAll(data);
        mCounter = downloadDataList.size();
        for (int i = 0; i < downloadDataList.size(); i++) {
            fileListMap.put(downloadDataList.get(i).getFileUniqueId(), downloadDataList.get(i).getFile().getPath());
        }
        if (progressBar != null)
            progressBar.setVisibility(View.VISIBLE);
        //  performing the action of archiveFile, clear selected item and update toolbar.
        secureMediaAdapter.clearSelection();
        // updating the toolbar with action bar  menus items
        updateToolbarView(secureMediaAdapter.getSelectedItemCount());
        deleteSecureFileSequentially(mCallBackCounter);

    }

    /**
     * Method to call delete file in sequential manner.
     *
     * @param count
     */
    private void deleteSecureFileSequentially(int count) {
        ImageDataModel model = downloadDataList.get(count);
        mediaVaultDataManager.deleteMediaFileFromPbc(model.getFileUniqueId(), SDKHelper.MEDIA_VAULT_TAG,
                model.getWalletAddress(), model.getCrc(), MediaVaultSecureTab.this);
    }

    /**
     * This method used for updating toolbar from secure tab
     *
     * @param count
     */
    public void updateToolbarView(int count) {
        ((LandingActivity) getActivity()).updateToolbarFromSecureTab(count);
    }

    /**
     * Method to perform cross key on Toolbar and notify the list
     */
    public void actionCrossKey() {
        secureMediaAdapter.clearSelection();
        updateToolbarView(secureMediaAdapter.getSelectedItemCount());
    }

    @Override
    public void receiveMediaVaultCallback(String status, String message, String fileLocation, MediaVaultBlockModel mediaVaultBlockModel) {
        if (getActivity() != null && progressBar != null) {
            mCallBackCounter++;
            if (status.equalsIgnoreCase(SDKHelper.TAG_CAP_OK)
                    || status.equalsIgnoreCase(SDKHelper.TAG_SUCCESS)) {
                mCounterSuccess++;
                if (getActivity() != null && mediaVaultBlockModel != null)
                    saveImageToInternalStorage(mediaVaultBlockModel, fileLocation);
                if (mCallBackCounter == mCounter && mCounterSuccess == mCounter) {
                    mCallBackCounter = 0;
                    mCounter = 0;
                    mCounterSuccess = 0;
                    fileListMap.clear();
                    downloadDataList.clear();
                    progressBar.setVisibility(View.GONE);
                } else {
                    if (mCallBackCounter < mCounter) {
                        receiveSecureFile(mCallBackCounter);
                    } else {
                        SDKUtils.showToast(getActivity(), getString(R.string.error_in_receiving_file));
                    }
                }
            } else {
                if (mCallBackCounter < mCounter) {
                    receiveSecureFile(mCallBackCounter);
                } else {
                    SDKUtils.showToast(getActivity(), getString(R.string.error_in_receiving_file));
                    mCallBackCounter = 0;
                    mCounter = 0;
                    mCounterSuccess = 0;
                    fileListMap.clear();
                    downloadDataList.clear();
                    progressBar.setVisibility(View.GONE);

                }
            }
        } else {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
                SDKUtils.showToast(getActivity(), getString(R.string.error_in_receiving_file));
            }
        }
    }

    @Override
    public void deletedFileFromPbc(String status, String message, String fileUniqueId) {
        if (getActivity() != null && progressBar != null && fileUniqueId != null) {
            mCallBackCounter++;
            if (status.equalsIgnoreCase(SDKHelper.TAG_CAP_OK)
                    || status.equalsIgnoreCase(SDKHelper.TAG_SUCCESS)) {
                mCounterSuccess++;
                // update file into local database with status
                secureMediaFileDb.updateFileStatus(fileUniqueId, Constant.DELETE_FROM_PBC);
                try {
                    // method to deleting the selected file
                    FileUtils.deleteFile(new File(fileListMap.get(fileUniqueId)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // method to scan the file and update its position
                FileUtils.scanFile(getActivity(), new File(fileListMap.get(fileUniqueId)));
                deleteDataOnPlayStore(fileUniqueId);
                if (mCallBackCounter == mCounter && mCounterSuccess == mCounter) {
                    mCallBackCounter = 0;
                    mCounter = 0;
                    mCounterSuccess = 0;
                    fileListMap.clear();
                    downloadDataList.clear();
                    if (progressBar != null)
                        progressBar.setVisibility(View.GONE);
                } else {
                    if (mCallBackCounter < mCounter) {
                        deleteSecureFileSequentially(mCallBackCounter);
                    } else {
                        SDKUtils.showToast(getActivity(), getString(R.string.error_in_deleting_file));
                    }
                }
            } else {
                if (mCallBackCounter < mCounter) {
                    deleteSecureFileSequentially(mCallBackCounter);
                } else {
                    SDKUtils.showToast(getActivity(), getString(R.string.error_in_deleting_file));
                    mCallBackCounter = 0;
                    mCounter = 0;
                    mCounterSuccess = 0;
                    fileListMap.clear();
                    downloadDataList.clear();
                    if (progressBar != null)
                        progressBar.setVisibility(View.GONE);

                }
                //  SDKUtils.showToast(getActivity(), message);
            }
        }
    }

    /**
     * Delete data from play store and update database
     *
     * @param fileId
     */
    private void deleteDataOnPlayStore(String fileId) {
        bitVaultAppStoreManager.deleteFileDetails(fileId, MediaVaultSecureTab.this);
    }

    /**
     * After getting response of app store delete the file or update its status on local db.
     *
     * @param status
     * @param message
     * @param id
     */
    @Override
    public void deleteMediaFromAppstoreCallBack(String status, String message, String id) {
        if (id != null)
            if (status.equalsIgnoreCase(SDKHelper.TAG_CAP_OK)
                    || status.equalsIgnoreCase(SDKHelper.TAG_SUCCESS)) {
                secureMediaFileDb.deleteSecureFile(id);
            } else {
                secureMediaFileDb.updateFileStatus(id, Constant.STATUS_FAILED_APP_STORE_DELETE);
            }
        if (mCounter == mCallBackCounter)
            updateUiInBackground();
    }

    /**
     * After getting response of app store update the file name on local db.
     *
     * @param status
     * @param message
     * @param id
     */
    @Override
    public void mediaFileDetailsOperationCallBack(String status, String message, String id) {
        if (id != null)
            if (status.equalsIgnoreCase(SDKHelper.TAG_CAP_OK)
                    || status.equalsIgnoreCase(SDKHelper.TAG_SUCCESS)) {
                if (renamedFile != null) {
                    progressBar.setVisibility(View.GONE);
                    updateFileName(secureMediaAdapter.getSelectedItems(), renamedFile);
                }
            } else {
                progressBar.setVisibility(View.GONE);
                Utils.showSnakbar(parentLayout, getString(R.string.error_in_renaming_file), Snackbar.LENGTH_SHORT);
            }
    }

    /**
     * Method to update the name of selected file on dialog click event
     *
     * @param selectedItems
     * @param renameFile
     */
    private void updateFileName(final List<ImageDataModel> selectedItems, final File renameFile) {
        //  performing the action of rename clear selected item and update toolbar.
        secureMediaAdapter.clearSelection();
        // updating the toolbar with action bar  menus items
        updateToolbarView(0);
        try {
            if (selectedItems != null && selectedItems.size() == 1) {
                ImageDataModel dataModel = selectedItems.get(0);
                File file = dataModel.getFile();
                // method to rename the file and then set to model data for update the index of file in main list
                dataModel.setFile(FileUtils.renameFile(dataModel.getFile(), FileUtils.removeExtension(renameFile.getName())));
                        /*
                        * Scan old and new file to update entry into content provider.
                        * */
                FileUtils.scanFile(getActivity(), renameFile);
                FileUtils.scanFile(getActivity(), file);
                // update local database for time
                mediaFileDuration.updateFileName(dataModel.getFile().getAbsolutePath(), file.getAbsolutePath());
                // Method to update UI
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        secureMediaFileDb.updateFileName(selectedItems.get(0).getFileUniqueId(), renameFile);
                        updateUiInBackground();
                    }
                }, 100);
            }
        } catch (Exception e) {
            SDKUtils.showToast(getActivity(), e.toString());
        }
    }

    /**
     * Method to update file in content provider.
     *
     * @param mediaVaultBlockModel
     * @param fileLocation
     */
    public void saveImageToInternalStorage(final MediaVaultBlockModel mediaVaultBlockModel, String fileLocation) {
        try {
            if (fileLocation != null && mediaVaultBlockModel != null) {
                try {
                    BufferedInputStream inStream = null;
                    BufferedOutputStream outStream = null;
                    int DEFAULT_BUFFER_SIZE = 32 * 1024;
                    try {
                        inStream = new BufferedInputStream(new FileInputStream(new File(fileLocation)));
                        outStream = new BufferedOutputStream(new FileOutputStream(new File(fileListMap.get(mediaVaultBlockModel.getResultSet().getId()))));
                        // Transfer bytes from in to outStream
                        byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
                        int len;
                        while ((len = inStream.read(buf)) > 0) {
                            outStream.write(buf, 0, len);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            outStream.close();
                            inStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        // method to deleting the selected file
                        FileUtils.deleteFile(new File(fileLocation));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // method to scan the file and update its position
                    FileUtils.scanFile(getActivity(), new File(fileLocation));
                } catch (Exception e) {
                }


                FileUtils.scanFile(getActivity(), new File(fileListMap.get(mediaVaultBlockModel.getResultSet().getId())));
                secureMediaFileDb.updateFileStatus(mediaVaultBlockModel.getResultSet().getId(), Constant.STATUS_SUCCESS);
//update ui after completing the download to all files which are selected.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        updateUiInBackground();
                    }
                }, 100);

                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
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
                            // Method to insert file path and time duration into local database
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
                if (secureMediaAdapter != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            secureMediaAdapter.notifyDataChanged();
                        }
                    });
                }
            }
        }
    }
}
