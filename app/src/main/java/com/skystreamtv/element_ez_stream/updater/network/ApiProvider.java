package com.skystreamtv.element_ez_stream.updater.network;


import android.os.Build;

import com.skystreamtv.element_ez_stream.updater.model.App;
import com.skystreamtv.element_ez_stream.updater.model.Skin;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiProvider {

    private static ApiProvider instance;
    private ApiService service;

    private ApiProvider() {

        OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        // Request customization: add request headers
                        Request.Builder requestBuilder = original.newBuilder().
                                addHeader("Content-Type", "application/json").
                                addHeader("Accept", "application/json");
                        requestBuilder.method(original.method(), original.body());
                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                });
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpBuilder.addInterceptor(logging);
        OkHttpClient client = httpBuilder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://s3.amazonaws.com/updaterbeta/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        service = retrofit.create(ApiService.class);
    }

    private static boolean isOSAboveKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static ApiProvider getInstance() {
        if (instance == null) {
            instance = new ApiProvider();
        }

        return instance;
    }

    public ApiService getApiService() {
        return service;
    }

    public void getUpdaterData(Callback<App> callback) {
        service.getUpdateData().enqueue(callback);
    }

    public void getPlayerData(Callback<App> callback) {
        if (isOSAboveKitKat()) {
            service.getMediaPlayerData().enqueue(callback);
        } else {
            service.getMediaPlayerDataV16().enqueue(callback);
        }
    }

    public void getSkinsData(Callback<List<Skin>> callback) {
        if (isOSAboveKitKat()) {
            service.getSkins().enqueue(callback);
        } else {
            service.getSkinsV16().enqueue(callback);
        }
    }
}
