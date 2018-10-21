package com.bitvault.mediavault.helper;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.webkit.MimeTypeMap;

import com.bitvault.mediavault.application.MediaVaultController;
import com.bitvault.mediavault.common.Constant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vvdn on 6/27/2017.
 */
/* This class is used to handle all type basic operation
 on media files like rename,delete,move,copy ....
* */
public class FileUtils {
    /**
     * Check the file name with valid character.
     *
     * @param s
     * @return
     */
    public static boolean getSpecialCharacterValidation(String s) {
        if (s == null || s.trim().isEmpty()) {
            System.out.println("Incorrect format of string");
            return false;
        }
        Pattern p = Pattern.compile(Constant.RegexFileName);
        Matcher m = p.matcher(s);
        // boolean b = m.matches();
        return m.find();
    }

    /**
     * Method to copy file from one location to other location
     *
     * @param src         source file
     * @param destination directory path
     * @return
     * @throws Exception
     */
    public static File copyFile(File src, File destination) throws Exception {
        try {

            if (src.isDirectory()) {

                if (src.getPath().equals(destination.getPath())) throw new Exception();

                File directory = createDirectory(destination, src.getName());

                for (File file : src.listFiles()) copyFile(file, directory);

                return directory;
            } else {

                File file = new File(destination, src.getName());
                FileChannel in = null;
                WritableByteChannel out = null;

                try {
                    in = new FileInputStream(src).getChannel();
                    out = new FileOutputStream(file).getChannel();
                    long bytesPerIteration = 50000;
                    long start = 0;
                    while (start < in.size()) {
                        in.transferTo(start, bytesPerIteration, out);
                        start += bytesPerIteration;
                    }

                } finally {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                }

                return file;
            }
        } catch (Exception e) {

            throw new Exception(String.format("Error copying %s", src.getName()));
        }
    }

    /**
     * Used to create a new folder in application
     *
     * @param path Path of directory when the folder created
     * @param name Name of directory
     * @return
     * @throws Exception
     */
    public static File createDirectory(File path, String name) throws Exception {

        File directory = new File(path, name);

        if (directory.mkdirs()) return directory;

        if (directory.exists()) throw new Exception(String.format("%s already exists", name));

        throw new Exception(String.format("Error creating %s", name));
    }

    /**
     * Used to create a new folder in application
     *
     * @param path Path of directory when the folder created
     * @param name Name of directory
     * @return
     * @throws Exception
     */
    public static String createDirectoryForName(File path, String name) {

        File directory = new File(path, name);

        if (directory.mkdirs()) return String.format("Directory created");

        if (directory.exists()) ;
        return String.format("%s already exists", name);


    }

    /**
     * Used for deleting the file
     *
     * @param file name of file which want to delete
     * @return
     * @throws Exception
     */
    public static File deleteFile(File file) throws Exception {
        if (file.exists()) {
            file.delete();
            return file;
        }

        throw new Exception(String.format("Error deleting %s", file.getName()));
    }

    /**
     * Used for deleting the directory
     *
     * @param file name of directory
     * @return
     * @throws Exception
     */
    public static File deleteDirectory(File file) throws Exception {
        if (file.isDirectory()) {
            if (file.listFiles().length == 0) {
                file.delete();
            }
        }

        throw new Exception(String.format("Error deleting %s", file.getName()));
    }

    /**
     * Used for renaming the file
     *
     * @param file File which we want to rename
     * @param name renamed file name
     * @return
     * @throws Exception
     */
    public static File renameFile(File file, String name) throws Exception {

        String extension = getExtension(file.getName());
        if (!extension.isEmpty()) name += "." + extension;

        File newFile = new File(file.getParent(), name);

        if (file.renameTo(newFile)) return newFile;

        throw new Exception(String.format("Error renaming %s", file.getName()));
    }

    /**
     * Passing the file name and gets its duration if its audio/video type
     *
     * @param file pass file name
     * @return
     */
    public static String getDuration(File file) {
        try {

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            retriever.setDataSource(file.getPath());

            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            long milliseconds = Long.parseLong(duration);

            long s = milliseconds / 1000 % 60;

            long m = milliseconds / 1000 / 60 % 60;

            long h = milliseconds / 1000 / 60 / 60 % 24;

            if (h == 0) return String.format(Locale.getDefault(), "%02d:%02d", m, s);
            retriever.release();

            return String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s);
        } catch (Exception e) {

            return null;
        }
    }


    /**
     * Gets the last modification date of the file
     *
     * @param file file name
     * @return
     */
    public static String getLastModified(File file) {

        //returns the last modified date of the given file as a formatted string

        return DateFormat.format("dd MMM yyyy hh:mm:ss", new Date(file.lastModified())).toString();
    }

    /**
     * Gets the last modification date of the file
     *
     * @param file file name
     * @return
     */
    public static String getLastModifiedNew(File file) {

        //returns the last modified date of the given file as a formatted string

        return DateFormat.format("dd MMMM yyyy ", new Date(file.lastModified())).toString();
    }

    /**
     * Gets the date of the file with respective format
     *
     * @param file file name
     * @return
     */
    public static String getDateNew(File file) {

        //returns the last modified date of the given file as a formatted string

        return DateFormat.format("EEEE hh:mm a", new Date(file.lastModified())).toString();
    }

    //Get date of file
    public static String getDate(File file) {

        //returns the last modified date of the given file as a formatted string

        return DateFormat.format("EEEE, dd MMMM yyyy ", new Date(file.lastModified())).toString();
    }

    /**
     * Gets the mime type of file
     *
     * @param file name of file
     * @return
     */
    public static String getMimeType(File file) {

        //returns the mime type for the given file or null iff there is none

        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getExtension(file.getName()));
    }

    /**
     * Gets the extension of file
     *
     * @param filename name of file
     * @return
     */
    public static String getExtension(String filename) {

        //returns the file extension or an empty string iff there is no extension

        return filename.contains(".") ? filename.substring(filename.lastIndexOf(".") + 1) : "";
    }

    /**
     * Get the directory name from file
     *
     * @param filename pass the parentPath of a file to get the directory name
     * @return
     */
    public static String getParentName(String filename) {

        //returns the file extension or an empty string iff there is no extension

        return filename.contains("/") ? filename.substring(filename.lastIndexOf("/") + 1) : "";
    }

    /**
     * Remove the extension of a file
     *
     * @param filename name of file
     * @return
     */

    public static String removeExtension(String filename) {

        int index = filename.lastIndexOf(".");

        return index != -1 ? filename.substring(0, index) : filename;
    }

    /**
     * Comparing the date of files while sorting
     *
     * @param file1
     * @param file2
     * @return
     */
    public static int compareDate(File file1, File file2) {

        long lastModified1 = file1.lastModified();

        long lastModified2 = file2.lastModified();

        return Long.compare(lastModified2, lastModified1);
    }

    /**
     * Comparing the name of file in ascending order of alphabet
     *
     * @param file1
     * @param file2
     * @return
     */
    public static int compareName(File file1, File file2) {

        String name1 = file1.getName();

        String name2 = file2.getName();

        return name1.compareToIgnoreCase(name2);
    }

    /**
     * Comparing the size of files as Descending order
     *
     * @param file1
     * @param file2
     * @return
     */
    public static int compareSizeHighToLow(File file1, File file2) {

        long length1 = file1.length();

        long length2 = file2.length();

        return Long.compare(length2, length1);
    }

    /**
     * Comparing the file of size as ascending order
     *
     * @param file1
     * @param file2
     * @return
     */
    public static int compareSizeLowToHigh(File file1, File file2) {

        long length1 = file1.length();

        long length2 = file2.length();

        return Long.compare(length1, length2);
    }

    /**
     * Method to rescan the media files path and update after
     * any action perform like delete,copy,paste,rename.
     *
     * @param ctx1 Context of class
     * @param file name of file to be scan and update
     */
    public static void scanFile(Context ctx1, File file) {
//        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        intent.setData(Uri.fromFile(file));
//        ctx.sendBroadcast(intent);
/**
 * Changes to scan file method for redmi devices.
 */
        Context ctx = MediaVaultController.getInstance();
        if (ctx != null)
            MediaScannerConnection.scanFile(ctx,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
    }

    // Get the path of internal storage
    public static File getInternalStorage() {

        //returns the path to the internal storage

        return Environment.getExternalStorageDirectory();
    }

    /**
     * Get the file size from file name
     *
     * @param context actvity/fragment context
     * @param file    name of file
     * @return retrun the size in string
     */
    public static String getSize(Context context, File file) {
        return Formatter.formatShortFileSize(context, file.length());
    }

    /**
     * Get the file size from all file length
     *
     * @param context activity/fragment context
     * @param size    total  size of file
     * @return return the size in string
     */
    public static String getTotalFileSize(Context context, long size) {
        return Formatter.formatShortFileSize(context, size);
    }

    // Custom readable  total file size..
    public static String readableFileSize(long size) {
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    /**
     * This method is used to check file type is image or not
     *
     * @param path
     * @return
     */
    public static boolean isImageFile(String path) {
        String mimeType = FileUtils.getMimeType(new File(path));
        return mimeType != null && mimeType.startsWith(Constant.IMAGE);
    }

    /**
     * This method is used to check file type is video or not
     *
     * @param path
     * @return
     */
    public static boolean isVideoFile(String path) {
        //   String mimeType = URLConnection.guessContentTypeFromName(path);
        String mimeType = FileUtils.getMimeType(new File(path));
        return mimeType != null && mimeType.startsWith(Constant.VIDEO);
    }

    /**
     * This method is used to check file type is audio or not
     *
     * @param path
     * @return
     */
    public static boolean isAudioFile(String path) {
        // String mimeType = URLConnection.guessContentTypeFromName(path);
        String mimeType = FileUtils.getMimeType(new File(path));
        return mimeType != null && mimeType.startsWith(Constant.AUDIO);
    }

    /**
     * Gets the name of secure file
     *
     * @param filename name of file
     * @return
     */
    public static String getSecureFileName(String filename) {

        //returns the file name

        return filename.contains(Constant.SECURE_FILE_SEPARATOR) ?
                filename.substring(filename.lastIndexOf(Constant.SECURE_FILE_SEPARATOR) + 3) : "";
    }

    /**
     * Gets the suffix  of secure file
     *
     * @param filename name of file
     * @return
     */
    public static String getSecureFileSuffixName(String filename) {
        //returns the file suffix
        return filename.contains(Constant.SECURE_FILE_SEPARATOR) ?
                filename.substring(0, filename.lastIndexOf(Constant.SECURE_FILE_SEPARATOR) + 3) : "";
    }
}
