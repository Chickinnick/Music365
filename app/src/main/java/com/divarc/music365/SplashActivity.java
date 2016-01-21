package com.divarc.music365;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.divarc.music365.entity.Datachannel;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends Activity implements  LoaderManager.LoaderCallbacks<Datachannel>{


    private static final String url =
            "http://365music.ru/xmlapp/ru_schedule.xml";


    public static final String DATA_EXTRA = "data";

    private View mContentView;
    private boolean mVisible;
    private static final int SPLASH_DURATION = 3000;
    private Handler myhandler;

    public static String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);


      //  mControlsView = findViewById(R.id.fullscreen_content_controls);

        // Set up the user interaction to manually show or hide the system UI.

        myhandler = new Handler();


        if(isOnline()){
            getLoaderManager().initLoader(0, Bundle.EMPTY, SplashActivity.this);

        }else{
            Toast.makeText(this, "no internet connection", Toast.LENGTH_LONG);
            myhandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                   finish();
                }
            }, 3000);
        }
        // run a thread to start the home screen

    }





    @Override
    protected void onResume() {
        super.onResume();


    }
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */





    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };








    @Override
    public Loader<Datachannel> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "on careate loadser");

        return new DataLoader(SplashActivity.this, myhandler, url);
    }

    @Override
    public void onLoadFinished(Loader<Datachannel> loader, Datachannel data) {
        Log.d(TAG, "on load finished");

        Datachannel  datachannel = data;

        Log.d(TAG, datachannel.getMessage().toString());
        Log.d(TAG, datachannel.getChannels().toString());



        final Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.putExtra(DATA_EXTRA, datachannel);
        myhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
            }
        }, SPLASH_DURATION);
    }

    @Override
    public void onLoaderReset(Loader<Datachannel> loader) {

    }
}
