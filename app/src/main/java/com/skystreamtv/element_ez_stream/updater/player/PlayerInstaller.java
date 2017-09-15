package com.skystreamtv.element_ez_stream.updater.player;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.ui.DisclaimerActivity;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;
import com.skystreamtv.element_ez_stream.updater.utils.PreferenceHelper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static com.skystreamtv.element_ez_stream.updater.utils.Constants.PLAYER_FILE_LOCATION;

public class PlayerInstaller {

    private static final String TAG = "PlayerInstaller";

    protected Context context;
    private PackageManager package_manager;
    private File PLAYER_CONF_DIRECTORY;

    public PlayerInstaller(Context context) {
        this.context = context;
        this.package_manager = context.getPackageManager();
        PLAYER_CONF_DIRECTORY = new File(Environment.getExternalStorageDirectory(),
                "Android/data/" + context.getString(R.string.player_id) + PLAYER_FILE_LOCATION);
    }

    public static void launchPlayer(Context context) {
        Intent launch_intent = context.getPackageManager().getLaunchIntentForPackage(context.getString(R.string.player_id));
        launch_intent.addCategory(Intent.CATEGORY_LAUNCHER);
        launch_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(launch_intent);
        Intent finishIntent = new Intent(context.getApplicationContext(), DisclaimerActivity.class);
        finishIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        finishIntent.putExtra(Constants.EXIT, true);
        context.startActivity(finishIntent);
    }

    public boolean isPlayerInstalled() {
        try {
            package_manager.getPackageInfo(context.getString(R.string.player_id), 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void installPlayer(String kodiUrl) {
        DisclaimerActivity disclaimer_activity = (DisclaimerActivity) context;
        try {
            Intent market_intent = new Intent(Intent.ACTION_VIEW);
            market_intent.setData(Uri.parse(kodiUrl));
            disclaimer_activity.startActivity(market_intent);
        } catch (ActivityNotFoundException e) {
            disclaimer_activity.showErrorDialog(context.getString(R.string.missing_play_store),
                    context.getString(R.string.play_store_not_installed));
        } catch (NullPointerException exception) {
            disclaimer_activity.showErrorDialog(context.getString(R.string.something_went_wrong),
                    context.getString(R.string.try_later_install));
        }

        Toast.makeText(context, "Please wait Media Center media player is installing", Toast.LENGTH_SHORT).show();
    }

    private Skin getInstalledSkin() {
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
        } else if (selectedSkin.getId() > 2) {
            // This would mean there is an app that is not a skin
            int lastVersion = PreferenceHelper.getPreference(context, String.valueOf(selectedSkin.getId()), 0);
            return lastVersion >= selectedSkin.getVersion();
        }

        return installedSkin.getId() == selectedSkin.getId() && installedSkin.getVersion() >= selectedSkin.getVersion();
    }

    public void isSkinMandatoryUpdate(Skin selectedSkin) {
        Skin installedSkin = getInstalledSkin();
        Constants.MANDATORY_SKIN_UPDATE = installedSkin.getId() != selectedSkin.getId() || installedSkin.getVersion() < selectedSkin.getLast_mandatory_version();
    }

    public boolean isSkinInstalled(Skin selectedSkin) {
        Skin installedSkin = getInstalledSkin();

        if (installedSkin.getId() == -1) {
            return installedSkin.getName().equals(selectedSkin.getName());
        } else if (selectedSkin.getId() > 2) {
            return true;
        }

        return installedSkin.getId() == selectedSkin.getId();
    }

    public void launchPlayer() {
        launchPlayer(context);
    }
}
