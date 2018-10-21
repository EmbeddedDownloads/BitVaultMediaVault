/*
 * Copyright (C) 2015 Tomás Ruiz-López.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bitvault.mediavault.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitvault.mediavault.R;
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
 * Created by tomas on 15/07/15.
 */
public class CountItemViewHolder extends RecyclerView.ViewHolder {

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

    public CountItemViewHolder(View view) {
        super(view);
        ButterKnife.bind(this, view);
    }

    public void setDataOnViewHolder(ImageDataModel imageDataModel, MediaFileDuration mediaFileDuration, boolean selected, MediaVaultLocalDb secureMediaFileDb) {
        if (imageDataModel.getFile().getAbsolutePath() != null) {
            // Hide file if file is in MediaVaultLocalDb db.
         //   if (!secureMediaFileDb.checkFileStatus(imageDataModel.getFile().getAbsolutePath())) {
                if (FileUtils.isAudioFile(imageDataModel.getFile().getAbsolutePath())) {
                    if (selected) {
                        imageSelected.setVisibility(View.VISIBLE);
                    } else {
                        imageSelected.setVisibility(View.GONE);
                    }
                    audioSquareLayout.setVisibility(View.VISIBLE);
                    image_squareLayout.setVisibility(View.GONE);
                    videoSquareLayout.setVisibility(View.GONE);
                    String path = imageDataModel.getFile().getAbsolutePath();
                    path = path.replaceAll("'", "''");
                    if (mediaFileDuration.getTimeDuration(path) != null)
                        audioTime.setText((mediaFileDuration.getTimeDuration(path)));
                } else if (FileUtils.isVideoFile(imageDataModel.getFile().getAbsolutePath())) {
                    audioSquareLayout.setVisibility(View.GONE);
                    videoSquareLayout.setVisibility(View.VISIBLE);
                    image_squareLayout.setVisibility(View.GONE);
                    if (selected) {
                        imageSelected.setVisibility(View.VISIBLE);
                    } else {
                        imageSelected.setVisibility(View.GONE);
                    }
                    String path = imageDataModel.getFile().getAbsolutePath();
                    path = path.replaceAll("'", "''");
                    if (mediaFileDuration.getTimeDuration(path) != null)
                        videoTime.setText((mediaFileDuration.getTimeDuration(path)));
                    //holder.fileName.setText(imageDataModel.getFile().getName());
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
                } else {
                    audioSquareLayout.setVisibility(View.GONE);
                    videoSquareLayout.setVisibility(View.GONE);
                    image_squareLayout.setVisibility(View.VISIBLE);
                    if (selected) {
                        imageSelected.setVisibility(View.VISIBLE);
                    } else {
                        imageSelected.setVisibility(View.GONE);
                    }
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
           // }
        }
    }
}
