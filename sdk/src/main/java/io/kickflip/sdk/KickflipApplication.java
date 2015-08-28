package io.kickflip.sdk;

import android.content.Context;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.kickflip.sdk.interceptor.KickflipInterceptor;
import io.kickflip.sdk.service.StreamService;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class KickflipApplication {

    private static boolean initialized = false;
    private static Context mContext;

    private static KickflipArguments arguments;

    private static StreamService streamService;

    private static void buildRestServices() {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        RestAdapter kanvasAdapter = new RestAdapter.Builder()
                .setEndpoint(arguments.getStreamBaseURL())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(gson))
                .setRequestInterceptor(new KickflipInterceptor())
                .build();

        streamService = kanvasAdapter.create(StreamService.class);
    }

    public static Context instance() {
        return mContext;
    }

    public static void init(Context context, KickflipArguments args) {
        if (initialized) return;
        initialized = true;
        mContext = context;
        arguments = args;
        buildRestServices();
    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Log.w("KICKFLIP", "kickflip application created");
//        instance = this;
//        buildRestServices();
//    }

    public static StreamService getStreamService() {
        return streamService;
    }
}
