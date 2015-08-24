package io.kickflip.sdk.service;

import java.util.List;

import io.kickflip.sdk.model.BucketDone;
import io.kickflip.sdk.model.BucketSession;
import io.kickflip.sdk.model.BucketStart;
import io.kickflip.sdk.model.HLSStream;
import io.kickflip.sdk.model.kanvas_live.ChatMessage;
import io.kickflip.sdk.model.kanvas_live.ChatMessages;
import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

public interface KanvasService {

    @GET("/live")
    public void getSession(Callback<BucketSession> cb);

//    Gives you 20 streams
    @GET("/live/top")
    public void getTopStreams(Callback<List<HLSStream>> cb);

    @PUT("/live/{lid}")
    public void finishStream(@Path("lid") String lid, @Body BucketDone bucketDone, Callback<Object> cb);

    @PUT("/live/{lid}")
    public void startStream(@Path("lid") String lid, @Body BucketStart bucketStart, Callback<Object> cb);

    @GET("/live/{lid}/chat/{since}/until/{until}")
    public void pollMessages(@Path("lid") String lid, @Path("since") long since, @Path("until") long until, Callback<ChatMessages> cb);

    @POST("/live/{lid}/chat")
    public void sendMessage(@Path("lid") String lid, @Body ChatMessage chatMessage, Callback<ChatMessage> cb);
}
