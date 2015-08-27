package io.kickflip.sdk.service;

import io.kickflip.sdk.model.BucketDone;
import io.kickflip.sdk.model.BucketSession;
import io.kickflip.sdk.model.BucketStart;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Created by ignaciogutierrez on 8/26/15.
 */
public interface StreamService {


    @GET("/live")
    public void getSession(Callback<BucketSession> cb);

    @PUT("/live/{lid}")
    public void finishStream(@Path("lid") String lid, @Body BucketDone bucketDone, Callback<Object> cb);

    @PUT("/live/{lid}")
    public void startStream(@Path("lid") String lid, @Body BucketStart bucketStart, Callback<Object> cb);
}
