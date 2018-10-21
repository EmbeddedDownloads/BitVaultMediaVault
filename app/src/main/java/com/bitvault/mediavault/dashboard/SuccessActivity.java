package com.bitvault.mediavault.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.baseclass.BaseActivity;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.utils.BitVaultFont;
import com.bitvault.mediavault.utils.Roboto;
import com.bitvault.mediavault.utils.RobotoBold;
import com.bitvault.mediavault.utils.RobotoItalic;
import com.bitvault.mediavault.utils.RobotoLightItalic;
import com.bitvault.mediavault.utils.RobotoMedium;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import model.WalletDetails;
import utils.SDKUtils;

/**
 * Created by vvdn on 9/14/2017.
 */

public class SuccessActivity extends BaseActivity {
    @BindView(R.id.wallet_info_back)
    BitVaultFont walletInfoBack;
    @BindView(R.id.toolbarTitle)
    RobotoBold toolbarTitle;
    @BindView(R.id.delete_icon)
    BitVaultFont deleteIcon;
    @BindView(R.id.img)
    ImageView img;
    @BindView(R.id.bitCoinsText)
    Roboto bitCoinsText;
    @BindView(R.id.txtMediaVaultMsg)
    Roboto txtMediaVaultMsg;
    @BindView(R.id.transaction_txt)
    RobotoMedium transactionTxt;
    @BindView(R.id.trans)
    TextView trans;
    @BindView(R.id.btn_done)
    RobotoItalic btnDone;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private Bundle mBundle;
    private AlertDialog backPressDialog;
    private String TAG = SuccessActivity.class.getSimpleName();
    private String mEotWalletAddress = "", mWalletType;
    private double mEotCoinsTotal;
    private WalletDetails mWalletDetails;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.success_activity);
        ButterKnife.bind(this);
        initAndSetData();
    }

    private void initAndSetData() {
        img.setImageResource(R.mipmap.mediavault_success);
        deleteIcon.setVisibility(View.GONE);
        walletInfoBack.setVisibility(View.GONE);
        if (getIntent().getExtras() != null) {
            mBundle = getIntent().getExtras();
        }
        if (mBundle != null) {
            txtMediaVaultMsg.setText(getResources().getString(R.string.file_sent));
            trans.setText(mBundle.getString(Constant.TXID));
            if (mBundle != null && mBundle.getString(Constant.WALLET_TYPE) != null) {
                mWalletType = mBundle.getString(Constant.WALLET_TYPE);
            }
            if (mWalletType != null && mWalletType.equalsIgnoreCase(getResources().getString(com.embedded.wallet.R.string.wallet_eot))) {
                if (getIntent() != null && getIntent().getStringExtra(Constant.EOT_WALLET_ADDRESS) != null) {
                    mEotWalletAddress = getIntent().getStringExtra(Constant.EOT_WALLET_ADDRESS);
                }
                mEotCoinsTotal = getIntent().getDoubleExtra(Constant.EOT_WALLET_BAL, 0.0);
                bitCoinsText.setText(mBundle.getString(Constant.MESSAGE_FEE) + " " +
                        getResources().getString(R.string.bitcoinText) + "\n" + getString(R.string.eot_wallet_msg));
            } else {
                if (mBundle.getSerializable(Constant.WALLET_DETAILS) != null) {
                    mWalletDetails = (WalletDetails) mBundle.getSerializable(Constant.WALLET_DETAILS);
                    bitCoinsText.setText(mBundle.getString(Constant.MESSAGE_FEE) + " " +
                            getResources().getString(R.string.bitcoinText) + "\n" + mWalletDetails.getWALLET_NAME());
                }
            }
        }
    }

    @OnClick({R.id.wallet_info_back, R.id.btn_done})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.wallet_info_back:
                // onBackPressed();
                break;
            case R.id.btn_done:
                clickOnDoneButton();
                break;
        }
    }

    private void clickOnDoneButton() {
        // TODO: 9/15/2017 Store data into local database

        // Moving  to main page
        if (progressBar != null && progressBar.getVisibility() != View.VISIBLE) {
            Intent homeIntent = new Intent(SuccessActivity.this, LandingActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        } else {
            SDKUtils.showToast(this, getString(R.string.record_saving_on_play_store));
        }
    }

    @Override
    public void onBackPressed() {
        //showConfirmDialog();
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
                // Moving  to main page
                Intent homeIntent = new Intent(SuccessActivity.this, LandingActivity.class);
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
}
