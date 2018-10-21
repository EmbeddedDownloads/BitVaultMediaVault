package com.bitvault.mediavault.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.animation.TranslateAnimation;

import com.bitvault.mediavault.common.Constant;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by linchpin on 8/5/17.
 */

public class Utils {


    /**
     * Method used to convert file in KB into KB,MB,GB,TB
     *
     * @param sizeInBytes - size in KB
     * @return - required format in KB,MB,GB,TB
     */
    public static String convertKBIntoHigherUnit(long sizeInBytes) {

        String hrSize = "";

        double kb = sizeInBytes / 1024;
        double mb = kb / 1024.0;
        double gb = mb / 1024.0;
        double tb = gb / 1024.0;

        DecimalFormat dec = new DecimalFormat("0.00");

        if (kb == 0) {
            hrSize = sizeInBytes + " bytes";
        }

        if (kb >= 1) {

            hrSize = dec.format(kb).concat(" KB");

        }
        if (mb >= 1) {

            hrSize = dec.format(mb).concat(" MB");
        }
        if (gb >= 1) {

            hrSize = dec.format(gb).concat(" GB");
        }
        if (tb >= 1) {

            hrSize = dec.format(tb).concat(" TB");
        }

        return hrSize;

    }


    /**
     * Method used to convert file in KB into KB,MB,GB,TB
     *
     * @param sizeInBytes - size in KB
     * @return - required format in KB,MB,GB,TB
     */
    public static String convertKBInto(long sizeInBytes) {

        String hrSize = "";

        double kb = sizeInBytes / 1024;
        double mb = kb / 1024.0;
        double gb = mb / 1024.0;
        double tb = gb / 1024.0;

        DecimalFormat dec = new DecimalFormat("0.00");

        if (kb == 0) {
            hrSize = sizeInBytes + "";
        }

        if (kb >= 1) {

            hrSize = dec.format(kb);

        }
        if (mb >= 1) {

            hrSize = dec.format(mb);
        }
        if (gb >= 1) {

            hrSize = dec.format(gb);
        }
        if (tb >= 1) {

            hrSize = dec.format(tb);
        }

        return hrSize;

    }

    /**
     * Method to animate a view from top to bottom
     *
     * @param view - view which needs to be animate
     */
    public static void slideToBottom(View view) {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, -view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(false);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    /**
     * Method to animate view slide out from bottom to top
     *
     * @param view - view which needs to be animate
     */
    public static void slideToTop(final View view) {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, -view.getHeight());
        animate.setDuration(500);
        animate.setFillAfter(false);
        view.startAnimation(animate);
        view.setVisibility(View.VISIBLE);
    }


    /**
     * Method to snackbar in the application
     *
     * @param mView    -- parent layout
     * @param message  -- message to be displayed
     * @param duration -- durattion of the snackbar
     */
    public static void showSnakbar(View mView, String message, int duration) {
        try {
            if (mView != null) {
                Snackbar.make(mView, message, duration).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Method used to check whether device is connected with internet or not
     *
     * @param mContext -- context of the application
     * @return -- either network is connected or not
     */
    public static boolean isNetworkConnected(Context mContext) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Method is used to prepare data from the arraylist of the file
     *
     * @param mFileList -- list of the file selected by the user
     * @return -- base64 + extension of data seperated by | & *
     */
    public static String prepareDataForFiles(ArrayList<File> mFileList) {

        String fileData = "";

        for (int i = 0; i < mFileList.size(); i++) {

            fileData += genBase64OfMediaFile(mFileList.get(i)) + "|"

                    + getFileExtension(mFileList.get(i)) + "*";

        }

        return fileData;
    }

    /**
     * Method used to get Base64 of Media file
     *
     * @param file -- Path of the image
     * @return -- Base64 value of the image
     */
    public static String genBase64OfMediaFile(File file) {
        String encodedfile = null;
        try {
            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStreamReader.read(bytes);
            encodedfile = new String(Base64.encodeBase64(bytes), "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return encodedfile;
    }


    /**
     * Method to get the extension of the file
     *
     * @param mFile -- file selected by the user
     * @return -- extension of the file
     */
    public static String getFileExtension(File mFile) {
        String extension = "";

        try {
            if (mFile != null && !mFile.getAbsolutePath().equals("")) {
                File mSelectedFile = mFile;
                extension = mFile.getAbsoluteFile().toString().substring(mFile.getAbsoluteFile().toString().lastIndexOf("."), mFile.getAbsoluteFile().toString().length());
                return extension;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return extension;
    }

    /**
     * Method used to get Base64 of any string value
     *
     * @param value -- text value of the message
     * @return -- base64 of the text
     */
    public static String getBase64(String value) {

        String base64Data = "";
        byte[] bytesEncoded = Base64.encodeBase64(value.getBytes());
        return (new String(bytesEncoded));
    }


    /**
     * Method used to take time difference
     *
     * @param millis
     * @return
     */
    public static String converDatewithSec(String millis) {

        try {

            long current = System.currentTimeMillis();
            long actual = Long.parseLong(millis);

            long difference = (current - actual) / 1000;

            String returnDate = "";

            if (difference < 60) {
                returnDate = difference + " seconds ago";
            } else if (difference >= 60 && difference < 60 * 60) {
                returnDate = difference / 60 + " minutes ago";
            } else if (difference >= 60 * 60 && difference < 24 * 60 * 60) {
                returnDate = difference / (60 * 60) + " hours ago";
            } else {
                Date date = new Date(Long.parseLong(millis));
                SimpleDateFormat requiredFormatwithtime = new SimpleDateFormat("EEEE, dd MMM - hh:mm a");
                returnDate = requiredFormatwithtime.format(date);
            }

            return returnDate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Method used to get decoded base64 value
     *
     * @param mBase64Data
     * @return
     */
    public static String getDecodedBase64(String mBase64Data) {

        String base64 = "";

        try {
            byte[] base64Data = org.spongycastle.util.encoders.Base64.decode(mBase64Data);
            return new String(base64Data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64;

    }

    /**
     * Method used to get decoded base64 value into media
     *
     * @param mBase64Data
     * @return
     */
    public static String getDecodedBase64Media(String mBase64Data, String extension) {

        String base64 = "";
        File mFile = null;
        FileOutputStream out = null;

        try {

            if (Utils.getMediaType(extension).equalsIgnoreCase(Constant.VIDEO)) {

                byte[] decodedString = org.spongycastle.util.encoders.Base64.decode(mBase64Data);

                try {

                    mFile = new File(Constant.RECEIVED_MEDIA_PATH + "/" + System.currentTimeMillis() + extension);
                    out = new FileOutputStream(mFile);
                    out.write(decodedString);
                    out.close();
                } catch (Exception e) {
                    // TODO: handle exception
                }
            } else if (Utils.getMediaType(extension).equalsIgnoreCase(Constant.IMAGE)) {
                byte[] decodedString = org.spongycastle.util.encoders.Base64.decode(mBase64Data);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                try {

                    mFile = new File(Constant.RECEIVED_MEDIA_PATH + "/" + System.currentTimeMillis() + extension);

                    out = new FileOutputStream(mFile);

                    // bmp is your Bitmap instance PNG is a lossless format, the compression factor (100) is ignored
                    decodedByte.compress(Bitmap.CompressFormat.JPEG, 90, out);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.flush();
                            out.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (Utils.getMediaType(extension).equalsIgnoreCase(Constant.AUDIO)) {

                byte[] decodedString = org.spongycastle.util.encoders.Base64.decode(mBase64Data);

                try {

                    mFile = new File(Constant.RECEIVED_MEDIA_PATH + "/" + System.currentTimeMillis() + extension);
                    out = new FileOutputStream(mFile);
                    out.write(decodedString);
                    out.close();
                } catch (Exception e) {
                    // TODO: handle exception

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return (mFile.getAbsolutePath());

    }

    /**
     * Method used to get decimal place value
     *
     * @param value
     * @return
     */
    public static String convertDecimalFormatPattern(Double value) {
        try {
            DecimalFormat df = new DecimalFormat("#0.########");
            return df.format(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value.toString();
    }

    /**
     * Method used to get media type
     *
     * @param extension
     * @return
     */
    public static String getMediaType(String extension) {
        if (extension.equalsIgnoreCase(".mp4") || extension.equalsIgnoreCase(".3gp") || extension.equalsIgnoreCase(".mkv")) {
            return Constant.VIDEO;
        } else if (extension.equalsIgnoreCase(".mp3") || extension.equalsIgnoreCase(".m4a") || extension.equalsIgnoreCase(".wav")) {
            return Constant.AUDIO;
        } else if (extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase(".jpeg") || extension.equalsIgnoreCase(".png")) {
            return Constant.IMAGE;
        } else {
            return Constant.IMAGE;
        }
    }

}
