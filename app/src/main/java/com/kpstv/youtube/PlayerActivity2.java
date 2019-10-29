package com.kpstv.youtube;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coremedia.iso.boxes.Container;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.jgabrielfreitas.core.BlurImageView;
import com.kpstv.youtube.models.YTConfig;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.SpotifyTrack;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTStatistics;
import com.kpstv.youtube.utils.YTutils;
import com.kpstv.youtube.ytextractor.ExtractorException;
import com.kpstv.youtube.ytextractor.YoutubeStreamExtractor;
import com.kpstv.youtube.ytextractor.model.YoutubeMedia;
import com.kpstv.youtube.ytextractor.model.YoutubeMeta;
import com.spyhunter99.supertooltips.ToolTip;
import com.spyhunter99.supertooltips.ToolTipManager;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.tools.jsc.Main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import at.huber.youtubeExtractor.Format;
import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class PlayerActivity2 extends AppCompatActivity {

    static String YouTubeUrl;
    static BlurImageView backImage;
    ToolTipManager toolTipManager;

    String[] apikeys = new String[]{"AIzaSyBYunDr6xBmBAgyQx7IW2qc770aoYBidLw", "AIzaSyBH8szUCt1ctKQabVeQuvWgowaKxHVjn8E"};

    LinearLayout downloadButton;
    static ConstraintLayout mainlayout;

    static TextView mainTitle, viewCount, currentDuration, totalDuration, warningText;
    int likeCounts, dislikeCounts; static Activity activity;

    static ImageView mainImageView;

    static ProgressBar mprogressBar, progressBar; String audioLink;
    static FloatingActionButton previousFab, playFab, nextFab;

    static IndicatorSeekBar indicatorSeekBar;
    private InterstitialAd mInterstitialAd;

     static Handler mHandler = new Handler();

    AsyncTask<String, String, String> mergeTask, cutTask;

    SharedPreferences preferences;

    AsyncTask<Void,Void,Void> setData;


/*    static ExoPlayer player;
    static MediaSource mediaSource;
    DefaultDataSourceFactory dataSourceFactory;
    DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
    TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        Log.e("DisplayMatrix",height+"");

        activity = this;

        setContentView(R.layout.activity_player);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView tms = findViewById(R.id.termsText);

        preferences = getSharedPreferences("settings", MODE_PRIVATE);
        toolTipManager = new ToolTipManager(PlayerActivity2.this);

        setTitle("");

        getAllViews();

        playFab.setOnClickListener(v -> changePlayBack(!MainActivity.isplaying));
        nextFab.setOnClickListener(v -> {
            MainActivity.playNext();
            if (setData!=null && setData.getStatus() == AsyncTask.Status.RUNNING)
                setData.cancel(true);
            setData = new loadData();
            setData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        });
        previousFab.setOnClickListener(v -> {
            MainActivity.playPrevious();
            if (setData!=null && setData.getStatus() == AsyncTask.Status.RUNNING)
                setData.cancel(true);
            setData = new loadData();
            setData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        });

        tms.setOnClickListener(v -> YTutils.StartURL("https://kaustubhpatange.github.io/YTPlayer", PlayerActivity2.this));

        indicatorSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);

                long progresstoSeek = YTutils.progressToTimer(seekBar.getProgress(), MainActivity.total_duration);
                Log.e("ProgresstoSeek", progresstoSeek + "");
                MainActivity.player.seekTo(progresstoSeek);

                updateProgressBar();
            }
        });

        loadAgain();

        mainImageView.setOnLongClickListener(v -> {
            YTutils.Vibrate(PlayerActivity2.this);
            callFinish();
            return true;
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            100);
                    return;
                } else showListDialog();
            }
        });
    }

    public static void loadAgain() {
        mainlayout.setVisibility(View.VISIBLE);
        mprogressBar.setVisibility(View.GONE);
        mainTitle.setText(MainActivity.videoTitle);
        viewCount.setText(MainActivity.viewCounts);
        backImage.setImageBitmap(MainActivity.bitmapIcon);
        backImage.setBlur(5);
        mainImageView.setImageBitmap(MainActivity.bitmapIcon);
        totalDuration.setText(YTutils.milliSecondsToTimer(MainActivity.total_duration));
        detectPlayback();
        YouTubeUrl = YTutils.getYtUrl(MainActivity.videoID);
        updateProgressBar();
    }

    class loadData extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            Glide.with(activity)
                    .asBitmap()
                    .load(MainActivity.imgUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            MainActivity.bitmapIcon = resource;
                            loadAgain();
                            MainActivity.rebuildNotification();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
            loadAgain();
            super.onPostExecute(aVoid);
        }

        String jsonResponse(String videoID, int apinumber) {
            HttpHandler httpHandler = new HttpHandler();
            String link = "https://www.googleapis.com/youtube/v3/videos?id=" + videoID + "&key=" + apikeys[apinumber] + "&part=statistics";
            return httpHandler.makeServiceCall(link);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String videoID = MainActivity.videoID;
            String json = jsonResponse(videoID, 0);

            YTMeta ytMeta = new YTMeta(videoID);
            if (ytMeta.getVideMeta() != null) {
                MainActivity.videoTitle = ytMeta.getVideMeta().getTitle();
                MainActivity.channelTitle = ytMeta.getVideMeta().getAuthor();
                MainActivity.imgUrl = ytMeta.getVideMeta().getImgUrl();
            }

            if (json != null && json.contains("\"error\":")) {
                json = jsonResponse(videoID, 1);
                if (json.contains("\"error\":")) {
                    YTStatistics ytStatistics = new YTStatistics(videoID);
                    MainActivity.viewCounts = ytStatistics.getViewCount();
                    MainActivity.likeCounts = Integer.parseInt(ytStatistics.getLikeCount());
                    MainActivity.dislikeCounts = Integer.parseInt(ytStatistics.getDislikeCount());
                    json = null;
                }
            }
            if (json != null) {
                try {
                    JSONObject statistics = new JSONObject(json).getJSONArray("items")
                            .getJSONObject(0).getJSONObject("statistics");
                    MainActivity.viewCounts = YTutils.getViewCount(Long.parseLong(statistics.getString("viewCount")));
                    MainActivity.likeCounts = 100;
                    MainActivity.dislikeCounts = 0;
                    try {
                        MainActivity.likeCounts = Integer.parseInt(statistics.getString("likeCount"));
                        MainActivity.dislikeCounts = Integer.parseInt(statistics.getString("dislikeCount"));
                    }catch (Exception e){e.printStackTrace();}

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("PlayerActivity_JSON", e.getMessage());
                }
            }
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showListDialog();
                } else {
                    Toast.makeText(PlayerActivity2.this, "Permission denied!",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

  /*  @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 200) {
            onClear();
            yturls = YTutils.convertArrayToArrayList(getIntent().getStringArrayExtra("youtubelink"));
            YouTubeUrl = yturls.get(0);
            datasync = new getData();
            datasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, YTutils.getVideoID(YouTubeUrl));
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        loadAgain();
    }


   /* @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String NewIntent = intent.getStringExtra("isNewIntent");
        if (NewIntent==null) NewIntent="false";
        sendActivity = intent.getStringExtra("sendActivity");
        csvString = intent.getStringExtra("data_csv");
        intentTitle = intent.getStringExtra("title");
        String changePlayBack = intent.getStringExtra("changePlayback");
        Log.e("PlayBackState",changePlayBack+"");
        if (changePlayBack!=null && changePlayBack.equals("true")) {
            changePlayBack(false);
        }else if (changePlayBack!=null && changePlayBack.equals("false"))
            changePlayBack(true);

        if (intent.getData()!=null) {
            Log.e("Firing","intent.getData()");
            if(yturls.size()>1) {
                // Insert to playlist and play
                yturls.add(ytIndex,intent.getData().toString());
                ytIndex=yturls.size()-1;
                YouTubeUrl = intent.getData().toString();
            }else {
                YouTubeUrl = intent.getData().toString();
            }
            datasync = new getData();
            datasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, YTutils.getVideoID(YouTubeUrl));
            return;
        }

        String[] arr = intent.getStringArrayExtra("youtubelink");
        if (arr!=null && !NewIntent.equals("true")) {
            Log.e("Firing","arr!=null");
            ytIndex = intent.getIntExtra("playfromIndex", 0);
            yturls = YTutils.convertArrayToArrayList(arr);
            YouTubeUrl = yturls.get(ytIndex);
            datasync = new getData();
            datasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, YTutils.getVideoID(YouTubeUrl));
            return;
        }
        CheckIntent(intent);
        String action = intent.getStringExtra("DO");
        Log.e("PRINTING_RESULT", "Code: " + action);
        if (action == null) return;
        switch (action) {
            case "play":
                changePlayBack(!isplaying);
                break;
            case "next":
                playNext();
                break;
            case "previous":
                playPrevious();
                break;
            case "add":
                YTutils.addToPlayList(this, YouTubeUrl, total_duration / 1000);
                break;
            case "focus":
                Log.e("FocusWindow","true");
                this.getCurrentFocus();
                break;
        }
    }
*/
  /*  boolean CheckIntent(Intent incoming) {
        if (Intent.ACTION_SEND.equals(incoming.getAction())
                && incoming.getType() != null && "text/plain".equals(incoming.getType())) {
            Log.e("Firing","checkIntent");
            String ytLink = incoming.getStringExtra(Intent.EXTRA_TEXT);
            Log.e("IntentYTLink",ytLink+"");
            if (YTutils.isValidID(ytLink)){
                yturls = new ArrayList<>();
                yturls.add(ytLink);
                ytIndex=0;
                YouTubeUrl = ytLink;
                datasync = new getData();
                datasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, YTutils.getVideoID(YouTubeUrl));
                return true;
            }else if (ytLink.contains("open.spotify.com")&&ytLink.contains("/track/")) {
                new makeData(ytLink).execute();
                return true;
            }else {
                YTutils.showAlert(PlayerActivity2.this,"Callback Error",
                        "The requested url is not a valid YouTube url", true);
                return true;
            }
        }
        return false;
    }*/




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*if (isSameActivity()) {
            startActivity(new Intent(this, MainActivity.class));
        }else
        {
            super.onBackPressed();
        }*/
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mUpdateTimeTask);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.player_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            callFinish();
            return true;
        } else if (itemId == R.id.action_youtube) {
            YTutils.StartURLIntent(YouTubeUrl, this);
        } else if (itemId == R.id.action_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, YouTubeUrl);
            startActivity(Intent.createChooser(shareIntent, "Share using..."));
        } else if (itemId == R.id.action_add) {
            YTutils.addToPlayList(this, YouTubeUrl, MainActivity.total_duration / 1000);
        } else if (itemId == R.id.action_loop) {
            MainActivity.isLoop = !MainActivity.isLoop;
            item.setChecked(MainActivity.isLoop);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.e("OnSupportFinished","called");
        callFinish();
        return false;
    }

    public static void detectPlayback() {
        if (MainActivity.isplaying)
            makePause();
        else makePlay();
    }

    public void changePlayBack(boolean isplay) {
        MainActivity.changePlayBack(isplay);
        Log.e("PlayingState", "Playing State: " + MainActivity.isplaying + ", isPlay:" + isplay);
        if (isplay) {
            makePause();
            MainActivity.player.setPlayWhenReady(true);
        } else {
            makePlay();
            MainActivity.player.setPlayWhenReady(false);
        }
        Log.e("CurrentDur", MainActivity.player.getCurrentPosition() + "");
        MainActivity.isplaying = isplay;
    }

    public static void makePlay() {
        playFab.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_play));
    }

    public static void makePause() {
        playFab.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_pause));
    }

    private void getAllViews() {
        progressBar = findViewById(R.id.progress_circular);
        warningText = findViewById(R.id.warningText);
        downloadButton = findViewById(R.id.downloadlayout);
        mprogressBar = findViewById(R.id.mainprogress);
        mainTitle = findViewById(R.id.maintitle);
        viewCount = findViewById(R.id.mainviews);
        currentDuration = findViewById(R.id.currentDur);
        totalDuration = findViewById(R.id.totalDur);
        mainImageView = findViewById(R.id.mainImage);
        previousFab = findViewById(R.id.rewindButton);
        playFab = findViewById(R.id.play_pause_button);
        nextFab = findViewById(R.id.forwardButton);
        indicatorSeekBar = findViewById(R.id.seekBar);
        mainlayout = findViewById(R.id.mainlayout);
        backImage = findViewById(R.id.background_image);
    }

    void callFinish() {
        String toput = "true";
        if (!MainActivity.isplaying) toput = "false";
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("yturl",YouTubeUrl);
        i.putExtra("is_playing",toput);
        i.putExtra("b_title",mainTitle.getText().toString());
       // Log.e("sendActivity",sendActivity+"");
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

    void showAd() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Log.e("TAG", "The interstitial wasn't loaded yet.");
        }
    }

    void LoadAd() {
        //TODO: Change ad unit ID, Sample ca-app-pub-3940256099942544/1033173712, Use ca-app-pub-1763645001743174/8453566324
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-1763645001743174/8453566324");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

    }

    void showListDialog() {

        LoadAd();

   //     Log.e("YOUTUBEURL",YouTubeUrl);

        ArrayList<String> tmplist = new ArrayList<>();
        final ArrayList<YTConfig> configs = new ArrayList<>();

        for (int i = 0; i < MainActivity.ytConfigs.size(); i++) {
            String text = MainActivity.ytConfigs.get(i).getText();
            boolean isalreadyadded = false;
            for (int j = 0; j < tmplist.size(); j++) {
                if (tmplist.get(j).contains(text))
                    isalreadyadded = true;
            }
            if (!isalreadyadded) {
                tmplist.add(MainActivity.ytConfigs.get(i).getText());
                configs.add(MainActivity.ytConfigs.get(i));
            }
        }

        final String[] arrays = new String[configs.size()];
        for (int i = 0; i < configs.size(); i++) {
            arrays[i] = configs.get(i).getText();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity2.this);
        builder.setTitle("Select Media Codec");

        builder.setItems(arrays, (dialog, which) -> {
            YTConfig config = configs.get(which);
            String filename;
            if (config.getText().length() > 55) {
                filename = config.getTitle().substring(0, 55) + "." + config.getExt();
            } else {
                filename = config.getTitle() + "." + config.getExt();
            }
            filename = filename.replaceAll("[\\\\><\"|*?%:#/]", "");
            final String fileCurrent = filename; // Using this since current filename cannot be placed as final
            if (arrays[which].contains("(no audio)")) {
                int icon = android.R.drawable.ic_dialog_info;
                final AlertDialog.Builder alert= new AlertDialog.Builder(PlayerActivity2.this);
                alert.setIcon(icon);
                alert.setTitle("Merge");
                alert.setMessage("The current sample you selected does not contain audio stream.\n\nDo you want to merge the audio with it?");
                alert.setPositiveButton("Yes", (dialog1, which1) -> {
                    showAd();
                    mergeTask = new MergeAudioVideo(PlayerActivity2.this,"/sdcard/Download/"+fileCurrent);
                    mergeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,audioLink,config.getUrl());
                });
                alert.setNegativeButton("No", (dialog12, which12) -> {
                    downloadFromUrl(config.getUrl(), config.getTitle(), fileCurrent);

                    Toast.makeText(PlayerActivity2.this, "Download started",
                            Toast.LENGTH_SHORT).show();
                    showAd();
                });
                alert.setNeutralButton("Cancel",null);
                alert.show();
                return;
            } else if (arrays[which].contains("Audio ")) {
                int icon = android.R.drawable.ic_dialog_info;
                final AlertDialog.Builder alert= new AlertDialog.Builder(PlayerActivity2.this);
                alert.setIcon(icon);
                alert.setTitle("Trim Sample");
                alert.setMessage("Do you want to download and cut sample in editor?\n\nIf so select \"Cut\" else \"Normal\" to begin usual download.");
                alert.setPositiveButton("Cut", (dialog1, which1) -> {
                    showAd();
                    cutTask = new cutTask(PlayerActivity2.this,
                            "Download/"+fileCurrent);
                    cutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,config.getUrl());
                });
                alert.setNeutralButton("Cancel",null);
                alert.setNegativeButton("Normal", (dialog12, which12) -> {
                    downloadFromUrl(config.getUrl(), config.getTitle(), fileCurrent);
                    Toast.makeText(PlayerActivity2.this, "Download started",
                            Toast.LENGTH_SHORT).show();
                    showAd();
                });
                alert.show();
                return;
            }
            downloadFromUrl(config.getUrl(), config.getTitle(), filename);

            Toast.makeText(PlayerActivity2.this, "Download started",
                    Toast.LENGTH_SHORT).show();
            showAd();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void downloadFromUrl(String youtubeDlUrl, String downloadTitle, String fileName) {
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    public static void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    public static Runnable mUpdateTimeTask = new Runnable() {
        public void run() {

            long totalDuration = MainActivity.player.getDuration();
            long currentDur = MainActivity.player.getCurrentPosition();

            // Displaying time completed playing
            currentDuration.setText("" + YTutils.milliSecondsToTimer(currentDur));

            // Updating progress bar
            int progress = (YTutils.getProgressPercentage(currentDur, totalDuration));
            //Log.d("Progress", ""+progress);
            indicatorSeekBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    class cutTask extends AsyncTask<String, String, String> {

        AlertDialog alertdialog;
        View dialogView;
        TextView tview;
        ProgressBar bar;
        Context con;
        String target,fileName;

        public cutTask(Context context, String targetfile) {
            this.con = context;
            this.target = targetfile;
        }

        @Override
        protected void onPreExecute() {
            Log.e("ExecutingTask","true");
            LayoutInflater inflater = getLayoutInflater();
            dialogView = inflater.inflate(R.layout.alert_merger, null);
            tview = dialogView.findViewById(R.id.textView);
            bar = dialogView.findViewById(R.id.progressBar);
            AlertDialog.Builder alert = new AlertDialog.Builder(PlayerActivity2.this);
            alert.setTitle("Download");
            alert.setMessage("This could take a while depending upon length of audio!");
            alert.setCancelable(false);
            alert.setView(dialogView);
            alert.setNegativeButton("Cancel", (dialog, which) -> {
                cutTask.cancel(true);
            });
            alertdialog = alert.create();
            alertdialog.show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            tview.setText(values[1]);
            if (Integer.parseInt(values[0])==-1) {
                bar.setIndeterminate(true);
                return;
            }
            bar.setIndeterminate(false);
            bar.setProgress(Integer.parseInt(values[0]));
        }

        @Override
        protected void onPostExecute(String s) {
         //   Toast.makeText(PlayerActivity.this, "Saved at /sdcard/"+target, Toast.LENGTH_LONG).show();
            Log.e("FileName",fileName);
            alertdialog.dismiss();
            startEditor("file:/"+YTutils.getFile("YTPlayer/"+fileName).toString());
        }

        private void startEditor(String filePathUri) {
            Intent intent = new Intent(con, RingdroidEditActivity.class);
            intent.putExtra("FILE_PATH", filePathUri);
            startActivity(intent);
        }

        @Override
        protected String doInBackground(String... sUrl) {
            try {
                fileName = YTutils.getFile(target).getName();

                String audioUrl = sUrl[0];

                // Download audio file first...
                URL url = new URL(audioUrl);
                URLConnection connection = url.openConnection();
                connection.connect();

                long fileLength = connection.getContentLength();
                File root = Environment.getExternalStorageDirectory();

                DataInputStream input = new DataInputStream(url.openStream());
                DataOutputStream output = new DataOutputStream(new FileOutputStream(
                        root.getAbsolutePath() + "/YTPlayer/"+fileName));

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(((int) (total * 100 / fileLength)) + "",
                            "Downloading Audio... 1/2");
                    output.write(data, 0, count);
                    output.flush();
                }
                output.flush();
                output.close();
                input.close();

                // Trimming audio

                /*publishProgress((-1) + "", "Trimming media... 2/2");
                Mp4Cutter mp4Cutter = new Mp4Cutter();
                mp4Cutter.startTrim(
                        YTutils.getFile("YTPlayer/audio.download"),
                        YTutils.getFile(target),
                        currentDuration, totalDuration

                );*/
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class MergeAudioVideo extends AsyncTask<String, String, String> {

        AlertDialog alertdialog;
        View dialogView;
        TextView tview;
        ProgressBar bar;
        Context con;
        String target;

        public MergeAudioVideo(Context context, String targetfile) {
            this.con = context;
            this.target = targetfile;
        }

        @Override
        protected void onPreExecute() {
            Log.e("ExecutingTask","true");
            LayoutInflater inflater = getLayoutInflater();
            dialogView = inflater.inflate(R.layout.alert_merger, null);
            tview = dialogView.findViewById(R.id.textView);
            bar = dialogView.findViewById(R.id.progressBar);
            AlertDialog.Builder alert = new AlertDialog.Builder(PlayerActivity2.this);
            alert.setTitle("Merging");
            alert.setMessage("This could take a while depending upon length of video!");
            alert.setCancelable(false);
            alert.setView(dialogView);
            alert.setNegativeButton("Cancel", (dialog, which) -> {
                mergeTask.cancel(true);
            });
            alertdialog = alert.create();
            alertdialog.show();
        }

        @Override
        protected String doInBackground(String... sUrl) {
            try {
                String audioUrl = sUrl[0];
                String videoUrl = sUrl[1];

                // Download audio file first...
                URL url = new URL(audioUrl);
                URLConnection connection = url.openConnection();
                connection.connect();

                long fileLength = connection.getContentLength();
                File root = Environment.getExternalStorageDirectory();

                DataInputStream input = new DataInputStream(url.openStream());
                DataOutputStream output = new DataOutputStream(new FileOutputStream(
                        root.getAbsolutePath() + "/YTPlayer/audio.download"));


                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(((int) (total * 100 / fileLength)) + "",
                            "Downloading Audio... 1/3");
                    output.write(data, 0, count);
                    output.flush();
                }
                output.flush();
                output.close();
                input.close();

                // Download video file second...
                url = new URL(videoUrl);
                connection = url.openConnection();
                connection.connect();

                fileLength = connection.getContentLength();
                input = new DataInputStream(url.openStream());
                output = new DataOutputStream(new FileOutputStream(
                        root.getAbsolutePath() + "/YTPlayer/video.download"));

                total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(((int) (total * 100 / fileLength)) + "", "Downloading Video... 2/3");
                    output.write(data, 0, count);
                    output.flush();
                }
                output.flush();
                output.close();
                input.close();

                // Merging audio and video third
                publishProgress((-1) + "", "Merging media... 3/3");
                mux("/sdcard/YTPlayer/video.download","/sdcard/YTPlayer/audio.download",
                        target);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            tview.setText(values[1]);
            if (Integer.parseInt(values[0])==-1) {
                bar.setIndeterminate(true);
                return;
            }
            bar.setIndeterminate(false);
            bar.setProgress(Integer.parseInt(values[0]));
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(PlayerActivity2.this, "Saved at "+target, Toast.LENGTH_LONG).show();
            alertdialog.dismiss();
        }

        public boolean mux(String videoFile, String audioFile, String outputFile) {
            Movie video;
            try {
                video = new MovieCreator().build(videoFile);
            } catch (RuntimeException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            Movie audio;
            try {

                audio = new MovieCreator().build(audioFile);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (NullPointerException e) {
                e.printStackTrace();
                return false;
            }

            Track audioTrack = audio.getTracks().get(0);
            video.addTrack(audioTrack);
            Container out = new DefaultMp4Builder().build(video);
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(outputFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            BufferedWritableFileByteChannel byteBufferByteChannel = new BufferedWritableFileByteChannel(fos);
            try {
                out.writeContainer(byteBufferByteChannel);
                byteBufferByteChannel.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }


    }

    class BufferedWritableFileByteChannel implements WritableByteChannel {
        //    private static final int BUFFER_CAPACITY = 1000000;
        private static final int BUFFER_CAPACITY = 10000000;

        private boolean isOpen = true;
        private final OutputStream outputStream;
        private final ByteBuffer byteBuffer;
        private final byte[] rawBuffer = new byte[BUFFER_CAPACITY];

        private void dumpToFile() {
            try {
                outputStream.write(rawBuffer, 0, byteBuffer.position());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private BufferedWritableFileByteChannel(OutputStream outputStream) {
            this.outputStream = outputStream;
            this.byteBuffer = ByteBuffer.wrap(rawBuffer);
        }

        @Override
        public int write(ByteBuffer inputBuffer) {
            int inputBytes = inputBuffer.remaining();

            if (inputBytes > byteBuffer.remaining()) {
                dumpToFile();
                byteBuffer.clear();

                if (inputBytes > byteBuffer.remaining()) {
                    throw new BufferOverflowException();
                }
            }

            byteBuffer.put(inputBuffer);

            return inputBytes;
        }

        @Override
        public boolean isOpen() {
            return isOpen;
        }

        @Override
        public void close() throws IOException {
            dumpToFile();
            isOpen = false;
        }
    }
}
