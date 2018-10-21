package com.bitvault.mediavault.croplibrary;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.bitvault.mediavault.R;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.model.ImageDataModel;

import butterknife.BindView;
import butterknife.ButterKnife;

//This class is use for crop a file
public class CropMainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private boolean check = true;
    private FragmentManager fm;
    private CropMainFragment cropMainFragment;
    private static final String TAG_TASK_FRAGMENT = "crop_fragment";
    private boolean intentStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_main);
        ButterKnife.bind(this);

        toolbar.setTitle(R.string.editor);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        /**
         * Disable the back key on navigation bar in toolbar
         */
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(false);
        }
        fm = getSupportFragmentManager();
        cropMainFragment = (CropMainFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (cropMainFragment == null) {
            cropMainFragment = new CropMainFragment();
            ImageDataModel dataModel = getIntent().getExtras().getParcelable(Constant.DATA);
            intentStatus = getIntent().getExtras().getBoolean(Constant.INTENT_STATUS);
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constant.DATA, dataModel);
            bundle.putBoolean(Constant.INTENT_STATUS, intentStatus);
            cropMainFragment.setArguments(bundle);
            fm.beginTransaction().replace(R.id.container, cropMainFragment, TAG_TASK_FRAGMENT).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.crop_menu, menu);
        return true;
    }

    //Invoked fragment onOptionsItemSelected menu from activity class
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return cropMainFragment != null && cropMainFragment.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

}
