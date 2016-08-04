package com.skystreamtv.element_ez_stream.updater.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.skystreamtv.element_ez_stream.updater.BuildConfig;
import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.background.AppsLoader;
import com.skystreamtv.element_ez_stream.updater.background.GitHubHelper;
import com.skystreamtv.element_ez_stream.updater.background.SkinsLoader;
import com.skystreamtv.element_ez_stream.updater.background.UpdateInstaller;
import com.skystreamtv.element_ez_stream.updater.background.Updater;
import com.skystreamtv.element_ez_stream.updater.model.App;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.player.AppInstaller;
import com.skystreamtv.element_ez_stream.updater.player.PlayerInstaller;
import com.skystreamtv.element_ez_stream.updater.utils.Connectivity;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;

import java.util.ArrayList;
import java.util.List;


public class DisclaimerActivity extends BaseActivity implements PlayerUpdaterActivity, GitHubHelper.GitHubCallbacks<Object> {

    private static final String TAG = "DisclaimerActivity";
    protected PlayerInstaller playerInstaller;
    protected ProgressDialog progressDialog;
    protected SkinsLoader skinsLoader;
    protected AppsLoader appsLoader;
    Button nextButton;
    TextView playerInstallTextView;
    private int selection;
    private boolean kodiInstalled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(Constants.EXIT, false))
            finish();
        setContentView(R.layout.activity_disclaimer);

        nextButton = (Button) findViewById(R.id.nextButton);
        playerInstallTextView = (TextView) findViewById(R.id.playerInstallTextView);

        setTitle(String.format(getString(R.string.disclaimer_activity_title),
                getString(R.string.app_name)));
        playerInstaller = new PlayerInstaller(this);
        skinsLoader = new SkinsLoader(this);
        appsLoader = new AppsLoader(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));

        if (isLicensed()) {
            enableButtons();
            if (Connectivity.isConnectionAvailable(this)) {
                checkForUpdates();
            } else {
                AlertDialog noConnection = Dialogs.buildErrorDialog(this,
                        "No Internet", "An internet connection is required to use this application", ERROR_ACTION_NO_ACTION);
                noConnection.show();
            }
        } else {
            AlertDialog licenseErrorDialog = Dialogs.buildErrorDialog(this,
                    getString(R.string.license_error),
                    getString(R.string.license_message), ERROR_ACTION_CLOSE_APP);
            licenseErrorDialog.show();
        }
    }

    private void checkForUpdates() {
        Updater updater = new Updater();
        updater.init(this);
        updater.execute();
        updater.setListener(new Updater.UpdateListener() {
            @Override
            public void onCheckComplete(final App update) {
                if (BuildConfig.VERSION_CODE < update.getVersion()) {
                    AlertDialog.Builder dialog_builder = new AlertDialog.Builder(DisclaimerActivity.this);
                    String message = "A new version of the Updater Application is available. Would you like to update now?";
                    dialog_builder.setTitle(R.string.update_available)
                            .setMessage(message)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            }).setPositiveButton(R.string.install_update, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UpdateInstaller installer = new UpdateInstaller();
                            installer.init(DisclaimerActivity.this);
                            installer.execute(update.getDownloadUrl());
                        }
                    }).show();
                }
            }
        });
    }

    protected boolean isLicensed() {
        return (Build.PRODUCT.equals("stvm8") &&
                Build.DEVICE.equals("stvm8") &&
                Build.BOARD.equals("stvm8")) ||
                Build.MODEL.equals("Skystream X4") ||
                Build.MODEL.equals("SkystreamX X4") ||
                Build.MODEL.equals("Skystream X2")
                || Build.MODEL.equals("Skystream One")
                || BuildConfig.DEBUG;
    }

    protected void enableButtons() {
        nextButton.setEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        kodiInstalled = playerInstaller.isPlayerInstalled();
        if (!kodiInstalled) {
            playerInstallTextView.setText(String.format(getString(R.string.player_not_installed_message),
                    getString(R.string.player_name)));
            nextButton.setText(R.string.install_player);
        } else {
            playerInstallTextView.setText("");
            nextButton.setText(R.string.continue_button);
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Call DisclaimerActivity.onPause()");
        super.onPause();
        if (skinsLoader != null)
            skinsLoader.contextDestroyed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.close_button:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void errorAction(int action) {
        switch (action) {
            case ERROR_ACTION_CLOSE_APP:
                finish();
        }
    }

    public void onNextButtonClick(View nextButtonView) {
        if (!kodiInstalled) {

            playerInstaller.installPlayer();
            AppInstaller appInstaller = new AppInstaller();
            appInstaller.init(this);
            appInstaller.execute();
        } else {
            updateOrLaunchPlayer();
        }
    }

    protected void updateOrLaunchPlayer() {
        progressDialog.show();
        skinsLoader.execute();
    }

    @Override
    public void onCancelled(String reason) {
        progressDialog.dismiss();
        if (reason != null)
            showErrorDialog(getResources().getString(R.string.github_error), reason);
    }

    @Override
    public void onPostExecute(Object result) {
        Log.d(TAG, "Call DisclaimerActivity.onPostExecute()");
        progressDialog.dismiss();
        @SuppressWarnings("unchecked")
        ArrayList<Skin> skins = (ArrayList<Skin>) result;
        final List<Skin> needsUpdate = new ArrayList<>();
        for (Skin each : skins) {
            if (!playerInstaller.isPlayerUpToDate(each)) needsUpdate.add(each);
        }

        if (needsUpdate.isEmpty()) {
            playerInstaller.launchPlayer(getIntent().getBooleanExtra(Constants.PLAYER_INSTALLED, false));
            finish();
        } else {
            CharSequence[] descriptions = new CharSequence[needsUpdate.size()];
            for (int i = 0; i < needsUpdate.size(); i++) {
                descriptions[i] = needsUpdate.get(i).getDescription();
            }

            selection = 0;
            AlertDialog.Builder dialog_builder = new AlertDialog.Builder(DisclaimerActivity.this);
            dialog_builder.setTitle(R.string.continue_with_update)
                    .setSingleChoiceItems(descriptions, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            selection = i;
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNegativeButton(R.string.use_current_version, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            playerInstaller.launchPlayer(getIntent().getBooleanExtra(Constants.PLAYER_INSTALLED, false));
                            finish();
                        }
                    }).setPositiveButton(R.string.install_update, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    doUpdate(needsUpdate.get(selection));
                }
            }).show();
        }
    }

    private void doUpdate(Skin selected_skin) {
        Intent update_intent = new Intent(this, UpdateActivity.class);
        update_intent.putExtra(Constants.SERVICE_RESET, true);
        update_intent.putExtra(Constants.SKINS, selected_skin);
        startActivity(update_intent);
    }
}
