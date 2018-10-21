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

public class VideoViewData {
    public static Map<String, ArrayList<ImageDataModel>> imageFolderMap = new HashMap<>();

    public static ArrayList<String> keyList = new ArrayList<>();
    public static ArrayList<ImageDataModel> imageDataModelList = new ArrayList<>();
    String[] projection = {"*"};

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
        String[] projection = {"*"};
//        String[] projection = new String[]{MediaStore.Video.Media.DATA,MediaStore.Video.Media.BUCKET_ID,
//                MediaStore.Video.Media.BUCKET_DISPLAY_NAME};;
        String BUCKET_ORDER_BY;
        Uri uri;
        Cursor cursor;
        int columnIndexDataPath, columnIndexFolderName, columnIndexTitleName,
                columnIndexDate, columnIndexSize, columnIndexBucketId, columnIndexAlbumId, columnIndexDuration;

        String absolutePathOfImage, folderName, imageTitle, fileDate, bucketId, AlbumId, fileSize, duration;

        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        BUCKET_ORDER_BY = MediaStore.Video.Media.DATE_MODIFIED + " DESC";
        cursor = activity.getContentResolver().query(uri, projection, null, null, BUCKET_ORDER_BY);
        if (cursor != null) {
            columnIndexFolderName = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);

            columnIndexBucketId = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID);
//            columnIndexTitleName = cursor
//                    .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);

            columnIndexDataPath = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            while (cursor.moveToNext()) {
                absolutePathOfImage = cursor.getString(columnIndexDataPath);

                folderName = cursor.getString(columnIndexFolderName);
                // imageTitle = cursor.getString(columnIndexTitleName);
                bucketId = cursor.getString(columnIndexBucketId);

                ImageDataModel imageDataModel = new ImageDataModel();
                imageDataModel.setFile(new File(absolutePathOfImage));
                imageDataModel.setDirFile(new File(FileUtils.getParentName(absolutePathOfImage)));
                imageDataModel.setFolderName(folderName);
                imageDataModel.setPath(absolutePathOfImage);
                imageDataModel.setImageTitle(imageDataModel.getFile().getAbsolutePath());
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
