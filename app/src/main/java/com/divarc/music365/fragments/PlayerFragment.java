package com.divarc.music365.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.divarc.music365.MainActivity;
import com.divarc.music365.Player;
import com.divarc.music365.PlayerActivity;
import com.divarc.music365.R;
import com.divarc.music365.player.DemoPlayer;
import com.divarc.music365.player.EventLogger;
import com.divarc.music365.player.HlsRendererBuilder;
import com.google.android.exoplayer.AspectRatioFrameLayout;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.audio.AudioCapabilitiesReceiver;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.metadata.GeobMetadata;
import com.google.android.exoplayer.metadata.PrivMetadata;
import com.google.android.exoplayer.metadata.TxxxMetadata;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.SubtitleLayout;
import com.google.android.exoplayer.util.Util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;
import java.util.Map;

public class
        PlayerFragment extends Fragment implements SurfaceHolder.Callback,
        DemoPlayer.Listener, DemoPlayer.CaptionListener, DemoPlayer.Id3MetadataListener,
        AudioCapabilitiesReceiver.Listener, View.OnClickListener {

    public static final int TYPE_HLS = 2;

    public static final String TAG = "PlayerFragment";

    private static final CookieManager defaultCookieManager;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private EventLogger eventLogger;
    private View debugRootView;
    private View shutterView;
    private AspectRatioFrameLayout videoFrame;
    private SurfaceView surfaceView;
    private TextView playerStateTextView;
    private SubtitleLayout subtitleLayout;

    private Button retryButton;
    private TextView retryMsg;

    private DemoPlayer player;
    private boolean playerNeedsPrepare;

    private long playerPosition;
    private   boolean enableBackgroundAudio = false;

    private Uri contentUri;
    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private AudioCapabilities audioCapabilities;

    ImageView fullscreen;

    MainActivity mainActivity;
SharedPreferences sharedPreferences;
    private RelativeLayout controlsLayout;
    private ImageView circlePlayPlayer;
    private boolean paused  = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentUri = Uri.parse(getTag());
    }

    private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

    long lastClickTime = 0;

    @Override
    public void onClick(View v) {
        Log.d("onclick" , v.getId()+ " " + (v.getId()==R.id.fullscreen));
        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
            onDoubleClick(v);
        } else {
            onSingleClick(v);
        }
        lastClickTime = clickTime;
    }

    public void onSingleClick(View v){

        if(v.getId() == R.id.fullscreen){
            onDoubleClick(v);
        } else {
            boolean b = player.getPlayWhenReady();


            circlePlayPlayer.setVisibility(b?View.VISIBLE:View.INVISIBLE);
            setPlayWhenReady(!b);
        }
    };
    public void onDoubleClick(View v){
        Player.getInstance().setPlayer(player);
        Intent intent = new Intent(mainActivity, PlayerActivity.class);
        intent.putExtra(MainActivity.fullscreen_url, contentUri.toString());
        startActivity(intent);
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_broadcast, container, false);
        mainActivity  = (MainActivity) getActivity();
        controlsLayout = (RelativeLayout) view.findViewById(R.id.controls_layout);
        circlePlayPlayer = (ImageView) view.findViewById(R.id.circle_play_player);
        fullscreen = (ImageView) view.findViewById(R.id.fullscreen);
        sharedPreferences = getActivity().getSharedPreferences(SettingsFragment.MESSAGE_PREF, Context.MODE_PRIVATE);
        enableBackgroundAudio = sharedPreferences.getBoolean(SettingsFragment.KEY_PLAY_IN_BG, false);
        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(getActivity().getApplicationContext(), this);
        shutterView = view.findViewById(R.id.shutter);
        videoFrame = (AspectRatioFrameLayout) view.findViewById(R.id.video_frame);
        surfaceView = (SurfaceView) view.findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);
        controlsLayout.setOnClickListener(this);
        fullscreen.setOnClickListener(this);
        playerStateTextView = (TextView) view.findViewById(R.id.player_state_view);
        subtitleLayout = (SubtitleLayout) view.findViewById(R.id.subtitles);

        circlePlayPlayer.setVisibility(paused?View.VISIBLE:View.INVISIBLE);
    //  retryButton = (Button) view.findViewById(R.id.retry_button);
    //  retryButton.setOnClickListener(this);
    //  retryMsg = (TextView) view.findViewById(R.id.retry_tv);
        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }



           return view;
    }




    @Override
    public void onResume() {
        super.onResume();
        // The player will be prepared on receiving audio capabilities.
        if(contentUri!= null){
            preparePlayer();

        }

          //  startPlaying();
        audioCapabilitiesReceiver.register();
    }


    @Override
    public void onPause() {
        super.onPause();

        audioCapabilitiesReceiver.unregister();
        shutterView.setVisibility(View.VISIBLE);
        if (!enableBackgroundAudio) {
            releasePlayer();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
         releasePlayer();
    }

    // OnClickListener methods


    public void startPlaying() {
        //releasePlayer();
        preparePlayer();
    }

    // AudioCapabilitiesReceiver.Listener methods

    @Override
    public void onAudioCapabilitiesChanged(AudioCapabilities audioCapabilities) {
        boolean audioCapabilitiesChanged = !audioCapabilities.equals(this.audioCapabilities);
        if (player == null || audioCapabilitiesChanged) {
            this.audioCapabilities = audioCapabilities;
            startPlaying();
        } else if (player != null) {
            player.setBackgrounded(false);
        }
    }

    // Internal methods

    private DemoPlayer.RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(getActivity().getApplicationContext(), "ExoPlayerDemo");
                return new HlsRendererBuilder(getActivity().getApplicationContext(), userAgent, contentUri.toString(), audioCapabilities);
    }

    private void preparePlayer() {
        if (player == null) {
            player = new DemoPlayer(getRendererBuilder());
            player.addListener(this);
            player.setCaptionListener(this);
            player.setMetadataListener(this);
            player.seekTo(playerPosition);
            playerNeedsPrepare = true;
            eventLogger = new EventLogger();
            eventLogger.startSession();
            player.addListener(eventLogger);
            player.setInfoListener(eventLogger);
            player.setInternalErrorListener(eventLogger);
            player.setBackgrounded(enableBackgroundAudio);


        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
          //  updateButtonVisibilities();
        }
        player.setSurface(surfaceView.getHolder().getSurface());
        circlePlayPlayer.setVisibility(paused ? View.VISIBLE : View.INVISIBLE);
        player.setPlayWhenReady(!paused);
    }


    private void releasePlayer() {
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
            eventLogger.endSession();
            eventLogger = null;
        }
    }

    // DemoPlayer.Listener implementation

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            debugRootView.setVisibility(View.VISIBLE);
        }
        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                showStateBuffering();
                text += "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                break;
            case ExoPlayer.STATE_IDLE:
                showStateBuffering();
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                showStateBuffering();
                text += "preparing";
                break;
            case ExoPlayer.STATE_READY:
                hideStateBuffering();
                text += "ready";
                break;
            default:
                text += "unknown";
                break;
        }
        if(!playWhenReady){
            showStateBuffering();
        }
//        playerStateTextView.setText(text);
        //updateButtonVisibilities();
    }


    @Override
    public void onError(Exception e) {
        if (e instanceof UnsupportedDrmException) {
            // Special case DRM failures.
            UnsupportedDrmException unsupportedDrmException = (UnsupportedDrmException) e;
 /*            int stringId = Util.SDK_INT < 18 ? R.string.drm_error_not_supported
                    : unsupportedDrmException.reason == UnsupportedDrmException.REASON_UNSUPPORTED_SCHEME
                    ? R.string.drm_error_unsupported_scheme : R.string.drm_error_unknown;
            Toast.makeText(getActivity().getApplicationContext(), stringId, Toast.LENGTH_LONG).show();
 */
        }
        playerNeedsPrepare = true;
        //updateButtonVisibilities();
//        debugRootView.setVisibility(View.VISIBLE);
    }

  //  private void updateButtonVisibilities() {
  //      retryButton.setVisibility(playerNeedsPrepare ? View.VISIBLE : View.GONE);
  //      retryMsg.setVisibility(playerNeedsPrepare? View.VISIBLE: View.GONE);
  //  }
    @Override
    public void onVideoSizeChanged(int width, int height, float pixelWidthAspectRatio) {
        shutterView.setVisibility(View.GONE);
        videoFrame.setAspectRatio(
                height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
    }




    @Override
    public void onCues(List<Cue> cues) {
        subtitleLayout.setCues(cues);
    }

    // DemoPlayer.MetadataListener implementation

    @Override
    public void onId3Metadata(Map<String, Object> metadata) {
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            if (TxxxMetadata.TYPE.equals(entry.getKey())) {
                TxxxMetadata txxxMetadata = (TxxxMetadata) entry.getValue();
                Log.i(TAG, String.format("ID3 TimedMetadata %s: description=%s, value=%s",
                        TxxxMetadata.TYPE, txxxMetadata.description, txxxMetadata.value));
            } else if (PrivMetadata.TYPE.equals(entry.getKey())) {
                PrivMetadata privMetadata = (PrivMetadata) entry.getValue();
                Log.i(TAG, String.format("ID3 TimedMetadata %s: owner=%s",
                        PrivMetadata.TYPE, privMetadata.owner));
            } else if (GeobMetadata.TYPE.equals(entry.getKey())) {
                GeobMetadata geobMetadata = (GeobMetadata) entry.getValue();
                Log.i(TAG, String.format("ID3 TimedMetadata %s: mimeType=%s, filename=%s, description=%s",
                        GeobMetadata.TYPE, geobMetadata.mimeType, geobMetadata.filename,
                        geobMetadata.description));
            } else {
                Log.i(TAG, String.format("ID3 TimedMetadata %s", entry.getKey()));
            }
        }
    }

    // SurfaceHolder.Callback implementation

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (player != null) {
            player.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (player != null) {
            player.blockingClearSurface();
        }
    }


    public void setPlayWhenReady(boolean b){
        paused = b;
        player.setPlayWhenReady(b);
    }



    public void showStateBuffering(){
   //         debugRootView.setVisibility(View.VISIBLE);
    }
    private void hideStateBuffering() {
//            debugRootView.setVisibility(View.GONE);
    }


}