package com.skystreamtv.element_ez_stream.updater.player;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.ui.DisclaimerActivity;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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

    protected Skin getInstalledSkin() {
        Skin skin = new Skin();
        Log.d(TAG, "Call PlayerInstaller.getInstalledSkin()");
        File updater_info_file = new File(PLAYER_CONF_DIRECTORY, "updater.inf");
        Log.d(TAG, "updater_info_file: " + updater_info_file.getAbsolutePath());
        try {
            JsonReader reader = new JsonReader(new FileReader(updater_info_file));
            String name = "";
            int version = -1;
            int id = -1;
            reader.beginObject();
            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key) {
                    case "id":
                        id = reader.nextInt();
                        break;
                    case "skin_name":
                        name = reader.nextString();
                        break;
                    case "version":
                        version = reader.nextInt();
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            skin.setName(name);
            skin.setVersion(version);
            skin.setId(id);
            reader.endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return skin;
    }

    public boolean isSkinUpToDate(Skin selectedSkin) {
        Skin installedSkin = getInstalledSkin();
        if (installedSkin.getId() == -1) {
            return installedSkin.getName().equals(selectedSkin.getName()) && installedSkin.getVersion() >= selectedSkin.getVersion();
        }

        return installedSkin.getId() == selectedSkin.getId() && installedSkin.getVersion() >= selectedSkin.getVersion();
    }

    public boolean isSkinInstalled(Skin selectedSkin) {
        Skin installedSkin = getInstalledSkin();

        if (installedSkin.getId() == -1) {
            return installedSkin.getName().equals(selectedSkin.getName());
        }

        return installedSkin.getId() == selectedSkin.getId();
    }

    public void launchPlayer() {
        Intent launch_intent = package_manager.getLaunchIntentForPackage(resources.getString(R.string.player_id));
        launch_intent.addCategory(Intent.CATEGORY_LAUNCHER);
        launch_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launch_intent);
    }
}
