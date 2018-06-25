package com.skystreamtv.element_ez_stream.updater.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.skystreamtv.element_ez_stream.updater.dialogs.OpenForSetup;
import com.skystreamtv.element_ez_stream.updater.player.PlayerInstaller;
import com.skystreamtv.element_ez_stream.updater.utils.ApplicationRunningHelper;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;
import com.skystreamtv.element_ez_stream.updater.utils.PreferenceHelper;
import com.skystreamtv.element_ez_stream.updater.utils.WaitTimeHelper;

import static com.skystreamtv.element_ez_stream.updater.broadcast.AlarmReceiver.SAVED_TIME;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PlayerInstaller installer = new PlayerInstaller(context);
        if (!installer.isPlayerInstalled() && isConnected(context)
                && WaitTimeHelper.over24HoursSinceLastUpdate(context)
                && !ApplicationRunningHelper.areAppsRunning()) {
            PreferenceHelper.savePreference(context, SAVED_TIME, System.currentTimeMillis());
            Intent startApp = new Intent(context, OpenForSetup.class);
            startApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startApp);
        } else {
            boolean firstConnect = PreferenceHelper.getPreference(context, Constants.FIRST_TIME_CONNECTED, true);

            if (isConnected(context)) { // connected to the internet
                if (firstConnect && !ApplicationRunningHelper.areAppsRunning()) {
//                    PreferenceHelper.savePreference(context, SAVED_TIME, System.currentTimeMillis());
//                    Intent startApp = new Intent(context, OpenActivity.class);
//                    startApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    context.startActivity(startApp);
                }
            }
        }
    }


    private boolean isConnected(final Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
