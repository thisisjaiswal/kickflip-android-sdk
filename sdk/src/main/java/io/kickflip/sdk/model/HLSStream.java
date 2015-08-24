package io.kickflip.sdk.model;

import java.io.Serializable;

public class HLSStream implements Serializable {

    private long duration;
    private String full;
    private String lid;
    private String live;
    private String owner;
    private int state;
    private String start;
    private long tssize;
    private String thumb;

    public long getDuration() {
        return duration;
    }

    public String getLive() {
        return live;
    }

    public String getLid() {
        return lid;
    }

    public String getFull() {
        return full;
    }

    public String getOwner() {
        return owner;
    }

    public int getState() {
        return state;
    }

    public String getStart() {
        return start;
    }

    public long getTssize() {
        return tssize;
    }

    public String getThumb() {
        return thumb;
    }

    public boolean isLive() {
        return state == 1;
    }
}
