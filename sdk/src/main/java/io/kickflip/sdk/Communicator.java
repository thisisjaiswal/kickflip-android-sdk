package io.kickflip.sdk;

import retrofit.Callback;

public interface Communicator {

    public void hitStart(String lid, Callback<Object> cb);
    public void hitDone(String lid, Callback<Object> cb);
}
