package com.skystreamtv.element_ez_stream.updater.dialogs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.background.BackgroundUpdateChecker;
import com.skystreamtv.element_ez_stream.updater.ui.DisclaimerActivity;
import com.skystreamtv.element_ez_stream.updater.utils.TextUtil;


public class OpenForUpdate extends AppCompatActivity {

    public static final String DESCRIPTION = "description";
    public static final String TITLE = "title";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DisclaimerActivity.IsRunning) finish();
        setContentView(R.layout.activity_open_for_update);

        String title = getIntent().getStringExtra(TITLE);
        String desc = getIntent().getStringExtra(DESCRIPTION);
        if (desc == null || desc.equals("") || title == null || title.equals("")) {
            finish();
        } else {
            setTitle(title);
            TextView textView = (TextView) findViewById(R.id.message);
            textView.setText(TextUtil.fromHtml(desc));
            textView.setMovementMethod(new ScrollingMovementMethod());
        }
    }

    public void onLaterClick(View v) {
        new AlertDialog.Builder(this)
                .setTitle("Are you sure?")
                .setMessage("You must run this update before customer service will be able to help you with any issues relating to the media center app.").setNegativeButton("Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        }).setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doUpdate();
            }
        }).show();
    }

    public void onYesClick(View v) {
        doUpdate();
    }

    private void doUpdate() {
        startActivity(new Intent(this, DisclaimerActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startService(new Intent(OpenForUpdate.this, BackgroundUpdateChecker.class));
    }
}
