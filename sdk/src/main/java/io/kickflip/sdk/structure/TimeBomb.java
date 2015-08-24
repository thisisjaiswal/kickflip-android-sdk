package io.kickflip.sdk.structure;

public class TimeBomb implements Runnable {

    private Thread t;
    private ExplosionListener listener;
    private Long time;
    private boolean started;

    public static TimeBomb create() {
        TimeBomb timeBomb = new TimeBomb();
        return timeBomb;
    }

    private TimeBomb() {
        t = new Thread(this);
        started = false;
    }

    public void start() {
        if (time == null || time <= 0 || listener == null)
            throw new IllegalStateException("internal state is not fully set");
        if (started) return;
        started = true;
        t.start();
    }

    public TimeBomb withTime(long time) {
        this.time = time;
        return this;
    }

    public TimeBomb withListener(ExplosionListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        listener.onExplosion();
    }

    public interface ExplosionListener {
        public void onExplosion();
    }
}