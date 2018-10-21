package com.bitvault.mediavault.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.model.ImageDataModel;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by vvdn on 9/11/2017.
 */
//This class stores the secure file information in local database.
public class MediaVaultLocalDb extends SQLiteOpenHelper {
    private static MediaVaultLocalDb instance = null;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "mediavault.db";
    // TABLE FOR STORING secure  file information
    private static final String TABLE_NAME = "media_vault_table";//table name
    public static final String TXD_ID = "transaction_id";
    public static final String WALLET_ADDRESS = "wallet_address";
    public static final String FILE_UNIQUE_ID = "file_unique_id";
    private static final String FILE_NAME = "file_name";
    private static final String FILE_TYPE = "file_type";
    private static final String FILE_ENC_KEY = "file_enc_key";
    private static final String FILE_TXD_ENC = "txd_enc_key";
    private static final String FILE_LOCATION = "file_location";
    private static final String CRC = "crc";
    public static final String STATUS = "status";  // Pending,Success,fail,archive,unarchive

    private static final String TEXT_TYPE = " TEXT,";
    private static final String TEXT_TYPE_END = " TEXT";


    //media duration  table  create query
    private static final String TABLE_CREATION_QUERY = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + "("
            + TXD_ID + TEXT_TYPE
            + WALLET_ADDRESS + TEXT_TYPE
            + FILE_UNIQUE_ID + TEXT_TYPE
            + FILE_NAME + TEXT_TYPE
            + FILE_TYPE + TEXT_TYPE
            + FILE_LOCATION + TEXT_TYPE
            + FILE_ENC_KEY + TEXT_TYPE
            + FILE_TXD_ENC + TEXT_TYPE
            + STATUS + TEXT_TYPE
            + CRC + TEXT_TYPE_END + ")";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATION_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '" + TABLE_NAME + "'");
    }

    /**
     * Make a single instance of MediaVaultLocalDb database class
     *
     * @param context
     * @return
     */
    public static synchronized MediaVaultLocalDb getMediaVaultDatabaseInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (instance == null) {
            instance = new MediaVaultLocalDb(context.getApplicationContext());
        }
        return instance;
    }

    // Constructor of database class
    private MediaVaultLocalDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*
   * Method to insert the file name and its duration
   * */
    public void insertSecureMediaData(ImageDataModel model) {
        SQLiteDatabase db = this.getWritableDatabase();
        String path = model.getFile().getAbsolutePath();
        path = path.replaceAll("'", "''");
        ContentValues values = new ContentValues();
        values.put(TXD_ID, model.getTxid());
        values.put(WALLET_ADDRESS, model.getWalletAddress());
        values.put(FILE_UNIQUE_ID, model.getFileUniqueId());
        values.put(FILE_NAME, path);
        values.put(FILE_LOCATION, path);
        values.put(FILE_TYPE, model.getType());
        values.put(FILE_ENC_KEY, model.getFileEncKey());
        values.put(FILE_TXD_ENC, model.getFileEncTxid());
        values.put(STATUS, model.getFileStatus());
        values.put(CRC, model.getCrc());
        db.insert(TABLE_NAME, null, values);
    }

    /**
     * Method to check that file path exists into database or not
     *
     * @param path
     * @return
     */
    public boolean checkFileStatus(String path) {
        String selectQuery = " SELECT  * FROM " + TABLE_NAME +
                "  WHERE  " + FILE_LOCATION + " = '" + path + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    return true;
                } while (cursor.moveToNext());
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return false;
    }

    /**
     * Method to get all secure file list
     *
     * @return
     */
    public ArrayList<ImageDataModel> getSecureFileList() {
        ArrayList<ImageDataModel> modelArrayList = new ArrayList<>();
        String selectQuery = " SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    ImageDataModel dataModel = new ImageDataModel();
                    dataModel.setTxid(cursor.getString(0));
                    dataModel.setWalletAddress(cursor.getString(1));
                    dataModel.setFileUniqueId(cursor.getString(2));
                    dataModel.setFile(new File(cursor.getString(3)));
                    dataModel.setType(cursor.getString(4));
                    dataModel.setPath(cursor.getString(5));
                    dataModel.setFileEncKey(cursor.getString(6));
                    dataModel.setFileEncTxid(cursor.getString(7));
                    dataModel.setFileStatus(cursor.getString(8));
                    dataModel.setCrc(cursor.getString(9));
                    if (dataModel != null && dataModel.getFile().length() > 0)
                        modelArrayList.add(dataModel);
                } while (cursor.moveToNext());
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return modelArrayList;
    }

    /**
     * Method to get all secure file list except archive files
     *
     * @return
     */
    public ArrayList<ImageDataModel> getSecureUnarchiveFileList() {
        ArrayList<ImageDataModel> modelArrayList = new ArrayList<>();
        String selectQuery = " SELECT  * FROM " + TABLE_NAME + "  WHERE  " +
                STATUS + " != '" + Constant.FILE_ARCHIVE + "'" + " AND " +
                STATUS + " != '" + Constant.DELETE_FROM_PBC + "'";
        //  String selectQuery = " SELECT  * FROM " + TABLE_NAME ;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    ImageDataModel dataModel = new ImageDataModel();
                    dataModel.setTxid(cursor.getString(0));
                    dataModel.setWalletAddress(cursor.getString(1));
                    dataModel.setFileUniqueId(cursor.getString(2));
                    dataModel.setFile(new File(cursor.getString(3)));
                    dataModel.setType(cursor.getString(4));
                    dataModel.setPath(cursor.getString(5));
                    dataModel.setFileEncKey(cursor.getString(6));
                    dataModel.setFileEncTxid(cursor.getString(7));
                    dataModel.setFileStatus(cursor.getString(8));
                    dataModel.setCrc(cursor.getString(9));
                    if (dataModel != null && dataModel.getFile().length() > 0)
                    modelArrayList.add(dataModel);
                } while (cursor.moveToNext());
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return modelArrayList;
    }

    // Get secured file model data
    public ImageDataModel getSecureFileDetail(String fileUniqueId) {
        ImageDataModel dataModel = new ImageDataModel();
        String selectQuery = " SELECT  * FROM " + TABLE_NAME + "  WHERE  " +
                FILE_UNIQUE_ID + " = '" + fileUniqueId + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    dataModel.setTxid(cursor.getString(0));
                    dataModel.setWalletAddress(cursor.getString(1));
                    dataModel.setFileUniqueId(cursor.getString(2));
                    dataModel.setFile(new File(cursor.getString(3)));
                    dataModel.setPath(cursor.getString(4));
                    dataModel.setType(cursor.getString(5));
                    dataModel.setFileEncKey(cursor.getString(6));
                    dataModel.setFileEncTxid(cursor.getString(7));
                    dataModel.setFileStatus(cursor.getString(8));
                    dataModel.setCrc(cursor.getString(9));
                } while (cursor.moveToNext());
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return dataModel;
    }

    //Method to update local db when user archive a file
    public void updateFileStatus(String FileUniqueId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(STATUS, status);
        db.update(TABLE_NAME, values, FILE_UNIQUE_ID + " = '" + FileUniqueId + "'", null);
    }

    //Method to update local db when user rename a file
    public void updateFileName(String FileUniqueId, File file) {
        SQLiteDatabase db = this.getWritableDatabase();
        String path = file.getAbsolutePath();
        path = path.replaceAll("'", "''");
        ContentValues values = new ContentValues();
        values.put(FILE_NAME, path);
        values.put(FILE_LOCATION, path);
        db.update(TABLE_NAME, values, FILE_UNIQUE_ID + " = '" + FileUniqueId + "'", null);
    }

    //Delete secure file from db
    public void deleteSecureFile(String FileUniqueId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, FILE_UNIQUE_ID + " = " + "'" + FileUniqueId + "'", null);
        db.close();
    }

}
