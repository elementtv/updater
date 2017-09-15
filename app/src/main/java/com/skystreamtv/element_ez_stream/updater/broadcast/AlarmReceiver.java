package com.skystreamtv.element_ez_stream.updater.broadcast;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.model.App;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.network.ApiProvider;
import com.skystreamtv.element_ez_stream.updater.player.PlayerInstaller;
import com.skystreamtv.element_ez_stream.updater.utils.PreferenceHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        ApiProvider.getInstance().getSkinsData(new Callback<List<Skin>>() {
            @Override
            public void onResponse(Call<List<Skin>> call, Response<List<Skin>> response) {
                if (response.body() != null) {
                    PlayerInstaller playerInstaller = new PlayerInstaller(context);
                    for (Skin each : response.body()) {
                        if (!playerInstaller.isSkinUpToDate(each) && !needsUpdate) {
                            needsUpdate = true;
                            updateText = each.getNotification();
                        }
                    }
                    if (needsUpdate) {
                        goToDialog(context, updateText, "Skystream Media Center Addons Update");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Skin>> call, Throwable t) {
                Crashlytics.logException(t);
            }
        });
    }

    private void checkForKodiUpdate(final Context context) {
        kodiApp = new App();
        kodiApp.setVersion(PreferenceHelper.getPreference(context, CURRENT_KODI_VERSION, 16));
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
                if (kodiApp.getVersion() < player.getVersion()) {
                    Log.e("Alarm", "Kodi needs update");
                    goToDialog(context, player.getDescription(), "Skystream Media Center Update");
                } else {
                    checkForSkinUpdate(context);
                }
            }

            @Override
            public void onFailure(Call<App> call, Throwable t) {
                Crashlytics.logException(t);
            }
        });
    }

    private void goToDialog(final Context context, String text, String title) {
//        if (!appIsForeground() && over24HoursSinceLastUpdate(context) && !DisclaimerActivity.IsRunning && !kodiRunning(context)) {
//            PreferenceHelper.savePreference(context, SAVED_TIME, System.currentTimeMillis());
//            Intent startApp = new Intent(context, OpenForUpdate.class);
//            startApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            startApp.putExtra(OpenForUpdate.DESCRIPTION, text);
//            startApp.putExtra(OpenForUpdate.TITLE, title);
//            context.startActivity(startApp);
//        }
    }

    private boolean over24HoursSinceLastUpdate(final Context context) {
        long lastCheck = PreferenceHelper.getPreference(context, SAVED_TIME, 0L);
        long currentTime = System.currentTimeMillis();
        Log.e("Time", "difference: " + Math.abs(currentTime - lastCheck));
        return Math.abs(currentTime - lastCheck) >= oneDay;
    }

    private boolean kodiRunning(Context context) {
        List<AndroidAppProcess> procInfos = AndroidProcesses.getRunningAppProcesses();
        for (int i = 0; i < procInfos.size(); i++) {
            Log.e("Network", procInfos.get(i).getPackageName());
            if (procInfos.get(i).getPackageName().equalsIgnoreCase(context.getString(R.string.player_id))) {
                return true;
            }
        }
        Log.e("Network", "False");
        return false;
    }
}
