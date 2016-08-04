package com.skystreamtv.element_ez_stream.updater.background;


import android.os.Parcel;
import android.os.Parcelable;

public class FailureReason implements Parcelable{

    protected String failure_reason = "";

    public FailureReason(String failure_reason) {
        this.failure_reason = failure_reason;
    }

    protected FailureReason(Parcel in) {
        this.failure_reason = in.readString();
    }

    public static final Creator<FailureReason> CREATOR = new Creator<FailureReason>() {
        @Override
        public FailureReason createFromParcel(Parcel in) {
            return new FailureReason(in);
        }

        @Override
        public FailureReason[] newArray(int size) {
            return new FailureReason[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(failure_reason);
    }

    public String getFailureReason() {
        return failure_reason;
    }
}
