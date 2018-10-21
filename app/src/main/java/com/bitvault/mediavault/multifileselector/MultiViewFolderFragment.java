package com.bitvault.mediavault.multifileselector;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import com.bitvault.mediavault.helper.GalleryHelper;
import com.bitvault.mediavault.helper.GlobalBus;
import com.bitvault.mediavault.helper.PhotoViewData;
import com.bitvault.mediavault.helper.SharedPref;
import com.bitvault.mediavault.ottonotification.LandingFragmentNotification;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import utils.SDKUtils;

/**
 * Created by vvdn on 11-Dec-17.
 */

public class MultiViewFolderFragment extends BaseSupportFragment implements DirectoryAdapter.OnItemClickListener {

    @BindView(R.id.directories_grid)
    RecyclerView directoriesGrid;
    Unbinder unbinder;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
/*    @BindView(R.id.toolbarTitle)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;*/
    private DirectoryAdapter directoryAdapter;
    private GridLayoutManager lLayout;
    private ArrayList<String> data = new ArrayList<>();
    private Handler handler = new Handler();
    private SharedPreferences sharedPreferences;
    private FetchDataInBackground fetchDataInBackground;
    private boolean multipleAllow = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_multi_view_folder, container, false);
        registerLib(view);
        getDataFromBundle();
        initializeAdapter();
        return view;
    }

    /**
     * Register third party library like butter knife,event bus ..
     *
     * @param view
     */
    private void registerLib(View view) {
        /**
         * Registering the ButterKnife view Injection
         */
        unbinder = ButterKnife.bind(this, view);
        /**
         * Registering the Otto bus event for Handling the notification
         * call back when user swipe to refresh the view.
         */
        GlobalBus.getBus().register(this);
    }

    /**
     * Get data from coming fragment
     */
    private void getDataFromBundle() {
        if (getArguments() != null) {
            multipleAllow = getArguments().getBoolean(Constant.MULTIPLE_SELECT);
            SDKUtils.showErrorLog("multipleAllow1111....", String.valueOf(multipleAllow));
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
        ((MultiFileSelectActivity)getActivity()).disableBackKey();
    }


    /**
     * Handling the call back of item click on grid view and pass data to Activity/Fragment
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
                    Bundle bundle = new Bundle();
                    MultiFileSelectFragment fragment = new MultiFileSelectFragment();
                    bundle.putString(Constant.KEY_NAME, name);
                    bundle.putBoolean(Constant.MULTIPLE_SELECT, multipleAllow);
                    fragment.setArguments(bundle);
                    replaceFragmentWithoutAnimation(fragment, R.id.frame_container);
                }
            });
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
        unbinder.unbind();
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
                        data.addAll(GalleryHelper.keyList);
                    } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getImageContentIntent(sharedPreferences))) {
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
                if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAnyContentIntent(sharedPreferences))) {
                    GalleryHelper.getImageFolderMap(getActivity());
                } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getImageContentIntent(sharedPreferences))) {
                    PhotoViewData.getImageFolderMap(getActivity());
                }
            return null;
        }
    }
}
