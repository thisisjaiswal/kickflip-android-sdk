package io.kickflip.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.eventbus.EventBus;

import java.io.File;
import java.io.IOException;

import io.kickflip.sdk.activity.GlassBroadcastActivity;
import io.kickflip.sdk.api.json.Stream;
import io.kickflip.sdk.av.BroadcastListener;
import io.kickflip.sdk.av.SessionConfig;
import io.kickflip.sdk.event.StreamLocationAddedEvent;
import io.kickflip.sdk.location.DeviceLocation;
import io.kickflip.sdk.model.BucketSession;
import io.kickflip.sdk.model.HLSStream;

import static com.google.common.base.Preconditions.checkNotNull;

public class Kickflip {
    public static final String TAG = "Kickflip";
    private static Context sContext;
    private static BucketSession sBucketSession;

    // Per-Stream settings
    private static SessionConfig sSessionConfig;          // Absolute path to root storage location
    private static BroadcastListener sBroadcastListener;

    /**
     * Register with Kickflip, creating a single new user identity per app installation.
     *
     * @param context the host application's {@link android.content.Context}
     */
    public static void setup(@NonNull Context context) {
        sContext = context;
    }

    public static void setBucketSession(BucketSession bucketSession) {
        sBucketSession = bucketSession;
    }

    public static BucketSession getBucketSession() {
        return sBucketSession;
    }

    public static void startBroadcastActivity(Activity host, BroadcastListener listener, Class activityClass) {
        checkNotNull(listener, host.getString(R.string.error_no_broadcastlistener));
        checkNotNull(sBucketSession);
        if (sSessionConfig == null) {
            setupDefaultSessionConfig();
        }
        sBroadcastListener = listener;
        Intent broadcastIntent = new Intent(host, activityClass);
        broadcastIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        host.startActivity(broadcastIntent);
    }

    public static void startGlassBroadcastActivity(Activity host, BroadcastListener listener) {
        checkNotNull(listener, host.getString(R.string.error_no_broadcastlistener));
        if (sSessionConfig == null) {
            setupDefaultSessionConfig();
        }
        sBroadcastListener = listener;
        Log.i(TAG, "startGlassBA ready? " + readyToBroadcast());
        Intent broadcastIntent = new Intent(host, GlassBroadcastActivity.class);
        broadcastIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        host.startActivity(broadcastIntent);
    }


//    public static void startMediaPlayerActivity(Activity host, String streamUrl, boolean newTask) {
//        Intent playbackIntent = new Intent(host, MediaPlayerActivity.class);
//        playbackIntent.putExtra("mediaUrl", streamUrl);
//        if (newTask) {
//            playbackIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        }
//        host.startActivity(playbackIntent);
//    }
//
//    public static void startMediaPlayerActivity(Activity host, HLSStream stream) {
//        Intent playbackIntent = new Intent(host, MediaPlayerActivity.class);
//        Bundle extras = new Bundle();
//        playbackIntent.putExtra("mediaUrl", stream.isLive()?stream.getLive():stream.getFull());
//        extras.putSerializable("STREAM", stream);
//        playbackIntent.putExtras(extras);
//        host.startActivity(playbackIntent);
//    }

    /**
     * Convenience method for attaching the current reverse geocoded device location to a given
     * {@link io.kickflip.sdk.api.json.Stream}
     *
     * @param context  the host application {@link android.content.Context}
     * @param stream   the {@link io.kickflip.sdk.api.json.Stream} to attach location to
     * @param eventBus an {@link com.google.common.eventbus.EventBus} to be notified of the complete action
     */
    public static void addLocationToStream(final Context context, final Stream stream, final EventBus eventBus) {
        DeviceLocation.getLastKnownLocation(context, false, new DeviceLocation.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                stream.setLatitude(location.getLatitude());
                stream.setLongitude(location.getLongitude());

                try {
                    Geocoder geocoder = new Geocoder(context);
                    Address address = geocoder.getFromLocation(location.getLatitude(),
                            location.getLongitude(), 1).get(0);
                    stream.setCity(address.getLocality());
                    stream.setCountry(address.getCountryName());
                    stream.setState(address.getAdminArea());
                    if (eventBus != null) {
                        eventBus.post(new StreamLocationAddedEvent());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * Get the {@link io.kickflip.sdk.av.BroadcastListener} to be notified on broadcast events.
     */
    public static BroadcastListener getBroadcastListener() {
        return sBroadcastListener;
    }

    /**
     * Set a {@link io.kickflip.sdk.av.BroadcastListener} to be notified on broadcast events.
     *
     * @param listener a {@link io.kickflip.sdk.av.BroadcastListener}
     */
    public static void setBroadcastListener(BroadcastListener listener) {
        sBroadcastListener = listener;
    }

    /**
     * Return the {@link io.kickflip.sdk.av.SessionConfig} responsible for configuring this broadcast.
     *
     * @return the {@link io.kickflip.sdk.av.SessionConfig} responsible for configuring this broadcast.
     * @hide
     */
    public static SessionConfig getSessionConfig() {
        return sSessionConfig;
    }

    /**
     * Clear the current SessionConfig, marking it as in use by a Broadcaster.
     * This is typically safe to do after constructing a Broadcaster, as it will
     * hold reference.
     *
     * @hide
     */
    public static void clearSessionConfig() {
        Log.i(TAG, "Clearing SessionConfig");
        sSessionConfig = null;
    }

    /**
     * Set the {@link io.kickflip.sdk.av.SessionConfig} responsible for configuring this broadcast.
     *
     * @param config the {@link io.kickflip.sdk.av.SessionConfig} responsible for configuring this broadcast.
     */
    public static void setSessionConfig(SessionConfig config) {
        sSessionConfig = config;
    }

    /**
     * Check whether credentials required for broadcast are provided
     *
     * @return true if credentials required for broadcast are provided. false otherwise
     */
    public static boolean readyToBroadcast() {
        return sSessionConfig != null && sBucketSession != null;
    }

    /**
     * Return whether the given Uri belongs to the kickflip.io authority.
     *
     * @param uri uri to test
     * @return true if the uri is of the kickflip.io authority.
     */
    public static boolean isKickflipUrl(Uri uri) {
        return uri != null && uri.getAuthority().contains("kickflip.io");
    }

    /**
     * Given a Kickflip.io url, return the stream id.
     * <p/>
     * e.g: https://kickflip.io/39df392c-4afe-4bf5-9583-acccd8212277/ returns
     * "39df392c-4afe-4bf5-9583-acccd8212277"
     *
     * @param uri the uri to test
     * @return the last path segment of the given uri, corresponding to the Kickflip {@link Stream#mStreamId}
     */
    public static String getStreamIdFromKickflipUrl(Uri uri) {
        if (uri == null) throw new IllegalArgumentException("uri cannot be null");
        return uri.getLastPathSegment().toString();
    }

    private static void setupDefaultSessionConfig() {
        Log.i(TAG, "Setting default SessonConfig");
        checkNotNull(sContext);
        String outputLocation = new File(sContext.getFilesDir(), "index.m3u8").getAbsolutePath();
        Kickflip.setSessionConfig(new SessionConfig.Builder(outputLocation)
                .withVideoBitrate(100 * 1000)
                .withPrivateVisibility(false)
                .withLocation(true)
                .withVideoResolution(480, 720)
                .build());
    }

    /**
     * Returns whether the current device is running Android 4.4, KitKat, or newer
     * <p/>
     * KitKat is required for certain Kickflip features like Adaptive bitrate streaming
     */
    public static boolean isKitKat() {
        return Build.VERSION.SDK_INT >= 19;
    }

}
