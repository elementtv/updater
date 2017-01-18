package com.skystreamtv.element_ez_stream.updater.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.skystreamtv.element_ez_stream.updater.R;

/**
 * Base Activity Class
 */
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    protected static final byte[] SALT = new byte[]{
            87, 67, -32, -20, 81, 58, 33, 126, -95, 37, -11,
            3, -77, -45, -102, -66, -48, -73, 15, 38
    };
    static final int ERROR_ACTION_CLOSE_APP = 1;
    static final int ERROR_ACTION_NO_ACTION = 2;

    public void showErrorDialog(final String title, final String message) {
        AlertDialog error_dialog = Dialogs.buildErrorDialog(this, title, message, ERROR_ACTION_CLOSE_APP);
        error_dialog.show();
        styleButton(error_dialog.getButton(DialogInterface.BUTTON_NEUTRAL));
    }

    protected void styleButton(final Button button) {
        button.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    button.setBackgroundColor(ContextCompat.getColor(BaseActivity.this, R.color.colorPrimary));
                    button.setTextColor(Color.WHITE);
                } else {
                    button.setBackgroundResource(android.R.drawable.btn_default);
                    button.setTextColor(Color.BLACK);
                }
            }
        });
    }
}
