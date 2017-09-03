package com.skystreamtv.element_ez_stream.updater.utils;

/**
 * Constants
 */
public class Constants {

    public static final String UPDATER_PREFERENCES = "updaterPreferences";
    public static final String FIRST_TIME_CONNECTED = "firstTimeConnected";
    public static final String CURRENT_KODI_VERSION = "currentKodiVersion";

    public static final long WAIT_TIME = 1000 * 60 * 60 * 3; // 3 hours

    // intents
    public static final String EXIT = "EXIT";
    public static final String SERVICE_RESET = "SERVICE_RESET";
    public static final String SKINS = "skins";
    public static final String PLAYER_INSTALLED = "PLAYER_INSTALLED";
    public static final String CHECK_FOR_UPDATES = "checkForUpdates";
    public static final String CLEAN_INSTALL = "cleanInstall";


    public static final int SKIN_UPDATE = 1001;
    public static final int PERMISSIONS_REQUEST = 1002;

    //element files
    public static final String UPDATE_JSON_FILE = "element_update.json";
    public static final String PLAYER_FILE_LOCATION = "/files/.ftmc";
    public static final String MANDATORY_UPDATE_KEY = "MANDATORY_UPDATE";
    static final String LOCATION_JSON_FILE = "/element_ez_locations.json";
    static final String KODI_LOCATION = "/element_kodi_app.json";
    static final String LOCATION_JSON_FILE_V16 = "/element_ez_locations_v16.json";
    static final String KODI_LOCATION_V16 = "/element_kodi_app_v16.json";
    // preferences
    static final String SHARED_PREFS_FILE = "element";
    // mandatory update
    public static boolean MANDATORY_UPDATE;
    public static boolean MANDATORY_SKIN_UPDATE;
}
