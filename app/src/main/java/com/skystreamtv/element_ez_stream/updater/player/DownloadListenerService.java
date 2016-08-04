package com.skystreamtv.element_ez_stream.updater.player;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;

public class DownloadListenerService extends BroadcastReceiver {

    private static final String TAG = "DownloadListenerService";

    @Override
    public void onReceive(final Context context, Intent intent) {

        long queForUrl = 0;

        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        String action = intent.getAction();

        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(queForUrl);
            Cursor c = dm.query(query);
            if (c.moveToFirst()) {
                int columnIndex = c
                        .getColumnIndex(DownloadManager.COLUMN_STATUS);
                if (DownloadManager.STATUS_SUCCESSFUL == c
                        .getInt(columnIndex)) {

                    String uri_String_abcd = c
                            .getString(c
                                    .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    Log.d(TAG, "" + uri_String_abcd);
                }
            }
            Log.d("downloadfinal", "" + action);
            Log.d("downloadfinal11", "" + intent);
        }

        Log.d("download", "" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Bite_Mexico.apk");
        Log.d("download11", "" + Environment.getExternalStorageDirectory() + "/Download/" + "Bite_Mexico.apk");
    }
}