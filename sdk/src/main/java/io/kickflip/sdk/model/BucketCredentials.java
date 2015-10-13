package io.kickflip.sdk.model;

public class BucketCredentials {

    private String access_key;
    private String secret_key;
    private String session_token;

    public String getAccessKey() {
        return access_key;
    }

    public String getSecretKey() {
        return secret_key;
    }

    public String getSessionToken() {
        return session_token;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();

        ans.append(access_key);
        ans.append('\n');
        ans.append(secret_key);
        ans.append('\n');
        ans.append(session_token);
        ans.append('\n');

        return ans.toString();
    }

}
