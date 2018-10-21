package com.bitvault.mediavault.helper;

import android.content.SharedPreferences;

import com.bitvault.mediavault.common.Constant;

/**
 * Created by vvdn on 6/28/2017.
 */

/**
 * This class stores the shared preference values like sorting status,tab,filter.
 */
public class SharedPref {
    public static void savePreference(SharedPreferences prefs, String key,
                                      String value) {
        SharedPreferences.Editor e = prefs.edit();
        e.putString(key, value);
        e.commit();
    }

    public static void saveBooleanPreference(SharedPreferences prefs, String key,
                                             boolean value) {
        SharedPreferences.Editor e = prefs.edit();
        e.putBoolean(key, value);
        e.commit();
    }

    public static void setSortType(SharedPreferences prefs, String value) {
        SharedPref.savePreference(prefs, Constant.SORT_TYPE, value);
    }

    public static String getSortType(SharedPreferences prefs) {
        return prefs.getString(Constant.SORT_TYPE, "0");
    }

    public static void setIsFirstLaunch(SharedPreferences prefs, String value) {
        SharedPref.savePreference(prefs, Constant.IS_FIRST_LAUNCH, value);
    }

    public static String getIsFirstLaunch(SharedPreferences prefs) {
        return prefs.getString(Constant.IS_FIRST_LAUNCH, "0");
    }

    public static void setAlbumType(SharedPreferences prefs, String value) {
        SharedPref.savePreference(prefs, Constant.ALBUM_VIEW, value);
    }

    public static String getAlbumType(SharedPreferences prefs) {
        return prefs.getString(Constant.ALBUM_VIEW, "0");
    }

    public static void setListType(SharedPreferences prefs, String value) {
        SharedPref.savePreference(prefs, Constant.LIST_VIEW, value);
    }

    public static String getListType(SharedPreferences prefs) {
        return prefs.getString(Constant.LIST_VIEW, "0");
    }

    public static void setSecureListType(SharedPreferences prefs, String value) {
        SharedPref.savePreference(prefs, Constant.LIST_VIEW_SECURE, value);
    }

    public static String getSecureListType(SharedPreferences prefs) {
        return prefs.getString(Constant.LIST_VIEW_SECURE, "0");
    }

    public static void setAudioType(SharedPreferences prefs, String value) {
        SharedPref.savePreference(prefs, Constant.AUDIO_VIEW, value);
    }

    public static String getAudioType(SharedPreferences prefs) {
        return prefs.getString(Constant.AUDIO_VIEW, "0");
    }

    public static void setAllType(SharedPreferences prefs, String value) {
        SharedPref.savePreference(prefs, Constant.ALL_VIEW, value);
    }

    public static String getAllType(SharedPreferences prefs) {
        return prefs.getString(Constant.ALL_VIEW, "0");
    }

    public static void setVideoType(SharedPreferences prefs, String value) {
        SharedPref.savePreference(prefs, Constant.VIDEO_VIEW, value);
    }

    public static String getVideoType(SharedPreferences prefs) {
        return prefs.getString(Constant.VIDEO_VIEW, "0");
    }

    public static void setPhotoType(SharedPreferences prefs, String value) {
        SharedPref.savePreference(prefs, Constant.PHOTO_VIEW, value);
    }

    public static String getPhotoType(SharedPreferences prefs) {
        return prefs.getString(Constant.PHOTO_VIEW, "0");
    }

    public static void setSecureTab(SharedPreferences prefs, String value) {
        SharedPref.savePreference(prefs, Constant.SECURE_TAB, value);
    }

    public static String getSecureTab(SharedPreferences prefs) {
        return prefs.getString(Constant.SECURE_TAB, "0");
    }

    public static void setVisibilityRestoreButton(SharedPreferences prefs, boolean value) {
        SharedPref.saveBooleanPreference(prefs, Constant.RESTORE_BUTTON, value);
    }

    public static boolean getVisibilityRestoreButton(SharedPreferences prefs) {
        return prefs.getBoolean(Constant.RESTORE_BUTTON, true);
    }

    public static void setAuthVisibility(SharedPreferences prefs, boolean value) {
        SharedPref.saveBooleanPreference(prefs, Constant.AUTH_SCREEN_VISIBILITY, value);
    }

    public static boolean getAuthVisibility(SharedPreferences prefs) {
        return prefs.getBoolean(Constant.AUTH_SCREEN_VISIBILITY, true);
    }

    public static void setImageContentIntent(SharedPreferences prefs, String value) {
        SharedPref.savePreference(prefs, Constant.PHOTO_VIEW, value);
    }

    public static String getImageContentIntent(SharedPreferences prefs) {
        return prefs.getString(Constant.PHOTO_VIEW, "0");
    }

    public static void setAnyContentIntent(SharedPreferences prefs, String value) {
        SharedPref.savePreference(prefs, Constant.ALL_VIEW, value);
    }

    public static String getAnyContentIntent(SharedPreferences prefs) {
        return prefs.getString(Constant.ALL_VIEW, "0");
    }
}
