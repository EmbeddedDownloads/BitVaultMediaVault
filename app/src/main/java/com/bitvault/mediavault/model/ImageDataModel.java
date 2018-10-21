package com.bitvault.mediavault.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.bitvault.mediavault.application.MediaVaultController;
import com.bitvault.mediavault.common.Constant;
import com.bitvault.mediavault.helper.FileUtils;
import com.bitvault.mediavault.helper.SharedPref;

import java.io.File;

/*
* Model class for storing the media files  from device and shows on grid,its implements the parcelable
* class to passing the whole model object to other activity/fragment
* */
public class ImageDataModel implements Parcelable, Comparable<ImageDataModel> {
    private String imageTitle;
    private String path;
    private String type;
    private String folderName;
    private String count;
    private String bucketId;
    private String txid;
    private String fileEncTxid;
    private String fileEncKey;
    private String walletAddress;
    private String fileUniqueId;
    private String crc;

    public String getCrc() {
        return crc;
    }

    public void setCrc(String crc) {
        this.crc = crc;
    }

    public String getFileUniqueId() {
        return fileUniqueId;
    }

    public void setFileUniqueId(String fileUniqueId) {
        this.fileUniqueId = fileUniqueId;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public String getFileEncTxid() {
        return fileEncTxid;
    }

    public void setFileEncTxid(String fileEncTxid) {
        this.fileEncTxid = fileEncTxid;
    }

    public String getFileEncKey() {
        return fileEncKey;
    }

    public void setFileEncKey(String fileEncKey) {
        this.fileEncKey = fileEncKey;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }

    public String getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(String fileStatus) {
        this.fileStatus = fileStatus;
    }

    private String fileStatus;
    private int itemPosition;
    private boolean selected;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getItemPosition() {
        return itemPosition;
    }

    public void setItemPosition(int itemPosition) {
        this.itemPosition = itemPosition;
    }

    public String getTimeDuration() {
        return timeDuration;
    }

    public void setTimeDuration(String timeDuration) {
        this.timeDuration = timeDuration;
    }

    private String timeDuration;
    private long size, albumId, dateModified, duration;
    private File file, directoryFile;
    private boolean isVideo, isAudio, isImage, isGif;

    public String getImageTitle() {
        return imageTitle;
    }

    public void setImageTitle(String imageTitle) {
        this.imageTitle = imageTitle;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getBucketId() {
        return bucketId;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.size = duration;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getDirFile() {
        return directoryFile;
    }

    public void setDirFile(File file) {
        this.directoryFile = file;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public boolean isAudio() {
        return isAudio;
    }

    public void setAudio(boolean audio) {
        isAudio = audio;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean image) {
        isImage = image;
    }

    public boolean isGif() {
        return isGif;
    }

    public void setGif(boolean gif) {
        isGif = gif;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.imageTitle);
        dest.writeString(this.path);
        dest.writeString(this.type);
        dest.writeString(this.folderName);
        dest.writeString(this.count);
        dest.writeString(this.bucketId);
        dest.writeString(this.timeDuration);

        dest.writeString(this.txid);
        dest.writeString(this.fileEncKey);
        dest.writeString(this.fileEncTxid);
        dest.writeString(this.walletAddress);
        dest.writeString(this.fileStatus);
        dest.writeString(this.fileUniqueId);
        dest.writeLong(this.size);
        dest.writeInt(this.itemPosition);
        dest.writeLong(this.albumId);
        dest.writeLong(this.dateModified);
        dest.writeLong(this.duration);
        dest.writeSerializable(this.file);
        dest.writeSerializable(this.directoryFile);
        dest.writeByte(this.selected ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isVideo ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isAudio ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isImage ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isGif ? (byte) 1 : (byte) 0);
        dest.writeString(this.crc);
    }

    public ImageDataModel() {
    }

    protected ImageDataModel(Parcel in) {
        this.imageTitle = in.readString();
        this.path = in.readString();
        this.type = in.readString();
        this.folderName = in.readString();
        this.count = in.readString();
        this.bucketId = in.readString();
        this.timeDuration = in.readString();

        this.txid = in.readString();
        this.fileEncKey = in.readString();
        this.fileEncTxid = in.readString();
        this.walletAddress = in.readString();
        this.fileStatus = in.readString();
        this.fileUniqueId = in.readString();
        this.size = in.readLong();
        this.itemPosition = in.readInt();
        this.albumId = in.readLong();
        this.dateModified = in.readLong();
        this.duration = in.readLong();
        this.file = (File) in.readSerializable();
        this.directoryFile = (File) in.readSerializable();
        this.selected = in.readByte() != 0;
        this.isVideo = in.readByte() != 0;
        this.isAudio = in.readByte() != 0;
        this.isImage = in.readByte() != 0;
        this.isGif = in.readByte() != 0;
        this.crc = in.readString();
    }

    public static final Parcelable.Creator<ImageDataModel> CREATOR = new Parcelable.Creator<ImageDataModel>() {
        @Override
        public ImageDataModel createFromParcel(Parcel source) {
            return new ImageDataModel(source);
        }

        @Override
        public ImageDataModel[] newArray(int size) {
            return new ImageDataModel[size];
        }
    };

    @Override
    public int compareTo(ImageDataModel imageDataModel) {
        int res = 0;
        if (SharedPref.getSecureTab(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.SECURE_TAB)) {
            res = FileUtils.compareDate(this.file, imageDataModel.getFile());
        } else if (SharedPref.getAlbumType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.ALBUM_VIEW)) {
            if (SharedPref.getSortType(MediaVaultController.getSharedPreferencesInstance()).equals("0")) {
                res = FileUtils.compareDate(this.file, imageDataModel.getFile());
            } else if (SharedPref.getSortType(MediaVaultController.getSharedPreferencesInstance()).equals("1")) {
                res = FileUtils.compareSizeHighToLow(this.file, imageDataModel.getFile());
            } else if (SharedPref.getSortType(MediaVaultController.getSharedPreferencesInstance()).equals("2")) {
                res = FileUtils.compareSizeLowToHigh(this.file, imageDataModel.getFile());
            } else if (SharedPref.getSortType(MediaVaultController.getSharedPreferencesInstance()).equals("3")) {
                res = FileUtils.compareDate(this.file, imageDataModel.getFile());
            }
        } else if (SharedPref.getListType(MediaVaultController.getSharedPreferencesInstance()).equals(Constant.LIST_VIEW)) {
            res = FileUtils.compareDate(this.file, imageDataModel.getFile());
        }
        return res;
    }

    @Override
    public boolean equals(Object object) {
        boolean result = false;
        if (object == null || object.getClass() != getClass()) {
            result = false;
        } else {
            ImageDataModel dataModel = (ImageDataModel) object;
            if (this.file != null && dataModel.getFile() != null)
                if (this.file.equals(dataModel.getFile())) {
                    result = true;
                }
        }
        return result;
    }


}
