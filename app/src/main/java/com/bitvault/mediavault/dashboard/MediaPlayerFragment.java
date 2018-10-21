package com.bitvault.mediavault.dashboard;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.baseclass.BaseSupportFragment;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.model.ImageDataModel;
import com.halilibo.bettervideoplayer.BetterVideoCallback;
import com.halilibo.bettervideoplayer.BetterVideoPlayer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * This class is responsible for playing  audio/video type of files.
 */
public class MediaPlayerFragment extends BaseSupportFragment implements BetterVideoCallback {

    @BindView(R.id.image_audio)
    ImageView imageAudio;
    @BindView(R.id.media_view)
    BetterVideoPlayer player;
    @BindView(R.id.activity_media_player)
    FrameLayout activityMediaPlayer;
    Unbinder unbinder;
    private boolean isFragmentVisible = false;
    private ImageDataModel imageDataModel = new ImageDataModel();
    Toolbar toolbar;

    /**
     * Check the fragment visibility
     *
     * @param isVisibleToUser is give the fragment visibility in boolean format.
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        // Make sure that we are currently visible
        if (!isVisibleToUser) {
            isFragmentVisible = false;
            /**
             * If fragment is visible then pause  media player and
             * start player on manually  click on play button.
             */
            if (player != null && player.isPlaying())
                player.pause();
        } else {
            isFragmentVisible = true;
            if (player != null)
                player.showControls();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFragmentVisible) {
            if (player != null && !player.isPlaying()) {
                player.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_media_player, container, false);
        /**
         * Registering the ButterKnife view Injection
         */
        unbinder = ButterKnife.bind(this, view);
        getDataFromIntent();
        initAndSetDataOnPlayer();
        return view;
    }

    /**
     * Initialising the player and data on player and show control
     */
    private void initAndSetDataOnPlayer() {
        if (imageDataModel != null && imageDataModel.getFile().getAbsolutePath() != null) {
            if (FileUtils.isAudioFile(imageDataModel.getFile().getAbsolutePath())) {
                imageAudio.setVisibility(View.VISIBLE);
                player.setBackground(null);
            } else {
                imageAudio.setVisibility(View.INVISIBLE);
            }
            player.setCallback(this);

            // To play files, you can use Uri.fromFile(new File("..."))
            player.setSource(Uri.fromFile(imageDataModel.getFile()));
            if (isFragmentVisible) {
                if (player != null) {
                    player.setAutoPlay(true);
                    player.showControls();
                }
            }
        }
    }

    /**
     * Get data from intent and set ite to model
     */
    private void getDataFromIntent() {
        Bundle args = getArguments();
        if (getArguments() != null) {
            imageDataModel = args.getParcelable(Constant.DATA);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //   Make sure the player stops playing if the user presses the home button.
        if (player != null)
            player.pause();
    }

    // Methods for the implemented BetterVideoPlayerCallback

    @Override
    public void onStarted(BetterVideoPlayer player) {

    }

    @Override
    public void onPaused(BetterVideoPlayer player) {
    }

    @Override
    public void onPreparing(BetterVideoPlayer player) {
    }

    @Override
    public void onPrepared(BetterVideoPlayer player) {

    }

    @Override
    public void onBuffering(int percent) {
    }

    @Override
    public void onError(BetterVideoPlayer player, Exception e) {
    }

    @Override
    public void onCompletion(BetterVideoPlayer player) {
    }

    @Override
    public void onToggleControls(BetterVideoPlayer player, boolean isShowing) {
        if (isShowing) {
            if (getActivity() != null)
                ((PhotoViewActivity) getActivity()).showSystemUI();
        }
        if (!isShowing) {
            if (getActivity() != null)
                ((PhotoViewActivity) getActivity()).hideSystemUI();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
            player.release();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        /**
         *if player is not null the release the media player instance.
         */
        if (player != null) {
            player.stop();
            player.release();
        }

        /**
         * Unbind  the Butter knife view injection
         */
        unbinder.unbind();
    }
}
