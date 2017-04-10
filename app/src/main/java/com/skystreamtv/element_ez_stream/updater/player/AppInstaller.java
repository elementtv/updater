package com.skystreamtv.element_ez_stream.updater.player;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.JsonReader;
import android.util.Log;

import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.background.GitHubHelper;
import com.skystreamtv.element_ez_stream.updater.model.App;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;
import com.skystreamtv.element_ez_stream.updater.utils.OSHelper;
import com.skystreamtv.element_ez_stream.updater.utils.PreferenceHelper;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class AppInstaller extends AsyncTask<Void, Integer, Void> {

    private static final String TAG = "AppInstaller";

    private Context context;
    private ProgressDialog progressDialog;
    private int kodiVersionBeingInstalled;

    public void init(Context context, int kodiVersionBeingInstalled) {
        this.context = context;
        this.kodiVersionBeingInstalled = kodiVersionBeingInstalled;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.installing_kodi));
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        progressDialog.dismiss();
        progressDialog.setProgress(0);
        PreferenceHelper.savePreference(context, Constants.CURRENT_KODI_VERSION, kodiVersionBeingInstalled);
    }

    @Override
    protected Void doInBackground(Void... app) {
        try {
            App kodi = new App();
            Log.d(TAG, "Get GitHub Repo");
            GHRepository repository = GitHubHelper.connectRepository();
            Log.d(TAG, "Getting apps.json");
            GHContent content = repository.getFileContent(OSHelper.getKodiApp());
            Log.d(TAG, "Prepare JSON reader for kodi_app.json");
            JsonReader reader = new JsonReader(new InputStreamReader(content.read()));
            Log.d(TAG, "Start reading JSON data");
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

            Log.d(TAG, "Get " + kodi.getDownloadUrl());
            URL url = new URL(kodi.getDownloadUrl());
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.connect();
            int lengthOfFile = c.getContentLength();
            File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "app.apk");
            if (outputFile.exists()) {
                outputFile.delete();
            } else {
                outputFile.createNewFile();
            }
            Log.d(TAG, "Getting ");
            FileOutputStream fos = new FileOutputStream(outputFile);
            InputStream is = c.getInputStream();
            Log.d(TAG, "Getting InputStream");

            byte[] buffer = new byte[1024];
            int len1;
            long total = 0;
            while ((len1 = is.read(buffer)) != -1) {
                total += len1;
                long percent = (total*100)/lengthOfFile;
                publishProgress((int)percent);
                fos.write(buffer, 0, len1);
            }
            fos.close();
            is.close();
            Log.d(TAG, "Closing file");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(outputFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
            context.startActivity(intent);
            Log.d(TAG, "DONE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
