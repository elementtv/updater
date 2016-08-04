package com.skystreamtv.element_ez_stream.updater.dialogs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.ui.DisclaimerActivity;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;

public class OpenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);
        setTitle(getString(R.string.check_for_updates));
    }

    public void onNotNowClick(View v) {
        SharedPreferences preferences = getSharedPreferences(Constants.UPDATER_PREFERENCES, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(Constants.FIRST_TIME_CONNECTED, false).apply();
        finish();
    }

    public void onLaterClick(View v) {
        finish();
    }

    public void onYesClick(View v) {
        SharedPreferences preferences = getSharedPreferences(Constants.UPDATER_PREFERENCES, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(Constants.FIRST_TIME_CONNECTED, false).apply();
        startActivity(new Intent(this, DisclaimerActivity.class));
    }
}
