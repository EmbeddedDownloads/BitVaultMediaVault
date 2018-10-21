package com.bitvault.mediavault.helper;

import com.squareup.otto.Bus;

/**
 * Created by vvdn on 6/16/2017.
 */

public class GlobalBus {
    /**
     * Make singleton class otto bus event
     */
    private static Bus sBus;
    public static Bus getBus() {
        if (sBus == null)
            sBus = new Bus();
        return sBus;
    }

}
