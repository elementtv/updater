package com.skystreamtv.element_ez_stream.updater.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
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

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.CustomEvent;
import com.skystreamtv.element_ez_stream.updater.BuildConfig;
import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.background.BackgroundUpdateService;
import com.skystreamtv.element_ez_stream.updater.background.UpdateInstaller;
import com.skystreamtv.element_ez_stream.updater.model.App;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.model.Version;
import com.skystreamtv.element_ez_stream.updater.network.ApiProvider;
import com.skystreamtv.element_ez_stream.updater.player.AppInstaller;
import com.skystreamtv.element_ez_stream.updater.player.PlayerInstaller;
import com.skystreamtv.element_ez_stream.updater.utils.Connectivity;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;
import com.skystreamtv.element_ez_stream.updater.utils.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.skystreamtv.element_ez_stream.updater.broadcast.AlarmReceiver.SAVED_TIME;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.CURRENT_KODI_VERSION;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.EXIT;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.MANDATORY_UPDATE;
import static com.skystreamtv.element_ez_stream.updater.utils.Constants.SPMC_ID;


@SuppressWarnings("deprecation")
public class DisclaimerActivity extends BaseActivity implements PlayerUpdaterActivity {

    private static final String TAG = "DisclaimerActivity";
    public static boolean IsRunning;
    private PlayerInstaller playerInstaller;
    private Button nextButton;
    private TextView playerInstallTextView;
    private boolean kodiInstalled;
    private App kodiApp;
    private boolean updateKodi = false;
    private final int UNINSTALL = 5678;
    private AlertDialog uninstallDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(EXIT, false))
            finish();
        IsRunning = true;
        PreferenceHelper.savePreference(this, SAVED_TIME, System.currentTimeMillis());
        PreferenceHelper.savePreference(this, Constants.FIRST_TIME_CONNECTED, false);
        setContentView(R.layout.activity_disclaimer);
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Startup Screen"));
        startService(new Intent(this, BackgroundUpdateService.class));
        nextButton = (Button) findViewById(R.id.nextButton);
        styleButton(nextButton);
        playerInstallTextView = (TextView) findViewById(R.id.playerInstallTextView);

        TextView textView = (TextView) findViewById(R.id.build_info);
        String textValue = BuildConfig.VERSION_NAME + ":" + BuildConfig.VERSION_CODE;
        textView.setText(textValue);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DisclaimerActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.PERMISSIONS_REQUEST);
        } else {
            completeSetup();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (isLicensed()) {
            Log.e(TAG, "On Post Resumed");
            playerInstaller = new PlayerInstaller(this);
            if (playerInstaller.isSPMCInstalled()) {
                uninstallDialog = new AlertDialog.Builder(DisclaimerActivity.this)
                        .setTitle("Upgrade The Media Center")
                        .setMessage(R.string.uninstall_text)
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Uri uri = Uri.fromParts("package", SPMC_ID, null);
                                intent.setData(uri);
                                startActivityForResult(intent, UNINSTALL);
                            }
                        }).setCancelable(false)
                        .show();
            } else {
                if (uninstallDialog != null) {
                    uninstallDialog.dismiss();
                }
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
                                getString(R.string.no_internet_title), getString(R.string.no_internet_info), ERROR_ACTION_NO_ACTION);
                        noConnection.show();
                        styleButton(noConnection.getButton(DialogInterface.BUTTON_NEUTRAL));
                    }
                }
            }

        } else {
            Answers.getInstance().logCustom(new CustomEvent("Licensed")
                    .putCustomAttribute("Licensed", "False - " + Build.MODEL));
            AlertDialog licenseErrorDialog = Dialogs.buildErrorDialog(this,
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
        IsRunning = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (uninstallDialog != null) {
            uninstallDialog.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "request: " + requestCode + " result: " + resultCode);
        if (requestCode == UNINSTALL) {
            completeSetup();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Constants.PERMISSIONS_REQUEST) {
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

    private void completeSetup() {
        if (PreferenceHelper.getPreference(this, Constants.CURRENT_KODI_VERSION,
                Constants.getDefaultPlayerValues()) <= Constants.getDefaultPlayerValues()) {
            Version version = new Version();
            version.loadFromFile();
            int currentVersion = Constants.getDefaultPlayerValues() > version.getKodiVersion()
                    ? Constants.getDefaultPlayerValues() : version.getKodiVersion();
            PreferenceHelper.savePreference(this, Constants.CURRENT_KODI_VERSION, currentVersion);
        }

        playerInstaller = new PlayerInstaller(this);

        setTitle(String.format(getString(R.string.disclaimer_activity_title),
                getString(R.string.app_name)));

        progressDialog = new ProgressDialog(this);
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
        showProgressDialog();
        Log.e(TAG, "Kodi Updates");
        kodiApp = new App();
        kodiApp.setVersion(PreferenceHelper.getPreference(this, CURRENT_KODI_VERSION, Constants.getDefaultPlayerValues()));
        ApiProvider.getInstance().getPlayerData(new Callback<App>() {
            @Override
            public void onResponse(Call<App> call, Response<App> response) {
                hideProgressDialog();
                App player = response.body();
                if (player == null) {
                    player = new App();
                    player.setVersion(0);
                    player.setLast_mandatory_version(0);
                }
                if (!Constants.MANDATORY_UPDATE) {
                    Constants.MANDATORY_UPDATE = BuildConfig.VERSION_CODE < player.getLast_mandatory_version();
                    PreferenceHelper.savePreference(DisclaimerActivity.this,
                            Constants.MANDATORY_UPDATE_KEY, MANDATORY_UPDATE);
                }
                if (kodiApp.getVersion() < player.getVersion()) {
                    kodiApp = player;
                    updateKodi = true;
                } else {
                    updateKodi = false;
                    kodiApp = player;
                }
                kodiApp = player;
                completeSetup();
            }

            @Override
            public void onFailure(Call<App> call, Throwable t) {
                onNetworkError(getString(R.string.network_error), t);
            }
        });
    }

    private void checkForUpdates() {
        Log.e(TAG, "Player Updates");
        showProgressDialog();
        ApiProvider.getInstance().getUpdaterData(new Callback<App>() {
            @Override
            public void onResponse(Call<App> call, Response<App> response) {
                Log.d(TAG, "Got response: " + response.toString());
                hideProgressDialog();
                App app = response.body();
                if (app == null) {
                    app = new App();
                    app.setVersion(0);
                    app.setLast_mandatory_version(0);
                }

                final App update = app;

                Constants.MANDATORY_UPDATE = BuildConfig.VERSION_CODE < update.getLast_mandatory_version();
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

            @Override
            public void onFailure(Call<App> call, Throwable t) {
                Log.e(TAG, t.getMessage(), t);
                onNetworkError(getString(R.string.network_error), t);
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
                || Build.MODEL.toLowerCase().contains("ti8")
                || Build.MODEL.equals("Element Ti4 Mini"));
    }

    protected void enableButtons() {
        nextButton.setEnabled(true);
    }

    public void onNextButtonClick(View nextButtonView) {
        if (!kodiInstalled || updateKodi) {
            if (playerInstaller == null) playerInstaller = new PlayerInstaller(this);
            playerInstaller.installPlayer(kodiApp.getDownloadUrl());
            AppInstaller appInstaller = new AppInstaller();
            appInstaller.init(this, kodiApp.getVersion(), kodiApp.getDownloadUrl());
            appInstaller.execute();
        } else {
            updateOrLaunchPlayer();
        }
    }

    protected void updateOrLaunchPlayer() {
        showProgressDialog();
        ApiProvider.getInstance().getSkinsData(new Callback<List<Skin>>() {
            @Override
            public void onResponse(Call<List<Skin>> call, Response<List<Skin>> response) {
                List<Skin> result = response.body();
                if (result == null) result = new ArrayList<>();
                hideProgressDialog();
                ArrayList<Skin> skins = new ArrayList<>();
                for (Skin each : result) {
                    each.setUpToDate(playerInstaller.isSkinUpToDate(each));
                    each.setInstalled(playerInstaller.isSkinInstalled(each));
                    skins.add(each);
                }

                Intent intent = new Intent(DisclaimerActivity.this, UpdateAvailableActivity.class);
                intent.putExtra(Constants.SKINS, skins);
                intent.putExtra(Constants.PLAYER_INSTALLED, getIntent().getBooleanExtra(Constants.PLAYER_INSTALLED, false));
                startActivity(intent);
            }

            @Override
            public void onFailure(Call<List<Skin>> call, Throwable t) {
                onNetworkError(getString(R.string.network_error_2), t);
            }
        });
    }

    private void onNetworkError(String message, Throwable t) {
        hideProgressDialog();
        Crashlytics.logException(t);
        try {
            showErrorDialog("Error", message);
        } catch (Exception e) {
            // can't show
        }
    }
}
