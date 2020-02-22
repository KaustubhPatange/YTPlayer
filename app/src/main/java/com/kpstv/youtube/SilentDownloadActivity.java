package com.kpstv.youtube;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.kpstv.youtube.models.MetaModel;
import com.kpstv.youtube.models.YTConfig;
import com.kpstv.youtube.services.IntentDownloadService;
import com.kpstv.youtube.utils.SpotifyTrack;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

import java.util.concurrent.ThreadLocalRandom;

import static android.view.View.VISIBLE;
import static com.kpstv.youtube.AppSettings.showAds;

public class SilentDownloadActivity extends AppCompatActivity {

    private RadioButton mRadioM4a;
    private RadioButton mRadio1080p;
    private RadioButton mRadio720p;
    private RadioButton mRadio480p;
    private MetaModel meta; private AlertDialog alertDialog;
    private String url; private boolean adComplete=false,interstitialLoad=false;
    private LinearLayout mLoadlayout;
    private LinearLayout mMainlayout;
    private static final String TAG = "SilentDownloadActivity";
    private AdView mAdview; InterstitialAd ad;
    private LinearLayout mAdviewlayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_dialog);
        initViews();
        setTitle("");

        YTutils.commonBilling(this);

        Log.e(TAG, "onCreate: Let's check Action: " + getIntent().getAction() + ", data: " + getIntent().getData());

        /*if (MainActivity.activity==null) {
            MainActivity.activity = this;
        }*/

        if (showAds) {
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("07153BA64BB64F7C3F726B71C4AE30B9").build();
            mAdview.loadAd(adRequest);
            mAdview.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    adComplete=true;
                    mAdview.setVisibility(VISIBLE);
                    super.onAdLoaded();
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    adComplete=true;
                    mAdview.setVisibility(View.GONE);
                    Log.e(TAG, "onAdFailedToLoad: Failed to load Ad"+i );
                    // Show built-in ads...
                   try {
                       int number = ThreadLocalRandom.current().nextInt(1, 4);
                       View view=null;
                       switch (number) {
                           case 1:
                               view = findViewById(R.id.adViewLayout_add);
                               view.setVisibility(VISIBLE);
                               break;
                           case 2:
                               view = findViewById(R.id.adViewLayout_add1);
                               view.setVisibility(VISIBLE);
                               break;
                           case 3:
                               view = findViewById(R.id.adViewLayout_add2);
                               view.setVisibility(VISIBLE);
                               break;
                       }
                       ((TextView)view.findViewById(R.id.textView)).setTextSize(12);
                   }catch (Exception ignored){}
                    super.onAdFailedToLoad(i);
                }
            });

            ad = new InterstitialAd(this);
            ad.setAdUnitId("ca-app-pub-1164424526503510/4801416648");
            ad.loadAd(new AdRequest.Builder().addTestDevice("07153BA64BB64F7C3F726B71C4AE30B9").build());
            ad.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    interstitialLoad=true;
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    interstitialLoad = true;
                }
            });

        }else {
            interstitialLoad=true;
            mAdview.setVisibility(View.GONE);
            adComplete=true;
        }

        if (getIntent().getData() != null) {
            url = getIntent().getData().toString();
            commonIntentLaunch();
        } else if (Intent.ACTION_SEND.equals(getIntent().getAction())
                && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {
            url = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            commonIntentLaunch();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void commonIntentLaunch() {
        if (url.contains("/playlist/")||url.contains("/album/")||url.contains("/playlist?")) {
            View v = getLayoutInflater().inflate(R.layout.alert_not_playlist,null);

            alertDialog = new AlertDialog.Builder(this)
                    .setView(v)
                    .setCancelable(false)
                    .setPositiveButton("OK",(dialogInterface, i) -> finish())
                    .create();

            alertDialog.show();
            return;
        }
        url = parseLinkfromText(url);
        Log.e(TAG, "onCreate: Action: " + getIntent().getAction() + ", Url: " + url);

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                if (url.contains("youtube.com") || url.contains("youtu.be")) {
                    YTMeta ytMeta = new YTMeta(SilentDownloadActivity.this,YTutils.getVideoID(url));
                    meta = ytMeta.getVideMeta();
                } else if (url.contains("open.spotify.com") && url.contains("/track/")) {
                    SpotifyTrack track = new SpotifyTrack(YTutils.getSpotifyID(url));
                    if (track.getYtUrl() == null) return null;
                    meta = new MetaModel(YTutils.getVideoID(track.getYtUrl()), track.getTitle(), track.getAuthor(), track.getImageUrl());
                } else if (url.contains("soundcloud.com")) {
                    meta = new MetaModel(url, "auto-generate", "auto-generate", "auto-generate");
                    mRadio480p.setEnabled(false);
                    mRadio720p.setEnabled(false);
                    mRadio1080p.setEnabled(false);
                    mRadioM4a.setEnabled(false);
                }
                do {
                    Log.e(TAG, "doInBackground: Waiting for ADComplete: "+adComplete );
                }while (!adComplete);
                do {
                    Log.e(TAG, "doInBackground: Waiting for InterstitialAd: "+interstitialLoad );
                }while (!interstitialLoad);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (meta == null) {
                    Toast.makeText(SilentDownloadActivity.this, "Error: Failed to extract this link!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                if (!showAds) {
                    mAdviewlayout.setVisibility(View.GONE);
                }
                mLoadlayout.setVisibility(View.GONE);
                mMainlayout.setVisibility(VISIBLE);
                super.onPostExecute(aVoid);
            }
        }.execute();
    }

    public String parseLinkfromText(String text) {
        if (text.contains(" ")) {
            String[] items = text.split("\\s");
            for (String item : items) {
                if (item.startsWith("http"))
                    return item;
            }
        }
        return text;
    }

    public void cancelClick(View view) {
        finish();
    }

    public void okClick(View view) {
        String ext = "mp3";
        if (mRadioM4a.isChecked()) ext = "m4a";
        else if (mRadio1080p.isChecked()) ext = "1080p";
        else if (mRadio720p.isChecked()) ext = "720p";
        else if (mRadio480p.isChecked()) ext = "480p";

        if (meta == null) {
            Toast.makeText(this, "Error: Parsing video!", Toast.LENGTH_SHORT).show();
            finish();
        }

        String title = meta.getTitle();
        String author = meta.getAuthor();

        if (title.contains("-") && !title.contains("auto-generate")) {
            title = meta.getTitle().split("-")[1];
            author = meta.getTitle().split("-")[0];
        }

        YTConfig config = new YTConfig("auto-generate", "auto-generate", ext
                , title, author, true, meta.getImgUrl());
        config.setVideoID(meta.getVideoID());

        Log.e(TAG, "okClick: Download Name: " + YTutils.getTargetName(config));

        config.setTargetName(YTutils.getTargetName(config));
        config.setTaskExtra("autoTask");

        if (showAds&&ad.isLoaded())
            ad.show();

        Intent serviceIntent = new Intent(this, IntentDownloadService.class);
        serviceIntent.putExtra("addJob", config);
        ContextCompat.startForegroundService(this, serviceIntent);
        finish();
    }

    private void initViews() {
        mRadioM4a = findViewById(R.id.radio_m4a);
        mRadio1080p = findViewById(R.id.radio_1080p);
        mRadio720p = findViewById(R.id.radio_720p);
        mRadio480p = findViewById(R.id.radio_480p);
        mLoadlayout = findViewById(R.id.loadLayout);
        mMainlayout = findViewById(R.id.mainLayout);
        mAdview = findViewById(R.id.adView);
        mAdviewlayout = findViewById(R.id.adViewLayout);
    }
}
