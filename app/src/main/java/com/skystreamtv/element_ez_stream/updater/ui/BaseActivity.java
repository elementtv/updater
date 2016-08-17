package com.skystreamtv.element_ez_stream.updater.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Base Activity Class
 */
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    protected static final int ERROR_ACTION_CLOSE_APP = 1;
    protected static final int ERROR_ACTION_NO_ACTION = 2;

    @SuppressWarnings("unused")
    protected static final byte[] SALT = new byte[]{
            87, 67, -32, -20, 81, 58, 33, 126, -95, 37, -11,
            3, -77, -45, -102, -66, -48, -73, 15, 38
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void showErrorDialog(final String title, final String message) {
        AlertDialog error_dialog = Dialogs.buildErrorDialog(this, title, message, ERROR_ACTION_CLOSE_APP);
        error_dialog.show();
    }
}
