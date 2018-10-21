package com.bitvault.mediavault.dialogfragment;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.helper.DialogData;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by vvdn on 7/4/2017.
 */

public class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.DialogAdapterViewHolder> {
    private ArrayList<String> data;
    private LayoutInflater inflater;
    private final SparseBooleanArray selectedItems;
    private Context context;
    /**
     * Object of interface
     */
    private OnItemClickListener onItemClickListener;

    /**
     * Interface for getting call back of item click
     */
    public interface OnItemClickListener {
        void directoryFolderClick(File file, int adapterPosition);
    }

    /**
     * Register listener for item click
     *
     * @param onItemClickListener
     */
    public void setOnItemClickListener(DialogAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * public adapter  of this Landing fragment  class  for display the media files on grid view
     *
     * @param context
     * @param data
     */
    public DialogAdapter(Activity context, ArrayList<String> data) {
        inflater = LayoutInflater.from(context);
        this.data = data;
        this.context = context;
        this.selectedItems = new SparseBooleanArray();
    }

    @Override
    public DialogAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.dialog_adapter, parent, false);
        return new DialogAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DialogAdapterViewHolder holder, final int position) {
        final String folderName = data.get(position);
        boolean selected = selectedItems.get(position);
        if (folderName != null)
            holder.dirName.setText(folderName);
        if (selected) {
            holder.image_selected.setVisibility(View.VISIBLE);
            //  holder.dir_holder.setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent));
            //  holder.dir_holder.setAlpha(0.6f);
        } else {
            holder.image_selected.setVisibility(View.GONE);
            //holder.dir_holder.setBackgroundColor(0);
            // holder.dir_holder.setAlpha(1.0f);
        }
        if (DialogData.imageFolderMap.get(folderName) != null) {
            holder.dirCount.setText(String.valueOf(DialogData.imageFolderMap.get(folderName).size()));
            Glide.with(holder.directoryIcon.getContext())
                    .load(DialogData.imageFolderMap.get(folderName).get(0).getPath())
                    .asBitmap().signature(new StringSignature(new File(DialogData.imageFolderMap.get(folderName).get(0).getPath()).length()
                    + "@" + DialogData.imageFolderMap.get(folderName).get(0).getPath()))
                    .centerCrop().error(R.drawable.ic_audiotrack)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .thumbnail(0.5f)
                    .animate(R.anim.fade_in)
                    .into(holder.directoryIcon);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * Item click on grid view,Check itemClickListener and folderName is not equals null
                 */
                if (DialogAdapter.this.onItemClickListener != null && folderName != null) {
                    onItemClickListener.directoryFolderClick(DialogData.imageFolderMap.get(folderName).get(0).getFile(), holder.getAdapterPosition());
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
    public void onViewRecycled(DialogAdapterViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.clear(holder.directoryIcon);
    }

    static class DialogAdapterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.directory_icon)
        ImageView directoryIcon;
        @BindView(R.id.image_selected)
        ImageView image_selected;
        @BindView(R.id.dir_name)
        TextView dirName;
        @BindView(R.id.dir_count)
        TextView dirCount;

        DialogAdapterViewHolder(View view) {
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
        clearSelection();
        selectedItems.append(position, true);
        notifyItemChanged(position);
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
     * Clear the selected list if any action perform like delete.
     */
    public void clearSelection() {
        ArrayList<Integer> selectedPositions = getSelectedPositions();
        selectedItems.clear();
        for (int i : selectedPositions) notifyItemChanged(i);
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
}

