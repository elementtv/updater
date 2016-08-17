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

    private int id;
    private String screenshot_url;
    private String name;
    private String description;
    private String download_url;
    private boolean enabled;
    private int version;
    private boolean upToDate;
    private boolean installed;
    private String details;

    public Skin(int id, String screenshot_url, String name, String description, String download_url, boolean enabled, int version, String details) {
        this.id = id;
        this.screenshot_url = screenshot_url;
        this.name = name;
        this.description = description;
        this.download_url = download_url;
        this.enabled = enabled;
        this.version = version;
        this.details = details;
    }

    public Skin(int id, String name, int version) {
        this.id = id;
        this.name = name;
        this.version = version;
    }

    public Skin() {
    }

    protected Skin(Parcel in) {
        id = in.readInt();
        screenshot_url = in.readString();
        name = in.readString();
        description = in.readString();
        download_url = in.readString();
        enabled = in.readInt() != 0;
        version = in.readInt();
        upToDate = in.readInt() != 0;
        installed = in.readInt() != 0;
        details = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDownloadUrl() {
        return download_url;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getScreenshotUrl() {
        return screenshot_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isUpToDate() {
        return upToDate;
    }

    public void setUpToDate(boolean upToDate) {
        this.upToDate = upToDate;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(screenshot_url);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(download_url);
        dest.writeInt(enabled ? 1 : 0);
        dest.writeInt(version);
        dest.writeInt(upToDate ? 1 : 0);
        dest.writeInt(installed ? 1 : 0);
        dest.writeString(details);
    }

    @Override
    public String toString() {
        return "Skin{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", version=" + version +
                '}';
    }
}

