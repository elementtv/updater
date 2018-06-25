package com.skystreamtv.element_ez_stream.updater.broadcast;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.skystreamtv.element_ez_stream.updater.dialogs.OpenForUpdate;
import com.skystreamtv.element_ez_stream.updater.model.App;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.network.ApiProvider;
import com.skystreamtv.element_ez_stream.updater.player.PlayerInstaller;
import com.skystreamtv.element_ez_stream.updater.ui.DisclaimerActivity;
import com.skystreamtv.element_ez_stream.updater.utils.ApplicationRunningHelper;
import com.skystreamtv.element_ez_stream.updater.utils.PreferenceHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.CURRENT_KODI_VERSION;
import static com.skystreamtv.element_ez_stream.updater.utils.WaitTimeHelper.over24HoursSinceLastUpdate;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    public static final String SAVED_TIME = "savedTime";
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

        ApiProvider.getInstance().getSkinsData(new Callback<List<Skin>>() {
            @Override
            public void onResponse(Call<List<Skin>> call, Response<List<Skin>> response) {
                if (response.body() != null) {
                    PlayerInstaller playerInstaller = new PlayerInstaller(context);
                    for (Skin each : response.body()) {
                        if (playerInstaller.isSkinInstalled(each) &&// only check installed skins
                                each.getId() < 3 &&
                                !playerInstaller.isSkinUpToDate(each) && !needsUpdate) {
                            needsUpdate = true;
                            updateText = each.getNotification();
                        }
                    }
                    if (needsUpdate) {
                        goToDialog(context, updateText, "Skystream Media Center Application Update");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Skin>> call, Throwable t) {

            }
        });

    }

    private void checkForKodiUpdate(final Context context) {
        Log.e("Kodi Update Check", "checking for player updates");
        kodiApp = new App();
        kodiApp.setVersion(PreferenceHelper.getPreference(context, CURRENT_KODI_VERSION, 18));
        ApiProvider.getInstance().getPlayerData(new Callback<App>() {
            @Override
            public void onResponse(Call<App> call, Response<App> response) {
                App player = response.body();
                if (player == null) {
                    player = new App();
                    player.setVersion(0);
                    player.setLast_mandatory_version(0);
                }
                Log.e("versions", "from " + kodiApp.getVersion() + " to " + player.getVersion());
//                if (kodiApp.getVersion() < player.getVersion()) {
//                    Log.e("Alarm", "Kodi needs update");
//                    goToDialog(context, player.getDescription(), "Skystream Media Center Update");
//                } else {
                checkForSkinUpdate(context);
//                }
            }

            @Override
            public void onFailure(Call<App> call, Throwable t) {
                Crashlytics.logException(t);
            }
        });
    }

    private void goToDialog(final Context context, String text, String title) {
        Log.e("Is Running", "Running: " + DisclaimerActivity.IsRunning);
        if (!appIsForeground()) {
            Log.e(TAG, "Not in foreground");
            if (over24HoursSinceLastUpdate(context)) {
                Log.e(TAG, "Over 24 hours");
                if (!DisclaimerActivity.IsRunning) {
                    Log.e(TAG, "Not running");
                    if (!ApplicationRunningHelper.areAppsRunning()){
                        Log.e(TAG, "good to go");
                    }
                }
            }
        }
        if (!appIsForeground() && over24HoursSinceLastUpdate(context)
                && !DisclaimerActivity.IsRunning && !ApplicationRunningHelper.areAppsRunning()) {
            PreferenceHelper.savePreference(context, SAVED_TIME, System.currentTimeMillis());
            Intent startApp = new Intent(context, OpenForUpdate.class);
            startApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startApp.putExtra(OpenForUpdate.DESCRIPTION, text);
            startApp.putExtra(OpenForUpdate.TITLE, title);
            context.startActivity(startApp);
        }
    }
}
