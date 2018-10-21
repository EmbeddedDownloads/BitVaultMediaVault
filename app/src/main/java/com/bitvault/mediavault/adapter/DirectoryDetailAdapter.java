package com.bitvault.mediavault.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.dashboard.DirectoryDetailActivity;
import com.bitvault.mediavault.database.MediaFileDuration;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.helper.SquareLayout;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import utils.SDKUtils;

/**
 * Created by vvdn on 6/8/2017.
 */

public class DirectoryDetailAdapter extends RecyclerView.Adapter<DirectoryDetailAdapter.DetailsViewHolder> {

    private ArrayList<ImageDataModel> data = new ArrayList<>();
    private ArrayList<ImageDataModel> adapterData;
    private LayoutInflater inflater;
    private final SparseBooleanArray selectedItems;
    private final static int FADE_DURATION = 500; // in milliseconds
    private ArrayList<Integer> UndoFileList = new ArrayList<>();
    private LoadFileDuration loadFileDuration;
    private MediaFileDuration mediaFileDuration;
    /**
     * Context  of DirectoryDetailActivity class
     */
    private Context context;
    /**
     * Object of interface
     */
    private OnItemClickListener onItemClickListener;
    private OnLongItemClickListener longItemClickListener;

    /**
     * Interface for getting call back of item click
     */
    public interface OnItemClickListener {
        public void itemClick(ImageDataModel model, int position);
    }

    public interface OnLongItemClickListener {
        public void itemLongClick(ImageDataModel model, int position);
    }

    /**
     * Register listener for item click
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnLongItemClickListener(OnLongItemClickListener onLongItemClickListener) {
        this.longItemClickListener = onLongItemClickListener;
    }

    /**
     * public adapter  of this DirectoryDetailActivity  class  for display the media files on grid view
     *
     * @param context
     * @param data
     * @param mediaFileDuration
     */
    public DirectoryDetailAdapter(Activity context, ArrayList<ImageDataModel> data, MediaFileDuration mediaFileDuration) {
        this.context = context;
        this.mediaFileDuration = mediaFileDuration;
        inflater = LayoutInflater.from(this.context);
        this.data = data;
        adapterData = new ArrayList<>();
        this.selectedItems = new SparseBooleanArray();
        updateFileDurationInBackground();
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

    @Override
    public DetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.directory_detail_adapter, parent, false);
        return new DetailsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DetailsViewHolder holder, final int position) {
        final ImageDataModel imageDataModel = adapterData.get(position);
        boolean selected = selectedItems.get(position);
        if (imageDataModel.getFile().getAbsolutePath() != null) {
            if (FileUtils.isAudioFile(imageDataModel.getFile().getAbsolutePath())) {
                if (selected) {
                    holder.imageSelected.setVisibility(View.VISIBLE);
                } else {
                    holder.imageSelected.setVisibility(View.GONE);
                }
                holder.audioSquareLayout.setVisibility(View.VISIBLE);
                holder.image_squareLayout.setVisibility(View.GONE);
                holder.videoSquareLayout.setVisibility(View.GONE);
                String path = imageDataModel.getFile().getAbsolutePath();
                path = path.replaceAll("'", "''");
                if (mediaFileDuration.getTimeDuration(path) != null)
                    holder.audioTime.setText((mediaFileDuration.getTimeDuration(path)));
            } else if (FileUtils.isVideoFile(imageDataModel.getFile().getAbsolutePath())) {
                holder.audioSquareLayout.setVisibility(View.GONE);
                holder.videoSquareLayout.setVisibility(View.VISIBLE);
                holder.image_squareLayout.setVisibility(View.GONE);
                if (selected) {
                    holder.imageSelected.setVisibility(View.VISIBLE);
                } else {
                    holder.imageSelected.setVisibility(View.GONE);
                }
                String path = imageDataModel.getFile().getAbsolutePath();
                path = path.replaceAll("'", "''");
                if (mediaFileDuration.getTimeDuration(path) != null)
                    holder.videoTime.setText((mediaFileDuration.getTimeDuration(path)));
                holder.fileName.setText(imageDataModel.getFile().getName());
                Glide.with(holder.videoThumbnail.getContext())
                        .load(imageDataModel.getFile().getAbsolutePath())
                        .asBitmap().signature(new StringSignature(imageDataModel.getFile().length()
                        + "@" + imageDataModel.getFile().getName()))
                        .placeholder(R.drawable.video_img).error(R.drawable.video_img)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .thumbnail(0.5f)
                        .animate(R.anim.fade_in)
                        .into(holder.videoThumbnail);
            } else {
                holder.audioSquareLayout.setVisibility(View.GONE);
                holder.videoSquareLayout.setVisibility(View.GONE);
                holder.image_squareLayout.setVisibility(View.VISIBLE);
                if (selected) {
                    holder.imageSelected.setVisibility(View.VISIBLE);
                } else {
                    holder.imageSelected.setVisibility(View.GONE);
                }
                holder.fileName.setText(imageDataModel.getFile().getName());
                Glide.with(holder.imageThumbnail.getContext())
                        .load(imageDataModel.getFile().getAbsolutePath())
                        .asBitmap().signature(new StringSignature(imageDataModel.getFile().length()
                        + "@" + imageDataModel.getFile().getName()))
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .thumbnail(0.5f)
                        .animate(R.anim.fade_in)
                        .into(holder.imageThumbnail);
            }


        }

        // Set the view to fade in
        //  setScaleAnimation(holder.itemView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * Item click on grid view,Check itemClickListener,position and imageDataModel is not equals null
                 */
                if (DirectoryDetailAdapter.this.onItemClickListener != null && imageDataModel != null) {
                    onItemClickListener.itemClick(imageDataModel, holder.getAdapterPosition());
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                /**
                 * On Long Item click on grid view,Check itemClickListener,position and imageDataModel is not equals null
                 */
                if (DirectoryDetailAdapter.this.onItemClickListener != null && imageDataModel != null) {
                    longItemClickListener.itemLongClick(imageDataModel, holder.getAdapterPosition());
                }
                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        return adapterData.size();
    }

    /**
     * Check if view is recycled then clear data from directoryIcon
     *
     * @param holder
     */
    @Override
    public void onViewRecycled(DetailsViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.clear(holder.imageThumbnail);
    }

    static class DetailsViewHolder extends RecyclerView.ViewHolder {
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


        DetailsViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    /**
     * on item long click/single click update the click position into SparseBooleanArray (selectedItems)
     * and then notify that item position by notifyItemChanged(position);
     *
     * @param position item notify position
     */
    public void toggle(int position) {

        if (getSelected(position)) selectedItems.delete(position);

        else selectedItems.append(position, true);

        notifyItemChanged(position);

    }

    /**
     * check that nay item is selected or not
     *
     * @return
     */
    public boolean anySelected() {

        return selectedItems.size() > 0;
    }

    /**
     * This method return boolean that will check that if any item is
     * selected already then deselected then else select them.
     *
     * @param position item position which is click
     * @return
     */
    private boolean getSelected(int position) {

        return selectedItems.get(position);
    }

    /**
     * return the selected item count
     *
     * @return
     */
    public int getSelectedItemCount() {

        return selectedItems.size();
    }

    /**
     * update adapter  with  selected list item position when user rotate the device and
     *
     * @param positions
     */
    public void select(ArrayList<Integer> positions) {

        selectedItems.clear();

        for (int i : positions) {

            selectedItems.append(i, true);

            notifyItemChanged(i);
        }

    }

    /**
     * Get the selected list item position when user rotate the device
     *
     * @return
     */
    public ArrayList<Integer> getSelectedPositions() {

        ArrayList<Integer> list = new ArrayList<>();

        for (int i = 0; i < getItemCount(); i++) {

            if (getSelected(i)) list.add(i);
        }

        return list;
    }

    /**
     * Clear the selected list if any action perform like delete.
     */
    public void clearSelection() {
        ArrayList<Integer> selectedPositions = getSelectedPositions();
        selectedItems.clear();
        for (int i : selectedPositions) notifyItemChanged(i);
    }

    /**
     * Get selected item Model data as list
     *
     * @return
     */
    public ArrayList<ImageDataModel> getSelectedItems() {

        ArrayList<ImageDataModel> list = new ArrayList<>();

        for (int i = 0; i < getItemCount(); i++) {

            if (getSelected(i)) list.add(get(i));
        }

        return list;
    }

    /**
     * Get selected item Model data as list
     *
     * @param index its the index of list position
     * @return model data
     */
    public ImageDataModel get(int index) {

        return adapterData.get(index);
    }

    /**
     * Update adapter  w.r.t. position when selected files is deleted
     *
     * @param files
     */
    public void removeFilesNotifyList(ArrayList<ImageDataModel> files) {
        UndoFileList.clear();
        selectedItems.clear();
        for (ImageDataModel file : files) {
            int count = adapterData.indexOf(file);
            UndoFileList.add(count);
            adapterData.remove(file);
            notifyItemRemoved(count);
        }

    }

    /**
     * Update adapter  w.r.t. position when selected file is deleted
     *
     * @param file
     */
    public void removeFileNotifyList(ImageDataModel file) {
        UndoFileList.clear();
        selectedItems.clear();
        int count = adapterData.indexOf(file);
        UndoFileList.add(count);
        adapterData.remove(file);
        notifyItemRemoved(count);

    }

    /**
     * Check the item found in list or not,if not then show message and close the activity
     */
    public void itemCheckInList() {
        if (adapterData.size() == 0) {
            //call finish activity when no data in folder
            SDKUtils.showToast(context, context.getString(R.string.no_item_found));
            ((DirectoryDetailActivity) context).finishActivity();
        }
    }

    /**
     * After click on snackbar undo button notify adapter w.r.t. their position
     *
     * @param files
     */
    public void undoFileNotifyList(ArrayList<ImageDataModel> files) {
        for (int i = files.size() - 1; i >= 0; i--) {
            adapterData.add(UndoFileList.get(i), files.get(i));
            notifyItemInserted(UndoFileList.get(i));
        }
        UndoFileList.clear();
    }

    /**
     * Scale the image view with respect to their position
     *
     * @param view
     */
    private void setScaleAnimation(View view) {
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(FADE_DURATION);
        view.startAnimation(anim);
    }

    /**
     * Async task for loading time duration of each file in background and update the local database
     */
    private class LoadFileDuration extends AsyncTask<ArrayList<ImageDataModel>, ImageDataModel, ArrayList<ImageDataModel>> {
        @Override
        protected ArrayList<ImageDataModel> doInBackground(ArrayList<ImageDataModel>... params) {
            for (int i = 0; i < data.size(); i++) {
                if (isCancelled()) {
                    break;
                }
                //   else {
                ImageDataModel model = data.get(i);
                if (!FileUtils.isImageFile(model.getFile().getAbsolutePath()))
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
                publishProgress(model);
                //  }
            }
            return data;

        }

        @Override
        protected void onCancelled() {
            SDKUtils.showErrorLog("onCancelled..", "..Calling");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<ImageDataModel> videoModels) {
            super.onPostExecute(videoModels);
            adapterData = data;
            notifyDataSetChanged();
        }

        @Override
        protected void onProgressUpdate(ImageDataModel... values) {
            super.onProgressUpdate(values);
            adapterData.add(values[0]);
            notifyItemInserted(adapterData.size() - 1);

        }
    }

    /**
     * Method to stop async task if activity is destroyed
     */
    public void stopAsyncTask() {
        if (loadFileDuration != null && loadFileDuration.getStatus() != AsyncTask.Status.FINISHED) {
            loadFileDuration.cancel(true);
        }

    }
}