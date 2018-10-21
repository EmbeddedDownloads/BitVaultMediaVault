package com.bitvault.mediavault.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.helper.AudioViewData;
import com.bitvault.mediavault.helper.GalleryHelper;
import com.bitvault.mediavault.helper.PhotoViewData;
import com.bitvault.mediavault.helper.SharedPref;
import com.bitvault.mediavault.helper.VideoViewData;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by vvdn on 6/7/2017.
 */

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.DirectoryAdapterViewHolder> {
    private ArrayList<String> data;
    private LayoutInflater inflater;
    private SharedPreferences sharedPreferences;
    private Context context;
    /**
     * Object of interface
     */
    private OnItemClickListener onItemClickListener;

    /**
     * Interface for getting call back of item click
     */
    public interface OnItemClickListener {
        void directoryFolderClick(String name);
    }

    /**
     * Register listener for item click
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(DirectoryAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * public adapter  of this Landing fragment  class  for display the media files on grid view
     *
     * @param context
     * @param data
     * @param sharedPreferences
     */
    public DirectoryAdapter(Activity context, ArrayList<String> data, SharedPreferences sharedPreferences) {
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.sharedPreferences = sharedPreferences;
        this.context = context;
    }

    @Override
    public DirectoryAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.directory_adapter, parent, false);
        return new DirectoryAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DirectoryAdapterViewHolder holder, final int position) {
        final String folderName = data.get(position);
        if (folderName != null && folderName.equalsIgnoreCase(Constant.AUDIO_FOLDER_NAME)) {
            holder.dirCount.setText("");
            holder.dirName.setText(Constant.AUDIO_TITLE);
            if (Constant.AUDIO_VIEW.equalsIgnoreCase(SharedPref.getAudioType(sharedPreferences))) {
                if (AudioViewData.imageFolderMap.get(folderName) != null && AudioViewData.imageFolderMap.get(folderName).size() > 0)
                    holder.dirCount.setText(String.valueOf(AudioViewData.imageFolderMap.get(folderName).size()));
            } else if (GalleryHelper.imageFolderMap.get(folderName) != null && GalleryHelper.imageFolderMap.get(folderName).size() > 0) {
                holder.dirCount.setText(String.valueOf(GalleryHelper.imageFolderMap.get(folderName).size()));
            }
            holder.directoryIcon.setBackgroundResource(R.drawable.ic_audiotrack);
        } else {
            holder.dirName.setText(folderName);
            holder.dirCount.setText("");
            String path = "";
            if (Constant.ALL_VIEW.equalsIgnoreCase(SharedPref.getAllType(sharedPreferences))) {
                if (GalleryHelper.imageFolderMap.get(folderName) != null && GalleryHelper.imageFolderMap.get(folderName).size() > 0) {
                    holder.dirCount.setText(String.valueOf(GalleryHelper.imageFolderMap.get(folderName).size()));
                    path = GalleryHelper.imageFolderMap.get(folderName).get(0).getPath();
                }
            } else if (Constant.VIDEO_VIEW.equalsIgnoreCase(SharedPref.getVideoType(sharedPreferences))) {
                if (VideoViewData.imageFolderMap.get(folderName) != null && VideoViewData.imageFolderMap.get(folderName).size() > 0) {
                    path = VideoViewData.imageFolderMap.get(folderName).get(0).getPath();
                    holder.dirCount.setText(String.valueOf(VideoViewData.imageFolderMap.get(folderName).size()));
                }
            } else if (Constant.PHOTO_VIEW.equalsIgnoreCase(SharedPref.getPhotoType(sharedPreferences))) {
                if (PhotoViewData.imageFolderMap.get(folderName) != null && PhotoViewData.imageFolderMap.get(folderName).size() > 0) {
                    path = PhotoViewData.imageFolderMap.get(folderName).get(0).getPath();
                    holder.dirCount.setText(String.valueOf(PhotoViewData.imageFolderMap.get(folderName).size()));
                }
            }
            Glide.with(holder.directoryIcon.getContext())
                    .load(path)
                    .asBitmap().signature(new StringSignature(new File(path).length()
                    + "@" + path))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .thumbnail(0.5f)
                    .dontAnimate()
                    .into(holder.directoryIcon);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * Item click on grid view,Check itemClickListener and folderName is not equals null
                 */
                if (DirectoryAdapter.this.onItemClickListener != null && folderName != null) {
                    onItemClickListener.directoryFolderClick(folderName);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * Check if view is recycled then clear data from directoryIcon
     *
     * @param holder
     */
    @Override
    public void onViewRecycled(DirectoryAdapterViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.clear(holder.directoryIcon);
    }

    static class DirectoryAdapterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.directory_icon)
        ImageView directoryIcon;
        @BindView(R.id.dir_name)
        TextView dirName;
        @BindView(R.id.dir_count)
        TextView dirCount;

        DirectoryAdapterViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
