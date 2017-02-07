package com.skystreamtv.element_ez_stream.updater.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.skystreamtv.element_ez_stream.updater.BuildConfig;
import com.skystreamtv.element_ez_stream.updater.R;
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

import static com.skystreamtv.element_ez_stream.updater.utils.Constants.EXIT;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.PERMISSIONS_REQUEST;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.PLAYER_INSTALLED;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.SKINS;


public class DisclaimerActivity extends BaseActivity implements PlayerUpdaterActivity,
        SkinsLoader.SkinsLoaderListener {

    private static final String TAG = "DisclaimerActivity";
    private PlayerInstaller playerInstaller;
    private ProgressDialog progressDialog;
    private SkinsLoader skinsLoader;
    private Button nextButton;
    private TextView playerInstallTextView;
    private boolean kodiInstalled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(EXIT, false))
            finish();
        setContentView(R.layout.activity_disclaimer);

        nextButton = (Button) findViewById(R.id.nextButton);
        styleButton(nextButton);
        playerInstallTextView = (TextView) findViewById(R.id.playerInstallTextView);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle(R.string.external_storage_title)
                    .setMessage(R.string.external_storage_info)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(DisclaimerActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    PERMISSIONS_REQUEST);
                        }
                    }).create();
            dialog.show();
            styleButton(dialog.getButton(DialogInterface.BUTTON_POSITIVE));
        } else {
            completeSetup();
        }
    }

    private void completeSetup() {
        playerInstaller = new PlayerInstaller(this);
        skinsLoader = new SkinsLoader(this, this);
        progressDialog = new ProgressDialog(this);

        setTitle(String.format(getString(R.string.disclaimer_activity_title),
                getString(R.string.app_name)));

        progressDialog.setMessage(getString(R.string.loading));

        kodiInstalled = playerInstaller.isPlayerInstalled();
        if (!kodiInstalled) {
            playerInstallTextView.setText(String.format(getString(R.string.player_not_installed_message),
                    getString(R.string.player_name)));
            nextButton.setText(R.string.install_player);
        } else {
            playerInstallTextView.setText("");
            nextButton.setText(R.string.continue_button);
        }
        nextButton.requestFocus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                completeSetup();
            } else {
                AlertDialog dialog = Dialogs.buildErrorDialog(this, getString(R.string.external_storage_title),
                        getString(R.string.external_storage_required), ERROR_ACTION_CLOSE_APP);
                dialog.show();
                styleButton(dialog.getButton(DialogInterface.BUTTON_NEUTRAL));
            }
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (isLicensed()) {
            enableButtons();
            boolean checkForUpdates = getIntent().getBooleanExtra(Constants.CHECK_FOR_UPDATES, true);
            if (checkForUpdates) {
                if (Connectivity.isConnectionAvailable(this)) {
                    getIntent().putExtra(Constants.CHECK_FOR_UPDATES, false);
                    checkForUpdates();
                } else {
                    AlertDialog noConnection = Dialogs.buildErrorDialog(this,
                            getString(R.string.no_internet_title), getString(R.string.no_internet_info),
                            ERROR_ACTION_NO_ACTION);
                    noConnection.show();
                    styleButton(noConnection.getButton(DialogInterface.BUTTON_NEUTRAL));
                }
            }
        } else {
            AlertDialog licenseErrorDialog = Dialogs.buildErrorDialog(this,
                    getString(R.string.license_error),
                    getString(R.string.license_message), ERROR_ACTION_CLOSE_APP);
            licenseErrorDialog.show();
            styleButton(licenseErrorDialog.getButton(DialogInterface.BUTTON_NEUTRAL));
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
                    String message = getString(R.string.new_version_app_info);
                    dialog_builder.setTitle(R.string.update_available)
                            .setMessage(message)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    nextButton.requestFocus();
                                }
                            }).setPositiveButton(R.string.install_update, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UpdateInstaller installer = new UpdateInstaller();
                            installer.init(DisclaimerActivity.this, new UpdateInstaller.UpdateCompleteListener() {
                                @Override
                                public void updateComplete() {
                                    // do nothing here
                                }
                            });
                            installer.execute(update.getDownloadUrl());
                        }
                    }).show();
                }
            }
        });
    }

    private boolean isLicensed() {
        return (Build.MODEL.equals("Element-Ti4")
                || Build.MODEL.equals("Element Ti4")
                || Build.MODEL.equals("Element-Ti5")
                || Build.MODEL.equals("Element Ti5")
                || Build.MODEL.equals("Element-Ti8")
                || Build.MODEL.equals("Element Ti8")
                || Build.MODEL.equals("Element Ti4 Mini"));
    }

    private void enableButtons() {
        nextButton.setEnabled(true);
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
            if (playerInstaller == null) playerInstaller = new PlayerInstaller(this);
            playerInstaller.installPlayer();
            AppInstaller appInstaller = new AppInstaller();
            appInstaller.init(this);
            appInstaller.execute();
        } else {
            updateOrLaunchPlayer();
        }
    }

    private void updateOrLaunchPlayer() {
        progressDialog.show();
        if (skinsLoader.hasRun()) {
            skinsLoader = new SkinsLoader(this, this);
        }
        skinsLoader.execute();
    }

    @Override
    public void onCancelled(String reason) {
        progressDialog.dismiss();
        if (reason != null)
            showErrorDialog(getResources().getString(R.string.github_error), reason);
    }

    @Override
    public void onPostExecute(ArrayList<Skin> result) {
        Log.d(TAG, "Call DisclaimerActivity.onPostExecute()");
        progressDialog.dismiss();
        ArrayList<Skin> skins = new ArrayList<>();
        for (Skin each : result) {
            each.setUpToDate(playerInstaller.isSkinUpToDate(each));
            each.setInstalled(playerInstaller.isSkinInstalled(each));
            skins.add(each);
        }

        Intent intent = new Intent(DisclaimerActivity.this, UpdateAvailableActivity.class);
        intent.putExtra(SKINS, skins);
        intent.putExtra(PLAYER_INSTALLED, getIntent().getBooleanExtra(PLAYER_INSTALLED, false));
        startActivity(intent);
    }
}
