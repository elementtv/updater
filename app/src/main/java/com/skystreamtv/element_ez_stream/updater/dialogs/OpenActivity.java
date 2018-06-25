package com.skystreamtv.element_ez_stream.updater.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.ui.DisclaimerActivity;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;
import com.skystreamtv.element_ez_stream.updater.utils.PreferenceHelper;

public class OpenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);
        setTitle(getString(R.string.check_for_updates));
    }

    public void onNotNowClick(View v) {
        PreferenceHelper.savePreference(this, Constants.FIRST_TIME_CONNECTED, false);
        finish();
    }

    public void onLaterClick(View v) {
        finish();
    }

    public void onYesClick(View v) {
        PreferenceHelper.savePreference(this, Constants.FIRST_TIME_CONNECTED, false);
        startActivity(new Intent(this, DisclaimerActivity.class));
    }
}
