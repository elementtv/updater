package com.skystreamtv.element_ez_stream.updater.utils;

import com.skystreamtv.element_ez_stream.updater.network.ApiProvider;

/**
 * Constants
 */
public class Constants {

    public static final String UPDATER_PREFERENCES = "updaterPreferences";
    public static final String FIRST_TIME_CONNECTED = "firstTimeConnected";
    public static final String CURRENT_KODI_VERSION = "currentKodiVersion";

    // intents
    public static final String EXIT = "EXIT";
    public static final String SERVICE_RESET = "SERVICE_RESET";
    public static final String SKINS = "skins";
    public static final String PLAYER_INSTALLED = "PLAYER_INSTALLED";
    public static final String CHECK_FOR_UPDATES = "checkForUpdates";
    public static final String CLEAN_INSTALL = "cleanInstall";

    public static final long WAIT_TIME = 1000 * 60 * 60; // 1 hour

    public static final int SKIN_UPDATE = 1001;
    public static final int PERMISSIONS_REQUEST = 1002;

    public static final String SPMC_ID = "com.semperpax.spmc16";


    //element files
    //public static final String PLAYER_FILE_LOCATION = "/files/.ftmc";
    public static final String MANDATORY_UPDATE_KEY = "MANDATORY_UPDATE";
    // preferences
    static final String SHARED_PREFS_FILE = "element";
    // mandatory update
    public static boolean MANDATORY_UPDATE;
    public static boolean MANDATORY_SKIN_UPDATE;

    public static String getPlayerFileLocation() {
        String file = "/files/";
        file = file + ".ftmc";
//        if (ApiProvider.isOSVersion7()) {
//            file = file + ".kodi";
//        } else {
//            file = file + ".ftmc";
//        }
        return file;
    }

    public static String getPlayerId() {
//        if (ApiProvider.isOSVersion7()) {
//            return "com.semperpax.spmc16";
//        }
        return "org.xbmc.kodi";
    }

    public static int getDefaultPlayerValues() {
        //return 34; // beta
        if (ApiProvider.isOSVersion7()) {
            return 19; // prod
        }
        return 18;
    }
}
