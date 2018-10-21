package com.bitvault.mediavault.baseclass;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.bitvault.mediavault.R;

/**
 * Created by vvdn on 6/1/2017.
 */

public abstract class BaseActivity extends AppCompatActivity {
   // @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//      //  this.overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
//
//    }
    /**
     * Method for replacing the fragment with animation
     * @param containerViewId
     * @param fragment
     */
    protected void replaceFragment(int containerViewId, android.support.v4.app.Fragment fragment) {
        String backStateName = fragment.getClass().getName();

        android.support.v4.app.FragmentManager manager = this.getSupportFragmentManager();
        boolean fragmentPopped = manager
                .popBackStackImmediate(backStateName, 0);
        // fragment not in back stack, create it.
        if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
            FragmentTransaction ft = manager.beginTransaction();
            ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
            ft.replace(containerViewId, fragment, backStateName);
            ft.addToBackStack(backStateName);
            ft.commit();
        }

    }
    /**
     * Method for replacing the fragment without animation
     * @param containerViewId
     * @param fragment
     */
   /* protected void replaceFragmentWithoutAnimation(int containerViewId, android.support.v4.app.Fragment fragment) {
        String backStateName = fragment.getClass().getName();

        android.support.v4.app.FragmentManager manager = this.getSupportFragmentManager();
        boolean fragmentPopped = manager
                .popBackStackImmediate(backStateName, 0);
        // fragment not in back stack, create it.
        if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(containerViewId, fragment, backStateName);
            ft.addToBackStack(backStateName);
            ft.commit();
        }

    }*/
}
