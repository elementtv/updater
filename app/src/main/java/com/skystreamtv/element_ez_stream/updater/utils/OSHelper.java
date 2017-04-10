package com.skystreamtv.element_ez_stream.updater.utils;

import android.os.Build;
import android.util.Log;

import static com.skystreamtv.element_ez_stream.updater.utils.Constants.KODI_LOCATION;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.KODI_LOCATION_V16;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.LOCATION_JSON_FILE;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.LOCATION_JSON_FILE_V16;

public class OSHelper {

    public static boolean isOSAboveKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static String getLocationsFile() {
        if (isOSAboveKitKat()) {
            Log.d("OS", "OS is above kitkat");
            return LOCATION_JSON_FILE;
        } else {
            Log.d("OS", "OS is kitkat");
            return LOCATION_JSON_FILE_V16;
        }
    }

    public static String getKodiApp() {
        if (isOSAboveKitKat()) {
            Log.d("OS", "OS is above kitkat");
            return KODI_LOCATION;
        } else {
            Log.d("OS", "OS is kitkat");
            return KODI_LOCATION_V16;
        }
    }
}
