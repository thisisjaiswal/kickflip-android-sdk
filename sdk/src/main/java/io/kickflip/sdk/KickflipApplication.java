package io.kickflip.sdk;

import android.content.Context;

public class KickflipApplication {

    private static boolean initialized = false;
    private static Context mContext;
    private static KickflipEventListener sKickflipEventListener;

    public static Context instance() {
        return mContext;
    }

    public static KickflipEventListener getKickflipEventListener() {
        return sKickflipEventListener;
    }

    public static void init(Context context) {
        if (initialized) return;
        initialized = true;
        mContext = context;
    }

    public static void setKickflipEventListener(KickflipEventListener kickflipEventListener) {
        sKickflipEventListener = kickflipEventListener;
    }
}
