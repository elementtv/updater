package com.skystreamtv.element_ez_stream.updater.utils;

import android.content.Context;
import android.util.Log;

import static com.skystreamtv.element_ez_stream.updater.broadcast.AlarmReceiver.SAVED_TIME;

public class WaitTimeHelper {

    private static final long oneDay = 1000 * 60 * 60 * 24; // 24 hours

    public static boolean over24HoursSinceLastUpdate(final Context context) {
        long lastCheck = PreferenceHelper.getPreference(context, SAVED_TIME, 0L);
        long currentTime = System.currentTimeMillis();
        Log.e("Time", "difference: " + Math.abs(currentTime - lastCheck));
        return Math.abs(currentTime - lastCheck) >= oneDay;
    }
}
