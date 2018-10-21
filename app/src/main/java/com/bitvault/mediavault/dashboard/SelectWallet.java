package com.bitvault.mediavault.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.adapter.WalletEotAdapter;
import com.bitvault.mediavault.adapter.WalletListAdaptor;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.utils.BitVaultFont;
import com.bitvault.mediavault.utils.RobotoBold;

import java.util.ArrayList;

import bitmanagers.BitVaultWalletManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import commons.SecureSDKException;
import iclasses.WalletArrayCallback;
import model.WalletDetails;

/**
 * This class is used for Selecting wallet to uploading the file.
 */

public class SelectWallet extends AppCompatActivity implements WalletArrayCallback {
    @BindView(R.id.wallet_info_back)
    BitVaultFont walletInfoBack;
    @BindView(R.id.toolbarTitle)
    RobotoBold toolbarTitle;
    @BindView(R.id.select_wallet_recycle_view)
    RecyclerView selectWalletRecycleView;
    private ArrayList<WalletDetails> mRequestedWallets;
    private ArrayList<ImageDataModel> dataModelArrayList = new ArrayList<>();
    private BitVaultWalletManager mBitVaultWalletManager = null;
    private String mWalletType;
    private double mEotCoinsTotal;
    private String mEotWalletAddress = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_wallet);
        ButterKnife.bind(this);
        mBitVaultWalletManager = BitVaultWalletManager.getWalletInstance();
        setData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null && getIntent().getStringExtra(Constant.WALLET_TYPE) != null) {
            mWalletType = getIntent().getStringExtra(Constant.WALLET_TYPE);
            if (mWalletType != null && mWalletType.equals(getResources().getString(R.string.wallet_eot))) {
                if (getIntent() != null && getIntent().getStringExtra(Constant.EOT_WALLET_ADDRESS) != null) {
                    mEotWalletAddress = getIntent().getStringExtra(Constant.EOT_WALLET_ADDRESS);
                }
                mEotCoinsTotal = getIntent().getDoubleExtra(Constant.EOT_WALLET_BAL, 0.0);
                setAdapter(mEotCoinsTotal);
            } else {
                mRequestedWallets = new ArrayList<>();
                getWalletsSDK();
            }
        }
    }

    /**
     * Method is used to set Adapter
     *
     * @param walletBeanClassList - Arraylist of WalletBeanClass
     */
    private void setAdapter(ArrayList<WalletDetails> walletBeanClassList) {
        selectWalletRecycleView.setLayoutManager(new LinearLayoutManager(this));
        WalletListAdaptor adaptor = new WalletListAdaptor(walletBeanClassList, SelectWallet.this);
        selectWalletRecycleView.setAdapter(adaptor);
    }

    /**
     * Method is used to set Adapter
     *
     * @param mEotCoinsTotal
     */
    private void setAdapter(double mEotCoinsTotal) {
        selectWalletRecycleView.setLayoutManager(new LinearLayoutManager(this));
        WalletEotAdapter adaptor = new WalletEotAdapter(mEotCoinsTotal, SelectWallet.this);
        selectWalletRecycleView.setAdapter(adaptor);
    }

    /**
     * Method is used to set data
     */

    private void setData() {
        toolbarTitle.setText(getResources().getString(R.string.select_wallet));
        mRequestedWallets = new ArrayList<>();
        if (getIntent() != null) {
            dataModelArrayList = getIntent().getParcelableArrayListExtra(Constant.DATA);
        }
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
                    mBitVaultWalletManager.getWallets(SelectWallet.this);
                } catch (SecureSDKException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * Method is called when user will click on any wallet from the list
     *
     * @param mWalletBeanClass -- selected wallet
     */
    public void navigateUser(WalletDetails mWalletBeanClass) {
        Bundle mBundle = new Bundle();
        Intent uploadFile = new Intent(SelectWallet.this, UploadFile.class);
        mBundle.putSerializable(Constant.WALLET_DETAILS, mWalletBeanClass);
        mBundle.putParcelableArrayList(Constant.DATA, dataModelArrayList);
        mBundle.putString(Constant.WALLET_TYPE, mWalletType);
        uploadFile.putExtras(mBundle);
        startActivity(uploadFile);
    }

    /**
     * Method is called when user will click on eot wallet
     *
     * @param position -- selected wallet
     */
    public void navigateUser(int position) {
        Bundle mBundle = new Bundle();
        Intent uploadFile = new Intent(SelectWallet.this, UploadFile.class);
        mBundle.putDouble(Constant.EOT_WALLET_BAL, mEotCoinsTotal);
        mBundle.putString(Constant.EOT_WALLET_ADDRESS, mEotWalletAddress);
        mBundle.putString(Constant.WALLET_TYPE, mWalletType);
        mBundle.putParcelableArrayList(Constant.DATA, dataModelArrayList);
        uploadFile.putExtras(mBundle);
        startActivity(uploadFile);
    }

    /**
     * Callback of getWallets from SDK
     *
     * @param mWallets - Wallet Details object
     */

    @Override
    public void getWallets(ArrayList<WalletDetails> mWallets) {
        if (mRequestedWallets != null && mRequestedWallets.size() > 0) {
            mRequestedWallets.clear();
        }
        mRequestedWallets.addAll(mWallets);
        setAdapter(mRequestedWallets);
    }


    @OnClick(R.id.wallet_info_back)
    public void onViewClicked() {
        finish();
    }
}
