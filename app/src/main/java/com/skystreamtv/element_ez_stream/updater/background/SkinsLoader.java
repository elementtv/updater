package com.skystreamtv.element_ez_stream.updater.background;

import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.utils.OSHelper;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class SkinsLoader extends AsyncTask<Void, Void, ArrayList<Skin>> implements GitHubHelper.GitHubTask {

    private static final String TAG = "SkinsLoader";
    private Context context;
    private String failure_reason;
    private boolean hasRun = false;
    private SkinsLoaderListener listener;

    public SkinsLoader(Context context, SkinsLoaderListener listener) {
        this.listener = listener;
        this.context = context;
        failure_reason = null;
    }

    public boolean hasRun() {
        return hasRun;
    }

    @Override
    protected ArrayList<Skin> doInBackground(Void... params) {
        hasRun = true;
        Log.d(TAG, "Running GitHub Task");
        try {
            Log.d(TAG, "Get GitHub Repo");
            GHRepository repository = GitHubHelper.connectRepository();
            Log.d(TAG, "Getting location.json");
            GHContent content = repository.getFileContent(OSHelper.getLocationsFile());
            Log.d(TAG, "Prepare JSON reader for location.json");
            JsonReader reader = new JsonReader(new InputStreamReader(content.read()));
            ArrayList<Skin> list = new ArrayList<>();
            Log.d(TAG, "Start reading JSON array");
            reader.beginArray();
            while (reader.hasNext()) {
                int id = -1;
                String name = null;
                String description = null;
                String screenshot_url = null;
                String brand_url = null;
                int version = -1;
                String details = null;
                Log.d(TAG, "Start reading JSON object");
                reader.beginObject();

                while (reader.hasNext()) {
                    String key = reader.nextName();
                    switch (key) {
                        case "id":
                            id = reader.nextInt();
                            break;
                        case "name":
                            name = reader.nextString();
                            break;
                        case "description":
                            description = reader.nextString();
                            break;
                        case "screenshot_url":
                            screenshot_url = reader.nextString();
                            break;
                        case "brand_url":
                            brand_url = reader.nextString();
                            break;
                        case "version":
                            version = reader.nextInt();
                            break;
                        case "update_details":
                            details = reader.nextString();
                            break;
                        default:
                            reader.skipValue();
                    }
                }
                reader.endObject();
                list.add(new Skin(id, screenshot_url, name, description, brand_url, true, version, details));
            }
            if (!reader.hasNext())
                reader.endArray();
            reader.close();
            return list;
        } catch (IOException | NullPointerException e) {
            failure_reason = context.getString(R.string.connection_error_message);
            cancel(true);
            return null;
        } catch (NumberFormatException e) {
            failure_reason = context.getString(R.string.github_internal_error);
            cancel(true);
            return null;
        }
    }

    @Override
    public void onCancelled() {
        Log.d("PlayerUpdater", "Call SkinsLoader.onCancelled()");
        if (listener != null) listener.onCancelled(failure_reason);
    }

    @Override
    public void onPostExecute(ArrayList<Skin> result) {
        Log.d("PlayerUpdater", "Call SkinsLoader.onPostExecute()");
        if (listener != null) listener.onPostExecute(result);
    }

    @Override
    public void contextDestroyed() {
        this.context = null;
    }

    public interface SkinsLoaderListener {
        void onCancelled(String failureReason);

        void onPostExecute(ArrayList<Skin> skins);
    }
}
