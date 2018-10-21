package com.bitvault.mediavault.helper;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.ottonotification.LandingFragmentNotification;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by vvdn on 9/1/2017.
 */

public class PhotoViewDataOnIdBasis {
    private static String[] projection = {"*"};
    public static ArrayList<ImageDataModel> dataModelArrayList = new ArrayList<>();

    /**
     * Getting All Media Path with folder name .
     *
     * @param activity the activity
     * @return ArrayList with images Path
     */
    /**
     */
    public static ArrayList<ImageDataModel> getMediaFilesOnIdBasis(Activity activity, String id) {
        dataModelArrayList.clear();

        /**
         * Fetching image type of files through content provider on basis of BucketId
         */
        fetchImages(activity, id);
//Send notification through main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                GlobalBus.getBus().post(new LandingFragmentNotification(Constant.UPDATE_UI));
            }
        });
        return dataModelArrayList;
    }

    /**
     * Fetching all  image files from phone through content provider
     *
     * @param activity activity/fragment context
     * @param id       ,on which basis images fetch
     */
    private static void fetchImages(Activity activity, String id) {
        String BUCKET_ORDER_BY;
        Uri uri;
        Cursor cursor;
        int columnIndexDataPath, columnIndexFolderName, columnIndexTitleName,
                columnIndexDate, columnIndexSize, columnIndexBucketId, columnIndexAlbumId, columnIndexDuration;

        String absolutePathOfImage, folderName, imageTitle, fileDate, bucketId, AlbumId, fileSize, duration;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] selection;
        String row;
        selection = new String[]{String.valueOf(id)};
        row = MediaStore.Images.Media.BUCKET_ID + " =?";

        BUCKET_ORDER_BY = MediaStore.Images.Media.DATE_MODIFIED + " DESC";

        cursor = activity.getContentResolver().query(uri,
                projection, // Which columns to return
                row,       // Which rows to return
                selection,       // Selection arguments
                BUCKET_ORDER_BY        // Ordering
        );
        if (cursor != null) {
            columnIndexDataPath = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            columnIndexFolderName = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            columnIndexBucketId = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
            columnIndexTitleName = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(columnIndexDataPath);
                folderName = cursor.getString(columnIndexFolderName);
                imageTitle = cursor.getString(columnIndexTitleName);
                bucketId = cursor.getString(columnIndexBucketId);

                ImageDataModel imageDataModel = new ImageDataModel();
                imageDataModel.setFile(new File(absolutePathOfImage));
                imageDataModel.setFolderName(folderName);
                imageDataModel.setPath(absolutePathOfImage);
                imageDataModel.setImageTitle(imageTitle);
                imageDataModel.setBucketId(bucketId);
                imageDataModel.setDuration(0);
                imageDataModel.setSelected(false);
                if (FileUtils.isImageFile(absolutePathOfImage)) {
                    imageDataModel.setImage(true);
                } else if (FileUtils.isVideoFile(absolutePathOfImage)) {
                    imageDataModel.setVideo(true);
                } else if (FileUtils.isAudioFile(absolutePathOfImage)) {
                    imageDataModel.setAudio(true);
                }
                if (imageDataModel.getFile().length() > 0) {
                    dataModelArrayList.add(imageDataModel);
                }

            }
            cursor.close();
        }
    }
}
