package com.skystreamtv.element_ez_stream.updater.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.skystreamtv.element_ez_stream.updater.R;
import com.skystreamtv.element_ez_stream.updater.controller.AppController;
import com.skystreamtv.element_ez_stream.updater.model.Skin;
import com.skystreamtv.element_ez_stream.updater.model.Skins;
import com.skystreamtv.element_ez_stream.updater.network.ApiProvider;
import com.skystreamtv.element_ez_stream.updater.player.PlayerInstaller;
import com.skystreamtv.element_ez_stream.updater.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@SuppressWarnings("deprecation")
public class SkinsActivity extends BaseActivity implements PlayerUpdaterActivity,
        AdapterView.OnItemClickListener {

    private static final String TAG = "SkinsActivity";

    protected Skins skins;
    protected BaseAdapter list_adapter;
    protected ProgressDialog progress_dialog;
    protected PlayerInstaller player_installer;
    protected View previously_selected_list_item;
    protected Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Call SkinsActivity.onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skins);
        Answers.getInstance().logContentView(new ContentViewEvent().putContentName("Add-Ons View"));
        setTitle(getString(R.string.select_brand));
        progress_dialog = new ProgressDialog(this);
        progress_dialog.setMessage(getString(R.string.loading));
        player_installer = new PlayerInstaller(this);
        previously_selected_list_item= null;
        resources = getResources();
        if (savedInstanceState == null) {
            Log.d(TAG, "No saved instance, creating list view data");
            loadListData();
        } else {
            Log.d(TAG, "Saved instance, load list view data");
            Skins skins = (Skins) savedInstanceState.getSerializable(Constants.SKINS);
            if (skins == null) {
                Log.d(TAG, "No loaded skins, re-run background task");
                loadListData();
            } else {
                Log.d(TAG, "Skins already loaded, loading into view");
                setListAdapter(skins.getSkins());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out_bundle) {
        Log.d(TAG, "Call SkinsActivity.onSaveInstanceState()");
        out_bundle.putSerializable(Constants.SKINS, skins);
        super.onSaveInstanceState(out_bundle);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void errorAction(int action) {

    }

    public void showErrorDialog(final String title, final String message) {
        Toast.makeText(getApplicationContext(), title, Toast.LENGTH_SHORT).show();

        AlertDialog error_dialog = Dialogs.buildErrorDialog(this, title, message, 0);
        error_dialog.show();
        styleButton(error_dialog.getButton(DialogInterface.BUTTON_NEUTRAL));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View listItem, int position, long id) {
        if (!listItem.isEnabled()) {
            return;
        }
        if (previously_selected_list_item != null) {
            previously_selected_list_item.setBackgroundColor(Color.TRANSPARENT);
        }
        listItem.setBackgroundColor(0xFFEEEEFF);
        previously_selected_list_item = listItem;
        Skin selectedSkin = skins.getSkins().get(position);
        if (player_installer.isPlayerInstalled()) {
            if (!player_installer.isSkinUpToDate(selectedSkin)) {
                Intent updateIntent = new Intent(this, UpdateActivity.class);
                updateIntent.putExtra(Constants.SERVICE_RESET, true);
                updateIntent.putExtra(Constants.SKINS, selectedSkin);
                startActivity(updateIntent);
            } else {
                player_installer.launchPlayer();
                finish();
            }
        } else {
            showErrorDialog(resources.getString(R.string.player_not_installed_title),
                    String.format(resources.getString(R.string.player_not_installed_message),
                            resources.getString(R.string.player_name)));
            previously_selected_list_item.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    protected void setListAdapter(final List<Skin> skinList) {
        Log.d(TAG, "Call SkinsActivity.setListAdapter()");
        if (skins == null) {
            skins = new Skins();
        }
        skins.setSkins(skinList);
        list_adapter = new BaseAdapter() {

            @Override
            public int getCount() {
                return skins.getSkins().size();
            }

            @Override
            public Object getItem(int position) {
                return skinList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return skinList.get(position).hashCode();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ListViewHolder holder;
                Skin skin = skinList.get(position);
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.skins_list_item, parent, false);
                    holder = new ListViewHolder();
                    holder.skinScreenShotImageView = convertView.findViewById(R.id.skinScreenShotImageView);
                    holder.skinNameTextView = convertView.findViewById(R.id.skinNameTextView);
                    holder.skinDescriptionTextView = convertView.findViewById(R.id.skinDescriptionTextView);
                    convertView.setTag(holder);
                }
                else
                    holder = (ListViewHolder)convertView.getTag();
                setListItem(convertView, holder, skin);
                return convertView;
            }

            private void setListItem(View listItemView, ListViewHolder holder, Skin skin) {
                boolean enabled = skin.isEnabled();
                listItemView.setEnabled(enabled);
                if (enabled) {
                    listItemView.setBackgroundColor(Color.TRANSPARENT);
                    holder.skinScreenShotImageView.clearColorFilter();
                    holder.skinNameTextView.setTextColor(Color.BLACK);
                    holder.skinDescriptionTextView.setTextColor(Color.DKGRAY);
                }
                else{
                    ColorMatrix matrix = new ColorMatrix();
                    matrix.setSaturation(0);
                    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                    listItemView.setBackgroundColor(0xFFEEEEEE);
                    holder.skinScreenShotImageView.setColorFilter(filter);
                    holder.skinNameTextView.setTextColor(Color.GRAY);
                    holder.skinDescriptionTextView.setTextColor(Color.GRAY);
                }
                holder.skinScreenShotImageView.setDefaultImageResId(R.drawable.inner);
                holder.skinScreenShotImageView.setErrorImageResId(R.drawable.inner);
                holder.skinScreenShotImageView.setImageUrl(skin.getScreenshotUrl(), AppController.getInstance().getImageLoader());
                holder.skinNameTextView.setText(skin.getName());
                holder.skinDescriptionTextView.setText(skin.getDescription());
                Log.d(TAG, "Skin Description: " + skin.getName());
            }
        };
        ListView skinsListView = (ListView) findViewById(R.id.skinsListView);
        skinsListView.setAdapter(list_adapter);
        skinsListView.setOnItemClickListener(this);
    }

    protected void loadListData() {
        Log.d(TAG, "Call SkinsActivity.loadListData()");
        progress_dialog.show();
        ApiProvider.getInstance().getSkinsData(new Callback<List<Skin>>() {
            @Override
            public void onResponse(Call<List<Skin>> call, Response<List<Skin>> response) {
                setListAdapter(response.body());
                progress_dialog.dismiss();
            }

            @Override
            public void onFailure(Call<List<Skin>> call, Throwable t) {
                progress_dialog.dismiss();
                showErrorDialog("Error", "An error has occurred");
                if (t != null) {
                    Log.e(TAG, t.getMessage());
                    Crashlytics.logException(t);
                }
            }
        });
    }

    private static class ListViewHolder {
        NetworkImageView skinScreenShotImageView;
        TextView skinNameTextView;
        TextView skinDescriptionTextView;
    }
}