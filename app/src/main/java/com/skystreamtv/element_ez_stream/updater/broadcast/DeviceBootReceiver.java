package com.skystreamtv.element_ez_stream.updater.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.skystreamtv.element_ez_stream.updater.background.BackgroundUpdateChecker;

public class DeviceBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("receiver", intent.getAction());
        BackgroundUpdateChecker.start(context);
    }
}
