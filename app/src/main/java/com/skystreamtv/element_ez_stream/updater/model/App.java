package com.skystreamtv.element_ez_stream.updater.model;


import android.os.Parcel;
import android.os.Parcelable;

public class App implements Parcelable {
    private String icon_url;
    private String name;
    private String description;
    private String download_url;
    private boolean enabled;
    private int version;

    public App() {

    }

    public App(String icon_url, String name, String description, String download_url, boolean enabled, int version) {
        this.icon_url = icon_url;
        this.name = name;
        this.description = description;
        this.download_url = download_url;
        this.enabled = enabled;
        this.version = version;
    }

    protected App(Parcel in) {
        icon_url = in.readString();
        name = in.readString();
        description = in.readString();
        download_url = in.readString();
        enabled = in.readInt() != 0;
        version = in.readInt();
    }

    public static final Creator<App> CREATOR = new Creator<App>() {
        @Override
        public App createFromParcel(Parcel in) {
            return new App(in);
        }

        @Override
        public App[] newArray(int size) {
            return new App[size];
        }
    };

    public void setDownloadUrl(String url) {
        download_url = url;
    }

    public void setVersion(int v) {
        version = v;
    }

    public void setIconUrl(String url) {
        icon_url = url;
    }

    public void setName(String n) {
        name = n;
    }

    public void setDescription(String d) {
        description = d;
    }

    public void setEnabled(boolean e) {
        enabled = e;
    }

    public String getDownloadUrl() {
        return download_url;
    }

    public int getVersion() {
        return version;
    }

    public String getIconUrl() {
        return icon_url;
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
        dest.writeString(icon_url);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(download_url);
        dest.writeInt(enabled ? 1 : 0);
        dest.writeInt(version);
    }
}