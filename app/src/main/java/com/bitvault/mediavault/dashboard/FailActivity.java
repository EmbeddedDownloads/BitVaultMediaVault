package com.bitvault.mediavault.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.baseclass.BaseActivity;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.model.ImageDataModel;
import com.bitvault.mediavault.utils.BitVaultFont;
import com.bitvault.mediavault.utils.Roboto;
import com.bitvault.mediavault.utils.RobotoBold;
import com.bitvault.mediavault.utils.RobotoItalic;
import com.bitvault.mediavault.utils.RobotoLightItalic;
import com.bitvault.mediavault.utils.Utils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import model.WalletDetails;

/**
 * Created by vvdn on 9/14/2017.
 */

public class FailActivity extends BaseActivity {
    @BindView(R.id.wallet_info_back)
    BitVaultFont walletInfoBack;
    @BindView(R.id.toolbarTitle)
    RobotoBold toolbarTitle;
    @BindView(R.id.delete_icon)
    BitVaultFont deleteIcon;
    @BindView(R.id.img)
    ImageView img;
    @BindView(R.id.retryButton)
    RobotoItalic retryButton;
    @BindView(R.id.cancel)
    RobotoItalic cancel;
    @BindView(R.id.button)
    LinearLayout button;
    @BindView(R.id.parentLayout)
    RelativeLayout parentLayout;
    @BindView(R.id.txtMediaVaultMsg)
    Roboto txtMediaVaultMsg;
    private Bundle mBundle;
    private ArrayList<ImageDataModel> dataModelArrayList;
    private WalletDetails mSelectedWallet;
    private String messageFee, txid = null;
    private AlertDialog backPressDialog;
    private String mEotWalletAddress = "", mWalletType;
    private double mEotCoinsTotal;
    private WalletDetails mWalletDetails;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.failed_activity);
        ButterKnife.bind(this);
        setData();
        getDataFromIntent();
    }

    /**
     * Set image for failure of sending message
     */
    private void setData() {
        img.setImageResource(R.mipmap.mediavault_fail);
        deleteIcon.setVisibility(View.GONE);
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().getString(Constant.ERRORMESSAGE) != null) {
            txtMediaVaultMsg.setText(getIntent().getExtras().getString(Constant.ERRORMESSAGE));
            txtMediaVaultMsg.setTextColor(getResources().getColor(R.color.red));
        } else {
            txtMediaVaultMsg.setText(getResources().getString(R.string.error_msg_failed));
            txtMediaVaultMsg.setTextColor(getResources().getColor(R.color.red));
        }
    }

    /**
     * Get data from incoming activity through intent
     */
    private void getDataFromIntent() {
        if (getIntent().getExtras() != null) {
            mBundle = getIntent().getExtras();
        }
        if (mBundle != null) {
            if (mBundle.getSerializable(Constant.WALLET_DETAILS) != null) {
                mSelectedWallet = (WalletDetails) mBundle.getSerializable(Constant.WALLET_DETAILS);
                dataModelArrayList = mBundle.getParcelableArrayList(Constant.DATA);
                messageFee = mBundle.getString(Constant.MESSAGE_FEE, "0.0");
                if (mBundle.getString(Constant.TXID) != null)
                    txid = mBundle.getString(Constant.TXID, "0.0");
            }
        }
    }

    @OnClick({R.id.wallet_info_back, R.id.retryButton, R.id.cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.wallet_info_back:
                onBackPressed();
                break;
            case R.id.retryButton:
                if (Utils.isNetworkConnected(this)) {
                    Intent sendingActivity = new Intent(FailActivity.this, SendingActivity.class);
                    sendingActivity.putExtras(getIntent().getExtras());
                    startActivity(sendingActivity);
                    finish();
                } else {
                    Utils.showSnakbar(parentLayout, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_SHORT);
                }
                break;
            case R.id.cancel:
                Intent homeIntent = new Intent(FailActivity.this, LandingActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                break;
        }
    }

    /**
     * Handling back press event
     */
    @Override
    public void onBackPressed() {
        showConfirmDialog();
    }

    /**
     * Confirm Dialog Box will appear
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
                // Sending to main page
                Intent homeIntent = new Intent(FailActivity.this, LandingActivity.class);
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
