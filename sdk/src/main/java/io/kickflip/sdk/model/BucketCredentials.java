package io.kickflip.sdk.model;

public class BucketCredentials {

    private String accessKey;
    private String secretKey;
    private String sessionToken;

    public String getAccessKey() {
        return accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();

        ans.append(accessKey);
        ans.append('\n');
        ans.append(secretKey);
        ans.append('\n');
        ans.append(sessionToken);
        ans.append('\n');

        return ans.toString();
    }

}
