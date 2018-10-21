package com.bitvault.mediavault.helper;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;

import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.ottonotification.LandingFragmentNotification;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
* GalleryHelper class is responsible for fetching
 * all type of media file through content provider.
* */
public class GalleryHelper {

    public static Map<String, ArrayList<ImageDataModel>> imageFolderMap = new HashMap<>();

    public static ArrayList<String> keyList = new ArrayList<>();
    public static ArrayList<ImageDataModel> imageDataModelList = new ArrayList<>();

    /**
     * Getting All Media Path with folder name .
     *
     * @param activity the activity
     * @return ArrayList with images Path
     */
    public static Map<String, ArrayList<ImageDataModel>> getImageFolderMap(Activity activity) {
        imageFolderMap.clear();
        keyList.clear();
        imageDataModelList.clear();
        String BUCKET_ORDER_BY;
        Uri uri;
        Cursor cursor;
        int columnIndexDataPath, columnIndexFolderName, columnIndexTitleName,
                columnIndexDate, columnIndexSize, columnIndexBucketId, columnIndexAlbumId, columnIndexDuration;

        String absolutePathOfImage, folderName, imageTitle, fileDate, bucketId, AlbumId, fileSize, duration;

        /**
         * Fetching Image type of files through content provider
         */
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {"*"};
        BUCKET_ORDER_BY = MediaStore.Images.Media.DATE_MODIFIED + " DESC";

        cursor = activity.getContentResolver().query(uri,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
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
                imageDataModel.setDirFile(new File(FileUtils.getParentName(absolutePathOfImage)));
                imageDataModel.setFolderName(folderName);
                imageDataModel.setPath(absolutePathOfImage);
                imageDataModel.setImageTitle(imageTitle);
                imageDataModel.setBucketId(bucketId);
                imageDataModel.setDuration(0);
                imageDataModel.setTimeDuration("0");
                imageDataModel.setSelected(false);
                if (FileUtils.isImageFile(absolutePathOfImage)) {
                    imageDataModel.setImage(true);
                } else if (FileUtils.isVideoFile(absolutePathOfImage)) {
                    imageDataModel.setVideo(true);
                } else if (FileUtils.isAudioFile(absolutePathOfImage)) {
                    imageDataModel.setAudio(true);
                }
                if (imageDataModel.getFile().length() > 0) {
                    imageDataModelList.add(imageDataModel);
                    if (imageFolderMap.containsKey(folderName)) {
                        imageFolderMap.get(folderName).add(imageDataModel);
                    } else {
                        ArrayList<ImageDataModel> listOfAllImages = new ArrayList<>();

                        listOfAllImages.add(imageDataModel);

                        imageFolderMap.put(folderName, listOfAllImages);
                    }
                }
            }
            cursor.close();
        }
/**
 * Fetching video type of files through content provider
 */

        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        BUCKET_ORDER_BY = MediaStore.Video.Media.DATE_MODIFIED + " DESC";
        cursor = activity.getContentResolver().query(uri, projection, null, null, BUCKET_ORDER_BY);
        if (cursor != null) {
            columnIndexFolderName = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);

            columnIndexBucketId = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);
            columnIndexTitleName = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);

            columnIndexDataPath = cursor.getColumnIndexOrThrow(MediaColumns.DATA);

            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(columnIndexDataPath);

                folderName = cursor.getString(columnIndexFolderName);
                imageTitle = cursor.getString(columnIndexTitleName);
                bucketId = cursor.getString(columnIndexBucketId);
                ImageDataModel imageDataModel = new ImageDataModel();
                imageDataModel.setFile(new File(absolutePathOfImage));
                imageDataModel.setDirFile(new File(FileUtils.getParentName(absolutePathOfImage)));
                imageDataModel.setFolderName(folderName);
                imageDataModel.setPath(absolutePathOfImage);
                imageDataModel.setImageTitle(imageTitle);
                imageDataModel.setBucketId(bucketId);
                imageDataModel.setTimeDuration("0");
                imageDataModel.setSelected(false);
                if (FileUtils.isImageFile(absolutePathOfImage)) {
                    imageDataModel.setImage(true);
                } else if (FileUtils.isVideoFile(absolutePathOfImage)) {
                    imageDataModel.setVideo(true);
                } else if (FileUtils.isAudioFile(absolutePathOfImage)) {
                    imageDataModel.setAudio(true);
                }
                if (imageDataModel.getFile().length() > 0) {
                    imageDataModelList.add(imageDataModel);
                    if (imageFolderMap.containsKey(folderName)) {
                        imageFolderMap.get(folderName).add(imageDataModel);
                    } else {

                        ArrayList<ImageDataModel> listOfAllImages = new ArrayList<ImageDataModel>();

                        listOfAllImages.add(imageDataModel);

                        imageFolderMap.put(folderName, listOfAllImages);
                    }
                }
            }
            cursor.close();
        }


/**
 * Fetching audio type of files through content provider
 */
        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        BUCKET_ORDER_BY = MediaStore.Audio.Media.DATE_MODIFIED + " DESC";
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection1 = new String[]{MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.ALBUM_ID,
                MediaStore.Audio.AudioColumns.DISPLAY_NAME,
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.SIZE,
                MediaStore.Audio.AudioColumns.DATE_MODIFIED,

                MediaStore.Audio.AudioColumns.DURATION};

        cursor = activity.getContentResolver().query(uri, projection1, selection, null, BUCKET_ORDER_BY);
        if (cursor != null) {
            columnIndexFolderName = cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM);
            columnIndexAlbumId = cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ALBUM_ID);
            columnIndexTitleName = cursor
                    .getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DISPLAY_NAME);

            columnIndexDataPath = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA);
            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(columnIndexDataPath);
                folderName = Constant.AUDIO_FOLDER_NAME;
                imageTitle = cursor.getString(columnIndexTitleName);

                ImageDataModel imageDataModel = new ImageDataModel();
                imageDataModel.setFile(new File(absolutePathOfImage));
                imageDataModel.setFolderName(folderName);
                imageDataModel.setPath(absolutePathOfImage);
                imageDataModel.setImageTitle(imageTitle);
                imageDataModel.setBucketId("1");
                imageDataModel.setTimeDuration("0");
                imageDataModel.setSelected(false);
                if (FileUtils.isImageFile(absolutePathOfImage)) {
                    imageDataModel.setImage(true);
                } else if (FileUtils.isVideoFile(absolutePathOfImage)) {
                    imageDataModel.setVideo(true);
                } else if (FileUtils.isAudioFile(absolutePathOfImage)) {
                    imageDataModel.setAudio(true);
                }
                if (imageDataModel.getFile().length() > 0) {
                    imageDataModelList.add(imageDataModel);
                    if (imageFolderMap.containsKey(folderName)) {
                        imageFolderMap.get(folderName).add(imageDataModel);
                    } else {
                        ArrayList<ImageDataModel> listOfAllImages = new ArrayList<ImageDataModel>();
                        listOfAllImages.add(imageDataModel);
                        imageFolderMap.put(folderName, listOfAllImages);
                    }
                }
            }
            cursor.close();
        }
        keyList.addAll(imageFolderMap.keySet());

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                GlobalBus.getBus().post(new LandingFragmentNotification(Constant.UPDATE_UI));
            }
        });

        return imageFolderMap;
    }
}
