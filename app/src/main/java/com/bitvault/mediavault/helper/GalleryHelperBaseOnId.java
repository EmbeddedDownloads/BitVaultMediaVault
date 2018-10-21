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
 * Created by vvdn on 6/22/2017.
 */

/**
 * This class is used for fetching media files on basis of BucketId
 */
public class GalleryHelperBaseOnId {

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


        /**
         * Fetching audio type of files through content provider
         */
        /*
        * Default audio  bucket id is 1,because we put all audio files into one
         * folder like "Audio files".So i put the id default to 1 to avoid fetch audio each time.
        * */
        if (id.equalsIgnoreCase("1")) {
            fetchAudio(activity);
        } else {
            /**
             * Fetching image type of files through content provider on basis of BucketId
             */
            fetchImages(activity, id);
            /**
             * Fetching video type of files through content provider on basis of BucketId
             */
            fetchVideo(activity, id);
        }
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

    /**
     * Fetching  all  audio files from phone through content provider
     *
     * @param activity
     */
    private static void fetchAudio(Activity activity) {
        String BUCKET_ORDER_BY;
        Uri uri;
        Cursor cursor;
        int columnIndexDataPath, columnIndexFolderName, columnIndexTitleName, columnIndexDate, columnIndexSize,
                columnIndexBucketId, columnIndexAlbumId, columnIndexDuration;

        String absolutePathOfImage, folderName, imageTitle, fileDate, bucketId, AlbumId, fileSize, duration;
        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        BUCKET_ORDER_BY = MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";


        cursor = activity.getContentResolver().query(uri, projection, selection, null, BUCKET_ORDER_BY);
        if (cursor != null) {
            columnIndexFolderName = cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            columnIndexAlbumId = cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            columnIndexTitleName = cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);

            columnIndexDataPath = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(columnIndexDataPath);
                folderName =  Constant.AUDIO_FOLDER_NAME;
                imageTitle = cursor.getString(columnIndexTitleName);

                ImageDataModel imageDataModel = new ImageDataModel();
                imageDataModel.setFile(new File(absolutePathOfImage));
                imageDataModel.setFolderName(folderName);
                imageDataModel.setPath(absolutePathOfImage);
                imageDataModel.setImageTitle(imageTitle);
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
