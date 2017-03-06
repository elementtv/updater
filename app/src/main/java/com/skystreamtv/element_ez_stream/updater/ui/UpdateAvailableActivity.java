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

import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.background.SkinsLoader;
import com.skystreamtv.element_ez_stream.updater.background.UpdateInstaller;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.player.PlayerInstaller;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;
import com.skystreamtv.element_ez_stream.updater.utils.DividerItemDecoration;
import com.skystreamtv.element_ez_stream.updater.utils.adapters.UpdateItemAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UpdateAvailableActivity extends BaseActivity implements UpdateItemAdapter.DoUpdate,
        SkinsLoader.SkinsLoaderListener {

    private ProgressDialog progressDialog;
    private List<Skin> skins;
    private RecyclerView recyclerView;
    private PlayerInstaller playerInstaller;
    private SkinsLoader skinsLoader;
    private File PLAYER_CONF_DIRECTORY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_available);

        PLAYER_CONF_DIRECTORY = new File(Environment.getExternalStorageDirectory(),
                "Android/data/" + getString(R.string.player_id) + "/files/.kodi");

        playerInstaller = new PlayerInstaller(this);
        skinsLoader = new SkinsLoader(this, this);


        Button playerButton = (Button) findViewById(R.id.skip_button);
        playerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerInstaller.launchPlayer();
            }
        });
        styleButton(playerButton);

        skins = getIntent().getParcelableArrayListExtra(Constants.SKINS);
        recyclerView = (RecyclerView) findViewById(R.id.skin_list);
        setupRecycleList();
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

            updateIntent.putExtra(Constants.SKINS, selectedSkin);
            updateIntent.putExtra(Constants.SERVICE_RESET, true);
            startActivityForResult(updateIntent, Constants.SKIN_UPDATE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("UpdateInfo", "OnActivityResult");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking for Available Updates");
        if (skinsLoader.hasRun()) {
            skinsLoader = new SkinsLoader(this, this);
        }
        skinsLoader.execute();
    }

    @Override
    public void doUpdate(Skin skin) {
        update(skin);
    }

    @Override
    public void onCancelled(String reason) {
        progressDialog.dismiss();
        if (reason != null)
            showErrorDialog(getResources().getString(R.string.github_error), reason);
    }

    @Override
    public void onPostExecute(ArrayList<Skin> skins) {
        progressDialog.dismiss();
        for (Skin each : skins) {
            Log.d("Update", each.getId() + " UTD: " + playerInstaller.isSkinUpToDate(each));
            each.setUpToDate(playerInstaller.isSkinUpToDate(each));
            each.setInstalled(playerInstaller.isSkinInstalled(each));
        }
        setupRecycleList();
    }
}
