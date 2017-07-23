package com.skystreamtv.element_ez_stream.updater.background;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.skystreamtv.element_ez_stream.updater.broadcast.AlarmReceiver;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;


public final class BackgroundUpdateChecker {

    private static BackgroundUpdateChecker instance;

    private BackgroundUpdateChecker() {

    }

    public static void start(Context context) {
        if (instance == null) {
            instance = new BackgroundUpdateChecker();
        }
        instance.startService(context);
    }

    private void startService(final Context context) {
        AlarmReceiver receiver = new AlarmReceiver();
        receiver.onReceive(context, null);
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Constants.WAIT_TIME, pendingIntent);
    }
}
