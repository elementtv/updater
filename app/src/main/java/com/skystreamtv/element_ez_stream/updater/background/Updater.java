package com.skystreamtv.element_ez_stream.updater.background;

import com.crashlytics.android.Crashlytics;
import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.model.App;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import java.io.InputStreamReader;

public class Updater extends AsyncTask<Void, Integer, App> {

    private static final String TAG = "Updater";

    private Context context;
    private ProgressDialog progressDialog;
    private UpdateListener listener;
    private boolean showDialog = true;

    @Override
    protected App doInBackground(Void... voids) {
        try {
            App update = new App();
            GHRepository repository = GitHubHelper.connectRepository();
            GHContent content = repository.getFileContent(Constants.UPDATE_JSON_FILE);
            JsonReader reader = new JsonReader(new InputStreamReader(content.read()));
            reader.beginObject();

            while (reader.hasNext()) {
                String key = reader.nextName();
                switch (key.toLowerCase()) {
                    case "version":
                        update.setVersion(reader.nextInt());
                        break;
                    case "download_url":
                        update.setDownloadUrl(reader.nextString());
                        break;
                    case "description":
                        update.setDescription(reader.nextString());
                        break;
                    case "last_mandatory_version":
                        update.setMandatoryVersion(reader.nextInt());
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }

            reader.endObject();
            reader.close();
            return update;

        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (showDialog) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage(context.getString(R.string.checking_for_updates));
            progressDialog.setIndeterminate(true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);
            progressDialog.setProgressNumberFormat(null);
            progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(App app) {
        super.onPostExecute(app);
        if (app == null) {
            app = new App();
            app.setVersion(0);
        }
        if (showDialog) {
            try {
                progressDialog.dismiss();
                progressDialog.setProgress(0);
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }
        Log.d(TAG, "Updater Version: " + app.getVersion());
        if (listener != null) listener.onCheckComplete(app);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (showDialog) {
            progressDialog.setProgress(values[0]);
        }
    }

    public void init(Context context) {
        this.context = context;
    }

    public void init(Context context, boolean showDialog) {
        this.context = context;
        this.showDialog = showDialog;
    }

    public void setListener(UpdateListener listener) {
        this.listener = listener;
    }

    public interface UpdateListener {
        void onCheckComplete(App update);
    }
}
