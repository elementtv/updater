package com.skystreamtv.element_ez_stream.updater.model;


import android.os.Parcel;
import android.os.Parcelable;

public class Skin implements Parcelable{
    public static final Creator<Skin> CREATOR = new Creator<Skin>() {
        @Override
        public Skin createFromParcel(Parcel in) {
            return new Skin(in);
        }

        @Override
        public Skin[] newArray(int size) {
            return new Skin[size];
        }
    };
    private String screenshot_url;
    private String name;
    private String description;
    private String download_url;
    private boolean enabled;
    private int version;

    public Skin(String screenshot_url, String name, String description, String download_url, boolean enabled, int version) {
        this.screenshot_url = screenshot_url;
        this.name = name;
        this.description = description;
        this.download_url = download_url;
        this.enabled = enabled;
        this.version = version;
    }

    protected Skin(Parcel in) {
        screenshot_url = in.readString();
        name = in.readString();
        description = in.readString();
        download_url = in.readString();
        enabled = in.readInt() != 0;
        version = in.readInt();
    }

    public String getDownloadUrl() {
        return download_url;
    }

    public int getVersion() {
        return version;
    }

    public String getScreenshotUrl() {
        return screenshot_url;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(screenshot_url);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(download_url);
        dest.writeInt(enabled ? 1 : 0);
        dest.writeInt(version);
    }

    @Override
    public String toString() {
        return getDescription();
    }
}

