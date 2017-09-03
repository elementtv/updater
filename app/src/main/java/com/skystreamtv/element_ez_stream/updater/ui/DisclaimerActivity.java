package com.skystreamtv.element_ez_stream.updater.ui;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.skystreamtv.element_ez_stream.updater.BuildConfig;
import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.background.BackgroundUpdateService;
import com.skystreamtv.element_ez_stream.updater.background.KodiUpdater;
import com.skystreamtv.element_ez_stream.updater.background.SkinsLoader;
import com.skystreamtv.element_ez_stream.updater.background.UpdateInstaller;
import com.skystreamtv.element_ez_stream.updater.background.Updater;
import com.skystreamtv.element_ez_stream.updater.model.App;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.player.AppInstaller;
import com.skystreamtv.element_ez_stream.updater.player.PlayerInstaller;
import com.skystreamtv.element_ez_stream.updater.utils.Connectivity;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;
import com.skystreamtv.element_ez_stream.updater.utils.PreferenceHelper;

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

import java.util.ArrayList;

import static com.skystreamtv.element_ez_stream.updater.broadcast.AlarmReceiver.SAVED_TIME;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.CURRENT_KODI_VERSION;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.EXIT;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.MANDATORY_UPDATE;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.PERMISSIONS_REQUEST;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.PLAYER_INSTALLED;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.SKINS;


public class DisclaimerActivity extends BaseActivity implements PlayerUpdaterActivity,
        SkinsLoader.SkinsLoaderListener {

    private static final String TAG = "DisclaimerActivity";
    public static boolean IsRunning;
    private PlayerInstaller playerInstaller;
    private ProgressDialog progressDialog;
    private SkinsLoader skinsLoader;
    private Button nextButton;
    private TextView playerInstallTextView;
    private boolean kodiInstalled;
    private App kodiApp;
    private boolean updateKodi = false;
    private AlertDialog licenseErrorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(EXIT, false))
            finish();
        IsRunning = true;
        PreferenceHelper.savePreference(this, SAVED_TIME, System.currentTimeMillis());
        setContentView(R.layout.activity_disclaimer);
        startService(new Intent(this, BackgroundUpdateService.class));
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Startup Screen"));

        nextButton = (Button) findViewById(R.id.nextButton);
        styleButton(nextButton);
        playerInstallTextView = (TextView) findViewById(R.id.playerInstallTextView);

        TextView textView = (TextView) findViewById(R.id.build_info);
        textView.setText(BuildConfig.VERSION_NAME + ":" + BuildConfig.VERSION_CODE);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DisclaimerActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST);

        } else {
            completeSetup();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d("Disclaimer", "On post resume");
        if (isLicensed()) {
            Answers.getInstance().logCustom(new CustomEvent("Licensed")
                    .putCustomAttribute("Licensed", "True")
                    .putCustomAttribute("Device Type", Build.MODEL));
            enableButtons();
            checkForKodiUpdates();
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
            Answers.getInstance().logCustom(new CustomEvent("Licensed")
                    .putCustomAttribute("Licensed", "False - " + Build.MODEL));
            licenseErrorDialog = Dialogs.buildErrorDialog(this,
                    getString(R.string.license_error),
                    getString(R.string.license_message), ERROR_ACTION_CLOSE_APP);
            try {
                if (licenseErrorDialog != null) {
                    licenseErrorDialog.show();
                    styleButton(licenseErrorDialog.getButton(DialogInterface.BUTTON_NEUTRAL));
                }
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        licenseErrorDialog = null;
        IsRunning = false;
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Call DisclaimerActivity.onPause()");
        super.onPause();
        if (skinsLoader != null) {
            skinsLoader.contextDestroyed();
        }
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

    @Override
    public void onCancelled(String reason) {
        progressDialog.dismiss();
        if (reason != null) {
            showErrorDialog(getResources().getString(R.string.github_error), reason);
        }
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
        } else if (updateKodi) {
            playerInstallTextView.setText(R.string.update_kodi_info);
            nextButton.setText(R.string.update_media_player);
        } else {
            playerInstallTextView.setText("");
            nextButton.setText(R.string.continue_button);
        }
        nextButton.requestFocus();
    }

    private void checkForKodiUpdates() {
        Log.e("Disclaimer", "Checking for Kodi updates");
        kodiApp = new App();
        kodiApp.setVersion(PreferenceHelper.getPreference(this, CURRENT_KODI_VERSION, 16));
        KodiUpdater kodiUpdater = new KodiUpdater();
        kodiUpdater.setListener(new KodiUpdater.KodiUpdateListener() {
            @Override
            public void onCheckComplete(App kodi) {
                if (!MANDATORY_UPDATE) {
                    MANDATORY_UPDATE = BuildConfig.VERSION_CODE < kodi.getMandatoryVersion();
                    PreferenceHelper.savePreference(DisclaimerActivity.this,
                            Constants.MANDATORY_UPDATE_KEY, MANDATORY_UPDATE);
                }
                if (kodiApp.getVersion() < kodi.getVersion()) {
                    kodiApp = kodi;
                    updateKodi = true;
                } else {
                    updateKodi = false;
                    kodiApp = kodi;
                }
                completeSetup();
            }
        });
        kodiUpdater.execute();
    }

    private void checkForUpdates() {
        Updater updater = new Updater();
        updater.init(this);
        updater.execute();
        updater.setListener(new Updater.UpdateListener() {
            @Override
            public void onCheckComplete(final App update) {

                MANDATORY_UPDATE = BuildConfig.VERSION_CODE < update.getMandatoryVersion();
                PreferenceHelper.savePreference(DisclaimerActivity.this,
                        Constants.MANDATORY_UPDATE_KEY, MANDATORY_UPDATE);

                if (BuildConfig.VERSION_CODE < update.getVersion()) {
                    AlertDialog.Builder dialog_builder = new AlertDialog.Builder(DisclaimerActivity.this);
                    String message = getString(R.string.new_version_app_info);
                    dialog_builder.setTitle(R.string.update_available)
                            .setMessage(message)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setNegativeButton(R.string.not_now, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Answers.getInstance().logCustom(new CustomEvent("Update application")
                                            .putCustomAttribute("Updated", "Update Skipped"));
                                    dialog.dismiss();
                                    nextButton.requestFocus();
                                }
                            }).setPositiveButton(R.string.install_update, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Answers.getInstance().logCustom(new CustomEvent("Update application")
                                    .putCustomAttribute("Updated", "Update Installed")
                                    .putCustomAttribute("Version Installed", "v:" + update.getVersion()));
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
                || Build.MODEL.equalsIgnoreCase("ezstream-ti8")
                || Build.MODEL.equals("Element Ti4 Mini"));
    }

    private void enableButtons() {
        nextButton.setEnabled(true);
    }

    public void onNextButtonClick(View nextButtonView) {
        if (!kodiInstalled || updateKodi) {
            if (playerInstaller == null) playerInstaller = new PlayerInstaller(this);
            playerInstaller.installPlayer(kodiApp.getDownloadUrl());
            AppInstaller appInstaller = new AppInstaller();
            appInstaller.init(this, kodiApp.getVersion());
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
}
