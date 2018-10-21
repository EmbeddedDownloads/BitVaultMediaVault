package com.bitvault.mediavault.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.database.MediaFileDuration;
import com.bitvault.mediavault.database.MediaVaultLocalDb;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.viewholders.CountHeaderViewHolder;
import com.bitvault.mediavault.viewholders.CountItemViewHolder;
import com.zhukic.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.util.ArrayList;

/**
 * Created by vvdn on 8/22/2017.
 */

public class ListViewAdapterNew extends SectionedRecyclerViewAdapter<CountHeaderViewHolder, CountItemViewHolder> {
    private ArrayList<ImageDataModel> dataModelArrayList;
    protected Context context = null;
    private MediaFileDuration mediaFileDuration;
    private ArrayList<ImageDataModel> dataModels = new ArrayList<>();
    private final SparseBooleanArray selectedItems;
    MediaVaultLocalDb secureMediaFileDb;
    // private ArrayList<ImageDataModel> adapterData;
    // private LoadDataInBackground loadDataInBackground;

    /**
     * Interface for getting call back of item click
     */
    public interface OnItemClickListener {
        void itemClickedPosition(ImageDataModel dataModel, int position);
    }

    /**
     * Interface for getting call back of item long click
     */
    public interface OnItemLongClickListener {
        void itemLongClickedPosition(ImageDataModel dataModel, int position);
    }

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    /**
     * Register listener for item click
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * Register listener for item long click
     *
     * @param onItemLongClickListener
     */
    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public ListViewAdapterNew(Context context, ArrayList<ImageDataModel> dataModels, MediaFileDuration mediaFileDuration, MediaVaultLocalDb secureMediaFileDb) {
        this.context = context;
        this.dataModelArrayList = dataModels;
        this.mediaFileDuration = mediaFileDuration;
        this.secureMediaFileDb = secureMediaFileDb;
        this.selectedItems = new SparseBooleanArray();
        //  adapterData = new ArrayList<>();
//        if (loadDataInBackground != null && loadDataInBackground.getStatus() != AsyncTask.Status.FINISHED)
//            loadDataInBackground.cancel(true);
//        loadDataInBackground = new LoadDataInBackground();
//        loadDataInBackground.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onPlaceSubheaderBetweenItems(int position) {
        if (dataModelArrayList != null && dataModelArrayList.size() > 0) {
            final ImageDataModel data = dataModelArrayList.get(position);
            final ImageDataModel nextData = dataModelArrayList.get(position + 1);
            return !FileUtils.getDate(data.getFile()).equals(FileUtils.getDate(nextData.getFile()));
        }
        return false;
    }

    @Override
    public CountHeaderViewHolder onCreateSubheaderViewHolder(ViewGroup parent, int viewType) {
        return new CountHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_header, parent, false));
    }

    @Override
    public CountItemViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new CountItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_adapter, parent, false));
    }

    @Override
    public void onBindSubheaderViewHolder(CountHeaderViewHolder subheaderViewHolder, int nextItemPosition) {

        //Setup subheader view
        //nextItemPosition - position of the first item in the section to which this subheader belongs
        if (dataModelArrayList != null && dataModelArrayList.size() > 0) {
            final ImageDataModel dataModel = dataModelArrayList.get(nextItemPosition);
            if (dataModel.getFile() != null)
                subheaderViewHolder.render(FileUtils.getDate(dataModel.getFile()));
        }

    }

    @Override
    public int getItemSize() {
        return dataModelArrayList.size();
    }

    @Override
    public void onBindItemViewHolder(CountItemViewHolder holder, final int position) {

        if (dataModelArrayList != null && dataModelArrayList.size() > 0)
            try {
                boolean selected = selectedItems.get(position);
                holder.setDataOnViewHolder(dataModelArrayList.get(position), mediaFileDuration, selected, secureMediaFileDb);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /**
                         * Item click on grid view,Check itemClickListener
                         */
                        if (onItemClickListener != null) {
                            onItemClickListener.itemClickedPosition(dataModelArrayList.get(position), position);
                        }
                    }
                });
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        /**
                         * Long Item click on grid view,Check onItemLongClickListener
                         */
                        if (onItemLongClickListener != null) {
                            onItemLongClickListener.itemLongClickedPosition(dataModelArrayList.get(position), position);
                        }
                        return true;
                    }
                });
            } catch (Exception e) {
                System.out.print(e);
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

        notifyItemChangedAtPosition(position);

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
     * Clear the selected list if any action perform like delete.
     */
    public void clearSelection() {
        ArrayList<Integer> selectedPositions = getSelectedPositions();
        selectedItems.clear();
        for (int i : selectedPositions) notifyItemChangedAtPosition(i);
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
     * check that nay item is selected or not
     *
     * @return
     */
    public boolean anySelected() {

        return selectedItems.size() > 0;
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

        return dataModelArrayList.get(index);
    }

    /**
     * Update adapter  w.r.t. position when selected files is deleted
     *
     * @param files
     */
    public void removeFilesNotifyList(ArrayList<ImageDataModel> files) {
        selectedItems.clear();
        for (ImageDataModel file : files) {
            int count = dataModelArrayList.indexOf(file);
            dataModelArrayList.remove(file);
            notifyItemRemovedAtPosition(count);
        }

    }

    /**
     * Update adapter  w.r.t. position when selected file is deleted
     *
     * @param file
     */
    public void removeFileNotifyView(ImageDataModel file) {
        selectedItems.clear();
        int count = dataModelArrayList.indexOf(file);
        dataModelArrayList.remove(file);
        notifyItemRemovedAtPosition(count);
    }

    /**
     * notify list after renaming the file.
     *
     * @param file model data which is changed
     */
    public void notifyItemAfterRename(ImageDataModel file) {
        int count = dataModelArrayList.indexOf(file);
        notifyItemChangedAtPosition(count);
    }
/*
    *//**
     * Async task for loading time duration of each file in background and update the local database
     *//*
    private class LoadDataInBackground extends AsyncTask<ArrayList<ImageDataModel>, ImageDataModel, ArrayList<ImageDataModel>> {
        @Override
        protected ArrayList<ImageDataModel> doInBackground(ArrayList<ImageDataModel>... params) {
            for (int i = 0; i < dataModelArrayList.size(); i++) {
                ImageDataModel model = dataModelArrayList.get(i);
                if (!model.isImage()) {
                    if (FileUtils.getDuration(model.getFile()) != null) {
                        String path = model.getFile().getAbsolutePath();
                        path = path.replaceAll("'", "''");
                        // Check the file is already exists into database or not
                        if (!mediaFileDuration.checkFileStatus(path)) {
                            model.setTimeDuration(FileUtils.getDuration(model.getFile()));
                            // Method to insert file path and time duration into local dadabase
                            mediaFileDuration.insertMediaTime(model);
                        }
                    }
                }
                publishProgress(model);
            }
            return dataModelArrayList;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<ImageDataModel> videoModels) {
            super.onPostExecute(videoModels);
            dataModelArrayList = videoModels;
            notifyDataChanged();
        }

        @Override
        protected void onProgressUpdate(ImageDataModel... values) {
            super.onProgressUpdate(values);
            dataModelArrayList.add(values[0]);
            notifyItemInsertedAtPosition(dataModelArrayList.size() - 1);

        }
    }*/
}