package io.kickflip.sdk;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.kickflip.sdk.service.KanvasService;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class KickflipApplication extends Application {

    private String ENDPOINT_KANVAS;

    private KanvasService kanvasService;

    private static KickflipApplication instance;

    private void buildRestServices() {
        ENDPOINT_KANVAS = instance().getResources().getString(R.string.kanvas_api);
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        RestAdapter kanvasAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT_KANVAS)
                .setConverter(new GsonConverter(gson))
                .build();

        kanvasService = kanvasAdapter.create(KanvasService.class);
    }

    public static KickflipApplication instance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        buildRestServices();
    }

    public KanvasService getKanvasService() {
        return kanvasService;
    }
}
