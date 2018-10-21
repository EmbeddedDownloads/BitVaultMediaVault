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

public class AudioViewDataOnIdBasis {
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
        fetchAudio(activity);
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
                folderName = Constant.AUDIO_FOLDER_NAME;
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
