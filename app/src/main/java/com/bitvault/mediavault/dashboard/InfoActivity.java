package com.bitvault.mediavault.dashboard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.model.ImageDataModel;

import butterknife.BindView;
import butterknife.ButterKnife;
import utils.SDKUtils;

//This class is used for show the file information
public class InfoActivity extends AppCompatActivity {

    @BindView(R.id.txt_date)
    TextView txtDate;
    @BindView(R.id.txt_time)
    TextView txtTime;
    @BindView(R.id.txt_path)
    TextView txtPath;
    @BindView(R.id.txt_size)
    TextView txtSize;
    ImageDataModel imageDataModel;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private int height;
    private int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        /**
         * Registering the ButterKnife view Injection
         */
        ButterKnife.bind(this);

        initialisingToolBar();
        getIntentData();
    }

    /**
     * Get data from intent
     */
    private void getIntentData() {
        if (getIntent() != null) {
            imageDataModel = getIntent().getParcelableExtra(Constant.DATA);
        }
        if (imageDataModel != null) {
            setDataOnView(imageDataModel);
        }
    }

    // Initialising the toolbar
    private void initialisingToolBar() {
        /**
         * Initialising the shared preference class instance.
         */
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(R.string.Info);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        /**
         * Enable the back key on navigation bar in toolbar
         */
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_back);
    }

    /**
     * Set data on info page from model class
     *
     * @param model
     */
    private void setDataOnView(ImageDataModel model) {
        if (FileUtils.isVideoFile(model.getFile().getAbsolutePath())) {
            MediaMetadataRetriever mdr = new MediaMetadataRetriever();
//            if (Build.VERSION.SDK_INT >= 14)
//                mdr.setDataSource(model.getFile().getAbsolutePath(), new HashMap<String, String>());
//            else
//                mdr.setDataSource(model.getFile().getAbsolutePath());
            SDKUtils.showErrorLog("path...", model.getFile().getAbsolutePath());
            mdr.setDataSource(model.getFile().getAbsolutePath());
            height = Integer.parseInt(mdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            width = Integer.parseInt(mdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        } else if (FileUtils.isImageFile(model.getFile().getAbsolutePath())) {
            Bitmap bitmap = BitmapFactory.decodeFile(model.getFile().getAbsolutePath());
            if (bitmap != null) {
                height = bitmap.getHeight();
                width = bitmap.getWidth();
            }
        }
        txtDate.setText(FileUtils.getLastModifiedNew(model.getFile()));
        txtTime.setText(FileUtils.getDateNew(model.getFile()));
        String name = model.getFile().getName();
        String actualName = FileUtils.getSecureFileName(name);
        if (actualName != null && !actualName.equalsIgnoreCase("")) {
            txtPath.setText(actualName);
        } else {
            txtPath.setText(name);
        }
        if (height != 0 && width != 0) {
            float megaPixelSize = (float) (height * width) / 1024000f;
            float roundOff = Math.round(megaPixelSize * 100) / 100f;
            String megaPixel = String.valueOf(roundOff);
            String data = megaPixel + "MP" + "  " + width + " x " + height + "   " + FileUtils.getSize(this, model.getFile());
            txtSize.setText(data);
        } else {
            txtSize.setText(FileUtils.getSize(this, model.getFile()));
        }


    }

    /**
     * Perform action as the menu items click
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
