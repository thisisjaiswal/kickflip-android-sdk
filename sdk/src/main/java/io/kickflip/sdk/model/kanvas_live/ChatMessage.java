package io.kickflip.sdk.model.kanvas_live;

public class ChatMessage {

    private Double elapse;
    private Boolean offline;
    private String message;
    private String owner;
    private String lid;
    private String cid;
    private Boolean invisible;

    public ChatMessage(String message, String owner, double elapse) {
        this.message = message;
        this.owner = owner;
        this.elapse = elapse;
    }

    public ChatMessage() {

    }

    public Double getElapse() {
        return elapse;
    }

    public String getLid() {
        return lid;
    }

    public String getMessage() {
        return message;
    }

    public String getOwner() {
        return owner;
    }

    public String getCid() {
        return cid;
    }

    public Boolean isOffline() {
        return offline;
    }

    public Boolean isInvisible() {
        return invisible != null && invisible;
    }

    public void setInvisible(Boolean invisible) {
        this.invisible = invisible;
    }
}
