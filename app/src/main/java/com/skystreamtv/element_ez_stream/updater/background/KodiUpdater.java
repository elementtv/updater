package com.skystreamtv.element_ez_stream.updater.background;

import com.skystreamtv.element_ez_stream.updater.model.App;
import com.skystreamtv.element_ez_stream.updater.utils.OSHelper;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import java.io.InputStreamReader;

public class KodiUpdater extends AsyncTask<Void, Integer, App> {
    private static final String TAG = "Updater";

    private KodiUpdateListener listener;

    @Override
    protected App doInBackground(Void... voids) {
        try {
            App kodi = new App();
            kodi.setVersion(0);
            GHRepository repository = GitHubHelper.connectRepository();

            GHContent content = repository.getFileContent(OSHelper.getKodiApp());
            JsonReader reader = new JsonReader(new InputStreamReader(content.read()));
            reader.beginObject();

            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key.toLowerCase()) {
                    case "version":
                        kodi.setVersion(reader.nextInt());
                        break;
                    case "download_url":
                        kodi.setDownloadUrl(reader.nextString().trim());
                        break;
                    case "description":
                        kodi.setDescription(reader.nextString());
                        break;
                    case "last_mandatory_version":
                        kodi.setMandatoryVersion(reader.nextInt());
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }

            reader.endObject();
            reader.close();
            return kodi;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(App kodi) {
        super.onPostExecute(kodi);
        if (kodi == null) {
            kodi = new App();
            kodi.setVersion(0);
        }
        Log.e(TAG, "Media Center Version: " + kodi.getVersion());
        if (listener != null) {
            listener.onCheckComplete(kodi);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    public void setListener(KodiUpdateListener listener) {
        this.listener = listener;
    }

    public interface KodiUpdateListener {
        void onCheckComplete(App kodi);
    }
}
