package com.skystreamtv.element_ez_stream.updater.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.skystreamtv.element_ez_stream.updater.dialogs.OpenActivity;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences(Constants.UPDATER_PREFERENCES, Context.MODE_PRIVATE);
        boolean firstConnect = preferences.getBoolean(Constants.FIRST_TIME_CONNECTED, true);
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                if (firstConnect) {
                    Intent startApp = new Intent(context, OpenActivity.class);
                    startApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(startApp);
                }
            }
        }
    }
}
