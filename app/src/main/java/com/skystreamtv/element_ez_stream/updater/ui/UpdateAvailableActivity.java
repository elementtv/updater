package com.skystreamtv.element_ez_stream.updater.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.background.UpdateInstaller;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.network.ApiProvider;
import com.skystreamtv.element_ez_stream.updater.player.PlayerInstaller;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;
import com.skystreamtv.element_ez_stream.updater.utils.DividerItemDecoration;
import com.skystreamtv.element_ez_stream.updater.utils.adapters.UpdateItemAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.skystreamtv.element_ez_stream.updater.utils.Constants.PLAYER_FILE_LOCATION;

@SuppressWarnings("deprecation")
public class UpdateAvailableActivity extends BaseActivity implements UpdateItemAdapter.DoUpdate {

    private ProgressDialog progressDialog;
    private List<Skin> skins;
    private RecyclerView recyclerView;
    private PlayerInstaller playerInstaller;
    private File PLAYER_CONF_DIRECTORY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_available);
        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Update/Install Kodi"));

        PLAYER_CONF_DIRECTORY = new File(Environment.getExternalStorageDirectory(),
                "Android/data/" + getString(R.string.player_id) + PLAYER_FILE_LOCATION);

        playerInstaller = new PlayerInstaller(this);

        Button playerButton = (Button) findViewById(R.id.skip_button);
        styleButton(playerButton);
        playerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerInstaller.launchPlayer();
            }
        });

        skins = getIntent().getParcelableArrayListExtra(Constants.SKINS);
        recyclerView = (RecyclerView) findViewById(R.id.skin_list);
        setupRecycleList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("UpdateInfo", "OnActivityResult");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking for Available Updates");
        ApiProvider.getInstance().getSkinsData(new Callback<List<Skin>>() {
            @Override
            public void onResponse(Call<List<Skin>> call, Response<List<Skin>> response) {
                progressDialog.dismiss();
                skins = response.body();
                if (skins == null) skins = new ArrayList<>();
                for (Skin each : skins) {
                    Log.d("Update", each.getId() + " UTD: " + playerInstaller.isSkinUpToDate(each));
                    each.setUpToDate(playerInstaller.isSkinUpToDate(each));
                    each.setInstalled(playerInstaller.isSkinInstalled(each));
                }
                setupRecycleList();
            }

            @Override
            public void onFailure(Call<List<Skin>> call, Throwable t) {
                showErrorDialog(getResources().getString(R.string.github_error), "An Error has Occurred");
            }
        });
    }

    @Override
    public void doUpdate(Skin skin) {
        update(skin);
    }

    private void setupRecycleList() {
        UpdateItemAdapter itemAdapter = new UpdateItemAdapter(skins, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL));
        recyclerView.setAdapter(itemAdapter);
        itemAdapter.setFocus();
    }

    private void update(Skin selectedSkin) {
        if (selectedSkin.getId() > 2) {
            UpdateInstaller installer = new UpdateInstaller();
            installer.init(this, new UpdateInstaller.UpdateCompleteListener() {
                @Override
                public void updateComplete() {
                    onActivityResult(0, 0, getIntent());
                }
            });
            installer.execute(selectedSkin.getDownloadUrl(), String.valueOf(selectedSkin.getId()),
                    String.valueOf(selectedSkin.getVersion()));
        } else {

            File addons_destination = new File(PLAYER_CONF_DIRECTORY, "addons");
            File userdata_destination = new File(PLAYER_CONF_DIRECTORY, "userdata");

            Intent updateIntent;

            if (addons_destination.exists() && addons_destination.listFiles().length > 0
                    && userdata_destination.exists() && userdata_destination.listFiles().length > 0) {
                updateIntent = new Intent(this, UpdateTypeActivity.class);
                startActivityForResult(updateIntent, Constants.SKIN_UPDATE);
            } else {
                updateIntent = new Intent(this, UpdateActivity.class);
                updateIntent.putExtra(Constants.CLEAN_INSTALL, true);

                startActivityForResult(updateIntent, Constants.SKIN_UPDATE);
            }
            isMandatory(selectedSkin);
            updateIntent.putExtra(Constants.SKINS, selectedSkin);
            updateIntent.putExtra(Constants.SERVICE_RESET, true);
            startActivityForResult(updateIntent, Constants.SKIN_UPDATE);
        }
    }

    private void isMandatory(Skin skin) {
        playerInstaller.isSkinMandatoryUpdate(skin);
    }
}
