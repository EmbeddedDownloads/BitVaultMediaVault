package com.bitvault.mediavault.dashboard;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.baseclass.BaseSupportFragment;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.helper.DialogData;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.photoview.OnOutsidePhotoTapListener;
import com.bitvault.mediavault.photoview.OnPhotoTapListener;
import com.bitvault.mediavault.photoview.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by vvdn on 6/9/2017.
 */

/**
 * This class is used for show photo with pinch to zoom
 * functionality with help of photoView Library
 */
public class PhotoViewFragment extends BaseSupportFragment implements OnPhotoTapListener, OnOutsidePhotoTapListener {
    @BindView(R.id.imageView)
    PhotoView imageView;
    Unbinder unbinder;
    private ImageDataModel imageDataModel = new ImageDataModel();
    private FetchDialogDataInBackground fetchDialogDataInBackground;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.photo_view_fragment, container, false);
        /**
         * Registering the ButterKnife view Injection
         */
        unbinder = ButterKnife.bind(this, view);
        getIntentData();
        showFile();
        registerListenerAndImageScale();
        return view;
    }
    /*Register photo tap listener*/
    private void registerListenerAndImageScale() {
        imageView.setOnPhotoTapListener(this);
        /*Register OutSidePhoto tap listener*/
        imageView.setOnOutsidePhotoTapListener(this);
        imageView.setMaximumScale(8.0F);
        imageView.setMediumScale(3.0F);
    }

    @Override
    public void onResume() {
        super.onResume();
        //fetchData();
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

    /**
     * Show media file on image view
     */
    private void showFile() {
        if (imageDataModel != null && imageDataModel.getFile() != null)
            displayMedia(imageView, imageDataModel.getFile());
    }

    /**
     * Get intent data
     */
    private void getIntentData() {
        Bundle args = getArguments();
        if (getArguments() != null) {
            imageDataModel = args.getParcelable(Constant.DATA);
        }
    }

    /**
     * Show images on PhotoView with help of Glide
     *
     * @param photoView
     * @param imagePath
     */
    private void displayMedia(PhotoView photoView, File imagePath) {
        Glide.with(getContext())
                .load(imagePath.getAbsoluteFile())
                .asBitmap().signature(new StringSignature(imagePath.length()
                + "@" + imagePath.getName()))
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .animate(R.anim.fade_in)
                .into(photoView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        /**
         * Unbind  the Butter knife view injection
         */
        unbinder.unbind();
    }

    @Override
    public void onPhotoTap(ImageView view, float x, float y) {

     /* Hide/show toolbar/status while tap on photo*/
        if (getActivity() != null)
            ((PhotoViewActivity) getActivity()).toggleSystemUI();
    }

    @Override
    public void onOutsidePhotoTap(ImageView imageView) {
           /* Hide/show toolbar/status while tap outside of photo*/
        if (getActivity() != null)
            ((PhotoViewActivity) getActivity()).toggleSystemUI();
    }
}
