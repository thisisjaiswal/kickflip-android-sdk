package io.kickflip.sdk.model;

public class MultipleShareObject {

    private ShareInfoObject sms;
    private ShareInfoObject email;
    private String share_url;

    public ShareInfoObject getSms() {
        return sms;
    }

    public ShareInfoObject getEmail() {
        return email;
    }

    public String getShare_url() {
        return share_url;
    }
}
