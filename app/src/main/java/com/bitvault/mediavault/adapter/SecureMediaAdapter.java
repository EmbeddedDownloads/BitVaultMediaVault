package com.bitvault.mediavault.adapter;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.application.MediaVaultController;
import com.bitvault.mediavault.database.MediaFileDuration;
import com.bitvault.mediavault.database.MediaVaultLocalDb;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.tabs.MediaVaultSecureTab;
import com.bitvault.mediavault.viewholders.CountHeaderViewHolder;
import com.bitvault.mediavault.viewholders.CountSecureItemViewHolder;
import com.zhukic.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import java.util.ArrayList;

import utils.SDKUtils;

/**
 * Created by vvdn on 9/20/2017.
 */

public class SecureMediaAdapter extends SectionedRecyclerViewAdapter<CountHeaderViewHolder, CountSecureItemViewHolder> {
    private ArrayList<ImageDataModel> dataModelArrayList;
    protected MediaVaultSecureTab context = null;
    private MediaFileDuration mediaFileDuration;
    private ArrayList<ImageDataModel> dataModels = new ArrayList<>();
    private final SparseBooleanArray selectedItems;
    private MediaVaultLocalDb secureMediaFileDb;

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

    public SecureMediaAdapter(MediaVaultSecureTab context, ArrayList<ImageDataModel> dataModels, MediaFileDuration mediaFileDuration, MediaVaultLocalDb secureMediaDb) {
        this.context = context;
        this.dataModelArrayList = dataModels;
        this.mediaFileDuration = mediaFileDuration;
        this.secureMediaFileDb = secureMediaDb;
        this.selectedItems = new SparseBooleanArray();
    }

    @Override
    public boolean onPlaceSubheaderBetweenItems(int position) {
        final ImageDataModel data = dataModelArrayList.get(position);
        final ImageDataModel nextData = dataModelArrayList.get(position + 1);
        return !FileUtils.getDate(data.getFile()).equals(FileUtils.getDate(nextData.getFile()));
    }

    @Override
    public CountHeaderViewHolder onCreateSubheaderViewHolder(ViewGroup parent, int viewType) {
        return new CountHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_header, parent, false));
    }

    @Override
    public CountSecureItemViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new CountSecureItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_adapter, parent, false));
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
    public void onBindItemViewHolder(CountSecureItemViewHolder holder, final int position) {

        if (dataModelArrayList != null && dataModelArrayList.size() > 0)
            try {
                boolean selected = selectedItems.get(position);
                holder.setDataOnViewHolder(dataModelArrayList.get(position), mediaFileDuration,
                        selected, secureMediaFileDb);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /**
                         * Item click on grid view,Check itemClickListener
                         */
                        if (onItemClickListener != null) {
                            if (context.progressBarVisibilityStatus()) {
                                if (dataModelArrayList != null && dataModelArrayList.get(position) != null)
                                    onItemClickListener.itemClickedPosition(dataModelArrayList.get(position), position);
                            } else
                                SDKUtils.showToast(MediaVaultController.getInstance(), context.getString(R.string.please_wait));

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
                            if (context.progressBarVisibilityStatus()) {
                                if (dataModelArrayList != null && dataModelArrayList.get(position) != null)
                                    onItemLongClickListener.itemLongClickedPosition(dataModelArrayList.get(position), position);
                            } else
                                SDKUtils.showToast(MediaVaultController.getInstance(), context.getString(R.string.please_wait));
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
}