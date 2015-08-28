package io.kickflip.sdk;

public class KickflipArguments {

    private String streamBaseURL;

    public KickflipArguments(String streamBaseURL) {
        this.streamBaseURL = streamBaseURL;
    }

    public String getStreamBaseURL() {
        return streamBaseURL;
    }
}
