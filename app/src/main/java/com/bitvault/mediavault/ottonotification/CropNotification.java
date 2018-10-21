package com.bitvault.mediavault.ottonotification;

import java.io.File;

/**
 * Created by vvdn on 7/22/2017.
 */

public class CropNotification {
    private File data;
    private boolean newFileStatus;

    public CropNotification(File data, boolean newFileStatus) {
        this.data = data;
        this.newFileStatus = newFileStatus;
    }

    public File getData() {
        return data;
    }

    public boolean getStatus() {
        return newFileStatus;
    }
}
