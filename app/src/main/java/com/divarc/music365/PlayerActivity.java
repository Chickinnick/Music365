package com.divarc.music365;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.CaptioningManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.divarc.music365.adapter.VolumeControlAdapter;
import com.divarc.music365.fragments.SettingsFragment;
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
import com.google.android.exoplayer.text.CaptionStyleCompat;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.util.Util;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerActivity extends Activity implements SurfaceHolder.Callback,
        DemoPlayer.Listener, DemoPlayer.CaptionListener, DemoPlayer.Id3MetadataListener,
        AudioCapabilitiesReceiver.Listener{

    public static final int TYPE_DASH = 0;
    public static final int TYPE_SS = 1;
    public static final int TYPE_HLS = 2;
    public static final int TYPE_OTHER = 3;

    public static final String CONTENT_TYPE_EXTRA = "content_type";
    public static final String CONTENT_ID_EXTRA = "content_id";

    public static final String TAG = "PlayerFragment";
    private static final int MENU_GROUP_TRACKS = 1;
    private static final int ID_OFFSET = 2;

    private static final CookieManager defaultCookieManager;

    static {
        defaultCookieManager = new CookieManager();
        defaultCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    private EventLogger eventLogger;
    private View debugRootView;

    private AspectRatioFrameLayout videoFrame;
    private SurfaceView surfaceView;
    private TextView playerStateTextView;

//  private Button retryButton;
//  private TextView retryMsg;

    private DemoPlayer player;
    private boolean playerNeedsPrepare;

    private long playerPosition;
    private    boolean enableBackgroundAudio = false;

    private Uri contentUri;
    private int contentType;

    private String contentId;



    public static final int CAPACITY = 8;

    private AudioCapabilitiesReceiver audioCapabilitiesReceiver;
    private AudioCapabilities audioCapabilities;
    SharedPreferences sharedPreferences;


    final ArrayList<String> volumeItems = new ArrayList<>(CAPACITY);

    Handler handler = new Handler();
    ListView volumeContainer;
    private ArrayAdapter<String> volumeAdapter;


    AudioManager am;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_player);
        contentType = 2;
        contentId = CONTENT_ID_EXTRA;
        contentUri = Uri.parse(getIntent().getExtras().getCharSequence(MainActivity.fullscreen_url).toString());
        sharedPreferences = getSharedPreferences(SettingsFragment.MESSAGE_PREF, Context.MODE_PRIVATE);

        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        int currentVolumeLevel = am.getStreamVolume(AudioManager.STREAM_MUSIC) / 2;
        for (int i = 0; i < currentVolumeLevel; i++)
            volumeItems.add(" ");


        volumeContainer = (ListView) findViewById(R.id.list_volume);
        volumeAdapter = new VolumeControlAdapter(this, R.layout.volume_item, volumeItems);
        volumeContainer.setAdapter(volumeAdapter);

        final GestureDetector gestureDetector  = new GestureDetector(this, new GestureListener());
        Log.d("PLAYER", "Player on create");
        View root = findViewById(R.id.root);
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    toggleControlsVisibility();
//                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                    view.performClick();
//                }onTouch
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });

        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {

                }
                return false;
            }
        });





        audioCapabilitiesReceiver = new AudioCapabilitiesReceiver(this, this);


        //debugRootView = findViewById(R.id.controls_root);

        videoFrame = (AspectRatioFrameLayout) findViewById(R.id.video_frame_f);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view_f);
        surfaceView.getHolder().addCallback(this);

        playerStateTextView = (TextView) findViewById(R.id.player_state_view);

        ImageView fullscreen_btn = (ImageView) findViewById(R.id.fullscreen_btn_fs);
        fullscreen_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    //   retryButton = (Button) findViewById(R.id.retry_button);
    //   retryButton.setOnClickListener(this);
    //   retryMsg = (TextView) view.findViewById(R.id.retry_tv);
        CookieHandler currentHandler = CookieHandler.getDefault();
        if (currentHandler != defaultCookieManager) {
            CookieHandler.setDefault(defaultCookieManager);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(contentUri!=null){
          preparePlayer();
        }
            startPlaying();
        // The player will be prepared on receiving audio capabilities.
        audioCapabilitiesReceiver.register();
    }


  // @Override
  // public void onActivityCreated(Bundle savedInstanceState) {
  //     super.onActivityCreated(savedInstanceState);
  //     configureSubtitleView();
  //     if(contentUri==null){
  //         StreamsAsyncTask streamsAsyncTask = new StreamsAsyncTask();
  //         streamsAsyncTask.execute();
    //     }
  //     toggleControlsVisibility();

    // }

    @Override
    public void onPause() {
        super.onPause();
        audioCapabilitiesReceiver.unregister();
        if (!enableBackgroundAudio) {
            releasePlayer();
        } else {
            player.setBackgrounded(true);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    // OnClickListener methods

 //   @Override
 //   public void onClick(View view) {
 //     //  contentUri = Uri.parse(sharedPreferences.getString(MainActivity.CH_NAME, "123"));
//
 //       if (view == retryButton) {
 //           if(contentUri.toString().equals("1")){
 //           StreamsAsyncTask streamsAsyncTask = new StreamsAsyncTask();
 //           streamsAsyncTask.execute();}
//
 //       }
//
 //   }

    public void startPlaying() {
        releasePlayer();
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
        String userAgent = Util.getUserAgent(getApplicationContext(), "ExoPlayerDemo");
        switch (contentType) {

            case TYPE_HLS:
                return new HlsRendererBuilder(this, userAgent, contentUri.toString(), audioCapabilities);
            case TYPE_OTHER:
   //             return new ExtractorRendererBuilder(this, userAgent, contentUri);
            default:
                throw new IllegalStateException("Unsupported type: " + contentType);
        }
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
            //updateButtonVisibilities();
        }
        player.setSurface(surfaceView.getHolder().getSurface());

        player.setPlayWhenReady(true);

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
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        showVolumeLayout();

        Log.d(MainActivity.class.getName(), String.valueOf(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)));
        Log.d(MainActivity.class.getName() + "!!", String.valueOf(am.getStreamVolume(AudioManager.STREAM_MUSIC)));
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // Or use adjustStreamVolume method.
            volumePlus();


            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            volumeDown();


            return true;
        }
        Log.d(MainActivity.class.getName() + "!!", String.valueOf(am.getStreamVolume(AudioManager.STREAM_MUSIC)));
        return super.onKeyDown(keyCode, event);
    }

    public void showVolumeLayout() {
        volumeContainer.setVisibility(View.VISIBLE);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                volumeContainer.setVisibility(View.GONE);

            }
        }, 5000);
    }

    public void volumeDown() {
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        volumeItems.remove(" ");
        volumeAdapter.notifyDataSetChanged();
    }

    public void volumePlus() {
        am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

        am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);


        if (volumeItems.size() < CAPACITY) {
            volumeItems.add(" ")
            ;
        }
        volumeAdapter.notifyDataSetChanged();
    }


    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            debugRootView.setVisibility(View.VISIBLE);
        }



        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
             //   showStateBuffering();
                text += "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                break;
            case ExoPlayer.STATE_IDLE:
             //   showStateBuffering();
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
             //   showStateBuffering();
                text += "preparing";
                break;
            case ExoPlayer.STATE_READY:
              //  hideStateBuffering();
                text += "ready";
                break;
            default:
                text += "unknown";
                break;
        }
        if(!playWhenReady){
          //  showStateBuffering();
        }
        playerStateTextView.setText(text);
       // updateButtonVisibilities();
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
      //  updateButtonVisibilities();
//        debugRootView.setVisibility(View.VISIBLE);
    }

 //  private void updateButtonVisibilities() {
 //      retryButton.setVisibility(playerNeedsPrepare ? View.VISIBLE : View.GONE);
 //      retryMsg.setVisibility(playerNeedsPrepare ? View.VISIBLE : View.GONE);
 //  }
    @Override
    public void onVideoSizeChanged(int width, int height, float pixelWidthAspectRatio) {
        videoFrame.setAspectRatio(
                height == 0 ? 1 : (width * pixelWidthAspectRatio) / height);
    }

    // User controls



    private boolean haveTracks(int type) {
        return player != null && player.getTrackCount(type) > 0;
    }








    // DemoPlayer.CaptionListener implementation

    @Override
    public void onCues(List<Cue> cues) {
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

    private void configureSubtitleView() {
        CaptionStyleCompat captionStyle;
        float captionFontScale;
        if (Util.SDK_INT >= 19) {
            captionStyle = getUserCaptionStyleV19();
            captionFontScale = getUserCaptionFontScaleV19();
        } else {
            captionStyle = CaptionStyleCompat.DEFAULT;
            captionFontScale = 1.0f;
        }
    }

    @TargetApi(19)
    private float getUserCaptionFontScaleV19() {
        CaptioningManager captioningManager =
                (CaptioningManager) this.getSystemService(Context.CAPTIONING_SERVICE);
        return captioningManager.getFontScale();
    }

    @TargetApi(19)
    private CaptionStyleCompat getUserCaptionStyleV19() {
        CaptioningManager captioningManager =
                (CaptioningManager) this.getSystemService(Context.CAPTIONING_SERVICE);
        return CaptionStyleCompat.createFromCaptionStyle(captioningManager.getUserStyle());
    }


    public void setPlayWhenReady(boolean b){
        player.setPlayWhenReady(b);
    }



  // public void showStateBuffering(){
  //         debugRootView.setVisibility(View.VISIBLE);
  // }
  // private void hideStateBuffering() {
  //         debugRootView.setVisibility(View.GONE);
  // }



    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) { return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
         Log.d("FLING",e1.getY()+  " "+ e2.getY() + "  " + velocityX + "   "+ velocityY);
          // showVolumeLayout();
          // if(velocityY<0  ) {
          //   volumePlus();

          // }else if(velocityY>0 ) {

          //   volumeDown();
          // }

            return true;
        }
    }

 //  public void showVolumeLayout() {
 //      volumeContainer.setVisibility(View.VISIBLE);

 //      handler.postDelayed(new Runnable() {
 //          @Override
 //          public void run() {
 //              volumeContainer.setVisibility(View.GONE);

 //          }
 //      }, 5000);
 //  }

 //  public void volumeDown() {
 //      am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
 //              AudioManager.ADJUST_LOWER,
 //              AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

 //      am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
 //              AudioManager.ADJUST_LOWER,
 //              AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
 //      volumeItems.remove(" ");
 //      volumeAdapter.notifyDataSetChanged();
 //  }

 //  public void volumePlus() {
 //      am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
 //              AudioManager.ADJUST_RAISE,
 //              AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

 //      am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
 //              AudioManager.ADJUST_RAISE,
 //              AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);


 //      if (volumeItems.size() < CAPACITY) {
 //          volumeItems.add(" ")
 //          ;
 //      }
 //      volumeAdapter.notifyDataSetChanged();
 //  }

//    class StreamsAsyncTask extends AsyncTask<Void, Void, String> {
//
//
//
//
//
//
//        @Override
//        protected String doInBackground(Void... params) {
//            OkHttpClient client = new OkHttpClient();
//            Request request = new Request.Builder()
//                    .url(url)
//                    .build();
//            Response response = null;
//            Channels osd = null;
//            Channel channel = null;
//            try {
//                response = client.newCall(request).execute();
//                String xmlData = response.body().string();
//
//                Serializer serializer = new Persister();
//
//                Reader reader = new StringReader(xmlData);
//                osd = serializer.read(Channels.class, reader, false);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            if (osd != null) {
//                channel = osd.getChannel();
//            }
//            if (channel != null) {
//                return channel.getStream();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//           super.onPostExecute(s);
//            if (s!=null) {
//                contentUri = Uri.parse(s);
//            }else{
//                contentUri = Uri.parse("1");
//            }
//           startPlaying();
//        }
//    }
}