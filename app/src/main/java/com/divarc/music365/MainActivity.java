package com.divarc.music365;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.vending.billing.IInAppBillingService;
import com.divarc.music365.entity.Channel;
import com.divarc.music365.entity.Datachannel;
import com.divarc.music365.fragments.ChannelsFragment;
import com.divarc.music365.fragments.ProgrammsFragment;
import com.divarc.music365.fragments.SettingsFragment;
import com.divarc.music365.util.IabBroadcastReceiver;
import com.divarc.music365.util.IabHelper;
import com.divarc.music365.util.IabResult;
import com.divarc.music365.util.Inventory;
import com.divarc.music365.util.Purchase;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import net.simonvt.menudrawer.MenuDrawer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener, IabBroadcastReceiver.IabBroadcastListener {

    private static final String STATE_ACTIVE_VIEW_ID = "activeViewId";

    private static final String STATE_MENUDRAWER = "menuDrawer";
    private static final String ITEM_ID_LIST = "ITEM_ID_LIST";
    private MenuDrawer mMenuDrawer;
    private int mActiveViewId;
    private String TAG = MainActivity.class.getSimpleName();

    public Datachannel datachannel;
    private List<Channel> channels;
    private List<String> channelNames = new ArrayList<>();

    Spinner spinner;
    Button menuButton;
    TextView titleActionBar;
    ProgrammsFragment programmsFragment;
        Intent shareIntent;

    static final String SKU_PREMIUM = "premium";

    static final int RC_REQUEST = 10001;

    String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnQACq1xZRKOB7C6aMsW1B9xP7aav7bGvAMQb/KBbQ+H8trLShcxMFvbx8GAJv+PEWfsZAl96FOK1UGRY7fwe+TFRWU6HSql3isdHrBrDZeifKTQvECVWKCa3E849XBD8rITE+EhPlf/CEd80SOeFRciYGhR9L8WWm/4bR1txSB8qXqCopIIOxsqHiH/gb06olIbxuORqhl/OLaGbJusinfNfryAe3R7lgIt5LAHw1JmPNXciBbRAAwzKc8F/zJELkj4o9iCr117IOSVWIiAAQxtRMXNj/63hwzP+O+KEUNlP41/LyHU4fpRcC4Fhye2dsYnUtCX0ROvesDx4SxqVGwIDAQAB";
    IabHelper mHelper;
    IabBroadcastReceiver mBroadcastReceiver;
   public boolean mIsPremium = false;

    AdsController adsController;
    IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };
    private ArrayAdapter spinnerArrayAdapter;
    public static final String fullscreen_url = "EXTRA_FULLSCREEN_URL";

    @Override
    protected void onCreate(Bundle inState) {
        super.onCreate(inState);
        if (inState != null) {
            mActiveViewId = inState.getInt(STATE_ACTIVE_VIEW_ID);
        }
        mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_CONTENT);
        mMenuDrawer.setContentView(R.layout.activity_main);
        mMenuDrawer.setMenuView(R.layout.menu_scrollview);
        mMenuDrawer.setMenuSize(550);
        adsController = new AdsController(this);
        Log.d("tag", mMenuDrawer.getMenuSize() + " ");
        findViewById(R.id.item1).setOnClickListener(this);
        findViewById(R.id.item2).setOnClickListener(this);
        findViewById(R.id.item3).setOnClickListener(this);
        findViewById(R.id.item4).setOnClickListener(this);
        findViewById(R.id.item5).setOnClickListener(this);
        findViewById(R.id.item6).setOnClickListener(this);
        findViewById(R.id.item7).setOnClickListener(this);

        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "http://365music.ru/");
        Toolbar mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);



        programmsFragment = new ProgrammsFragment();
        menuButton = (Button) findViewById(R.id.menu_btn);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new ChannelsFragment()).commit();

        datachannel = (Datachannel) getIntent().getSerializableExtra(SplashActivity.DATA_EXTRA);

        channels = datachannel.getChannels();
        for (Channel channel :channels){
            channelNames.add(channel.getTitle());
        }
        spinner = (Spinner)findViewById(R.id.action_bar_spinner);
        titleActionBar = (TextView) findViewById(R.id.textTitle);
        titleActionBar.setText(R.string.all_channels);
        titleActionBar.setVisibility(View.VISIBLE);
        spinnerArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.item_spinner, channelNames
                );
        spinnerArrayAdapter.setDropDownViewResource(R.layout.item_spinner);
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(this);
        spinner.setVisibility(View.GONE);


        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        ArrayList<String> skuList = new ArrayList<String> ();
        skuList.add("premiumUpgrade");;
        Bundle querySkus = new Bundle();
        querySkus.putStringArrayList(ITEM_ID_LIST, skuList);
        adsController.showFullScreenAd();

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                   // complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;

                // Important: Dynamically register for broadcast messages about updated purchases.
                // We register the receiver here instead of as a <receiver> in the Manifest
                // because we always call getPurchases() at startup, so therefore we can ignore
                // any broadcasts sent while the app isn't running.
                // Note: registering this listener in an Activity is a bad idea, but is done here
                // because this is a SAMPLE. Regardless, the receiver must be registered after
                // IabHelper is setup, but before first call to getPurchases().
                mBroadcastReceiver = new IabBroadcastReceiver(MainActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);

                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(!mIsPremium){
        adsController.showFullScreenAd();
    }}


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
        // very important:
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

        // very important:
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState) {
        super.onRestoreInstanceState(inState);
        mMenuDrawer.restoreState(inState.getParcelable(STATE_MENUDRAWER));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_MENUDRAWER, mMenuDrawer.saveState());
        outState.putInt(STATE_ACTIVE_VIEW_ID, mActiveViewId);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mMenuDrawer.toggleMenu();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);


        MenuItem shareItem = menu.findItem(R.id.action_share);
        ShareActionProvider myShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        myShareActionProvider.setShareIntent(shareIntent);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        final int drawerState = mMenuDrawer.getDrawerState();
        if (drawerState == MenuDrawer.STATE_OPEN || drawerState == MenuDrawer.STATE_OPENING) {
            mMenuDrawer.closeMenu();
            return;
        }

    }

    @Override
    public void onClick(View v) {
        mMenuDrawer.setActiveView(v);
        mMenuDrawer.closeMenu();
        mActiveViewId = v.getId();
        Log.d(TAG, "id: " + v.getId());
        switch(mActiveViewId){
            case R.id.item1:
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new ChannelsFragment()).commit();

                break;
            case R.id.item2:
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, programmsFragment).commit();

                break;
            case R.id.item3:
                onUpgradeAppButtonClicked(v);
                break;
            case R.id.item4:
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new SettingsFragment()).commit();
                break;
            case R.id.item5:
                break;
            case R.id.item6:
                String url = "http://365music.ru/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            case R.id.item7:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"info@365music.ru"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "FEEDBACK");
                startActivity(Intent.createChooser(intent, "Send Email"));
                break;
        }


        toggleActionBar();
    }

    private void toggleActionBar() {
        titleActionBar.setVisibility(titleActionBar.isShown() ? View.GONE : View.VISIBLE);
        spinner.setVisibility((mActiveViewId != R.id.item2) ? View.GONE : View.VISIBLE);
        if(mActiveViewId==R.id.item1){
            titleActionBar.setText(R.string.all_channels);
            titleActionBar.setVisibility((mActiveViewId == R.id.item1) ? View.VISIBLE : View.GONE);
        }else if(mActiveViewId==R.id.item4){
            titleActionBar.setText(R.string.item_4_settings);
            titleActionBar.setVisibility((mActiveViewId == R.id.item4) ? View.VISIBLE : View.GONE);
        }
    }

    public void toggleMenu(View v){
        if(mMenuDrawer.isMenuVisible()){
            mMenuDrawer.closeMenu();
        }else {mMenuDrawer.openMenu();}
    }




    public void showChannelInfoPopup(View view) {

    }



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        programmsFragment.setIndexOfChannel(position);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void setChannelTitle(String name) {
        String broadcast = getResources().getString(R.string.broadcast);
        titleActionBar.setText(broadcast + " " + name);
    }

    public void openVk(View view) {
        String url = "http://vk.com/music365tv";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void openFB(View view) {
        String url = "https://www.facebook.com/365musictv";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }



    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));


            updateUi();
            setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        mHelper.queryInventoryAsync(mGotInventoryListener);
    }



    // User clicked the "Upgrade to Premium" button.
    public void onUpgradeAppButtonClicked(View arg0) {
        Log.d(TAG, "Upgrade button clicked; launching purchase flow for upgrade.");
        setWaitScreen(true);

        /* TODO: for security, generate your payload here for verification. See the comments on
         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
         *        an empty string, but on a production app you should carefully generate this. */
        String payload = "";

        mHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_REQUEST,
                mPurchaseFinishedListener, payload);
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");


            if (purchase.getSku().equals(SKU_PREMIUM)) {
                // bought the premium upgrade!
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                alert("Thank you for upgrading to premium!");
                mIsPremium = true;
                updateUi();
                setWaitScreen(false);
            }

        }
    };





    // updates UI to reflect model
    public void updateUi() {
        // update the car color to reflect premium status or lack thereof
    //    ((ImageView)findViewById(R.id.adView)).setImageResource(mIsPremium ? R.drawable.premium : R.drawable.free);

        // "Upgrade" button is only visible if the user is not premium
        findViewById(R.id.disable_ad_btn).setVisibility(mIsPremium ? View.GONE : View.VISIBLE);
              findViewById(R.id.adView).setVisibility(mIsPremium ? View.GONE : View.VISIBLE);


    }

    // Enables or disables the "please wait" screen.
    void setWaitScreen(boolean set) {
      //  findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
      //  findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
    }

    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }



}
