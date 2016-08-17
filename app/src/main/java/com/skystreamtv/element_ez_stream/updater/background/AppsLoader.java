package com.skystreamtv.element_ez_stream.updater.background;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import com.skystreamtv.element_ez_stream.updater.BuildConfig;
import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.model.App;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.io.InputStreamReader;

@SuppressWarnings("unchecked")
public class AppsLoader extends AsyncTask<Void, Void, App> implements GitHubHelper.GitHubTask {
    protected Context context;
    protected String failure_reason;
    protected App kodi;

    public AppsLoader(Context context) {
        if (BuildConfig.DEBUG && !(context instanceof GitHubHelper.GitHubCallbacks))
            throw new AssertionError("AppsLoader requires a context implementing GitHubCallbacks");
        this.context = context;
        failure_reason = null;
    }

    @Override
    protected App doInBackground(Void... params) {
        Log.d("AppLoader", "Running GitHub Task");
        Resources resources;
        try {
            resources = context.getResources();
        } catch (NullPointerException e) {
            cancel(true);
            return null;
        }
        try {
            Log.d("AppLoader", "Get GitHub Repo");
            GHRepository repository = GitHubHelper.connectRepository(resources);
            Log.d("AppLoader", "Getting apps.json");
            GHContent content = repository.getFileContent("/kodi_app.json");
            Log.d("AppLoader", "Prepare JSON reader for kodi_app.json");
            JsonReader reader = new JsonReader(new InputStreamReader(content.read()));
            reader.beginObject();
            Log.d("AppLoader", "Start reading JSON array");
            reader.beginObject();

            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key.toLowerCase()) {
                    case "name":
                        kodi.setName(reader.nextString());
                        break;
                    case "description":
                        kodi.setDescription(reader.nextString());
                        break;
                    case "download_url":
                        kodi.setDownloadUrl(reader.nextString());
                        break;
                    case "version":
                        kodi.setVersion(reader.nextInt());
                        break;
                    default:
                        reader.skipValue();
                }

            }

            reader.endObject();
            reader.close();
            return kodi;
        } catch (IOException | NullPointerException e) {
            failure_reason = e.toString(); //resources.getString(R.string.connection_error_message);
            cancel(true);
            return null;
        } catch (NumberFormatException e) {
            failure_reason = resources.getString(R.string.github_internal_error);
            cancel(true);
            return null;
        }
    }

    @Override
    public void onCancelled() {
        Log.d("AppLoader", "Call AppsLoader.onCancelled()");
        GitHubHelper.GitHubCallbacks<App> github_callbacks = (GitHubHelper.GitHubCallbacks<App>) context;
        if (github_callbacks != null)
            github_callbacks.onCancelled(failure_reason);
    }

    @Override
    public void onPostExecute(App result) {
        Log.d("AppLoader", "Call AppsLoader.onPostExecute()");
        GitHubHelper.GitHubCallbacks<App> github_callbacks = (GitHubHelper.GitHubCallbacks<App>) context;
        if (github_callbacks != null)
            github_callbacks.onPostExecute(result);
    }

    @Override
    public void contextDestroyed() {
        context = null;
    }
}