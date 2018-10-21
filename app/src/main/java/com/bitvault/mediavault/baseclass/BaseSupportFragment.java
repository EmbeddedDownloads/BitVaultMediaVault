package com.bitvault.mediavault.baseclass;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.bitvault.mediavault.R;


/**
 * Created by vvdn on 6/1/2017.
 */

public class BaseSupportFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
        setRetainInstance(true);
    }


    /**
     * Shows a {@link Snackbar} message.
     *
     * @param message An string representing a message to be shown.
     */
    protected void showSnackMessage(String message) {
        if (this.getActivity() != null)
            Snackbar.make(this.getView(), message, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", null).show();
    }

    /**
     * Method for replacing the fragment with animation
     *
     * @param containerViewId
     * @param fragment
     */
    protected void replaceFragment(int containerViewId, android.support.v4.app.Fragment fragment) {
        String backStateName = fragment.getClass().getName();

        android.support.v4.app.FragmentManager manager = this.getFragmentManager();
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
     * method to replace fragment without animation
     *
     * @param fragment
     */
    protected void replaceFragmentWithoutAnimation(Fragment fragment,int id) {
        if (fragment != null) {
            String backStateName = fragment.getClass().getName();

            android.support.v4.app.FragmentManager manager = this.getFragmentManager();
            boolean fragmentPopped = manager
                    .popBackStackImmediate(backStateName, 0);
            // fragment not in back stack, create it.
            if (!fragmentPopped && manager.findFragmentByTag(backStateName) == null) {
                FragmentTransaction ft = manager.beginTransaction();
                ft.replace(id, fragment, backStateName);
                ft.addToBackStack(backStateName);
                ft.commitAllowingStateLoss();
            }
        }
    }
}
