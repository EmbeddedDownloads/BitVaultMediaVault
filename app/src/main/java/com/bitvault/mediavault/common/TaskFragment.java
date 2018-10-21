package com.bitvault.mediavault.common;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.application.MediaVaultController;
import com.bitvault.mediavault.helper.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by vvdn on 7/10/2017.
 */

public class TaskFragment extends Fragment {

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity.
     */
    public interface TaskCallbacks {
        void onPreExecute();

        void onProgressUpdate(int percent);

        void onCancelled();

        void onPostExecute(String message);
    }

    private TaskCallbacks mCallbacks;
    private CopyAsyncTask mTask;
    private boolean actionType;
    private boolean messageType;
    private Context ctx;

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (TaskCallbacks) activity;
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);


    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public void StartCopyTask(Activity directoryDetailActivity, ArrayList<File> src, ArrayList<File> desc, boolean actionType) {
        // Create and execute the background task.
        this.actionType = actionType;
        mTask = new CopyAsyncTask();
        mTask.execute(src, desc);
    }

    /**
     * A LoadInBackground  that performs some (dumb) background work and
     * proxies progress updates and results back to the Activity.
     * <p>
     * Note that we need to check if the callbacks are null in each
     * method in case they are invoked after the Activity's and
     * Fragment's onDestroy() method have been called.
     */
    private class CopyAsyncTask extends AsyncTask<ArrayList<File>, Integer, Void> {

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
            }
        }

        /**
         * Note that we do NOT call the callback object's methods
         * directly from the background thread, as this could result
         * in a race condition.
         */
        @Override
        protected Void doInBackground(ArrayList<File>... lists) {
            ArrayList<File> source = lists[0];
            ArrayList<File> destination = lists[1];
            int DEFAULT_BUFFER_SIZE = 32 * 1024;
            for (int i = 0; i < destination.size(); i++) {
                File newFile = new File(destination.get(i), source.get(i).getName());
                File oldFile = source.get(i);
                BufferedInputStream inStream = null;
                BufferedOutputStream outStream = null;
                if (!newFile.exists()) {
                    try {
                        inStream = new BufferedInputStream(new FileInputStream(oldFile));
                        outStream = new BufferedOutputStream(new FileOutputStream(newFile));
                        // Transfer bytes from in to out
                        byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
                        int len;
                        while ((len = inStream.read(buf)) > 0) {
                            outStream.write(buf, 0, len);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            outStream.close();
                            inStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (MediaVaultController.getInstance() != null) {
                        FileUtils.scanFile(MediaVaultController.getInstance(), newFile);
                        if (actionType) {
                            try {
                                FileUtils.deleteFile(oldFile);
                                FileUtils.deleteDirectory(oldFile.getParentFile());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            FileUtils.scanFile(MediaVaultController.getInstance(), oldFile);
                        }
                    }
                } else {
                    messageType = true;
                }

            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            if (mCallbacks != null) {
                // mCallbacks.onProgressUpdate(percent[0]);
            }
        }

        @Override
        protected void onCancelled() {
            if (mCallbacks != null) {
                mCallbacks.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(Void ignore) {
            if (mCallbacks != null) {
                if (messageType) {
                    if (actionType)
                        mCallbacks.onPostExecute(getString(R.string.some_file_not_moved));
                    else mCallbacks.onPostExecute(getString(R.string.already_exists_file_in_copy));
                } else {
                    if (actionType)
                        mCallbacks.onPostExecute(getString(R.string.move_file_message));
                    else mCallbacks.onPostExecute(getString(R.string.copy_file_message));
                }
                messageType = false;
            }
        }
    }

    // check that async task is running status
    public boolean isRunningAsync() {
        if (mTask != null)
            if (mTask.getStatus() == AsyncTask.Status.RUNNING) {
                return true;
            }
        return false;
    }
}