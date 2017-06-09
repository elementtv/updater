package com.skystreamtv.element_ez_stream.updater.background;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.model.App;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;

import java.io.InputStreamReader;

public class Updater extends AsyncTask<Void, Integer, App> {

    private static final String TAG = "Updater";

    private Context context;
    private ProgressDialog progressDialog;
    private UpdateListener listener;

    public void init(Context context) {
        this.context = context;
    }

    public void setListener(UpdateListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.checking_for_updates));
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setProgressNumberFormat(null);
        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        progressDialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(App app) {
        super.onPostExecute(app);
        if (app == null) {
            app = new App();
            app.setVersion(0);
        }
        progressDialog.dismiss();
        progressDialog.setProgress(0);
        Log.d(TAG, "Updater Version: " + app.getVersion());
        if (listener != null) listener.onCheckComplete(app);
    }

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
        }

        return null;
    }

    public interface UpdateListener {
        void onCheckComplete(App update);
    }
}
