package io.kickflip.sdk;

import java.io.File;

public interface S3UploadManager {

    public void requestUpload(String accessKey,
                              String secretKey,
                              String sessionToken,
                              String region,
                              String bucket,
                              String key,
                              File file,
                              boolean isLastUpload,
                              String lid);
}
