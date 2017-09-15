package com.skystreamtv.element_ez_stream.updater.network;

import com.skystreamtv.element_ez_stream.updater.model.App;
import com.skystreamtv.element_ez_stream.updater.model.Skin;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {

    @GET("element_kodi_app.json")
    Call<App> getMediaPlayerData();

    @GET("element_kodi_app_v16.json")
    Call<App> getMediaPlayerDataV16();

    @GET("element_update.json")
    Call<App> getUpdateData();

    @GET("element_ez_locations.json")
    Call<List<Skin>> getSkins();

    @GET("element_ez_locations_v16.json")
    Call<List<Skin>> getSkinsV16();
}
