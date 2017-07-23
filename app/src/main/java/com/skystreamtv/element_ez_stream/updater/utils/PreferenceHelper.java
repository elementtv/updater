package com.skystreamtv.element_ez_stream.updater.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Preference Helper
 */
public class PreferenceHelper {

    public static void savePreference(Context context, String name, int preference) {
        getSharedPreferences(context).edit().putInt(name, preference).apply();
    }

    public static void savePreference(Context context, String name, String preference) {
        getSharedPreferences(context).edit().putString(name, preference).apply();
    }

    public static void savePreference(Context context, String name, long preference) {
        getSharedPreferences(context).edit().putLong(name, preference).apply();
    }

    public static void savePreference(Context context, String name, boolean preference) {
        getSharedPreferences(context).edit().putBoolean(name, preference).apply();
    }

    public static boolean getPreference(Context context, String name, boolean defaultValue) {
        return getSharedPreferences(context).getBoolean(name, defaultValue);
    }

    public static int getPreference(Context context, String name, int defaultValue) {
        return getSharedPreferences(context).getInt(name, defaultValue);
    }

    public static String getPreference(Context context, String name, String defaultValue) {
        return getSharedPreferences(context).getString(name, defaultValue);
    }

    public static long getPreference(Context context, String name, long defaultValue) {
        return getSharedPreferences(context).getLong(name, defaultValue);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(Constants.SHARED_PREFS_FILE, Context.MODE_PRIVATE);
    }
}
