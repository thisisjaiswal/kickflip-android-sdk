package io.kickflip.sdk.av;

import android.content.Context;
import android.util.Log;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;

import io.kickflip.sdk.Kickflip;
import io.kickflip.sdk.S3UploadManager;
import io.kickflip.sdk.api.json.HlsStream;
import io.kickflip.sdk.event.HlsManifestWrittenEvent;
import io.kickflip.sdk.event.HlsSegmentWrittenEvent;
import io.kickflip.sdk.event.MuxerFinishedEvent;
import io.kickflip.sdk.event.S3UploadEvent;
import io.kickflip.sdk.event.ThumbnailWrittenEvent;
import io.kickflip.sdk.model.BucketCredentials;
import io.kickflip.sdk.model.BucketSession;


// TODO: Make HLS / RTMP Agnostic
public class Broadcaster extends AVRecorder {

    private static final String TAG = "Broadcaster";
    private static final boolean VERBOSE = false;

    private Context mContext;

    //    Events
    private BroadcastListener mBroadcastListener;
    private EventBus mEventBus;

    //    Config
    private SessionConfig mConfig;

    //    HLS
    private HlsFileObserver mFileObserver;

    //    Amazon
    private S3UploadManager s3UploadManager;
    private BucketSession bucketSession;
    private BucketCredentials bucketCredentials;

    //    Control variables
    private boolean mLastSegment;

    /**
     * Construct a Broadcaster with Session settings and Kickflip credentials
     *
     * @param context the host application {@link android.content.Context}.
     * @param config  the Session configuration. Specifies bitrates, resolution etc.
     */
    public Broadcaster(Context context, SessionConfig config, BucketSession bucketSession) throws IOException {
        super(config);
        init();
        mContext = context;
        mConfig = config;
        if (bucketSession != null) {
            this.bucketSession = bucketSession;
            this.bucketCredentials = bucketSession.getCred();
        }
        mConfig.getMuxer().setEventBus(mEventBus);
        if (VERBOSE) Log.i(TAG, "Initial video bitrate : " + mConfig.getVideoBitrate());

        String watchDir = config.getOutputDirectory().getAbsolutePath();
        mFileObserver = new HlsFileObserver(watchDir, mEventBus);
        mFileObserver.startWatching();
        if (VERBOSE) Log.i(TAG, "Watching " + watchDir);

        Kickflip.setup(context);
    }

    public void setBucketSession(BucketSession bucketSession) {
        this.bucketSession = bucketSession;
        this.bucketCredentials = bucketSession.getCred();
    }

    public void setS3UploadManager(S3UploadManager s3UploadManager) {
        this.s3UploadManager = s3UploadManager;
    }

    private void init() {
        mEventBus = new EventBus("Broadcaster");
        mEventBus.register(this);
    }

    /**
     * Set a Listener to be notified of basic Broadcast events relevant to
     * updating a broadcasting UI.
     * e.g: Broadcast begun, went live, stopped, or encountered an error.
     * <p/>
     * See {@link io.kickflip.sdk.av.BroadcastListener}
     *
     * @param listener
     */
    public void setBroadcastListener(BroadcastListener listener) {
        mBroadcastListener = listener;
    }

    /**
     * Set an {@link com.google.common.eventbus.EventBus} to be notified
     * of events between {@link io.kickflip.sdk.av.Broadcaster},
     * {@link io.kickflip.sdk.av.HlsFileObserver}
     * e.g: A HLS MPEG-TS segment or .m3u8 Manifest was written to disk, or uploaded.
     * See a list of events in {@link io.kickflip.sdk.event}
     *
     * @return
     */
    public EventBus getEventBus() {
        return mEventBus;
    }

    /**
     * Start broadcasting.
     * <p/>
     * Must be called after {@link Broadcaster#setPreviewDisplay(io.kickflip.sdk.view.GLCameraView)}
     */
    @Override
    public void startRecording() {
        super.startRecording();
        if (bucketSession == null || bucketCredentials == null)
            throw new IllegalStateException("No credentials available!");
        mCamEncoder.requestThumbnailOnDeltaFrameWithScaling(10, 1);
        onGotStreamResponse(null);
        if (mBroadcastListener != null) mBroadcastListener.onBroadcastStart(getLid());
    }

    private void onGotStreamResponse(HlsStream stream) {
        mConfig.getStream().setTitle(mConfig.getTitle());
        mConfig.getStream().setDescription(mConfig.getDescription());
        mConfig.getStream().setExtraInfo(mConfig.getExtraInfo());
        mConfig.getStream().setIsPrivate(mConfig.isPrivate());
    }

    /**
     * Check if the broadcast has gone live
     *
     * @return
     */
    public boolean isLive() {
//        TODO: implement isLive()
        return false;
    }

    /**
     * Stop broadcasting and release resources.
     * After this call this Broadcaster can no longer be used.
     */
    @Override
    public void stopRecording() {
        super.stopRecording();
        mLastSegment = true;
        if (mBroadcastListener != null) mBroadcastListener.onBroadcastStop(getLid());
    }

    /**
     * A .ts file was written in the recording directory.
     * <p/>
     * Use this opportunity to verify the segment is of expected size
     * given the target bitrate
     * <p/>
     * Called on a background thread
     */
    @Subscribe
    public void onSegmentWritten(HlsSegmentWrittenEvent event) {
        Log.w(TAG, "segmentWritten! lastUpload: " + mLastSegment);
        try {
            File hlsSegment = event.getSegment();
            queueOrSubmitUpload(keyForFilename(hlsSegment.getName()), hlsSegment);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getLid() {
        return bucketSession.getLid();
    }

    /**
     * A .m3u8 file was written in the recording directory.
     * <p/>
     * Called on a background thread
     */
    @Subscribe
    public void onManifestUpdated(HlsManifestWrittenEvent e) {
    }

    /**
     * An S3 .m3u8 upload completed.
     * <p/>
     * Called on a background thread
     */
    private void onManifestUploaded(S3UploadEvent uploadEvent) {
        Log.w("KICKFLIP", "Time to panic. Should not have uploaded the manifest.");
    }

    /**
     * A thumbnail was written in the recording directory.
     * <p/>
     * Called on a background thread
     */
    @Subscribe
    public void onThumbnailWritten(ThumbnailWrittenEvent e) {
        try {
            queueOrSubmitUpload(keyForFilename("thumb.jpg"), e.getThumbnailFile());
        } catch (Exception ex) {
            Log.i(TAG, "Error writing thumbanil");
            ex.printStackTrace();
        }
    }

    @Subscribe
    public void onDeadEvent(DeadEvent e) {
        if (VERBOSE) Log.i(TAG, "DeadEvent ");
    }


    @Subscribe
    public void onMuxerFinished(MuxerFinishedEvent e) {
        // TODO: Broadcaster uses AVRecorder reset()
        // this seems better than nulling and recreating Broadcaster
        // since it should be usable as a static object for
        // bg recording
    }

    /**
     * Construct an S3 Key for a given filename
     */
    private String keyForFilename(String fileName) {
        return fileName;
    }

    /**
     * Handle an upload, either submitting to the S3 client
     * or queueing for submission once credentials are ready
     *
     * @param key  destination key
     * @param file local file
     */
    private void queueOrSubmitUpload(String key, File file) {
        submitUpload(key, file);
    }

    private void submitUpload(final String key, final File file) {
        submitUpload(key, file, mLastSegment);
    }

    private void submitUpload(final String key, final File file, boolean lastUpload) {
        if (s3UploadManager == null)
            throw new IllegalStateException("s3UploadManager can't be null at this point!");

        Log.w(TAG, "Requesting upload to bucket");

        String accessKey = bucketCredentials.getAccessKey();
        String secretKey = bucketCredentials.getSecretKey();
        String sessionToken = bucketCredentials.getSessionToken();
        String region = bucketSession.getRegion();
        String bucket = bucketSession.getBucket();
        String fullKey = bucketSession.getKey() + "/" + key;
        s3UploadManager.requestUpload(accessKey,
                secretKey,
                sessionToken,
                region,
                bucket,
                fullKey,
                file);
    }

    public SessionConfig getSessionConfig() {
        return mConfig;
    }
}
