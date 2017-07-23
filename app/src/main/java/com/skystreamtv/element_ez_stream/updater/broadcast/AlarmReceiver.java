package com.skystreamtv.element_ez_stream.updater.broadcast;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.skystreamtv.element_ez_stream.updater.background.KodiUpdater;
import com.skystreamtv.element_ez_stream.updater.background.SkinsLoader;
import com.skystreamtv.element_ez_stream.updater.dialogs.OpenForUpdate;
import com.skystreamtv.element_ez_stream.updater.model.App;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.player.PlayerInstaller;
import com.skystreamtv.element_ez_stream.updater.ui.DisclaimerActivity;
import com.skystreamtv.element_ez_stream.updater.utils.PreferenceHelper;

import java.util.ArrayList;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.CURRENT_KODI_VERSION;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String SAVED_TIME = "savedTime";
    private final long oneDay = 1000 * 60 * 60 * 24; // 24 hours
    private boolean needsUpdate = false;
    private String updateText = "";
    private App kodiApp;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("Alarm Receiver", "on Receive");
        try {
            checkForKodiUpdate(context);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    public boolean appIsForeground() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND || appProcessInfo.importance == IMPORTANCE_VISIBLE);

    }

    private void checkForSkinUpdate(final Context context) {
        SkinsLoader loader = new SkinsLoader(context, new SkinsLoader.SkinsLoaderListener() {
            @Override
            public void onCancelled(String failureReason) {

            }

            @Override
            public void onPostExecute(ArrayList<Skin> result) {
                PlayerInstaller playerInstaller = new PlayerInstaller(context);
                for (Skin each : result) {
                    if (!playerInstaller.isSkinUpToDate(each) && !needsUpdate) {
                        needsUpdate = true;
                        updateText = each.getNotification();
                    }
                }
                if (needsUpdate) {
                    goToDialog(context, updateText, "Skystream Media Center Addons Update");
                }
            }
        });
        loader.execute();

    }

    private void checkForKodiUpdate(final Context context) {
        kodiApp = new App();
        kodiApp.setVersion(PreferenceHelper.getPreference(context, CURRENT_KODI_VERSION, 16));
        KodiUpdater kodiUpdater = new KodiUpdater();
        kodiUpdater.setListener(new KodiUpdater.KodiUpdateListener() {
            @Override
            public void onCheckComplete(App kodi) {
                Log.e("versions", "from " + kodiApp.getVersion() + " to " + kodi.getVersion());
                if (kodiApp.getVersion() < kodi.getVersion()) {
                    Log.e("Alarm", "Kodi needs update");
                    goToDialog(context, kodi.getDescription(), "Skystream Media Center Update");
                } else {
                    checkForSkinUpdate(context);
                }
            }
        });
        kodiUpdater.execute();
    }

    private void goToDialog(final Context context, String text, String title) {
        if (!appIsForeground() && over24HoursSinceLastUpdate(context) && !DisclaimerActivity.IsRunning) {
            PreferenceHelper.savePreference(context, SAVED_TIME, System.currentTimeMillis());
            Intent startApp = new Intent(context, OpenForUpdate.class);
            startApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startApp.putExtra(OpenForUpdate.DESCRIPTION, text);
            startApp.putExtra(OpenForUpdate.TITLE, title);
            context.startActivity(startApp);
        }
    }

    private boolean over24HoursSinceLastUpdate(final Context context) {
        long lastCheck = PreferenceHelper.getPreference(context, SAVED_TIME, 0L);
        long currentTime = System.currentTimeMillis();
        Log.e("Time", "difference: " + Math.abs(currentTime - lastCheck));
        return Math.abs(currentTime - lastCheck) >= oneDay;
    }
}
