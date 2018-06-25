package com.skystreamtv.element_ez_stream.updater.utils;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.skystreamtv.element_ez_stream.updater.R;

import android.content.Context;

import java.util.List;

public final class ApplicationRunningHelper {

    public static boolean areAppsRunning() {
        return isAppRunning("element_ez_stream")
                || isAppRunning(Constants.getPlayerId());
    }

    private static boolean isAppRunning(String bundleId) {
        List<AndroidAppProcess> procInfos = AndroidProcesses.getRunningAppProcesses();
        for (AndroidAppProcess process : procInfos) {
            if (process.getPackageName().contains(bundleId)) {
                return true;
            }
            if (process.name.contains(bundleId)) {
                return true;
            }
        }
        return false;
    }
}
