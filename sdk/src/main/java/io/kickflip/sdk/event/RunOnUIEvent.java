package io.kickflip.sdk.event;

public class RunOnUIEvent {

    private Runnable runnable;

    public RunOnUIEvent(Runnable runnable) {
        this.runnable = runnable;
    }

    public Runnable getRunnable() {
        return runnable;
    }
}
