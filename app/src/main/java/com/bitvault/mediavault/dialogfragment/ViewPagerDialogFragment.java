package com.bitvault.mediavault.dialogfragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.dashboard.PhotoViewActivity;
import com.bitvault.mediavault.helper.DialogData;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.helper.GlobalBus;
import com.bitvault.mediavault.helper.SimpleDividerItemDecoration;
import com.bitvault.mediavault.ottonotification.DialogDataNotification;
import com.bitvault.mediavault.ottonotification.DialogResponse;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import utils.SDKUtils;

/**
 * Created by vvdn on 7/14/2017.
 */

public class ViewPagerDialogFragment extends DialogFragment implements DialogAdapter.OnItemClickListener {

    @BindView(R.id.recycle_view_folder_grid)
    RecyclerView recycleViewFolderGrid;
    @BindView(R.id.create_new_directory)
    TextView createNewDirectory;
    @BindView(R.id.cancel)
    TextView cancel;
    @BindView(R.id.ok)
    TextView ok;
    @BindView(R.id.ll_bottom)
    LinearLayout llBottom;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    Unbinder unbinder;
    private DialogAdapter directoryAdapter;
    private GridLayoutManager lLayout;
    private ArrayList<String> data = new ArrayList<>();
    private String parentDirectory = null;
    private boolean actionType;
    private String keyName;
    private Handler handler=new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.dialog_fragament_layout, container, false);
        /**
         * Registering the ButterKnife view Injection
         */
        unbinder = ButterKnife.bind(this, view);
        GlobalBus.getBus().register(this);
        getIntentData();
        // initialise adapter view
        initializeAdapter();
        return view;
    }

    /**
     * Get data from intent
     */
    private void getIntentData() {
        Bundle mArgs = getArguments();
        if (mArgs != null)
            actionType = mArgs.getBoolean(Constant.ACTION_TYPE);
        keyName = mArgs.getString(Constant.KEY_NAME);
        if (actionType)
            getDialog().setTitle(R.string.move_here);
        else getDialog().setTitle(R.string.copy_here);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /***
     * Initialise the adapter class and set adapter on recycler view.
     */
    private void initializeAdapter() {
        final int columns = getResources().getInteger(R.integer.album_view);
        lLayout = new GridLayoutManager(getActivity(), columns);
        recycleViewFolderGrid.setHasFixedSize(true);
        recycleViewFolderGrid.setLayoutManager(lLayout);
        data.clear();
        data.addAll(DialogData.keyList);
        data.remove(Constant.AUDIO_FOLDER_NAME);
        directoryAdapter = new DialogAdapter(getActivity(), data);
        /**
         * Registering call back of item click
         */
        //  directoryAdapter.setOnItemClickListener(this);
        recycleViewFolderGrid.setAdapter(directoryAdapter);
        /**
         * Add item Decorator for Grid view in recycler view
         */
        recycleViewFolderGrid.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        directoryAdapter.setOnItemClickListener(this);
        if (data != null && data.size() > 0) {
            progressBar.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Update  UI view when get notification from GalleryHelper class
     *
     * @param s
     */
    @Subscribe
    public void getMessage(DialogDataNotification s) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (progressBar != null)
                    progressBar.setVisibility(View.GONE);
                data.clear();
                data.addAll(DialogData.keyList);
                data.remove(Constant.AUDIO_FOLDER_NAME);
                directoryAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        GlobalBus.getBus().unregister(this);
    }

    @OnClick({R.id.create_new_directory, R.id.cancel, R.id.ok})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.create_new_directory:
                openDialog();
                break;
            case R.id.cancel:
                GlobalBus.getBus().post(new DialogResponse(getString(R.string.cancel)));
                getDialog().dismiss();
                break;
            case R.id.ok:
                if (parentDirectory != null && parentDirectory.length() > 0) {
                    if (FileUtils.getParentName(parentDirectory).equalsIgnoreCase(keyName)) {
                        SDKUtils.showToast(getActivity(), getString(R.string.dialog_same_destination_error));
                    } else {
                        ((PhotoViewActivity) getActivity()).actionCopyOnDialogResponse(parentDirectory, actionType);
                        GlobalBus.getBus().post(new DialogResponse(getString(R.string.ok)));
                        getDialog().dismiss();
                    }
                } else {
                    SDKUtils.showToast(getActivity(), getString(R.string.no_folder_select_alert));
                }


                break;
        }
    }

    /**
     * Method to open dialog
     */
    private void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText input;
        builder.setTitle(R.string.create_directory);
        View view = View.inflate(getActivity(), R.layout.dialog_edit_text, null);
        input = (EditText) view.findViewById(R.id.dialog_edit_text);
        builder.setView(view);
        builder.setPositiveButton(R.string.create, null);
        builder.setNegativeButton(R.string.rename_dialog_cancel_button, null);
        final AlertDialog dialog = builder.create();
        // for showing the keyboard when dialog is open,must call before dialog.show to get desired result
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String directoryName = input.getText().toString().trim();
                String parentDirectoryName = null;
                if (directoryName.length() != 0) {
                    if (directoryName.length() <= 20) {
                        if (FileUtils.getSpecialCharacterValidation(directoryName)) {
                            try {

                                parentDirectoryName = FileUtils.createDirectoryForName(FileUtils.getInternalStorage(), input.getText().toString());
                                parentDirectory = parentDirectoryName;
                            } catch (Exception e) {
                                e.printStackTrace();
                                parentDirectoryName = e.toString();
                                parentDirectory = null;
                            }
                            if (parentDirectory.contains(getString(R.string.directory_created))) {
                                // Method to pass new file path with boolean value to perform action like copy/move as false/true
                                ((PhotoViewActivity) getActivity()).actionCopyOnDialogResponse(FileUtils.getInternalStorage() + "/" +
                                        input.getText().toString(), actionType);
                                //Global bas event fire for update the UI
                                GlobalBus.getBus().post(new DialogResponse(getString(R.string.ok)));
                                dialog.dismiss();
                                getDialog().dismiss();


                            } else
                                SDKUtils.showToast(getActivity(), parentDirectoryName);
                            //Hide the keyboard after dismiss the dialog.
                            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        } else {
                            SDKUtils.showToast(getActivity(), getString(R.string.directory_name_invalid_character));
                        }
                    } else {
                        SDKUtils.showToast(getActivity(), getString(R.string.rename_directory_name_length));
                    }
                } else {
                    SDKUtils.showToast(getActivity(), getString(R.string.error_toast_for_directory));
                }
            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parentDirectory = null;
                dialog.dismiss();
                //Hide the keyboard after dismiss the dialog.
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            }
        });
    }

    @Override
    public void directoryFolderClick(final File file, final int adapterPosition) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    directoryAdapter.toggle(adapterPosition);
                    parentDirectory = file.getParent();
                }
            });
    }


}
