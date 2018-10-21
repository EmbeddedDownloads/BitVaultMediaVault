package com.bitvault.mediavault.viewholders;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.database.MediaFileDuration;
import com.bitvault.mediavault.database.MediaVaultLocalDb;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.helper.SquareLayout;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by vvdn on 9/21/2017.
 */

public class CountSecureItemViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.imageThumbnail)
    ImageView imageThumbnail;
    @BindView(R.id.videoThumbnail)
    ImageView videoThumbnail;
    @BindView(R.id.image_selected)
    ImageView imageSelected;
    @BindView(R.id.file_name)
    TextView fileName;
    @BindView(R.id.audioTime)
    TextView audioTime;
    @BindView(R.id.videoTime)
    TextView videoTime;
    @BindView(R.id.audioSquareLayout)
    SquareLayout audioSquareLayout;
    @BindView(R.id.image_squareLayout)
    SquareLayout image_squareLayout;
    @BindView(R.id.videoSquareLayout)
    SquareLayout videoSquareLayout;

    public CountSecureItemViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void setDataOnViewHolder(ImageDataModel imageDataModel, MediaFileDuration mediaFileDuration,
                                    boolean selected, MediaVaultLocalDb secureMediaFileDb) {
        if (imageDataModel.getFile().getAbsolutePath() != null) {
            if (FileUtils.isAudioFile(imageDataModel.getFile().getAbsolutePath())) {
                if (selected) {
                    imageSelected.setVisibility(View.VISIBLE);
                } else {
                    imageSelected.setVisibility(View.GONE);
                }
                // Set for archive status
                //Checking the status of file and update the list according the status.
                if (imageDataModel.getFileStatus() != null &&
                        imageDataModel.getFileStatus().equalsIgnoreCase(Constant.FILE_ARCHIVE)) {
                    audioSquareLayout.setVisibility(View.GONE);
                    videoSquareLayout.setVisibility(View.GONE);
                    image_squareLayout.setVisibility(View.VISIBLE);
                    imageThumbnail.setImageResource(R.mipmap.ic_download);
                } else if (imageDataModel.getFileStatus() != null &&
                        imageDataModel.getFileStatus().equalsIgnoreCase(Constant.STATUS_FAILED_PBC)) {
                    audioSquareLayout.setVisibility(View.GONE);
                    videoSquareLayout.setVisibility(View.GONE);
                    image_squareLayout.setVisibility(View.VISIBLE);
                    imageThumbnail.setImageResource(R.mipmap.ic_upload);
                } else {
                    audioSquareLayout.setVisibility(View.VISIBLE);
                    image_squareLayout.setVisibility(View.GONE);
                    videoSquareLayout.setVisibility(View.GONE);
                    String path = imageDataModel.getFile().getAbsolutePath();
                    path = path.replaceAll("'", "''");
                    if (mediaFileDuration.getTimeDuration(path) != null)
                        audioTime.setText((mediaFileDuration.getTimeDuration(path)));
                }


            } else if (FileUtils.isVideoFile(imageDataModel.getFile().getAbsolutePath())) {
                if (selected) {
                    imageSelected.setVisibility(View.VISIBLE);
                } else {
                    imageSelected.setVisibility(View.GONE);
                }
                if (imageDataModel.getFileStatus() != null &&
                        imageDataModel.getFileStatus().equalsIgnoreCase(Constant.FILE_ARCHIVE)) {
                    audioSquareLayout.setVisibility(View.GONE);
                    videoSquareLayout.setVisibility(View.GONE);
                    image_squareLayout.setVisibility(View.VISIBLE);
                    imageThumbnail.setImageResource(R.mipmap.ic_download);
                } else if (imageDataModel.getFileStatus() != null &&
                        imageDataModel.getFileStatus().equalsIgnoreCase(Constant.STATUS_FAILED_PBC)) {
                    audioSquareLayout.setVisibility(View.GONE);
                    videoSquareLayout.setVisibility(View.GONE);
                    image_squareLayout.setVisibility(View.VISIBLE);
                    imageThumbnail.setImageResource(R.mipmap.ic_upload);
                } else {
                    audioSquareLayout.setVisibility(View.GONE);
                    videoSquareLayout.setVisibility(View.VISIBLE);
                    image_squareLayout.setVisibility(View.GONE);
                    String path = imageDataModel.getFile().getAbsolutePath();
                    path = path.replaceAll("'", "''");
                    if (mediaFileDuration.getTimeDuration(path) != null)
                        videoTime.setText((mediaFileDuration.getTimeDuration(path)));
                    Glide.with(videoThumbnail.getContext())
                            .load(imageDataModel.getFile().getAbsolutePath())
                            .asBitmap().signature(new StringSignature(imageDataModel.getFile().length()
                            + "@" + imageDataModel.getFile().getName()))
                            .placeholder(R.drawable.video_img).error(R.drawable.video_img)
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .thumbnail(0.5f)
                            .animate(R.anim.fade_in)
                            .into(videoThumbnail);
                }

            } else {
                audioSquareLayout.setVisibility(View.GONE);
                videoSquareLayout.setVisibility(View.GONE);
                image_squareLayout.setVisibility(View.VISIBLE);
                if (selected) {
                    imageSelected.setVisibility(View.VISIBLE);
                } else {
                    imageSelected.setVisibility(View.GONE);
                }
                //Checking the status of file and update the list according the status.
                if (imageDataModel.getFileStatus() != null &&
                        imageDataModel.getFileStatus().equalsIgnoreCase(Constant.FILE_ARCHIVE)) {
                    imageThumbnail.setImageResource(R.mipmap.ic_download);
                } else if (imageDataModel.getFileStatus() != null &&
                        imageDataModel.getFileStatus().equalsIgnoreCase(Constant.STATUS_FAILED_PBC)) {
                    imageThumbnail.setImageResource(R.mipmap.ic_upload);
                } else {
                    Glide.with(imageThumbnail.getContext())
                            .load(imageDataModel.getFile().getAbsolutePath())
                            .asBitmap().signature(new StringSignature(imageDataModel.getFile().length()
                            + "@" + imageDataModel.getFile().getName()))
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT)
                            .thumbnail(0.5f)
                            .animate(R.anim.fade_in)
                            .into(imageThumbnail);
                }

            }
        }

    }
}
