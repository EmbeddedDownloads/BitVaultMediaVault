package com.bitvault.mediavault.adapter;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.dashboard.SelectWallet;
import com.bitvault.mediavault.utils.Roboto;
import com.bitvault.mediavault.utils.RobotoLight;
import com.bitvault.mediavault.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * this class is used to set the adaptor of eot wallet
 */

public class WalletEotAdapter extends RecyclerView.Adapter<WalletEotAdapter.WalletHolder> {

    private final String TAG = getClass().getSimpleName();
    private final Context mContext;
    private double mEotCoinsTotal = 0.0;

    /**
     * @param eotCoinsTotal
     * @param mContext      context from where this adaptor is called
     */
    public WalletEotAdapter(double eotCoinsTotal, Context mContext) {
        this.mContext = mContext;
        this.mEotCoinsTotal = eotCoinsTotal;
    }

    @Override
    public WalletHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_eot_wallet, parent, false);
        WalletHolder holder = new WalletHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final WalletHolder holder, final int position) {

        holder.walletAvailableCoinTextView.setText(mContext.getResources().getString(R.string.wallet_eot) + " " +
                Utils.convertDecimalFormatPattern(mEotCoinsTotal));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mEotCoinsTotal <= 0.0){
                    Utils.showSnakbar(holder.parentRelativeLayout, mContext.getResources().getString(R.string.insufficient_balance), Snackbar.LENGTH_SHORT);

                }else {
                    ((SelectWallet) mContext).navigateUser(position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return 1;
    }

    static class WalletHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.eotWalletNameTextView)
        RobotoLight eotWalletNameTextView;
        @BindView(R.id.walletAvailableCoinTextView)
        Roboto walletAvailableCoinTextView;
        @BindView(R.id.walletAvailableCoinLay)
        LinearLayout walletAvailableCoinLay;
        @BindView(R.id.parentRelativeLayout)
        RelativeLayout parentRelativeLayout;

        WalletHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}

