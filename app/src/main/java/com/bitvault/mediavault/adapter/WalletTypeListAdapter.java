package com.bitvault.mediavault.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.eotwallet.SelectWalletType;
import com.bitvault.mediavault.utils.BitVaultFont;
import com.bitvault.mediavault.utils.Roboto;
import com.bitvault.mediavault.utils.RobotoLight;
import com.bitvault.mediavault.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * this class is used to set the adaptor of wallet types
 */
public class WalletTypeListAdapter extends RecyclerView.Adapter<WalletTypeListAdapter.WalletHolder> {

    private final String TAG = getClass().getSimpleName();
    private double mBitcoinsTotal = 0;
    private double mEotCoinsTotal = 0;
    private final Context mContext;

    /**
     * @param bitcoinsTotal,eotCoinsTotal
     * @param mContext
     */
    public WalletTypeListAdapter(double bitcoinsTotal, double eotCoinsTotal, Context mContext) {
        this.mContext = mContext;
        this.mBitcoinsTotal = bitcoinsTotal;
        this.mEotCoinsTotal = eotCoinsTotal;
    }

    @Override
    public WalletHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_wallet_type, parent, false);
        WalletHolder holder = new WalletHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(WalletHolder holder, final int position) {
        if (position == 0) {
            holder.typelogoImageView.setBackground(mContext.getDrawable(R.drawable.bitcoinstype));
            holder.walletTypeNameTextView.setText(mContext.getString(R.string.bit_coin_wallet_name));
            holder.logoTextView.setText("n");
            holder.walletAvailableCoinTextView.setText(" " + Utils.convertDecimalFormatPattern(mBitcoinsTotal));
        } else {
            holder.typelogoImageView.setBackground(mContext.getDrawable(R.drawable.eottype));
            holder.walletTypeNameTextView.setText(mContext.getString(R.string.eot_wallet_name));
            holder.logoTextView.setText("");
            holder.walletAvailableCoinTextView.setText(mContext.getResources().getString(R.string.wallet_eot) + " " +
                    Utils.convertDecimalFormatPattern(mEotCoinsTotal));
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((SelectWalletType) mContext).navigateUser(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    static class WalletHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.typelogoImageView)
        ImageView typelogoImageView;
        @BindView(R.id.walletTypeNameTextView)
        RobotoLight walletTypeNameTextView;
        @BindView(R.id.logoTextView)
        BitVaultFont logoTextView;
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