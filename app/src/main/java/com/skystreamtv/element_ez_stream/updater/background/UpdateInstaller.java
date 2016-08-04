package com.skystreamtv.element_ez_stream.updater.background;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class UpdateInstaller extends AsyncTask<String, Integer, Void> {

    private static final String TAG = "UpdateInstaller";

    private Context context;
    private ProgressDialog progressDialog;

    public void init(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Installing Updates. Please wait...");
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
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {

            String urlString = strings[0];
            Log.d(TAG, "Get " + urlString);
            URL url = new URL(urlString);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.connect();
            int lengthOfFile = c.getContentLength();
            File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "update.apk");
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

