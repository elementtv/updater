package com.skystreamtv.element_ez_stream.updater.player;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.util.Pair;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.ui.DisclaimerActivity;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class PlayerInstaller {

    private static final String TAG = "PlayerInstaller";

    protected Context context;
    protected PackageManager package_manager;
    protected Resources resources;
    protected File PLAYER_CONF_DIRECTORY;

    public PlayerInstaller(Context context) {
        this.context = context;
        this.resources = context.getResources();
        this.package_manager = context.getPackageManager();
        PLAYER_CONF_DIRECTORY = new File(Environment.getExternalStorageDirectory(), "Android/data/" + resources.getString(R.string.player_id) + "/files/.kodi");
    }

    public boolean isPlayerInstalled() {
        try {
            package_manager.getPackageInfo(resources.getString(R.string.player_id), 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void installPlayer() {
        DisclaimerActivity disclaimer_activity = (DisclaimerActivity) context;
        try {
            Intent market_intent = new Intent(Intent.ACTION_VIEW);
            market_intent.setData(Uri.parse("https://www.dropbox.com/s/cjch4vr34fk0jht/kodi-160-elitemods-032816-skinmod.apk?dl=1"));
            disclaimer_activity.startActivity(market_intent);
        } catch (ActivityNotFoundException e) {
            disclaimer_activity.showErrorDialog(resources.getString(R.string.missing_play_store),
                    resources.getString(R.string.play_store_not_installed));
        }

        Toast.makeText(context,"Please wait Kodi media player is installing",Toast.LENGTH_SHORT).show();
    }

    protected Pair<String, Integer> getInstalledSkin() {
        Log.d(TAG, "Call PlayerInstaller.getInstalledSkin()");
        File updater_info_file = new File(PLAYER_CONF_DIRECTORY, "updater.inf");
        Log.d(TAG, "updater_info_file: " + updater_info_file.getAbsolutePath());
        try {
            JsonReader reader = new JsonReader(new FileReader(updater_info_file));
            String installed_skin_name = "";
            int installed_skin_version = -1;
            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case "skin_name":
                        installed_skin_name = reader.nextString();
                        break;
                    case "version":
                        installed_skin_version = reader.nextInt();
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();
            Log.d(TAG, "New pair: " + installed_skin_name + ", " + installed_skin_version);
            return new Pair<>(installed_skin_name, installed_skin_version);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isPlayerUpToDate(Skin selected_skin) {
        Pair<String, Integer> installed_skin = getInstalledSkin();
        return installed_skin != null && installed_skin.first.equals(selected_skin.getName()) && installed_skin.second == selected_skin.getVersion();
    }

    public Skin getSkinToUpdate(ArrayList<Skin> skins) {
        Log.d(TAG, "Call PlayerInstaller.getSkinToUpdate()");
        Pair<String, Integer> installed_skin = getInstalledSkin();
        if (installed_skin != null) {
            Log.d(TAG, "Installed skin found: " + installed_skin.first + ", version: " + installed_skin.second);
            int i = 0;
            while (i < skins.size() && !skins.get(i).getName().equals(installed_skin.first))
                i++;
            if (i < skins.size())
                return skins.get(i);
        }
        return skins.get(0);
    }

    public void launchPlayer(boolean first_time) {
        Log.d(TAG, "Player just installed?: " + first_time);
        if (first_time) {
            Log.d(TAG, "Trying to clear cache");
            //clear_all_system_cache();
        }
        Intent launch_intent = package_manager.getLaunchIntentForPackage(resources.getString(R.string.player_id));
        launch_intent.addCategory(Intent.CATEGORY_LAUNCHER);
        launch_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launch_intent);
    }
}
