package com.skystreamtv.element_ez_stream.updater.ui;

import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;
import com.skystreamtv.element_ez_stream.updater.utils.PreferenceHelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class UpdateTypeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_type);

        TextView installDetails = (TextView) findViewById(R.id.install_details_textview);

        Button freshInstall = (Button) findViewById(R.id.fresh_button);
        freshInstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextActivity(true);
            }
        });
        styleButton(freshInstall);

        Button saveUserInstall = (Button) findViewById(R.id.user_settings_button);

        if (Constants.MANDATORY_UPDATE || PreferenceHelper.getPreference(this,
                Constants.MANDATORY_UPDATE_KEY, false) || Constants.MANDATORY_SKIN_UPDATE) {
            saveUserInstall.setEnabled(false);
            installDetails.setText(R.string.mandatory_install_details);
        } else {
            installDetails.setText(R.string.install_details);
            saveUserInstall.setEnabled(true);
            saveUserInstall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToNextActivity(false);
                }
            });
        }


        styleButton(saveUserInstall);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.SKIN_UPDATE) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void goToNextActivity(boolean cleanInstall) {
        Skin skin = getIntent().getParcelableExtra(Constants.SKINS);
        Intent updateIntent = new Intent(this, UpdateActivity.class);
        updateIntent.putExtra(Constants.SERVICE_RESET, true);
        updateIntent.putExtra(Constants.CLEAN_INSTALL, cleanInstall);
        updateIntent.putExtra(Constants.SKINS, skin);
        startActivityForResult(updateIntent, Constants.SKIN_UPDATE);
    }
}
