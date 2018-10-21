package com.bitvault.mediavault.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bitvault.mediavault.model.ImageDataModel;

/**
 * Created by vvdn on 8/16/2017.
 */
//Database for handling time duration
public class MediaFileDuration extends SQLiteOpenHelper {
    private static MediaFileDuration instance = null;
    private SQLiteDatabase sqLiteDatabase;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "mediafileduration.db";

    // TABLE FOR STORING media file duration
    private static final String MEDIAFILEDURATIONTABLE = "media_file_duration_table";//table name
    private static final String FILE_PATH = "file_path";
    private static final String FILE_DURATION = "file_duration";
    //media duration  table  create query
    private static final String TABLE_MEDIA_DURATION = "CREATE TABLE IF NOT EXISTS "
            + MEDIAFILEDURATIONTABLE + "("
            + FILE_PATH + " TEXT,"
            + FILE_DURATION + " TEXT" + ")";

    /**
     * Make a single instance of mediafileduration database class
     *
     * @param context
     * @return
     */
    public static synchronized MediaFileDuration getDatabaseInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (instance == null) {
            instance = new MediaFileDuration(context.getApplicationContext());
        }
        return instance;
    }

    // method to create the tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_MEDIA_DURATION);
    }

    // Method when database version change i.e. any update into database schema
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '" + MEDIAFILEDURATIONTABLE + "'");
    }

    // Constructor of database class
    private MediaFileDuration(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /*
    * Method to insert the file name and its duration
    * */
    public void insertMediaTime(ImageDataModel model) {
        SQLiteDatabase db = this.getWritableDatabase();
        String path = model.getFile().getAbsolutePath();
        path = path.replaceAll("'", "''").trim();
        ContentValues values = new ContentValues();
        values.put(FILE_PATH, path);
        values.put(FILE_DURATION, model.getTimeDuration());
        db.insert(MEDIAFILEDURATIONTABLE, null, values);
    }

    //Method to update local db when file rename
    public void updateFileName(String NewFilepath, String filePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        NewFilepath = NewFilepath.replaceAll("'", "''").trim();
        filePath = filePath.replaceAll("'", "''").trim();
        ContentValues values = new ContentValues();
        values.put(FILE_PATH, NewFilepath);
        db.update(MEDIAFILEDURATIONTABLE, values, FILE_PATH + " = '" + filePath + "'", null);
    }

    /**
     * Method to getback the time duration of mediafile base on their file path
     *
     * @param filepath
     * @return
     */
    public String getTimeDuration(String filepath) {
        Cursor cursor = null;
        filepath = filepath.replaceAll("'", "''").trim();
        String time = "00:00";
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            cursor = db.rawQuery("SELECT * FROM " + MEDIAFILEDURATIONTABLE + " WHERE " + FILE_PATH + " = ?", new String[]{String.valueOf(filepath)});
            if (cursor != null && cursor.moveToFirst()) {
                //   cursor.moveToFirst();
                time = cursor.getString(cursor.getColumnIndex(FILE_DURATION));
                cursor.close();
            }
        } finally {
            // this gets called even if there is an exception somewhere above
            if (cursor != null)
                cursor.close();
        }
        return time;
    }

    /**
     * Method to check that file path exists into database or not
     *
     * @param path
     * @return
     */
    public boolean checkFileStatus(String path) {
        path = path.replaceAll("'", "''").trim();
        String selectQuery = " SELECT  * FROM " + MEDIAFILEDURATIONTABLE + "  WHERE  " + FILE_PATH + " = '" + path + "'";
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


}
