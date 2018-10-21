package com.bitvault.mediavault.croplibrary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.baseclass.BaseSupportFragment;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.helper.GlobalBus;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.ottonotification.CropNotification;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import utils.SDKUtils;

/**
 * Created by vvdn on 7/18/2017.
 */
//This class is used for crop a file

public class CropMainFragment extends BaseSupportFragment
        implements CropImageView.OnSetImageUriCompleteListener, CropImageView.OnCropImageCompleteListener {

    //region: Fields and Consts

    //  private CropDemoPreset mDemoPreset;

    private CropImageView mCropImageView;
    private ImageDataModel dataModel;
    private Handler handler;
    private AlertDialog fineNameDialog, deleteDialog;
    private boolean intentStatus;

    /**
     * Set the initial rectangle to use.
     */
    public void setInitialCropRect() {
        mCropImageView.setCropRect(new Rect(100, 300, 500, 1200));
    }

    /**
     * Reset crop window to initial rectangle.
     */
    public void resetCropRect() {
        mCropImageView.resetCropRect();
    }

    public void updateCurrentCropViewOptions() {
        CropImageViewOptions options = new CropImageViewOptions();
        options.scaleType = mCropImageView.getScaleType();
        options.cropShape = mCropImageView.getCropShape();
        options.guidelines = mCropImageView.getGuidelines();
        options.aspectRatio = mCropImageView.getAspectRatio();
        options.fixAspectRatio = mCropImageView.isFixAspectRatio();
        options.showCropOverlay = mCropImageView.isShowCropOverlay();
        options.showProgressBar = mCropImageView.isShowProgressBar();
        options.autoZoomEnabled = mCropImageView.isAutoZoomEnabled();
        options.maxZoomLevel = mCropImageView.getMaxZoom();
        options.flipHorizontally = mCropImageView.isFlippedHorizontally();
        options.flipVertically = mCropImageView.isFlippedVertically();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_rect, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mCropImageView = (CropImageView) view.findViewById(R.id.cropImageView);
        mCropImageView.setOnSetImageUriCompleteListener(this);
        mCropImageView.setOnCropImageCompleteListener(this);
        dataModel = getActivity().getIntent().getExtras().getParcelable(Constant.DATA);
        intentStatus = getActivity().getIntent().getExtras().getBoolean(Constant.INTENT_STATUS);
        handler = new Handler();
        if (savedInstanceState == null) {
            mCropImageView.setImageUriAsync(Uri.fromFile(dataModel.getFile()));
        }

        updateCurrentCropViewOptions();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.main_action_crop) {
            mCropImageView.getCroppedImageAsync();
            return true;
        } else if (item.getItemId() == R.id.main_action_rotate) {
            mCropImageView.rotateImage(90);
            return true;
        } else if (item.getItemId() == R.id.main_action_flip_horizontally) {
            mCropImageView.flipImageHorizontally();
            return true;
        } else if (item.getItemId() == R.id.main_action_flip_vertically) {
            mCropImageView.flipImageVertically();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mCropImageView != null) {
            mCropImageView.setOnSetImageUriCompleteListener(null);
            mCropImageView.setOnCropImageCompleteListener(null);
        }
    }

    @Override
    public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
        if (error == null) {
        } else {
            Toast.makeText(getActivity(), getString(R.string.image_load_failed) + error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
        handleCropResult(result);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            handleCropResult(result);
        }
    }

    /**
     * Handle crop result of image
     *
     * @param result
     */
    private void handleCropResult(CropImageView.CropResult result) {
        if (result.getError() == null) {
            if (result.getUri() != null) {
            } else {
                Bitmap bitmap = mCropImageView.getCropShape() == CropImageView.CropShape.OVAL
                        ? CropImage.toOvalBitmap(result.getBitmap())
                        : result.getBitmap();
                fileNameDialog(bitmap);

            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.crop_failed) + result.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Save new file in phone
     *
     * @param image    new crop file bitmap
     * @param fileName name of file with full directory path
     * @return
     */
    public boolean saveImageToInternalStorage(Bitmap image, String fileName) {
        try {
            // Use the compress method on the Bitmap object to write image to
            // the OutputStream
            final File newFile = new File(dataModel.getFile().getParent(), fileName);
            final File oldFile = (dataModel.getFile());
            BufferedOutputStream ous = null;
            //  FileOutputStream fos = new FileOutputStream(newFile);
            ous = new BufferedOutputStream(new FileOutputStream(newFile));
            // Writing the bitmap to the output stream
            image.compress(Bitmap.CompressFormat.JPEG, 100, ous);
            ous.flush();
            ous.close();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (newFile.equals(oldFile)) {
                        FileUtils.scanFile(getActivity(), newFile);
                    } else {
                        FileUtils.scanFile(getActivity(), oldFile);
                        FileUtils.scanFile(getActivity(), newFile);
                    }
                }
            }, 100);

            // check the coming intent i.e coming from DirectoryDetailActivity/PhotoViewActivity,
            // So that update the view pager data on PhotoViewActivity
            if (intentStatus) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (newFile.equals(oldFile)) {
                            GlobalBus.getBus().post(new CropNotification(oldFile, false));
                        } else {
                            GlobalBus.getBus().post(new CropNotification(newFile, true));
                        }
                    }
                }, 500);

            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Save bit map of file into phone in background
     *
     * @param bitmap
     * @param fileName
     */
    private void overWriteFileInBackground(Bitmap bitmap, String fileName) {
        Observable<Boolean> listObservable = Observable.just(saveImageToInternalStorage(bitmap, fileName));
        listObservable.subscribe(new Observer<Boolean>() {
            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().finish();
                    }
                }, 500);
            }

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(@NonNull Boolean aBoolean) {

            }
        });

    }

    /**
     * Show dialog  for new crop file
     *
     * @param bitmap crop new file bitmap
     */
    private void fileNameDialog(final Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText input;
        builder.setTitle(R.string.rename_dialog_title);
        View view = View.inflate(getActivity(), R.layout.dialog_edit_text, null);

        input = (EditText) view.findViewById(R.id.dialog_edit_text);

        builder.setView(view);
        builder.setPositiveButton(R.string.ok, null);
        builder.setNegativeButton(R.string.cancel, null);
        fineNameDialog = builder.create();
        if (dataModel.getFile().getName() != null)
            input.setText((FileUtils.removeExtension(dataModel.getFile().getName())));
        input.setSelection(input.length());
        // for showing the keyboard when dialog is open,must call before dialog.show to get desired result
        fineNameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        fineNameDialog.show();
        fineNameDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = input.getText().toString().trim();
                if (fileName.length() != 0) {
                    if (fileName.length() <= 40) {
                        if (FileUtils.getSpecialCharacterValidation(fileName)) {
                            final File newFile = new File(dataModel.getFile().getParent(), fileName + "." + FileUtils.getExtension(dataModel.getFile().getName()));
                            if (newFile.exists()) {
                                showWarningDialog(bitmap, fileName + "." + FileUtils.getExtension(dataModel.getFile().getName()));
                            } else {
                                overWriteFileInBackground(bitmap, fileName + "." + FileUtils.getExtension(dataModel.getFile().getName()));
                                fineNameDialog.dismiss();
                            }

                            //Hide the keyboard after dismiss the dialog.
                            fineNameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                        } else {
                            SDKUtils.showToast(getActivity(), getString(R.string.file_name_invalid_character));
                        }
                    } else {
                        SDKUtils.showToast(getActivity(), getString(R.string.rename_file_name_length));
                    }
                } else {
                    SDKUtils.showToast(getActivity(), getString(R.string.empty_dialog_message));
                }
            }
        });
        fineNameDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fineNameDialog.dismiss();
                //Hide the keyboard after dismiss the dialog.
                fineNameDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            }
        });
    }

    /**
     * method to show warning dialog for fle
     *
     * @param bitmap   bitmap of crop file
     * @param fileName name of file
     */
    private void showWarningDialog(final Bitmap bitmap, final String fileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = View.inflate(getActivity(), R.layout.overwrite_dialog, null);
        builder.setView(view);
        TextView okBtn = (TextView) view.findViewById(R.id.alert_yes_button);
        TextView cancelBtn = (TextView) view.findViewById(R.id.alert_no_button);
        deleteDialog = builder.create();
        deleteDialog.show();
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // method to delete file after click on OK button
                // method to delete file after click on OK button
                overWriteFileInBackground(bitmap, fileName);
                deleteDialog.dismiss();
                if (fineNameDialog != null) {
                    fineNameDialog.dismiss();
                }
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteDialog.dismiss();
            }
        });
    }


}
