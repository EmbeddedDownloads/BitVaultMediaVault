package com.bitvault.mediavault.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.dashboard.MediaPlayerFragment;
import com.bitvault.mediavault.dashboard.PhotoViewActivity;
import com.bitvault.mediavault.dashboard.PhotoViewFragment;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.model.ImageDataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vvdn on 6/9/2017.
 */

public class CustomPagerAdapter extends FragmentStatePagerAdapter {
    protected Context mContext;
    private List<ImageDataModel> imageDataModels = new ArrayList<>();

    /**
     * public adapter  of this PhotoViewActivity class  for display the images in view pager
     *
     * @param fm
     * @param context
     * @param imageDataModels
     */
    public CustomPagerAdapter(FragmentManager fm, Context context, List<ImageDataModel> imageDataModels) {
        super(fm);
        this.mContext = context;
        this.imageDataModels = imageDataModels;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new Fragment();
        ImageDataModel imageDataModel = imageDataModels.get(position);
        // Attach some data to it that we'll
        // use to populate our fragment(PhotoViewFragment class) layouts
        if (FileUtils.isImageFile(imageDataModel.getFile().getAbsolutePath())) {
            fragment = new PhotoViewFragment();
            Bundle args = new Bundle();
            args.putParcelable(Constant.DATA, imageDataModel);
            fragment.setArguments(args);
        } else if (FileUtils.isVideoFile(imageDataModel.getFile().getAbsolutePath()) ||
                FileUtils.isAudioFile(imageDataModel.getFile().getAbsolutePath())) {
            fragment = new MediaPlayerFragment();
            Bundle args = new Bundle();
            args.putParcelable(Constant.DATA, imageDataModel);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return imageDataModels.size();
    }

    @Override
    public int getItemPosition(Object object) {
        int index = imageDataModels.indexOf(object);

        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    /**
     * Check the item found in list or not,if not then show message and close the activity
     */
    public void itemCheckInList() {
        if (imageDataModels.size() == 0) {
            //call finish activity when no data in folder
        //    SDKUtils.showToast(mContext, mContext.getString(R.string.no_item_found));
            ((PhotoViewActivity) mContext).finishActivity();
        }
    }

}
