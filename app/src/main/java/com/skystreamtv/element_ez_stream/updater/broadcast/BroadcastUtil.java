package com.skystreamtv.element_ez_stream.updater.broadcast;

import android.content.Intent;
import android.content.IntentFilter;

/**
 * Broadcast Utility
 */
public class BroadcastUtil {

    public static final String STOP = BroadcastUtil.class.getSimpleName() + ".STOP";
    public static final String ERROR = BroadcastUtil.class.getSimpleName() + ".ERROR";

    public static Intent stop() {
        return new Intent(STOP);
    }

    public static Intent error() {
        return new Intent(ERROR);
    }

    public static IntentFilter stopFilter() {
        return new IntentFilter(STOP);
    }

    public static IntentFilter errorFilter() {
        return new IntentFilter(ERROR);
    }
}
