package com.bitvault.mediavault.ottonotification;

/**
 * Created by vvdn on 6/16/2017.
 */

/**
 * Its passing the notification from Gallery helper class to fragment /activity classes
 */
public class LandingFragmentNotification {

    private String data;

    public LandingFragmentNotification(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
}
