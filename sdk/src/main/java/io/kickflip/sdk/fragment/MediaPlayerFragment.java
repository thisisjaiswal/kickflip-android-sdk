package io.kickflip.sdk.fragment;


import android.app.Fragment;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.chilicat.m3u8.Element;
import net.chilicat.m3u8.Playlist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.kickflip.sdk.KickflipApplication;
import io.kickflip.sdk.R;
import io.kickflip.sdk.adapter.ChatMessageAdapter;
import io.kickflip.sdk.av.M3u8Parser;
import io.kickflip.sdk.model.HLSStream;
import io.kickflip.sdk.model.kanvas_live.ChatMessage;
import io.kickflip.sdk.model.kanvas_live.ChatMessages;
import io.kickflip.sdk.structure.TimeBomb;
import io.kickflip.sdk.utilities.MathUtils;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MediaPlayerFragment extends Fragment implements TextureView.SurfaceTextureListener, MediaController.MediaPlayerControl {
    private static final String TAG = "MediaPlayerFragment";
    private static final boolean VERBOSE = false;
    private static final String ARG_URL = "url";
    private static final String ARG_STREAM = "STREAM";
    private static final long interval = 2000;

    private ProgressBar mProgress;
    private TextureView mTextureView;
    private TextView mLiveLabel;
    private EditText mEditText;
    private View mSendButton;
    private View mChatInput;
    private MediaPlayer mMediaPlayer;
    private MediaController mMediaController;
    private String mMediaUrl;

    // M3u8 Media properties inferred from .m3u8
    private int mDurationMs;
    private boolean mIsLive;
    private boolean mediaPlayerReleased = false;

    private Surface mSurface;

    private HLSStream hlsStream;

    private ListView mListView;
    private ChatMessageAdapter mAdapter;
    private List<ChatMessage> messages;

    private TimeBomb.ExplosionListener explosionListener;

    private long since;
    private long until;

    private M3u8Parser.M3u8ParserCallback m3u8ParserCallback = new M3u8Parser.M3u8ParserCallback() {
        @Override
        public void onSuccess(Playlist playlist) {
            updateUIWithM3u8Playlist(playlist);
            setupMediaPlayer(mSurface);
        }

        @Override
        public void onError(Exception e) {
            if (VERBOSE) Log.i(TAG, "m3u8 parse failed " + e.getMessage());
        }
    };

    private View.OnTouchListener mTextureViewTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mMediaController != null && isResumed()) {
//                mMediaController.show();
            }
            return false;
        }
    };

    public static MediaPlayerFragment newInstance(HLSStream stream, String mediaUrl) {
        MediaPlayerFragment fragment = new MediaPlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_URL, mediaUrl);
        args.putSerializable(ARG_STREAM, stream);
        fragment.setArguments(args);
        return fragment;
    }

    public MediaPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMediaUrl = getArguments().getString(ARG_URL);
            hlsStream = (HLSStream) getArguments().getSerializable(ARG_STREAM);
            explosionListener = new TimeBomb.ExplosionListener() {
                @Override
                public void onExplosion() {
                    fetchMessages();
                }
            };
            if (mMediaUrl.substring(mMediaUrl.lastIndexOf(".") + 1).equals("m3u8")) {
                parseM3u8FromMediaUrl();
            } else {
                throw new IllegalArgumentException("Unknown HLS media url format: " + mMediaUrl);
            }
        }
    }

    private void updateUIWithM3u8Playlist(Playlist playlist) {
        int durationSec = 0;
        for (Element element : playlist.getElements()) {
            durationSec += element.getDuration();
        }
        mIsLive = !playlist.isEndSet();
        mDurationMs = durationSec * 1000;

        if (mIsLive) {
            mLiveLabel.setVisibility(View.VISIBLE);
        }
    }

    private void setupMediaPlayer(Surface displaySurface) {
        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setSurface(displaySurface);
            mMediaPlayer.setDataSource(mMediaUrl);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (VERBOSE) Log.i(TAG, "media player prepared");
                    mProgress.setVisibility(View.GONE);
                    mMediaController.setEnabled(false);
//                    mMediaController.setEnabled(true);
//                    mTextureView.setOnTouchListener(mTextureViewTouchListener);
                    mMediaPlayer.start();
                    mChatInput.setVisibility(View.VISIBLE);
                    init();
                }
            });
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (getActivity() != null) {
                        if (VERBOSE) Log.i(TAG, "media player complete. finishing");
                        getActivity().finish();
                    }
                }
            });

            mMediaController = new MediaController(getActivity());
            mMediaController.setAnchorView(mTextureView);
            mMediaController.setMediaPlayer(this);

            mMediaPlayer.prepareAsync();
        } catch (IOException ioe) {

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mProgress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mMediaPlayer != null) {
            mediaPlayerReleased = true;
            mMediaPlayer.release();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_media_player, container, false);
        if (root != null) {
            mTextureView = (TextureView) root.findViewById(R.id.textureView);
            mTextureView.setSurfaceTextureListener(this);
            mProgress = (ProgressBar) root.findViewById(R.id.progress);
            mLiveLabel = (TextView) root.findViewById(R.id.liveLabel);
            mListView = (ListView) root.findViewById(R.id.fragment_media_player_chat_listview);
            mEditText = (EditText) root.findViewById(R.id.fragment_media_player_message_input);
            mSendButton = root.findViewById(R.id.fragment_media_player_send_button);
            mChatInput = root.findViewById(R.id.fragment_media_player_chat_input);
            mSendButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    sendMessage();
                }
            });
        }
        return root;
    }

    private void init() {
        messages = new ArrayList<ChatMessage>();
        mAdapter = new ChatMessageAdapter(getActivity(), messages);
        mListView.setAdapter(mAdapter);
        setExplosion();
    }

    private void setExplosion() {
        TimeBomb.create()
                .withListener(explosionListener)
                .withTime(interval)
                .start();
    }

    private void sendMessage() {
        String message = mEditText.getText().toString();
        mEditText.setText("");
        if (message.equals("")) return;


        KickflipApplication.getKanvasService().sendMessage(hlsStream.getLid(), new ChatMessage(message, null, MathUtils.round(mMediaPlayer.getCurrentPosition() / 1000.0, 2)), new Callback<ChatMessage>() {
            @Override
            public void success(final ChatMessage chatMessage, Response response) {

            }

            @Override
            public void failure(RetrofitError error) {
                Log.w(TAG, "error sending message: " + error.getMessage());
            }
        });
    }

    private void fetchMessages() {
        if (mediaPlayerReleased || !mMediaPlayer.isPlaying()) return;
//        until = (new Date().getTime() - startedAt)/1000 ;
        until = mMediaPlayer.getCurrentPosition() / 1000;
        Log.w(TAG, "since: " + since + ", until: " + until + " - " + (hlsStream == null));
        KickflipApplication.getKanvasService().pollMessages(hlsStream.getLid(), since, until, new Callback<ChatMessages>() {
            @Override
            public void success(final ChatMessages chatMessages, Response response) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        messages.addAll(chatMessages.getChats());
//                        mAdapter.notifyDataSetChanged();
////                        This makes messages go up smoothly, but fading out animation is still pending
//                        mListView.smoothScrollToPosition(mListView.getCount() - 1);
                        addMessages(chatMessages);
                        since = until;
                        setExplosion();
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void addMessage(ChatMessage chatMessage) {
        messages.add(chatMessage);
        mAdapter.notifyDataSetChanged();
        mListView.smoothScrollToPosition(mListView.getCount() - 1);
    }

    private void addMessages(ChatMessages chatMessages) {
        messages.addAll(chatMessages.getChats());
        mAdapter.notifyDataSetChanged();
        mListView.smoothScrollToPosition(mListView.getCount() - 1);
    }

    private void parseM3u8FromMediaUrl() {
        M3u8Parser.parseM3u8(mMediaUrl, m3u8ParserCallback);
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        mSurface = new Surface(surfaceTexture);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void start() {
        mMediaPlayer.start();
    }

    @Override
    public void pause() {
        mMediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int pos) {
        mMediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return !mIsLive;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
