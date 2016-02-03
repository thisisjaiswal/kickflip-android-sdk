package io.kickflip.sdk;

import android.content.Context;

public class KickflipApplication {

    private static boolean initialized = false;
    private static Context mContext;

    public static Context instance() {
        return mContext;
    }

    public static void init(Context context) {
        if (initialized) return;
        initialized = true;
        mContext = context;
    }
}
