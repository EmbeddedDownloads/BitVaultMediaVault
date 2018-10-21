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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vvdn on 8/28/2017.
 */

@SuppressWarnings("ALL")
public class AudioViewData {
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
         * Fetching Audio type of files through content provider
         */
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
        //Send notification through main thread
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                GlobalBus.getBus().post(new LandingFragmentNotification(Constant.UPDATE_UI));
            }
        });
        return imageFolderMap;
    }
}
