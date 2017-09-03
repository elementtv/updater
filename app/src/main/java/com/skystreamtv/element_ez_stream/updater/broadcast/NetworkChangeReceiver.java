package com.skystreamtv.element_ez_stream.updater.broadcast;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.dialogs.OpenActivity;
import com.skystreamtv.element_ez_stream.updater.dialogs.OpenForSetup;
import com.skystreamtv.element_ez_stream.updater.player.PlayerInstaller;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.util.List;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PlayerInstaller installer = new PlayerInstaller(context);
        if (!installer.isPlayerInstalled() && isConnected(context)) {
            Intent startApp = new Intent(context, OpenForSetup.class);
            startApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startApp);
        } else {
            SharedPreferences preferences = context.getSharedPreferences(Constants.UPDATER_PREFERENCES, Context.MODE_PRIVATE);
            boolean firstConnect = preferences.getBoolean(Constants.FIRST_TIME_CONNECTED, true);

            if (isConnected(context)) { // connected to the internet
                if (firstConnect && !kodiRunning(context)) {
                    Intent startApp = new Intent(context, OpenActivity.class);
                    startApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(startApp);
                }
            }
        }
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

    private boolean isConnected(final Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
