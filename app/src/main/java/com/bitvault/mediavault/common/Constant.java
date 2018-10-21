package com.bitvault.mediavault.common;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by vvdn on 7/12/2017.
 */

public class Constant {
    public static String ALBUM_TYPE = "album_type";
    public static String LIST_TYPE = "list_type";
    public static String ALBUM_VIEW = "album_view";
    public static String LIST_VIEW = "list_view";
    public static String LIST_VIEW_SECURE = "list_view_secure";
    public static String SECURE_TAB = "secure_tab";
    public static String RESTORE_BUTTON = "restore_button";
    public static String AUTH_SCREEN_VISIBILITY = "auth_screen_visiblity";
    public static String ALL_VIEW = "all_view";
    public static String PHOTO_VIEW = "photo_view";
    public static String VIDEO_VIEW = "video_view";
    public static String AUDIO_VIEW = "audio_view";
    public static String SORT_TYPE = "sort_type";
    public static String IS_FIRST_LAUNCH = "is_first_launch";
    public static String RegexFileName = "^[^<>'\\\"/;.`%]*$";
    public static String SECURE_FILE_SEPARATOR = "@_@";
    public static String VIDEO = "video";
    public static String IMAGE = "image";
    public static String AUDIO = "audio";
    public static String AUDIO_TITLE = "Audio";
    public static String WALLET_DETAILS = "wallet_details";
    public static String FILE_LIST = "file_list";
    public static String DATA = "data";
    public static String UPDATE_UI = "updateUi";
    public static String RECEIVED_MEDIA_PATH = Environment.getExternalStorageDirectory() + "/" + "ReceivedMedia";
    /**
     * for Status
     */
    public static String STATUS_OK = "ok";
    public static String STATUS_SUCCESS = "success";
    public static String STATUS_FAILED_PBC = "fail_to_pbc";
    public static String FILE_ARCHIVE = "archive";
    public static String DELETE_FROM_PBC = "delete_from_pbc";
    public static String STATUS_FAILED_APP_STORE_ADD = "fail_to_app_store_add";
    public static String STATUS_FAILED_APP_STORE_DELETE = "fail_to_app_store_delete";
    public static String TXID = "tx_id";
    public static String MESSAGE_FEE = "message_fee";
    public static String ERRORMESSAGE = "error_message";
    public static String RECEIVER_ADDRESS = "receiver_address";
    public static String AUDIO_FOLDER_NAME = "#_#Audio";
    public static String BALANCE_INFO = "0.0 BTC";
    public static String BALANCE_INFO_EOT = "0.0 EOT";
    public static String KEY_NAME = "key_name";
    public static String CANCEL = "cancel";
    public static String ACTION_TYPE = "action_type";
    public static String TYPE = "type";
    public static String POSITION = "position";
    public static String INTENT_STATUS = "intentStatus";
    public static String SECURE_FILES_DIR = "SecureFiles";
    public static int AUTH_ACTIVITY_RESULT_CODE = 111;
    public static int FILE_NAME_LENGTH = 40;
    public static final int FILE_LIMIT = 5;
    public static String result = "result";
    public static String WALLET_TYPE = "wallet_type";
    public static String EOT_WALLET_BAL = "eot_balance";
    public static String EOT_WALLET_ADDRESS = "eot_wallet_address";
    public static String EOT_RECEIVER_ADDRESS = "ESHMLyTQhHWzjSZ5eDRwD9qjDF7oLGadjD";
    public static String BTC_RECEIVER_ADDRESS = "mgJS12VDsDFjTPQrALG4ec38ajExZCv4mC";
    public static String EOT = "EOT";
    public static String BTC = "BTC";
    public static String MULTIPLE_SELECT="multiple_select";

    // Making a directory at root level which is accessible by application only.
    public static File getSecureFileDir(Context context) {
        return context.getDir(Constant.SECURE_FILES_DIR, Context.MODE_PRIVATE);
    }
    // public static String RegexFileName="[^$&+,:;=?@#|a-zA-Z0-9-_ ']";
}
