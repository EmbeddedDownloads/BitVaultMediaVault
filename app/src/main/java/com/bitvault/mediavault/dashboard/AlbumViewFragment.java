package com.bitvault.mediavault.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.adapter.DirectoryAdapter;
import com.bitvault.mediavault.baseclass.BaseSupportFragment;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.helper.AlbumViewDividerDecorator;
import com.bitvault.mediavault.helper.AudioViewData;
import com.bitvault.mediavault.helper.GalleryHelper;
import com.bitvault.mediavault.helper.GlobalBus;
import com.bitvault.mediavault.helper.PhotoViewData;
import com.bitvault.mediavault.helper.SharedPref;
import com.bitvault.mediavault.helper.VideoViewData;
import com.bitvault.mediavault.ottonotification.LandingFragmentNotification;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by vvdn on 6/1/2017.
 */

/**
 * This class is used for show the media files into the specific folder into Grid view
 */
public class AlbumViewFragment extends BaseSupportFragment implements DirectoryAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    @BindView(R.id.directories_grid)
    RecyclerView directoriesGrid;
    Unbinder unbinder;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private DirectoryAdapter directoryAdapter;
    private GridLayoutManager lLayout;
    private ArrayList<String> data = new ArrayList<>();
    private Handler handler = new Handler();
    private SharedPreferences sharedPreferences;
    private FetchDataInBackground fetchDataInBackground;

    public AlbumViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.landing_fragment, container, false);
        /**
         * Registering the ButterKnife view Injection
         */
        unbinder = ButterKnife.bind(this, view);
        /**
         * Registering the Otto bus event for Handling the notification
         * call back when user swipe to refresh the view.
         */
        GlobalBus.getBus().register(this);
        initializeAdapter();
        return view;
    }

    /***
     * Initialise the adapter class and set adapter on recycler view.
     */
    private void initializeAdapter() {
        /**
         * Initialising the shared preference class instance.
         */
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        progressBar.setVisibility(View.VISIBLE);
        final int columns = getResources().getInteger(R.integer.album_view);
        lLayout = new GridLayoutManager(getActivity(), columns);
        directoriesGrid.setHasFixedSize(true);
        directoriesGrid.setLayoutManager(lLayout);
        data.clear();
        directoryAdapter = new DirectoryAdapter(getActivity(), data, sharedPreferences);
        /**
         * Registering call back of item click
         */
        directoryAdapter.setOnItemClickListener(this);
        directoriesGrid.setAdapter(directoryAdapter);
        /**
         * Add item Decorator for Grid view in recycler view
         */
        directoriesGrid.addItemDecoration(new AlbumViewDividerDecorator(getActivity()));
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

    @Override
    public void onResume() {
        super.onResume();
        /**
         * Fetching all media files while use resume the application
         */
        if (fetchDataInBackground != null && fetchDataInBackground.getStatus() != AsyncTask.Status.FINISHED)
            fetchDataInBackground.cancel(true);
        fetchDataInBackground = new FetchDataInBackground();
        fetchDataInBackground.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }


    /**
     * Handling the call back of item click on grid view and pass data to DirectoryDetailActivity
     * class with keyName
     *
     * @param name
     */
    @Override
    public void directoryFolderClick(final String name) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getActivity(), DirectoryDetailActivity.class);
                    intent.putExtra(Constant.KEY_NAME, name);
                    startActivity(intent);
                }
            });
    }

    /**
     * Handling swipe to refresh method an update the UI
     */
    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        if (fetchDataInBackground != null && fetchDataInBackground.getStatus() != AsyncTask.Status.FINISHED)
            fetchDataInBackground.cancel(true);
        fetchDataInBackground = new FetchDataInBackground();
        fetchDataInBackground.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Update  UI view when get notification from GalleryHelper class
     *
     * @param s
     */
    @Subscribe
    public void getMessage(LandingFragmentNotification s) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);
                updateUI();
            }
        });

    }

    @Override
    public void onDestroyView() {
        GlobalBus.getBus().unregister(this);
        super.onDestroyView();
    }

    /**
     * Update the UI on Main Thread
     */
    private void updateUI() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    data.clear();
                    if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                        data.addAll(GalleryHelper.keyList);
                    } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                        data.addAll(VideoViewData.keyList);
                    } else if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                        data.addAll(AudioViewData.keyList);
                    } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                        data.addAll(PhotoViewData.keyList);
                    }
                    directoryAdapter.notifyDataSetChanged();
                }
            });
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
}