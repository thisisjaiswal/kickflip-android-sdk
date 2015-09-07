package io.kickflip.sdk;

import android.content.Context;

public class KickflipApplication {

    private static boolean initialized = false;
    private static Context mContext;

    private static Communicator communicator;

    public static Context instance() {
        return mContext;
    }

    public static void init(Context context, KickflipArguments args) {
        if (initialized) return;
        initialized = true;
        mContext = context;
        communicator = args.getCommunicator();
    }

    public static Communicator getCommunicator() {
        return communicator;
    }
}
