package com.kpstv.youtube;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.kpstv.youtube.models.MetaModel;
import com.kpstv.youtube.models.YTConfig;
import com.kpstv.youtube.models.spotify.Track;
import com.kpstv.youtube.models.spotify.Tracks;
import com.kpstv.youtube.services.IntentDownloadService;
import com.kpstv.youtube.utils.SpotifyApi;
import com.kpstv.youtube.utils.SpotifyTrack;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTSearch;
import com.kpstv.youtube.utils.YTutils;
import com.naveed.ytextractor.model.YTMedia;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

import static android.view.View.VISIBLE;
import static com.kpstv.youtube.AppSettings.showAds;

public class SilentDownloadActivity extends AppCompatActivity {

    private RadioButton mRadioM4a;
    private RadioButton mRadio1080p;
    private RadioButton mRadio720p;
    private RadioButton mRadio480p;
    private MetaModel meta;
    private AlertDialog alertDialog;
    private String url;
    private boolean adComplete = false, interstitialLoad = false;
    private LinearLayout mLoadlayout;
    private LinearLayout mMainlayout;
    private String link;
    private static final String TAG = "SilentDownloadActivity";
    private AdView mAdview;
    InterstitialAd ad;
    private LinearLayout mAdviewlayout;
    private int REQUEST_CODE = 100;
    private SpotifyApi spotifyApi;
    private String queryString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_dialog);
        initViews();
        setTitle("");

        YTutils.commonBilling(this);

        Log.e(TAG, "onCreate: Let's check Action: " + getIntent().getAction() + ", data: " + getIntent().getData());

        if (showAds) {
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("07153BA64BB64F7C3F726B71C4AE30B9").build();
            mAdview.loadAd(adRequest);
            mAdview.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    adComplete = true;
                    mAdview.setVisibility(VISIBLE);
                    super.onAdLoaded();
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    adComplete = true;
                    mAdview.setVisibility(View.GONE);
                    Log.e(TAG, "onAdFailedToLoad: Failed to load Ad" + i);
                    // Show built-in ads...
                    try {
                        int number = ThreadLocalRandom.current().nextInt(1, 4);
                        View view = null;
                        switch (number) {
                            case 1:
                                view = findViewById(R.id.adViewLayout_add);
                                view.setVisibility(VISIBLE);
                                link = "https://androdevkit.github.io";
                                break;
                            case 2:
                                view = findViewById(R.id.adViewLayout_add1);
                                view.setVisibility(VISIBLE);
                                link = "https://github.com/KaustubhPatange/Kling";
                                break;
                            case 3:
                                view = findViewById(R.id.adViewLayout_add2);
                                view.setVisibility(VISIBLE);
                                link = "https://kaustubhpatange.github.io/Iso2Usb";
                                break;
                        }
                        ((TextView) view.findViewById(R.id.textView)).setTextSize(12);
                        view.findViewById(R.id.ad_banner_click).setOnClickListener(view1 -> {
                            YTutils.StartURL(link, SilentDownloadActivity.this);
                        });
                    } catch (Exception ignored) {
                    }
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
                    interstitialLoad = true;
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    interstitialLoad = true;
                }
            });

        } else {
            interstitialLoad = true;
            mAdview.setVisibility(View.GONE);
            adComplete = true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            } else executeSequential();
        } else executeSequential();

    }

    private void executeSequential() {
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
        if (url.contains("/playlist/") || url.contains("/album/") || url.contains("/playlist?")) {
            View v = getLayoutInflater().inflate(R.layout.alert_not_playlist, null);

            alertDialog = new AlertDialog.Builder(this)
                    .setView(v)
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialogInterface, i) -> finish())
                    .create();

            alertDialog.show();
            return;
        }
        url = parseLinkfromText(url);
        Log.e(TAG, "onCreate: Action: " + getIntent().getAction() + ", Url: " + url);

        if (url.contains("open.spotify.com") && url.contains("/track/")) {
            spotifyApi = new SpotifyApi(SilentDownloadActivity.this);

            String SpotifyId = YTutils.getSpotifyID(url);
            if (SpotifyId != null) {
                spotifyApi.getTrackDetail(SpotifyId, new SpotifyApi.ResponseAction<Track>() {
                    @Override
                    public void onComplete(Track track) {
                        Tracks tracks = track.getTracks().get(0);
                        String query = tracks.getName();
                        if (tracks.getArtists() != null && tracks.getArtists().size() > 0)
                            query = tracks.getName() +" by " + tracks.getArtists().get(0).getName();
                        queryString = query;
                        new WorkTask().execute();
                    }

                    @Override
                    public void onError(@NotNull Exception e) {
                        Toast.makeText(SilentDownloadActivity.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else
                Toast.makeText(SilentDownloadActivity.this, "Error: Couldn't parse this spotify url", Toast.LENGTH_SHORT).show();
        }else
            new WorkTask().execute();

        // Execute
    }

    private class WorkTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (url.contains("youtube.com") || url.contains("youtu.be")) {
                YTMeta ytMeta = new YTMeta(SilentDownloadActivity.this, YTutils.getVideoID(url));
                meta = ytMeta.getVideMeta();
            } else if (url.contains("open.spotify.com") && url.contains("/track/")) {
                YTSearch ytSearch = new YTSearch(queryString);
                YTMeta ytMeta = new YTMeta(SilentDownloadActivity.this, ytSearch.getVideoIDs().get(0));
                meta = ytMeta.getVideMeta();
            } else if (url.contains("soundcloud.com")) {
                meta = new MetaModel(url, "auto-generate", "auto-generate", "auto-generate");
                mRadio480p.setEnabled(false);
                mRadio720p.setEnabled(false);
                mRadio1080p.setEnabled(false);
                mRadioM4a.setEnabled(false);
            }
            do {
                Log.e(TAG, "doInBackground: Waiting for ADComplete: " + adComplete);
            } while (!adComplete);
            do {
                Log.e(TAG, "doInBackground: Waiting for InterstitialAd: " + interstitialLoad);
            } while (!interstitialLoad);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        spotifyApi.processResponse(requestCode, resultCode, data, new SpotifyApi.ResponseAction<SpotifyApi.AuthResponse>() {
            @Override
            public void onComplete(SpotifyApi.AuthResponse authResponse) {

            }

            @Override
            public void onError(@NotNull Exception e) {
                Toast.makeText(SilentDownloadActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        super.onActivityResult(requestCode, resultCode, data);
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

        if (showAds && ad.isLoaded())
            ad.show();

        Intent serviceIntent = new Intent(this, IntentDownloadService.class);
        serviceIntent.putExtra("addJob", config);
        ContextCompat.startForegroundService(this, serviceIntent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                executeSequential();
            } else {
                Toast.makeText(this, "You need to accept WRITE_STORAGE Permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
