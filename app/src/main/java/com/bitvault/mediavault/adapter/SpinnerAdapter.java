package com.bitvault.mediavault.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.utils.Utils;

import java.util.List;

import model.WalletDetails;

/**
 * Created by vvdn on 9/7/2017.
 */

public class SpinnerAdapter extends ArrayAdapter {
    LayoutInflater flater;
    private List<WalletDetails> selectFilesAdapters;
    private Context context;

    public SpinnerAdapter(Activity context, int resouceId, int textviewId, List<WalletDetails> list) {

        super(context, resouceId, textviewId, list);
        flater = context.getLayoutInflater();
        selectFilesAdapters = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        WalletDetails rowItem = selectFilesAdapters.get(position);

        View rowview = flater.inflate(R.layout.spinner_layout, null, true);

        TextView txtTitle = (TextView) rowview.findViewById(R.id.walletName);
        txtTitle.setText(rowItem.getWALLET_NAME().length() > 15 ? rowItem.getWALLET_NAME().substring(0, 15) : rowItem.getWALLET_NAME());
        TextView mwalletBalance = (TextView) rowview.findViewById(R.id.walletBalance);
        if (rowItem != null && rowItem.getWALLET_LAST_UPDATE_BALANCE() != null && rowItem.getWALLET_LAST_UPDATE_BALANCE().length() > 8) {
            mwalletBalance.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(rowItem.getWALLET_LAST_UPDATE_BALANCE())) + " BTC");
        } else {
            mwalletBalance.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(rowItem.getWALLET_LAST_UPDATE_BALANCE())) + " BTC");
        }

        return rowview;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }


    /**
     * Method to get custom view
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getCustomView(int position, View convertView, ViewGroup parent) {

        WalletDetails rowItem = selectFilesAdapters.get(position);

        View rowview = flater.inflate(R.layout.spinner_layout, null, true);

        TextView txtTitle = (TextView) rowview.findViewById(R.id.walletName);
        txtTitle.setText(rowItem.getWALLET_NAME().length() > 20 ? rowItem.getWALLET_NAME().substring(0, 20) : rowItem.getWALLET_NAME());

        TextView mwalletBalance = (TextView) rowview.findViewById(R.id.walletBalance);

        if (rowItem != null && rowItem.getWALLET_LAST_UPDATE_BALANCE() != null && rowItem.getWALLET_LAST_UPDATE_BALANCE().length() > 8) {
            mwalletBalance.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(rowItem.getWALLET_LAST_UPDATE_BALANCE())) + " BTC");
        } else {
            mwalletBalance.setText(Utils.convertDecimalFormatPattern(Double.parseDouble(rowItem.getWALLET_LAST_UPDATE_BALANCE())) + " BTC");
        }

        return rowview;
    }
}



