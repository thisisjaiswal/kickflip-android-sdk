package io.kickflip.sdk.av;

import io.kickflip.sdk.api.json.Stream;
import io.kickflip.sdk.exception.KickflipException;

/**
 * Provides callbacks for the major lifecycle benchmarks of a Broadcast.
 */
public interface BroadcastListener {
    /**
     * The broadcast has started, and is currently buffering.
     */
    public void onBroadcastStart(String lid);

    /**
     * The broadcast has ended.
     */
    public void onBroadcastStop(String lid);
}
