package io.kickflip.sdk;

import android.app.Application;
import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.kickflip.sdk.interceptor.KickflipInterceptor;
import io.kickflip.sdk.service.KanvasService;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class KickflipApplication {

    private static boolean initialized = false;
    private static Context mContext;

    private static String ENDPOINT_KANVAS;

    private static KanvasService kanvasService;

    private static void buildRestServices() {
        ENDPOINT_KANVAS = instance().getResources().getString(R.string.kanvas_api);
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        RestAdapter kanvasAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT_KANVAS)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new KickflipInterceptor())
                .build();

        kanvasService = kanvasAdapter.create(KanvasService.class);
    }

    public static Context instance() {
        return mContext;
    }

    public static void init(Context context) {
        if (initialized) return;
        initialized = true;
        mContext = context;
        buildRestServices();
    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.w("KICKFLIP", "kickflip application created");
//        instance = this;
//        buildRestServices();
//    }

    public static KanvasService getKanvasService() {
        return kanvasService;
    }
}
