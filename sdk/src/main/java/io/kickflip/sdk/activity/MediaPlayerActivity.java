package io.kickflip.sdk.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import io.kickflip.sdk.R;
import io.kickflip.sdk.fragment.MediaPlayerFragment;
import io.kickflip.sdk.model.HLSStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @hide
 */
public class MediaPlayerActivity extends ImmersiveActivity {
    private static final String TAG = "MediaPlayerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiChangeListener();
        setContentView(R.layout.activity_media_playback);
        // This must be setup before
        //Uri intentData = getIntent().getData();
        //String mediaUrl = isKickflipUrl(intentData) ? intentData.toString() : getIntent().getStringExtra("mediaUrl");
        String mediaUrl = getIntent().getStringExtra("mediaUrl");
        HLSStream stream = (HLSStream) getIntent().getSerializableExtra("STREAM");
        checkNotNull(mediaUrl, new IllegalStateException("MediaPlayerActivity started without a mediaUrl"));
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, MediaPlayerFragment.newInstance(stream, mediaUrl))
                    .commit();
        }
    }

    public void UiChangeListener() {
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }
        });
    }
}
