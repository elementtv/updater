package com.skystreamtv.element_ez_stream.updater.model;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Version implements Serializable {

    private static final String FILE_NAME = "version_file.txt";

    public Version() {
    }

    public Version(int kodiVersion) {
        this.kodiVersion = kodiVersion;
    }

    private int uid = 123;

    private int kodiVersion = 0;

    public int getKodiVersion() {
        return kodiVersion;
    }

    public void setKodiVersion(int kodiVersion) {
        this.kodiVersion = kodiVersion;
    }

    private String getFileDir() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FILE_NAME;
    }

    public void writeToFile() {
        try {
            File file = new File(getFileDir());
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(this);
            os.close();
            fos.close();
        } catch (Exception e) {
            Log.e("Version", e.getMessage(), e);
        }
    }

    public void loadFromFile() {
        try {
            FileInputStream fis = new FileInputStream(new File(getFileDir()));
            ObjectInputStream is = new ObjectInputStream(fis);
            Version version = (Version) is.readObject();
            this.uid = version.uid;
            this.kodiVersion = version.kodiVersion;
            is.close();
            fis.close();
        } catch (Exception e) {
            Log.e("Version", e.getMessage(), e);
        }
    }
}
