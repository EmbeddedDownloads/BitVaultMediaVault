package com.bitvault.mediavault.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.baseclass.BaseActivity;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.database.MediaVaultLocalDb;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.utils.BitVaultFont;
import com.bitvault.mediavault.utils.Roboto;
import com.bitvault.mediavault.utils.RobotoBold;
import com.bitvault.mediavault.utils.RobotoLightItalic;
import com.bitvault.mediavault.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import bitmanagers.BitVaultAppStoreManager;
import bitmanagers.BitVaultDataManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import commons.SDKHelper;
import iclasses.mediavaultcallback.MediaFileDetailsOperationCallBack;
import iclasses.mediavaultcallback.SendMediaVaultCallback;
import model.DataMoverModel;
import model.MediaVaultDataToPBCModel;
import model.WalletDetails;

/**
 * Created by vvdn on 9/14/2017.
 */

public class SendingActivity extends BaseActivity implements SendMediaVaultCallback, MediaFileDetailsOperationCallBack {

    @BindView(R.id.wallet_info_back)
    BitVaultFont walletInfoBack;
    @BindView(R.id.toolbarTitle)
    RobotoBold toolbarTitle;
    @BindView(R.id.delete_icon)
    BitVaultFont deleteIcon;
    @BindView(R.id.txt_secure_msg)
    Roboto txtSecureMsg;
    @BindView(R.id.circle_progress_bar)
    ProgressBar circleProgressBar;
    @BindView(R.id.progress)
    RelativeLayout progress;
    @BindView(R.id.parentLayout)
    LinearLayout parentLayout;
    private BitVaultDataManager mediaVaultDataManager;
    private ArrayList<ImageDataModel> dataModelArrayList;
    private ArrayList<ImageDataModel> failedMediaFileList;
    private DataMoverModel mDataMoverModel;
    private AlertDialog backPressDialog;
    private WalletDetails mSelectedWallet;
    private String txid;
    private MediaVaultLocalDb secureMediaFileDb;
    private String TAG = SendingActivity.class.getSimpleName();
    private int mCounter = 0, mCallBackCounter = 0, mCounterSuccess = 0;
    private BitVaultAppStoreManager bitVaultAppStoreManager;
    private String fileLocation;
    private boolean backGroundCheck, fileStatus;
    private String mEotWalletAddress = "", mWalletType;
    private double mEotCoinsTotal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sending_activity);
        ButterKnife.bind(this);
        initAndSetData();
        getDataFromIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        backGroundCheck = true;
    }

    /**
     * To pause the running task.
     */
    @Override
    protected void onPause() {
        super.onPause();
        backGroundCheck = false;
    }

    /**
     * Method to initialise and set data.
     */
    private void initAndSetData() {
        toolbarTitle.setText(getResources().getString(R.string.sending));
        walletInfoBack.setVisibility(View.GONE);
        txtSecureMsg.setText(getResources().getString(R.string.secure_file_sending));
        deleteIcon.setVisibility(View.GONE);
        dataModelArrayList = new ArrayList<>();
        failedMediaFileList = new ArrayList<>();
        mediaVaultDataManager = BitVaultDataManager.getSecureMessangerInstance();
        bitVaultAppStoreManager = BitVaultAppStoreManager.getAppStoreManagerInstance();
        mDataMoverModel = new DataMoverModel();
        secureMediaFileDb = MediaVaultLocalDb.getMediaVaultDatabaseInstance(this);
    }


    /**
     * Method is used to get data from the intent
     */
    private void getDataFromIntent() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle mBundleData = getIntent().getExtras();

            if (mBundleData != null && mBundleData.getString(Constant.WALLET_TYPE) != null) {
                mWalletType = mBundleData.getString(Constant.WALLET_TYPE);
            }
            if (mWalletType != null && mWalletType.equalsIgnoreCase(getResources().getString(com.embedded.wallet.R.string.wallet_eot))) {
                if (getIntent() != null && getIntent().getStringExtra(Constant.EOT_WALLET_ADDRESS) != null) {
                    mEotWalletAddress = getIntent().getStringExtra(Constant.EOT_WALLET_ADDRESS);
                }
                mEotCoinsTotal = getIntent().getDoubleExtra(Constant.EOT_WALLET_BAL, 0.0);
            } else {
                if (mBundleData != null && mBundleData.getSerializable(Constant.WALLET_DETAILS) != null) {
                    mSelectedWallet = (WalletDetails) mBundleData.getSerializable(Constant.WALLET_DETAILS);
                }
            }
            dataModelArrayList = mBundleData.getParcelableArrayList(Constant.FILE_LIST);
            mCounter = dataModelArrayList.size();
        }
        if (mCounter > 0) {
            uploadData(mCallBackCounter);
        }
    }

    /**
     * Method to use upload data on PBC
     *
     * @param count
     */
    private void uploadData(int count) {
        if (Utils.isNetworkConnected(SendingActivity.this)) {
            if (getIntent().getExtras().getString(Constant.TXID) != null) {
                if (mWalletType != null && mWalletType.equalsIgnoreCase(getResources().getString(com.embedded.wallet.R.string.wallet_eot))) {
                    mediaVaultDataManager.uploadMediaVaultFile(-1,
                            calculateSizeInLong(), setUpDataMoverModel(count), SendingActivity.this,
                            getIntent().getExtras().getString(Constant.TXID), getResources().getString(R.string.webserver_key_value), mEotWalletAddress, mWalletType);
                } else {
                    if (mSelectedWallet != null)
                        mediaVaultDataManager.uploadMediaVaultFile(Integer.parseInt(mSelectedWallet.getWALLET_ID()),
                                calculateSizeInLong(), setUpDataMoverModel(count), SendingActivity.this,
                                getIntent().getExtras().getString(Constant.TXID), getResources().getString(R.string.webserver_key_value), "", mWalletType);
                }

            } else {
                if (mWalletType != null && mWalletType.equalsIgnoreCase(getResources().getString(com.embedded.wallet.R.string.wallet_eot))) {
                    mediaVaultDataManager.uploadMediaVaultFile(-1,
                            calculateSizeInLong(), setUpDataMoverModel(count), SendingActivity.this, "",
                            getResources().getString(R.string.webserver_key_value), mEotWalletAddress, mWalletType);
                } else {
                    if (mSelectedWallet != null)
                        mediaVaultDataManager.uploadMediaVaultFile(Integer.parseInt(mSelectedWallet.getWALLET_ID()),
                                calculateSizeInLong(), setUpDataMoverModel(count), SendingActivity.this, "",
                                getResources().getString(R.string.webserver_key_value), "", mWalletType);
                }

            }
        } else {
            Utils.showSnakbar(parentLayout, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
        }
    }

    /**
     * method to upload data on pbc in sequence
     *
     * @param count
     */
    private void uploadDataInSequence(int count) {
        if (txid != null) {
            if (mWalletType != null && mWalletType.equalsIgnoreCase(getResources().getString(com.embedded.wallet.R.string.wallet_eot))) {
                mediaVaultDataManager.uploadMediaVaultFile(-1,
                        calculateSizeInLong(), setUpDataMoverModel(count), SendingActivity.this, txid,
                        getResources().getString(R.string.webserver_key_value), mEotWalletAddress, mWalletType);
            } else {
                if (mSelectedWallet != null)
                    mediaVaultDataManager.uploadMediaVaultFile(Integer.parseInt(mSelectedWallet.getWALLET_ID()),
                            calculateSizeInLong(), setUpDataMoverModel(count), SendingActivity.this,
                            txid, getResources().getString(R.string.webserver_key_value), "", mWalletType);
            }
        }
    }

    /**
     * Method to create data to send
     *
     * @return
     */
    private DataMoverModel setUpDataMoverModel(int count) {
        if (mDataMoverModel == null) {
            mDataMoverModel = new DataMoverModel();
        }
        if (mDataMoverModel != null) {
            mDataMoverModel.setMessage_tag(SDKHelper.MEDIA_VAULT_TAG);
            mDataMoverModel.setPbcId(SDKHelper.TAG_PBC_ID);
            fileLocation = dataModelArrayList.get(count).getFile().getAbsolutePath();
            mDataMoverModel.setMessageFiles(dataModelArrayList.get(count).getFile().getAbsolutePath());
            if (mWalletType != null && mWalletType.equalsIgnoreCase(getResources().getString(com.embedded.wallet.R.string.wallet_eot))) {
                mDataMoverModel.setReceiverAddress(Constant.EOT_RECEIVER_ADDRESS);
            } else
                mDataMoverModel.setReceiverAddress(Constant.BTC_RECEIVER_ADDRESS);
        }
        return mDataMoverModel;
    }

    @Override
    public void onBackPressed() {
        showConfirmDialog();
    }

    /**
     * Handling back press event
     */
    private void showConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = View.inflate(this, R.layout.alert_dialog_layout, null);
        builder.setView(view);
        RobotoLightItalic okBtn = (RobotoLightItalic) view.findViewById(R.id.okButton);
        RobotoLightItalic cancelBtn = (RobotoLightItalic) view.findViewById(R.id.cancelButton);
        RobotoLightItalic message = (RobotoLightItalic) view.findViewById(R.id.message);
        RobotoBold title = (RobotoBold) view.findViewById(R.id.title);
        backPressDialog = builder.create();
        backPressDialog.show();
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backPressDialog.dismiss();
                Intent homeIntent = new Intent(SendingActivity.this, LandingActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backPressDialog.dismiss();
            }
        });
    }

    /**
     * Method to return file size in KB
     *
     * @return
     */

    private long calculateSizeInLong() {
        long mSize = 0;
        if (dataModelArrayList != null && dataModelArrayList.size() > 0) {
            for (int fileSize = 0; fileSize < dataModelArrayList.size(); fileSize++) {
                if (dataModelArrayList.get(fileSize).getFile() != null)
                    mSize += dataModelArrayList.get(fileSize).getFile().length();
            }
        }
        if (mSize != 0) {
            mSize = mSize / 1024;
        } else {
            mSize = 0;
        }
        return mSize;
    }

    @Override
    public void sendMediaVaultCallback(String status, String message, final MediaVaultDataToPBCModel dataToPBCModel) {
        String filePath = fileLocation;
        if (dataToPBCModel != null && filePath != null)
            dataToPBCModel.setFileLocation(filePath);
        mCallBackCounter++;
        if (status.equalsIgnoreCase(SDKHelper.TAG_CAP_OK)
                || status.equalsIgnoreCase(SDKHelper.TAG_SUCCESS)
                || message.equalsIgnoreCase(getString(R.string.file_uploaded))
                ) {
            mCounterSuccess++;
            // After successful uploading data on PBC now call to play store api for updating file
            // info for restore purpose in future
            if (dataToPBCModel != null && dataToPBCModel.getFileLocation() != null) {
                uploadDataOnPlayStore(dataToPBCModel);
                // insert data into local database with status
                insertInLocalDatabase(dataToPBCModel, Constant.STATUS_SUCCESS);
            }
            if (mCallBackCounter == mCounter && mCounterSuccess == mCounter) {
                mCallBackCounter = 0;
                mCounter = 0;
                mCounterSuccess = 0;
                Intent successIntent = new Intent(this, SuccessActivity.class);
                Bundle mBundle = new Bundle();
                if (mWalletType != null && mWalletType.equalsIgnoreCase(getResources().getString(com.embedded.wallet.R.string.wallet_eot))) {
                    mBundle.putDouble(Constant.EOT_WALLET_BAL, mEotCoinsTotal);
                    mBundle.putString(Constant.EOT_WALLET_ADDRESS, mEotWalletAddress);
                } else {
                    mBundle.putSerializable(Constant.WALLET_DETAILS, mSelectedWallet);
                }
                mBundle.putString(Constant.WALLET_TYPE, mWalletType);
                mBundle.putParcelable(Constant.DATA, dataToPBCModel);
                mBundle.putSerializable(Constant.MESSAGE_FEE, getIntent().getExtras().getString(Constant.MESSAGE_FEE));
                if (dataToPBCModel != null) {
                    mBundle.putString(Constant.TXID, dataToPBCModel.getTxId());
                }
                successIntent.putExtras(mBundle);
                startActivity(successIntent);
            } else {
                if (mCallBackCounter < mCounter && dataToPBCModel != null && dataToPBCModel.getTxId() != null) {
                    txid = dataToPBCModel.getTxId();
                    uploadDataInSequence(mCallBackCounter);
                }
            }

        } else {
            ImageDataModel dataModel;
            dataModel = new ImageDataModel();
            if (dataToPBCModel != null) {
                dataModel.setFile(new File(dataToPBCModel.getFileLocation()));
                failedMediaFileList.add(dataModel);
            }
            if (mCallBackCounter < mCounter && dataToPBCModel != null && dataToPBCModel.getTxId() != null) {
                txid = dataToPBCModel.getTxId();
                uploadDataInSequence(mCallBackCounter);
            } else {
                Utils.showSnakbar(parentLayout, message, Snackbar.LENGTH_SHORT);
                Intent failureIntent = new Intent(this, FailActivity.class);
                Bundle mBundle = getIntent().getExtras();
                if (dataToPBCModel != null) {
                    mBundle.putString(Constant.TXID, dataToPBCModel.getTxId());
                }
                if (mCounterSuccess > 0) {
                    mBundle.putString(Constant.ERRORMESSAGE, getString(R.string.some_file_uploading_error));
                } else {
                    mBundle.putString(Constant.ERRORMESSAGE, getResources().getString(R.string.error_msg_failed));
                }
                if (mWalletType != null && mWalletType.equalsIgnoreCase(getResources().getString(com.embedded.wallet.R.string.wallet_eot))) {
                    mBundle.putDouble(Constant.EOT_WALLET_BAL, mEotCoinsTotal);
                    mBundle.putString(Constant.EOT_WALLET_ADDRESS, mEotWalletAddress);
                } else {
                    mBundle.putSerializable(Constant.WALLET_DETAILS, mSelectedWallet);
                }
                mBundle.putString(Constant.WALLET_TYPE, mWalletType);
                mBundle.putParcelableArrayList(Constant.DATA, failedMediaFileList);
                mBundle.putSerializable(Constant.MESSAGE_FEE, getIntent().getExtras().getString(Constant.MESSAGE_FEE));
                failureIntent.putExtras(mBundle);
                startActivity(failureIntent);
                mCallBackCounter = 0;
                mCounter = 0;
                mCounterSuccess = 0;
            }

        }
    }

    /**
     * Method to update local database with file details
     *
     * @param dataToPBCModel
     * @param status         file
     */
    private void insertInLocalDatabase(MediaVaultDataToPBCModel dataToPBCModel, String status) {
        if (dataToPBCModel != null) {
            final ImageDataModel dataModel = new ImageDataModel();
            dataModel.setTxid(dataToPBCModel.getTxId());
            dataModel.setCrc(dataToPBCModel.getCrc());
            dataModel.setWalletAddress(dataToPBCModel.getWalletAddress());
            dataModel.setFileUniqueId(dataToPBCModel.getEncryptedUniqueFileId());
            // Get new  secure file location
            dataModel.setFile(storeDataInPrivateStorage(dataToPBCModel.getFileLocation()));
            dataModel.setType(FileUtils.getExtension(dataToPBCModel.getFileLocation()));
            dataModel.setFileEncKey(dataToPBCModel.getEncryptedFileKey());
            dataModel.setFileEncTxid(dataToPBCModel.getEncryptedTXId());
            dataModel.setFileStatus(status);
            // Inserting data into local data after file upload to pbc
            secureMediaFileDb.insertSecureMediaData(dataModel);
        }
    }

    /**
     * Method to upload data on playStore....
     *
     * @param dataToPBCModel
     */
    private void uploadDataOnPlayStore(MediaVaultDataToPBCModel dataToPBCModel) {
        if (dataToPBCModel != null)
            bitVaultAppStoreManager.saveFileDetails(dataToPBCModel.getEncryptedFileKey(), dataToPBCModel.getEncryptedTXId()
                    , new File(dataToPBCModel.getFileLocation()).getName(), dataToPBCModel.getEncryptedUniqueFileId(),
                    FileUtils.getExtension(dataToPBCModel.getFileLocation()), dataToPBCModel.getWalletAddress(),
                    SendingActivity.this);
    }

    /**
     * Method to move file on secure directory and delete old file.
     *
     * @param path path of old file
     * @return secure file location
     */
    private File storeDataInPrivateStorage(String path) {
        // Create directory on root level for storing the Secure files.
        File file = Constant.getSecureFileDir(this);
        if (!file.exists())
            file.mkdirs();
        String randomId = UUID.randomUUID().toString();
        // Get unique file name of secure files
        String newPath = (randomId
                + Constant.SECURE_FILE_SEPARATOR + new File(path).getName());
        File secureFile = new File(file, newPath);
        try {
            BufferedInputStream inStream = null;
            BufferedOutputStream outStream = null;
            int DEFAULT_BUFFER_SIZE = 32 * 1024;
            try {
                inStream = new BufferedInputStream(new FileInputStream(new File(path)));
                outStream = new BufferedOutputStream(new FileOutputStream(secureFile));
                // Transfer bytes from in to outStream
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
            try {
                // method to deleting the selected file
                FileUtils.deleteFile(new File(path));
            } catch (Exception e) {
                e.printStackTrace();
            }
            // method to scan the file and update its position
            FileUtils.scanFile(this, new File(path));
        } catch (Exception e) {
        }
        return secureFile;
    }

    @Override
    public void mediaFileDetailsOperationCallBack(String status, String message, String id) {
        if (status.equalsIgnoreCase(SDKHelper.TAG_CAP_OK)
                || status.equalsIgnoreCase(SDKHelper.TAG_SUCCESS)
                || message.equalsIgnoreCase(getString(R.string.record_added))
                ) {
            secureMediaFileDb.updateFileStatus(id, Constant.STATUS_SUCCESS);
        } else {
            secureMediaFileDb.updateFileStatus(id, Constant.STATUS_FAILED_APP_STORE_ADD);
        }
    }
}
