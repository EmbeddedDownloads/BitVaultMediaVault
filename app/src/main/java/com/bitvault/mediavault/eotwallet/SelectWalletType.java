package com.bitvault.mediavault.eotwallet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.VolleyError;
import com.bitvault.mediavault.R;
import com.bitvault.mediavault.adapter.WalletTypeListAdapter;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.dashboard.SelectWallet;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.utils.BitVaultFont;
import com.bitvault.mediavault.utils.Roboto;
import com.bitvault.mediavault.utils.RobotoBold;
import com.bitvault.mediavault.utils.Utils;

import java.util.ArrayList;

import bitmanagers.BitVaultWalletManager;
import bitmanagers.EOTWalletManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import commons.SecureSDKException;
import iclasses.EotCallbacks.EotWalletCallback;
import iclasses.WalletArrayCallback;
import iclasses.WalletBalanceCallback;
import model.WalletBalanceModel;
import model.WalletDetails;
import model.eotmodel.EotWalletDetail;

/**
 * Selecting wallet Type to do payment for message sending.
 */
public class SelectWalletType extends AppCompatActivity implements View.OnClickListener,
        WalletArrayCallback, WalletBalanceCallback, EotWalletCallback {

    @BindView(R.id.toolbarTitle)
    RobotoBold toolbarTitle;
    @BindView(R.id.payWith)
    Roboto payWith;
    @BindView(R.id.delete_icon)
    BitVaultFont deleteIcon;
    @BindView(R.id.img)
    ImageView mediaIcon;
    @BindView(R.id.txtMediaVaultMsg)
    Roboto txtMediaVaultMsg;
    @BindView(R.id.rvWalletType)
    RecyclerView rvWalletType;
    @BindView(R.id.parentLayout)
    LinearLayout parentLayout;
    private String from = "", mReceiverAddress = "";
    private BitVaultWalletManager mBitVaultWalletManager = null;
    private EOTWalletManager mEotWalletManager = null;
    private double mBitcoinsTotal = 0.0;
    private double mEotCoinsTotal = 0.0;
    private String mEotWalletAddress = "";
    private ArrayList<ImageDataModel> dataModelArrayList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_wallet_type);
        ButterKnife.bind(this);
        mBitVaultWalletManager = BitVaultWalletManager.getWalletInstance();
        mEotWalletManager = EOTWalletManager.getEOTWalletInstance();
        setData();
        getDataFromIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWalletsSDK();
        mEotWalletManager.getEotWallet(this);
    }

    /**
     * Method is used to get data from the intent
     */
    private void getDataFromIntent() {
        dataModelArrayList.clear();
        if (getIntent() != null) {
            if (getIntent().getParcelableArrayListExtra(Constant.DATA) != null) {
                dataModelArrayList = getIntent().getParcelableArrayListExtra(Constant.DATA);
            }
        }
    }

    /**
     * Method is used to set Adapter
     *
     * @param mBitcoinsTotal
     * @param mEotCoinsTotal
     */
    private void setAdapter(double mBitcoinsTotal, double mEotCoinsTotal) {
        rvWalletType.setLayoutManager(new LinearLayoutManager(this));
        WalletTypeListAdapter adaptor = new WalletTypeListAdapter(mBitcoinsTotal, mEotCoinsTotal, SelectWalletType.this);
        rvWalletType.setAdapter(adaptor);
    }


    /**
     * Method is used to set data
     */

    private void setData() {
        toolbarTitle.setText(getResources().getString(R.string.select_wallet_type));
        payWith.setText(getResources().getString(R.string.you_want_to_pay_with));
        deleteIcon.setVisibility(View.INVISIBLE);
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
                    mBitVaultWalletManager.getWallets(SelectWalletType.this);
                } catch (SecureSDKException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View mView) {

        switch (mView.getId()) {

            case R.id.wallet_info_back:
                onBackPressed();
                break;
        }
    }

    /**
     * Method is called when user will click on any wallet type from the list
     *
     * @param walletTypePosition -- selected wallet type
     */
    public void navigateUser(int walletTypePosition) {
        Bundle mBundle = new Bundle();
        Intent selectWallet = new Intent(SelectWalletType.this, SelectWallet.class);
        if (walletTypePosition == 0) {
            if (mBitcoinsTotal <= 0) {
                Utils.showSnakbar(parentLayout, getResources().getString(R.string.insufficient_balance), Snackbar.LENGTH_SHORT);
            } else {
                selectWallet.putParcelableArrayListExtra(Constant.DATA, dataModelArrayList);
                selectWallet.putExtra(Constant.WALLET_TYPE, getResources().getString(R.string.wallet_bitcoin));
                selectWallet.putExtras(mBundle);
                startActivity(selectWallet);
            }
        } else if (walletTypePosition == 1) {
            if (mEotCoinsTotal <= 0) {
                Utils.showSnakbar(parentLayout, getResources().getString(R.string.insufficient_balance), Snackbar.LENGTH_SHORT);
            } else {
                selectWallet.putExtra(Constant.WALLET_TYPE, getResources().getString(com.embedded.wallet.R.string.wallet_eot));
                selectWallet.putParcelableArrayListExtra(Constant.DATA, dataModelArrayList);
                selectWallet.putExtra(Constant.EOT_WALLET_BAL, mEotCoinsTotal);
                selectWallet.putExtra(Constant.EOT_WALLET_ADDRESS, mEotWalletAddress);
                selectWallet.putExtras(mBundle);
                startActivity(selectWallet);
            }
        }

    }


    /**
     * Callback of getWallets from SDK
     *
     * @param mWallets - Wallet Details object
     */

    @Override
    public void getWallets(ArrayList<WalletDetails> mWallets) {
        mBitcoinsTotal=0.0;
        int walletListSize = mWallets.size();
        for (int i = 0; i < walletListSize; i++) {
            mBitcoinsTotal += Double.parseDouble(mWallets.get(i).getWALLET_LAST_UPDATE_BALANCE());
        }
    }

    @Override
    public void successWalletBalanceCallback(WalletBalanceModel mWalletBalanceModel) {
        mEotCoinsTotal=0.0;
        if (mWalletBalanceModel.getmWalletBalance() != null) {
            mEotCoinsTotal = Double.parseDouble(mWalletBalanceModel.getmWalletBalance());
        }
        setAdapter(mBitcoinsTotal, mEotCoinsTotal);
    }

    @Override
    public void failedWalletBalanceCallback(VolleyError mError) {
        Utils.showSnakbar(parentLayout, getResources().getString(R.string.EotBalNotUpdated), Snackbar.LENGTH_SHORT);
        setAdapter(mBitcoinsTotal, mEotCoinsTotal);
    }

    @Override
    public void eotWallet(EotWalletDetail mEotWalletDetail) {
        if (mEotWalletDetail != null) {
            mEotWalletAddress = mEotWalletDetail.getAddress();
        } else {
            Utils.showSnakbar(parentLayout, getResources().getString(R.string.EotBalNotUpdated), Snackbar.LENGTH_SHORT);
        }
        mEotWalletManager.getBalance(this);
    }

    @OnClick(R.id.wallet_info_back)
    public void onViewClicked() {
        finish();
    }
}
