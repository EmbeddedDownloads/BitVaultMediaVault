package com.bitvault.mediavault.helper;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;

import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.ottonotification.LandingFragmentNotification;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by vvdn on 9/1/2017.
 */

public class VideoViewDataOnIdBasis {
    private static String[] projection = {"*"};
    public static ArrayList<ImageDataModel> dataModelArrayList = new ArrayList<>();

    /**
     * Getting All Media Path with folder name .
     *
     * @param activity the activity
     * @return ArrayList with images Path
     */
    public static ArrayList<ImageDataModel> getMediaFilesOnIdBasis(Activity activity, String id) {
        dataModelArrayList.clear();


        /*
             * Fetching video type of files through content provider on basis of BucketId
             */
        fetchVideo(activity, id);

//Send notification through main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                GlobalBus.getBus().post(new LandingFragmentNotification("updateUi"));
            }
        });
        return dataModelArrayList;
    }

    /**
     * * Fetching all  video files from phone through content provider
     *
     * @param activity activity/fragment context
     * @param id       ,on which basis video fetch
     */
    private static void fetchVideo(Activity activity, String id) {
        String BUCKET_ORDER_BY;
        Uri uri;
        Cursor cursor;
        int columnIndexDataPath, columnIndexFolderName, columnIndexTitleName, columnIndexDate, columnIndexSize,
                columnIndexBucketId, columnIndexAlbumId, columnIndexDuration;

        String absolutePathOfImage, folderName, imageTitle, fileDate, bucketId, AlbumId, fileSize, duration;

        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] selection;
        String row;

        if (id == null) {
            selection = null;
            row = null;
        } else {
            selection = new String[]{String.valueOf(id)};
            row = MediaStore.Video.Media.BUCKET_ID + " =?";
        }

        BUCKET_ORDER_BY = MediaStore.Video.Media.DATE_MODIFIED + " DESC";

        cursor = activity.getContentResolver().query(uri,
                projection, // Which columns to return
                row,       // Which rows to return (all rows)
                selection,       // Selection arguments (none)
                BUCKET_ORDER_BY        // Ordering
        );

        if (cursor != null) {
            columnIndexFolderName = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);

            columnIndexBucketId = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);
            columnIndexTitleName = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);

            columnIndexDataPath = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
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