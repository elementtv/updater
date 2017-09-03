package com.skystreamtv.element_ez_stream.updater.model;


import android.os.Parcel;
import android.os.Parcelable;

@SuppressWarnings("unused")
public class App implements Parcelable {
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
    private String icon_url;
    private String name;
    private String description;
    private String download_url;
    private boolean enabled;
    private int version;
    private int mandatoryVersion;

    public App() {

    }

    public App(String icon_url, String name, String description, String download_url,
            boolean enabled, int version, int mandatoryVersion) {
        this.icon_url = icon_url;
        this.name = name;
        this.description = description;
        this.download_url = download_url;
        this.enabled = enabled;
        this.version = version;
        this.mandatoryVersion = mandatoryVersion;
    }

    protected App(Parcel in) {
        icon_url = in.readString();
        name = in.readString();
        description = in.readString();
        download_url = in.readString();
        enabled = in.readInt() != 0;
        version = in.readInt();
        mandatoryVersion = in.readInt();
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
        dest.writeInt(mandatoryVersion);
    }

    public String getDownloadUrl() {
        return download_url;
    }

    public void setDownloadUrl(String url) {
        download_url = url;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int v) {
        version = v;
    }

    public String getIconUrl() {
        return icon_url;
    }

    public void setIconUrl(String url) {
        icon_url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String n) {
        name = n;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        description = d;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean e) {
        enabled = e;
    }

    public int getMandatoryVersion() {
        return mandatoryVersion;
    }

    public void setMandatoryVersion(int mandatoryVersion) {
        this.mandatoryVersion = mandatoryVersion;
    }
}