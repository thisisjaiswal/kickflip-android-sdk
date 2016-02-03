package io.kickflip.sdk.model;

import java.io.Serializable;

public class BucketSession implements Serializable {

    private String lid;
    private BucketCredentials cred;
    private String region;
    private String bucket;
    private String key;

    public String getLid() {
        return lid;
    }

    public BucketCredentials getCred() {
        return cred;
    }

    public String getRegion() {
        return region;
    }

    public String getBucket() {
        return bucket;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        StringBuilder ans = new StringBuilder();

        ans.append(lid);
        ans.append('\n');
        ans.append('\n');
        ans.append(cred);
        ans.append('\n');
        ans.append('\n');
        ans.append(region);
        ans.append('\n');
        ans.append(bucket);
        ans.append('\n');
        ans.append(key);
        ans.append('\n');

        return ans.toString();
    }

}
