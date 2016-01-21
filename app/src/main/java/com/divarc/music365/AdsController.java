package com.divarc.music365;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class AdsController {
    public static int fullscreenAdCounter;
    public static InterstitialAd mInterstitialAd;
    Context context;

    public AdsController(Context context) {
        this.context = context;
        mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId(context.getResources().getString(R.string.fullscreen_ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });
        requestNewInterstitial();
    }

    public void showFullScreenAd() {

        if((fullscreenAdCounter % 5) == 0 || fullscreenAdCounter==0 )
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
            }
        fullscreenAdCounter++;
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(adRequest);
    }


}
