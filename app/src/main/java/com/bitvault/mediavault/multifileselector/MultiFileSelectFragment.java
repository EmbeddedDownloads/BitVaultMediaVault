package com.bitvault.mediavault.multifileselector;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bitvault.mediavault.BuildConfig;
import com.bitvault.mediavault.R;
import com.bitvault.mediavault.adapter.MultiFileSelectAdapter;
import com.bitvault.mediavault.baseclass.BaseSupportFragment;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.database.MediaFileDuration;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.helper.GalleryHelper;
import com.bitvault.mediavault.helper.GalleryHelperBaseOnId;
import com.bitvault.mediavault.helper.GlobalBus;
import com.bitvault.mediavault.helper.PhotoViewData;
import com.bitvault.mediavault.helper.PhotoViewDataOnIdBasis;
import com.bitvault.mediavault.helper.SharedPref;
import com.bitvault.mediavault.helper.SimpleDividerItemDecoration;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.ottonotification.LandingFragmentNotification;
import com.bumptech.glide.Glide;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import utils.SDKUtils;

import static android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE;

/**
 * Created by vvdn on 11-Dec-17.
 */

public class MultiFileSelectFragment extends BaseSupportFragment implements MultiFileSelectAdapter.OnItemClickListener {

    @BindView(R.id.directories_grid_detail)
    RecyclerView directoriesGridDetail;
    private MultiFileSelectAdapter multiFileSelectAdapter;
    private GridLayoutManager lLayout;
    private ArrayList<ImageDataModel> data = new ArrayList<>();
    private String nameKey;
    private Handler handler = new Handler();
    private SharedPreferences sharedPreferences;
    Unbinder unbinder;
    private MediaFileDuration mediaFileDuration;
    private LoadFileDuration loadFileDuration;
    private boolean multipleAllow = false;
    private ArrayList<Uri> uriArrayList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_multi_image_select, container, false);
        init(view);
        getIntentData();
        initializeAdapter();
        return view;
    }

    /**
     * Register third party library like butter knife,event bus  and initialise database
     *
     * @param view
     */
    private void init(View view) {
        /**
         * Registering the ButterKnife view Injection
         */
        unbinder = ButterKnife.bind(this, view);
        /**
         * Registering the Otto bus event for Handling the notification
         * call back when user swipe to refresh the view.
         */
        GlobalBus.getBus().register(this);
        mediaFileDuration = MediaFileDuration.getDatabaseInstance(getActivity());

    }

    /**
     * Method to get intent data from other fragment/activity
     */
    private void getIntentData() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            nameKey = bundle.getString(Constant.KEY_NAME);
            multipleAllow = bundle.getBoolean(Constant.MULTIPLE_SELECT);
        }
        if (nameKey != null && nameKey.equalsIgnoreCase(Constant.AUDIO_FOLDER_NAME)) {
            ((MultiFileSelectActivity) getActivity()).updateToolbar(0, Constant.AUDIO_TITLE);
        } else {
            if (nameKey != null)
                ((MultiFileSelectActivity) getActivity()).updateToolbar(0, nameKey);
        }
    }

    /***
     * Initialise the adapter class and set adapter on recycler view.
     */
    private void initializeAdapter() {
        /**
         * Initialising the shared preference class instance.
         */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final int columns = getResources().getInteger(R.integer.list_view);
        lLayout = new GridLayoutManager(getActivity(), columns);
        directoriesGridDetail.setHasFixedSize(true);
        directoriesGridDetail.setLayoutManager(lLayout);
        data.clear();
        /**
         * Getting all media files on basis of folderName from GalleryHelper class
         */
        if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAnyContentIntent(sharedPreferences))) {
            if (GalleryHelper.imageFolderMap != null && nameKey != null && GalleryHelper.imageFolderMap.size() > 0 && GalleryHelper.imageFolderMap.get(nameKey) != null)
                data.addAll(GalleryHelper.imageFolderMap.get(nameKey));
        } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getImageContentIntent(sharedPreferences))) {
            if (PhotoViewData.imageFolderMap != null && nameKey != null && PhotoViewData.imageFolderMap.size() > 0 && PhotoViewData.imageFolderMap.get(nameKey) != null)
                data.addAll(PhotoViewData.imageFolderMap.get(nameKey));
        }
        if (data != null && data.size() > 0)
            Collections.sort(data);
        multiFileSelectAdapter = new MultiFileSelectAdapter(getActivity(), data, mediaFileDuration);
        /**
         * Registering call back of item click
         */
        multiFileSelectAdapter.setOnItemClickListener(this);
        directoriesGridDetail.setAdapter(multiFileSelectAdapter);
        /**
         * Add item Decorator for Grid view in recycler view
         */
        directoriesGridDetail.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
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
                updateUI();
            }
        }, 500);
    }

    @Override
    public void onResume() {
        super.onResume();
        /**
         * Get the bucketId on basis of folder name and pass it  to GalleryHelperBaseOnId class
         */
        fetchFilesOnActionPerform();
        if (multiFileSelectAdapter != null && multiFileSelectAdapter.getSelectedItemCount() <= 0)
            ((MultiFileSelectActivity) getActivity()).enableBackKey();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        ((MultiFileSelectActivity) getActivity()).disableBackKey();
    }

    /**
     * Update the UI on Main Thread
     */
    private void updateUI() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    data.clear();
                    if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAnyContentIntent(sharedPreferences))) {
                        data.addAll(GalleryHelperBaseOnId.dataModelArrayList);
                    } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getImageContentIntent(sharedPreferences))) {
                        data.addAll(PhotoViewDataOnIdBasis.dataModelArrayList);
                    }
                    //Sort the data according to sorting value
                    if (data != null && data.size() > 0) {
                        Collections.sort(data);
                    }
                    multiFileSelectAdapter.notifyDataSetChanged();
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
     * Fetch files from content provider
     */
    private void fetchFilesOnActionPerform() {
        if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAnyContentIntent(sharedPreferences))) {
            if (GalleryHelper.imageFolderMap != null && nameKey != null && GalleryHelper.imageFolderMap.size() > 0 && GalleryHelper.imageFolderMap.get(nameKey) != null) {
                GalleryHelperBaseOnId.getMediaFilesOnIdBasis(getActivity(), GalleryHelper.imageFolderMap.get(nameKey).get(0).getBucketId());
            }
        } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getImageContentIntent(sharedPreferences))) {
            if (PhotoViewData.imageFolderMap != null && nameKey != null && PhotoViewData.imageFolderMap.size() > 0 && PhotoViewData.imageFolderMap.get(nameKey) != null) {
                PhotoViewDataOnIdBasis.getMediaFilesOnIdBasis(getActivity(), PhotoViewData.imageFolderMap.get(nameKey).get(0).getBucketId());
            }
        }
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
                            if (path != null)
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
            multiFileSelectAdapter.notifyDataSetChanged();
        }

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
     * Handling the call back of item click on grid view and pass data to PhotoViewActivity/MediaPlayerFragment class
     * class with keyName of folder and its position
     *
     * @param model
     * @param position
     */
    @Override
    public void itemClick(final ImageDataModel model, final int position) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (multiFileSelectAdapter != null) {
                    if (multipleAllow) {
                        multiFileSelectAdapter.toggle(position);
                    } else {
                        multiFileSelectAdapter.toggleSingleSelect(position);
                    }
                    ((MultiFileSelectActivity) getActivity()).updateToolbar(multiFileSelectAdapter.getSelectedItemCount(), nameKey);
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
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
                actionBackPress();
                return true;
            case R.id.action_done:
                if (multiFileSelectAdapter != null && multiFileSelectAdapter.getSelectedItemCount() > 0) {
                    backResultToActivity();
                } else {
                    SDKUtils.showToast(getActivity(), getString(R.string.please_select_a_file));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*
     Back result to main activity after click on done button
     */
    private void backResultToActivity() {
        uriArrayList.clear();
        int size = multiFileSelectAdapter.getSelectedItemCount();
        for (int i = 0; i < size; i++) {
            Uri photoURI = FileProvider.getUriForFile(getActivity(),
                    BuildConfig.APPLICATION_ID + ".provider",
                    multiFileSelectAdapter.getSelectedItems().get(i).getFile());
            uriArrayList.add(photoURI);
        }
        if (uriArrayList != null && uriArrayList.size() > 0)
            ((MultiFileSelectActivity) getActivity()).backResultToLaunchingApp(uriArrayList);
    }

    /**
     * Check that on back press from Navigation bar or back key, Then finish the activity if
     * there were <=1 fragment in stack else back the fragment.
     */
    public void actionBackPress() {
        if (multiFileSelectAdapter.anySelected()) {
            multiFileSelectAdapter.clearSelection();
            ((MultiFileSelectActivity) getActivity()).updateToolbar(multiFileSelectAdapter.getSelectedItemCount(), nameKey);
            return;
        } else {
            FragmentManager manager = getFragmentManager();
            if (manager.getBackStackEntryCount() <= 1) {
                getActivity().finish();
            } else {
                ((MultiFileSelectActivity) getActivity()).updateToolbar(0, getResources().getString(R.string.app_name));
                manager.popBackStack();
            }
        }

    }
}
