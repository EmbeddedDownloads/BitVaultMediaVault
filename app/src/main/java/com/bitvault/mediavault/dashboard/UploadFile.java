package com.bitvault.mediavault.dashboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.adapter.SpinnerAdapter;
import com.bitvault.mediavault.authentication.AuthActivity;
import com.bitvault.mediavault.baseclass.BaseActivity;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.utils.BitVaultFont;
import com.bitvault.mediavault.utils.Roboto;
import com.bitvault.mediavault.utils.RobotoBold;
import com.bitvault.mediavault.utils.RobotoItalic;
import com.bitvault.mediavault.utils.RobotoMedium;
import com.bitvault.mediavault.utils.Utils;

import java.util.ArrayList;

import bitmanagers.BitVaultDataManager;
import bitmanagers.BitVaultWalletManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import commons.SecureSDKException;
import iclasses.FeeCallback;
import iclasses.WalletArrayCallback;
import model.WalletDetails;
import utils.SDKUtils;

/**
 * Created by vvdn on 9/6/2017.
 */

/**
 * this class is used to upload file and show the information fo file and wallets
 */
public class UploadFile extends BaseActivity implements WalletArrayCallback, FeeCallback {
    @BindView(R.id.wallet_info_back)
    BitVaultFont walletInfoBack;
    @BindView(R.id.toolbarTitle)
    RobotoBold toolbarTitle;
    @BindView(R.id.parentLayout)
    RelativeLayout parentLayout;
    @BindView(R.id.sendSpinner)
    AppCompatSpinner sendSpinner;
    @BindView(R.id.file_size)
    Roboto fileSize;
    @BindView(R.id.transaction_fee)
    Roboto transactionFee;
    @BindView(R.id.upload_button)
    RobotoItalic uploadButton;
    @BindView(R.id.cancel)
    RobotoItalic cancel;
    @BindView(R.id.eotTextView)
    RobotoMedium eotTextView;
    private ArrayList<WalletDetails> mRequestedWallets;
    private ArrayList<ImageDataModel> dataModelArrayList;
    private WalletDetails mSelectedWallet;
    private Bundle mBundle;
    private BitVaultDataManager mediaVaultDataManager;
    private String TAG = UploadFile.class.getSimpleName();
    private String mEotWalletAddress = "", mWalletType;
    private double mEotCoinsTotal;
    /**
     * BitVaultWalletManager object reference of the class
     */
    private BitVaultWalletManager mBitVaultWalletManager = null;
    private long mSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_file);
        init();
        initAndSetData();
        setSpinnerListener();
    }

    /**
     * Initialise classes
     */
    private void init() {
        ButterKnife.bind(this);
        mBitVaultWalletManager = BitVaultWalletManager.getWalletInstance();
    }

    /**
     * Set spinner item selection listener
     */
    private void setSpinnerListener() {
        sendSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                mSelectedWallet = mRequestedWallets.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Method is used to set data
     */
    private void initAndSetData() {
        toolbarTitle.setText(getResources().getString(R.string.information));
        mRequestedWallets = new ArrayList<>();
        dataModelArrayList = new ArrayList<>();
        mediaVaultDataManager = BitVaultDataManager.getSecureMessangerInstance();
        Bundle mBundleData = getIntent().getExtras();
        if (mBundleData != null && mBundleData.getString(Constant.WALLET_TYPE) != null) {
            mWalletType = mBundleData.getString(Constant.WALLET_TYPE);
        }
        if (mWalletType != null && mWalletType.equalsIgnoreCase(getResources().getString(com.embedded.wallet.R.string.wallet_eot))) {
            sendSpinner.setVisibility(View.GONE);
            eotTextView.setVisibility(View.VISIBLE);
            if (getIntent() != null && getIntent().getStringExtra(Constant.EOT_WALLET_ADDRESS) != null) {
                mEotWalletAddress = getIntent().getStringExtra(Constant.EOT_WALLET_ADDRESS);
            }
            mEotCoinsTotal = getIntent().getDoubleExtra(Constant.EOT_WALLET_BAL, 0.0);
            eotTextView.setText(Constant.EOT + "  " + Utils.convertDecimalFormatPattern(mEotCoinsTotal));
        } else {
            getWalletsSDK();
            sendSpinner.setVisibility(View.VISIBLE);
            eotTextView.setVisibility(View.GONE);
            if (mBundleData != null && mBundleData.getSerializable(Constant.WALLET_DETAILS) != null)
                mSelectedWallet = (WalletDetails) mBundleData.getSerializable(Constant.WALLET_DETAILS);
        }
        if (mBundleData != null && mBundleData.getParcelableArrayList(Constant.DATA) != null)
            dataModelArrayList = mBundleData.getParcelableArrayList(Constant.DATA);
        // set total size of file(s) on view
        fileSize.setText(calculateSize());
        // call media file fee api
        getMediaFileFee();
    }


    @OnClick({R.id.wallet_info_back, R.id.upload_button, R.id.cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.wallet_info_back:
                finish();
                break;
            case R.id.upload_button:
                if (mWalletType != null && mWalletType.equalsIgnoreCase(getResources().getString(com.embedded.wallet.R.string.wallet_eot))) {
                    if (!transactionFee.getText().toString().equals(Constant.BALANCE_INFO_EOT)) {
                        if (mEotCoinsTotal >= Double.parseDouble(transactionFee.getText().toString().replace(Constant.EOT, ""))) {
                            //Call authentication activity before uploading the data on PBC
                            Intent i = new Intent(this, AuthActivity.class);
                            startActivityForResult(i, Constant.AUTH_ACTIVITY_RESULT_CODE);
                        } else {
                            Utils.showSnakbar(parentLayout, getResources().getString(R.string.insufficient_balance), Snackbar.LENGTH_SHORT);
                        }
                    } else {
                        Utils.showSnakbar(parentLayout, getResources().getString(R.string.message_fee_cannot_be_zero), Snackbar.LENGTH_SHORT);
                    }
                } else {
                    if (!transactionFee.getText().toString().equals(Constant.BALANCE_INFO)) {
                        if (Double.parseDouble(mSelectedWallet.getWALLET_LAST_UPDATE_BALANCE()) >= Double.parseDouble(transactionFee.getText().toString().replace(Constant.BTC, ""))) {
                            //Call authentication activity before uploading the data on PBC
                            Intent i = new Intent(this, AuthActivity.class);
                            startActivityForResult(i, Constant.AUTH_ACTIVITY_RESULT_CODE);
                        } else {
                            Utils.showSnakbar(parentLayout, getResources().getString(R.string.insufficient_balance), Snackbar.LENGTH_SHORT);
                        }
                    } else {
                        Utils.showSnakbar(parentLayout, getResources().getString(R.string.message_fee_cannot_be_zero), Snackbar.LENGTH_SHORT);
                    }
                }

                break;
            case R.id.cancel:
                finish();
                break;
        }
    }

    /*
     *  Get the result back after authentication success.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.AUTH_ACTIVITY_RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                //Upload file after authentication successful.
                uploadFileOnPBC();
            }
        } else {
        }
    }

    /**
     * Method to upload the file on PBC
     */
    private void uploadFileOnPBC() {
        Intent intent = new Intent(this, SendingActivity.class);
        if (mBundle == null) {
            mBundle = getIntent().getExtras();
        }
        mBundle.putString(Constant.WALLET_TYPE, mWalletType);
        mBundle.putParcelableArrayList(Constant.FILE_LIST, dataModelArrayList);
        mBundle.putString(Constant.MESSAGE_FEE, transactionFee.getText().toString());
        if (mWalletType != null && mWalletType.equalsIgnoreCase(getResources().getString(com.embedded.wallet.R.string.wallet_eot))) {
            mBundle.putDouble(Constant.EOT_WALLET_BAL, mEotCoinsTotal);
            mBundle.putString(Constant.EOT_WALLET_ADDRESS, mEotWalletAddress);
        } else {
            mBundle.putSerializable(Constant.WALLET_DETAILS, mSelectedWallet);
        }
        intent.putExtras(mBundle);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // getWalletsSDK();
    }

    /**
     * Method to get Wallets from SDK
     */
    private void getWalletsSDK() {

        Handler mHandler = new Handler();

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mBitVaultWalletManager.getWallets(UploadFile.this);
                } catch (SecureSDKException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void getWallets(ArrayList<WalletDetails> mWallets) {
        if (mRequestedWallets != null && mRequestedWallets.size() > 0) {
            mRequestedWallets.clear();
        }
        mRequestedWallets.addAll(mWallets);
        setSpinner(mRequestedWallets);
    }

    /**
     * Method is used the spinner
     */
    private void setSpinner(ArrayList<WalletDetails> mRequestedWallets) {
        int position = 0, size = 0;
        if (mRequestedWallets != null) {
            size = mRequestedWallets.size();
        }
        for (int i = 0; i < size; i++) {
            if (mRequestedWallets.get(i).getWALLET_ID().equals(mSelectedWallet.getWALLET_ID())) {
                mSelectedWallet = mRequestedWallets.get(i);
                position = i;
            }
        }
        if (size > 0) {
            SpinnerAdapter dataAdapter = new SpinnerAdapter(this, R.layout.wallet_spinner_item, R.layout.spinner_layout, mRequestedWallets);
            sendSpinner.setAdapter(dataAdapter);
            sendSpinner.setSelection(position);

        }
    }

    /**
     * @return total file size
     */
    private String calculateSize() {
        String totalFileSize = null;
        if (dataModelArrayList != null && dataModelArrayList.size() > 0) {
            for (int fileSize = 0; fileSize < dataModelArrayList.size(); fileSize++) {
                mSize += dataModelArrayList.get(fileSize).getFile().length();
            }
        }
        if (mSize != 0) {
            totalFileSize = FileUtils.getTotalFileSize(this, mSize);
        } else {
            totalFileSize = "0";
        }
        return totalFileSize;
    }

    /**
     * Method used to get the message fees
     */
    private void getMediaFileFee() {
        if (mediaVaultDataManager != null) {
            if (Utils.isNetworkConnected(this)) {
                mediaVaultDataManager.getFee(0, calculateSizeInLong(), UploadFile.this);
            } else {
                SDKUtils.showToast(UploadFile.this, getResources().getString(R.string.internet_connection));
            }
        }
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
    public void getFeeCallBack(String status, String message, double bitcoins) {
        if (status.equalsIgnoreCase(Constant.STATUS_OK)) {
            if (mWalletType != null && mWalletType.equalsIgnoreCase(getResources().getString(com.embedded.wallet.R.string.wallet_eot))) {
                transactionFee.setText(Utils.convertDecimalFormatPattern(bitcoins) + getString(R.string.eot));
            } else
                transactionFee.setText(Utils.convertDecimalFormatPattern(bitcoins) + getString(R.string.btc));
        }
    }
}
