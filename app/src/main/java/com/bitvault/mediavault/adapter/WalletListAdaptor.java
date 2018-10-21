package com.bitvault.mediavault.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.dashboard.SelectWallet;
import com.bitvault.mediavault.utils.BitVaultFont;
import com.bitvault.mediavault.utils.Roboto;
import com.bitvault.mediavault.utils.RobotoLight;
import com.bitvault.mediavault.utils.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import model.WalletDetails;


/**
 * this class is used to set the adaptor of wallet
 */

public class WalletListAdaptor extends RecyclerView.Adapter<WalletListAdaptor.WalletHolder> {

    private final String TAG = getClass().getSimpleName();
    private final List<WalletDetails> walletBeanClassList;
    private final Context mContext;

    /**
     * @param walletBeanClassList list of all wallet
     * @param mContext            context from where this adaptor is called
     */
    public WalletListAdaptor(List<WalletDetails> walletBeanClassList, Context mContext) {
        this.walletBeanClassList = walletBeanClassList;
        this.mContext = mContext;
    }

    @Override
    public WalletHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_wallet_adapter, parent, false);
        WalletHolder holder = new WalletHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(WalletHolder holder, int position) {
        final WalletDetails mWalletDetails = walletBeanClassList.get(position);
        holder.walletNameTextView.setText(mWalletDetails.getWALLET_NAME());
        holder.walletAvailableCoinTextView.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(mWalletDetails.getWALLET_LAST_UPDATE_BALANCE())));
        if (mWalletDetails.getWALLET_ICON() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(mWalletDetails.getWALLET_ICON(), 0, mWalletDetails.getWALLET_ICON().length);
            BitmapDrawable ob = new BitmapDrawable(mContext.getResources(), bitmap);
            holder.logoImageView.setBackground(ob);
        } else {
            holder.logoImageView.setBackgroundResource(R.drawable.wallet_logo);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mWalletDetails != null)
                    ((SelectWallet) mContext).navigateUser(mWalletDetails);
            }
        });

    }

    @Override
    public int getItemCount() {
        return walletBeanClassList.size();
    }

    static class WalletHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.logoImageView)
        ImageView logoImageView;
        @BindView(R.id.walletNameTextView)
        RobotoLight walletNameTextView;
        @BindView(R.id.bitcoinlogoTextView)
        BitVaultFont bitcoinlogoTextView;
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

