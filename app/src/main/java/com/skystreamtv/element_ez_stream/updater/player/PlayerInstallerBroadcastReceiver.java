package com.skystreamtv.element_ez_stream.updater.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.ui.DisclaimerActivity;

public class PlayerInstallerBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Resources resources = context.getResources();

        Log.e("PlayerUpdater", "Broadcast Receiver Report: player installed11");

        if (intent.getData().getSchemeSpecificPart().equals(resources.getString(R.string.player_id))) {
            Log.e("PlayerUpdater", "Broadcast Receiver Report: player installed");
            Intent wakeup_intent = new Intent(context, DisclaimerActivity.class);
            wakeup_intent.setAction(Intent.ACTION_MAIN);
            wakeup_intent.addCategory(Intent.CATEGORY_LAUNCHER);
            wakeup_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            wakeup_intent.putExtra("PLAYER_INSTALLED", true);
            context.startActivity(wakeup_intent);
            //context.unregisterReceiver(this);
        }
    }
}
