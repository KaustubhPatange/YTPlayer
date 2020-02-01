package com.kpstv.youtube;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.kpstv.youtube.fragments.DownloadBottomSheet;
import com.kpstv.youtube.models.MetaModel;
import com.kpstv.youtube.models.YTConfig;
import com.kpstv.youtube.services.IntentDownloadService;
import com.kpstv.youtube.utils.SoundCloud;
import com.kpstv.youtube.utils.SpotifyTrack;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

public class SilentDownloadActivity extends AppCompatActivity {

    private RadioButton mRadioM4a;
    private RadioButton mRadio1080p;
    private RadioButton mRadio720p;
    private RadioButton mRadio480p;
    private MetaModel meta;
    private String url;
    private LinearLayout mLoadlayout;
    private LinearLayout mMainlayout;
    private static final String TAG = "SilentDownloadActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_dialog);
        initViews();
        setTitle("");

        Log.e(TAG, "onCreate: Let's check Action: "+getIntent().getAction()+", data: "+getIntent().getData() );


        if (getIntent().getData() != null) {
            url = getIntent().getData().toString();
            commonIntentLaunch();
        }else if (Intent.ACTION_SEND.equals(getIntent().getAction())
                && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {
            url = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            commonIntentLaunch();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void commonIntentLaunch() {
        url = parseLinkfromText(url);
        Log.e(TAG, "onCreate: Action: "+getIntent().getAction()+", Url: "+url );

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                if (url.contains("youtube.com") || url.contains("youtu.be")) {
                    YTMeta ytMeta = new YTMeta(YTutils.getVideoID(url));
                    meta = ytMeta.getVideMeta();
                } else if (url.contains("open.spotify.com")&&url.contains("/track/")) {
                    SpotifyTrack track = new SpotifyTrack(YTutils.getSpotifyID(url));
                    if (track.getYtUrl()==null) return null;
                    meta = new MetaModel(YTutils.getVideoID(track.getYtUrl()),track.getTitle(),track.getAuthor(),track.getImageUrl());
                }else if (url.contains("soundcloud.com")) {
                    meta = new MetaModel(url,"auto-generate","auto-generate","auto-generate");
                    mRadio480p.setEnabled(false);
                    mRadio720p.setEnabled(false);
                    mRadio1080p.setEnabled(false);
                    mRadioM4a.setEnabled(false);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (meta==null) {
                    Toast.makeText(SilentDownloadActivity.this, "Error: Failed to extract this link!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                mLoadlayout.setVisibility(View.GONE);
                mMainlayout.setVisibility(View.VISIBLE);
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
                , title,author, true, meta.getImgUrl());
        config.setVideoID(meta.getVideoID());

        Log.e(TAG, "okClick: Download Name: "+YTutils.getTargetName(config) );

        config.setTargetName(YTutils.getTargetName(config));
        config.setTaskExtra("autoTask");

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
    }
}
