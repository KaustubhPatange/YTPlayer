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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coremedia.iso.boxes.Container;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.gw.swipeback.SwipeBackLayout;
import com.jgabrielfreitas.core.BlurImageView;
import com.kpstv.youtube.models.YTConfig;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.Mp4Cutter;
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
import org.w3c.dom.Text;

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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import at.huber.youtubeExtractor.Format;
import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;

public class PlayerActivity extends AppCompatActivity {

    String YouTubeUrl;
    BlurImageView backImage;
    NotificationManagerCompat notificationManagerCompat;
    NotificationManager notificationManager;
    NotificationChannel notificationChannel;
    RemoteViews collpaseView, expandedView;
    ToolTipManager toolTipManager;

    static AsyncTask<String, String, Void> datasync;

    String[] apikeys = new String[]{"AIzaSyBYunDr6xBmBAgyQx7IW2qc770aoYBidLw", "AIzaSyBH8szUCt1ctKQabVeQuvWgowaKxHVjn8E"};

    LinearLayout downloadButton;
    static ConstraintLayout mainlayout;

    static TextView mainTitle, viewCount, currentDuration, totalDuration, warningText;
    int likeCounts, dislikeCounts; boolean isLoop = false;

    ImageView mainImageView; boolean isgoing = false;
    public static boolean isplaying = false, isfirst = true;

    ProgressBar mprogressBar, progressBar; String audioLink;
    FloatingActionButton previousFab, playFab, nextFab;

    static IndicatorSeekBar indicatorSeekBar;
    private InterstitialAd mInterstitialAd;

    Notification notification;
    NotificationCompat.Builder builder;

    ConnectionQuality connectionQuality = ConnectionQuality.MODERATE;

    static Handler mHandler = new Handler();

    AsyncTask<String, String, String> mergeTask, cutTask;

    SharedPreferences preferences;

    long total_duration = 0;
    int total_seconds;
    ArrayList<String> yturls;
    int ytIndex = 0; static String sendActivity,csvString,intentTitle;

    ArrayList<YTConfig> ytConfigs;

    static ExoPlayer player;
    static MediaSource mediaSource;
    DefaultDataSourceFactory dataSourceFactory;
    DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
    TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);

    private static final CookieManager DEFAULT_COOKIE_MANAGER;

    static {
        DEFAULT_COOKIE_MANAGER = new CookieManager();
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        Log.e("DisplayMatrix",height+"");

        // Using this fuzzy logic to support screen sizes since there is no flexibility using
        // default method, can't work for custom resolutions!

        setContentView(R.layout.activity_player);

        /*if (height>2770) {
            setContentView(R.layout.activity_player_2880);
        }else if (height>1920&&height<2000) {
            setContentView(R.layout.activity_player_1920);
        } else if (height>2000&&height<2770) {
            setContentView(R.layout.activity_player_2000);
        }else
            setContentView(R.layout.activity_player);*/

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView tms = findViewById(R.id.termsText);

        preferences = getSharedPreferences("settings", MODE_PRIVATE);
        toolTipManager = new ToolTipManager(PlayerActivity.this);
        yturls = new ArrayList<>();

        setTitle("");

        setNotification();

        getAllViews();

        if (CookieHandler.getDefault() != DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER);
        }
        dataSourceFactory = new DefaultDataSourceFactory(PlayerActivity.this,
                Util.getUserAgent(PlayerActivity.this,
                        getResources().getString(R.string.app_name)), BANDWIDTH_METER);
        player = ExoPlayerFactory.newSimpleInstance(PlayerActivity.this, trackSelector);
        ytConfigs = new ArrayList<>();

        playFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePlayBack(!isplaying);
            }
        });
        nextFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        });
        previousFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrevious();
            }
        });

        tms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YTutils.StartURL("https://kaustubhpatange.github.io/YTPlayer", PlayerActivity.this);
            }
        });

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

                long progresstoSeek = YTutils.progressToTimer(seekBar.getProgress(), total_duration);
                Log.e("ProgresstoSeek", progresstoSeek + "");
                player.seekTo(progresstoSeek);

                updateProgressBar();
            }
        });

        mainImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                YTutils.Vibrate(PlayerActivity.this);
                callFinish();
                return true;
            }
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

        if (!CheckIntent(getIntent())) {
            // Get the links loaded using schemes
            Intent appLinkIntent = getIntent();
            sendActivity = appLinkIntent.getStringExtra("sendActivity");
            csvString = appLinkIntent.getStringExtra("data_csv");
            intentTitle = appLinkIntent.getStringExtra("title");
            Uri appLinkData = appLinkIntent.getData();
            if (appLinkData != null) {
                String url_link = appLinkData.toString();
                yturls.add(url_link);
            } else {
                Intent intent = getIntent();
                ytIndex = intent.getIntExtra("playfromIndex", 0);
                yturls = YTutils.convertArrayToArrayList(intent.getStringArrayExtra("youtubelink"));
            }

            if (yturls.size() > 0) {
                YouTubeUrl = yturls.get(ytIndex);
                datasync = new getData();
                datasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, YTutils.getVideoID(YouTubeUrl));
            }
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
                    Toast.makeText(PlayerActivity.this, "Permission denied!",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    void playPrevious() {
        if (ytIndex <= 0) {
            Toast.makeText(getApplicationContext(), "No previous song in playlist", Toast.LENGTH_SHORT).show();
            return;
        }
        onClear();
        YouTubeUrl = yturls.get(ytIndex-1);
        ytIndex--;
        datasync = new getData();
        datasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, YTutils.getVideoID(YouTubeUrl));
    }

    void playNext() {
        if ((ytIndex + 1) == yturls.size()) {
            if (isLoop) {
                ytIndex=-1;
            }else {
                Toast.makeText(getApplicationContext(), "No new song in playlist", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        onClear();
        YouTubeUrl = yturls.get(ytIndex+1);
        ytIndex++;
        datasync = new getData();
        datasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, YTutils.getVideoID(YouTubeUrl));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 200) {
            onClear();
            yturls = YTutils.convertArrayToArrayList(getIntent().getStringArrayExtra("youtubelink"));
            YouTubeUrl = yturls.get(0);
            datasync = new getData();
            datasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, YTutils.getVideoID(YouTubeUrl));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /* Fuzzy logic to save notification channel as bringing activity back to stack
         * is removing the channel. */
        if (notificationManagerCompat!=null && notification!=null) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    notificationManagerCompat.notify(1, notification);
                }
            },500);
        }
    }

    @Override
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

    boolean CheckIntent(Intent incoming) {
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
                YTutils.showAlert(PlayerActivity.this,"Callback Error",
                        "The requested url is not a valid YouTube url", true);
                return true;
            }
        }
        return false;
    }

    class makeData extends AsyncTask<Void,Void,Void> {

        String spotifyUrl,ytLink;
        ProgressDialog dialog;
        public makeData(String yturl) {
            this.spotifyUrl = yturl;
            dialog = new ProgressDialog(PlayerActivity.this);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            if (ytLink!=null) {
               yturls.add(ytLink);
               ytIndex=yturls.size()-1;
               YouTubeUrl=ytLink;

                datasync = new getData();
                datasync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, YTutils.getVideoID(YouTubeUrl));
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onPreExecute() {
            dialog.setCancelable(false);
            dialog.setMessage("Parsing spotify url...");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.e("Original_URL",spotifyUrl+"");
            SpotifyTrack track = new SpotifyTrack(YTutils.getSpotifyID(spotifyUrl));
            ytLink = track.getYtUrl();
            Log.e("GOTURL_Here",ytLink+"");
            return null;
        }
    }

    private void setNotification() {

        // Need to create notification channel to display notification on/above android 8.0
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("channel_01",
                    "YTPlayer",NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Playing song");
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManagerCompat = NotificationManagerCompat.from(this);

        collpaseView = new RemoteViews(getPackageName(),
                R.layout.notification_layout_small);

        expandedView = new RemoteViews(getPackageName(),
                R.layout.notification_layout);

        setListener();

        builder = new NotificationCompat.Builder(this, "channel_01")
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle("YTApp")
                .setContent(collpaseView)
                .setCustomBigContentView(expandedView);

        notification = builder.build();

        notificationManagerCompat.notify(1, notification);
    }


    class getData extends AsyncTask<String, String, Void> {

        String videoTitle = "", channelTitle = "", viewCounts, imgUrl, link;

        @SuppressLint("StaticFieldLeak")
        @Override
        protected void onPostExecute(Void aVoid) {

            mainTitle.setText(videoTitle);
            collpaseView.setTextViewText(R.id.nTitle, videoTitle);
            expandedView.setTextViewText(R.id.nTitle, videoTitle);
            collpaseView.setTextViewText(R.id.nAuthor, channelTitle);
            expandedView.setTextViewText(R.id.nAuthor, channelTitle);
            viewCount.setText(viewCounts);

            Glide.with(PlayerActivity.this)
                    .asBitmap()
                    .load(imgUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            backImage.setImageBitmap(resource);
                            backImage.setBlur(5);
                            mainImageView.setImageBitmap(resource);

                            collpaseView.setImageViewBitmap(R.id.nImage, resource);
                            expandedView.setImageViewBitmap(R.id.nImage, resource);
                            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
                                notificationManager.notify(1,builder.build());
                            }
                            notificationManagerCompat.notify(1, builder.build());
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });


           /* new YoutubeStreamExtractor(new YoutubeStreamExtractor.ExtractorListner(){

                @Override
                public void onExtractionDone(List<YoutubeMedia> adativeStream, List<YoutubeMedia> muxedStream, YoutubeMeta meta) {
                    if (muxedStream.isEmpty()) {
                        showAlert("Failed!", "Couldn't get the required audio stream. Try again!", true);
                        return;
                    }
                    ytConfigs.clear();

                    List<YoutubeMedia> bestStream = getBestStream(adativeStream);

                    for(int i=0; i<bestStream.size();i++) addVideoToList(bestStream.get(i),videoTitle);

                    if (videoTitle.isEmpty() && meta.getTitle() != null) {
                        videoTitle = meta.getTitle();
                        channelTitle = meta.getAuthor();
                        mainTitle.setText(videoTitle);
                        collpaseView.setTextViewText(R.id.nTitle, videoTitle);
                        expandedView.setTextViewText(R.id.nTitle, videoTitle);
                        collpaseView.setTextViewText(R.id.nAuthor, channelTitle);
                        expandedView.setTextViewText(R.id.nAuthor, channelTitle);
                    }

                    continueinMainThread(audioLink);
                }

                @Override
                public void onExtractionGoesWrong(final ExtractorException e) {
                    showAlert("Failed!", "Couldn't get the required audio stream. Try again!", true);
                }
            }).Extract(YTutils.getVideoID(YouTubeUrl));*/

            new YouTubeExtractor(PlayerActivity.this) {

                @Override
                protected void onPostExecute(SparseArray<YtFile> ytFiles) {

                    if (ytFiles == null) {
                        parseVideoNewMethod(YouTubeUrl,videoTitle);
                        return;
                    }

                        YtFile ytaudioFile = getBestStream(ytFiles);
                        if (ytaudioFile.getUrl() == null) {
                            parseVideoNewMethod(YouTubeUrl, videoTitle);
                            return;
                        } else {
                            link = ytaudioFile.getUrl();
                            link = link.replace("\\", "");

                            Log.e("PlayerActivity", "videoTitle: " + videoTitle + ", channelTitle: " + channelTitle);

                            Log.e("PlayerActivity", "Stream: " + link);
                            ytConfigs.clear();
                            for (int i = 0, itag; i < ytFiles.size(); i++) {
                                itag = ytFiles.keyAt(i);
                                YtFile ytFile = ytFiles.get(itag);

                                if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                                    addFormatToList(videoTitle, ytFile);
                                }
                            }

                            if (audioLink == null) audioLink = link;

                            continueinMainThread(link);
                        }

                    super.onPostExecute(ytFiles);
                }

                @Override
                protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                    if (videoTitle.isEmpty() && videoMeta.getTitle() != null) {
                        videoTitle = videoMeta.getTitle();
                        channelTitle = videoMeta.getAuthor();
                        mainTitle.setText(videoTitle);
                        collpaseView.setTextViewText(R.id.nTitle, videoTitle);
                        expandedView.setTextViewText(R.id.nTitle, videoTitle);
                        collpaseView.setTextViewText(R.id.nAuthor, channelTitle);
                        expandedView.setTextViewText(R.id.nAuthor, channelTitle);
                    }
                }
            }.execute(YouTubeUrl);
            super.onPostExecute(aVoid);
        }


        String jsonResponse(String videoID, int apinumber) {
            HttpHandler httpHandler = new HttpHandler();
            String link = "https://www.googleapis.com/youtube/v3/videos?id=" + videoID + "&key=" + apikeys[apinumber] + "&part=statistics";
            return httpHandler.makeServiceCall(link);
        }

        @Override
        protected Void doInBackground(String... arg0) {
            String videoID = arg0[0];
            String json = jsonResponse(videoID, 0);

            YTMeta ytMeta = new YTMeta(videoID);
            if (ytMeta.getVideMeta() != null) {
                videoTitle = ytMeta.getVideMeta().getTitle();
                channelTitle = ytMeta.getVideMeta().getAuthor();
                imgUrl = ytMeta.getVideMeta().getImgUrl();
            }

            Log.e("ImageUrl", imgUrl + "");

            if (json != null && json.contains("\"error\":")) {
                json = jsonResponse(videoID, 1);
                if (json.contains("\"error\":")) {
                    YTStatistics ytStatistics = new YTStatistics(videoID);
                    viewCounts = ytStatistics.getViewCount();
                    likeCounts = Integer.parseInt(ytStatistics.getLikeCount());
                    dislikeCounts = Integer.parseInt(ytStatistics.getDislikeCount());
                    json = null;
                }
            }
            if (json != null) {
                try {
                    JSONObject statistics = new JSONObject(json).getJSONArray("items")
                            .getJSONObject(0).getJSONObject("statistics");
                    viewCounts = YTutils.getViewCount(Long.parseLong(statistics.getString("viewCount")));
                    likeCounts = 100;
                    dislikeCounts = 0;
                    try {
                        likeCounts = Integer.parseInt(statistics.getString("likeCount"));
                        dislikeCounts = Integer.parseInt(statistics.getString("dislikeCount"));
                    }catch (Exception e){e.printStackTrace();}

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("PlayerActivity_JSON", e.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            mainlayout.setVisibility(View.GONE);
            playFab.setEnabled(false);
            mprogressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }
    }

    void continueinMainThread(String link) {
        playFab.setEnabled(true);

        player.stop();
        player.release();
        mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(link));
        player = ExoPlayerFactory.newSimpleInstance(PlayerActivity.this, trackSelector);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);

        makePause();
        isplaying = true;

        player.addListener(new Player.EventListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {
                    case ExoPlayer.STATE_BUFFERING:
                        playFab.setVisibility(View.INVISIBLE);
                        break;
                    case ExoPlayer.STATE_ENDED:
                        makePlay();
                        isplaying = false;
                        playNext();
                        break;
                    case ExoPlayer.STATE_READY:
                        mprogressBar.setVisibility(View.GONE);
                        mainlayout.setVisibility(View.VISIBLE);
                        playFab.setVisibility(View.VISIBLE);
                        total_duration = player.getDuration();
                        total_seconds = (int) total_duration / 1000;
                        totalDuration.setText(YTutils.milliSecondsToTimer(total_duration));
                        updateProgressBar();
                        break;
                }
            }
        });


        if (yturls.size() > 1) {
            warningText.setText(Html.fromHtml("Saving video offline is illegal  &#8226;  " + (ytIndex + 1) + "/" + yturls.size()));
        }

        if (!preferences.getBoolean("showHome",false)) {
            ToolTip toolTip = new ToolTip()
                    .withText("Long press the image to minimize the player.")
                    .withColor(getResources().getColor(R.color.colorAccent)) //or whatever you want
                    .withAnimationType(ToolTip.AnimationType.FROM_MASTER_VIEW)
                    .withShadow();
            toolTipManager.showToolTip(toolTip,R.id.mainImage);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("showHome",true);
            editor.apply();
        }

        // Store video into history
        new saveToHistory().execute(YouTubeUrl);
    }

    private class saveToHistory extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(String... strings) {
            String url_link = strings[0];
            SharedPreferences pref = getSharedPreferences("history", MODE_PRIVATE);
            String set = pref.getString("urls", "");

            // Get playlist
            ArrayList<String> urls = new ArrayList<>();
            if (!Objects.requireNonNull(set).isEmpty()) {
                urls.addAll(Arrays.asList(set.split(",")));
            }

            // Add to playlist by removing it first
            for (int i = 0; i < urls.size(); i++) {
                if (urls.get(i).contains(Objects.requireNonNull(YTutils.getVideoID(url_link)))) {
                    urls.remove(i);
                }
            }
            String formattedDate = YTutils.getTodayDate();
            int percent = 100;
            try {
               percent = likeCounts*100/(likeCounts+dislikeCounts);
            }catch (Exception e){e.printStackTrace();}
            Log.e("StringtoAdd", url_link + "|" + formattedDate+"|"+percent);
            urls.add(0, url_link + "|" + formattedDate+"|"+percent);

            // Save playlist
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < urls.size(); i++) {
                sb.append(urls.get(i)).append(",");
            }
            SharedPreferences.Editor prefsEditor = pref.edit();
            prefsEditor.putString("urls", sb.toString());
            prefsEditor.apply();
            return null;
        }
    }

    private void addVideoToList(final YoutubeMedia media, final String videoTitle) {

        String ytText;
        if (media.getResSize()!=null) {
            ytText = "Video "+media.getResolution();
            if (media.isVideoOnly()) {
                ytText+=" (no audio)";
            }
            Log.e("VideoUrlFound",media.getUrl()+"");
        }else {
            ytText = "Audio "+YTutils.getAvgBitRate(Integer.parseInt(media.getBitrate()))+" kbit/s";
            if (media.getCodec().contains("mp4a")){
                audioLink = media.getUrl();
                media.setExtension("m4a");
                Log.e("AudioSpecialLink",media.getUrl()+"");
            }else {
                Log.e("AudioURLFOUND",media.getUrl()+"");
            }
        }
      /*  Format ytFrVideo = ytfile.getFormat();

        String ytText;
        if (ytFrVideo.getHeight() == -1)
            ytText = "Audio " + ytFrVideo.getAudioBitrate() + " kbit/s";
        else {
            ytText = (ytFrVideo.getFps() == 60) ? "Video " + ytFrVideo.getHeight() + "p60" :
                    "Video " + ytFrVideo.getHeight() + "p";
            if (ytfile.getFormat().getAudioBitrate() == -1) {
                ytText += " (no audio)";
            }
        }
        if (ytText.contains("128 kbit/s"))
            audioLink = ytfile.getUrl();*/
        ytConfigs.add(new YTConfig(ytText, media.getUrl(), media.getExtension(), videoTitle));
    }

    private void parseVideoNewMethod(String yturl, String videoTitle) {
        isgoing = true;
        new YoutubeStreamExtractor(new YoutubeStreamExtractor.ExtractorListner(){

            @Override
            public void onExtractionDone(List<YoutubeMedia> adativeStream, List<YoutubeMedia> muxedStream, YoutubeMeta meta) {
                if (muxedStream.isEmpty()) {
                    showAlert("Failed!", "Couldn't get the required audio stream. Try again!", true);
                    return;
                }

                Log.e("Method2","Extracted using new method");

                ytConfigs.clear();

                List<YoutubeMedia> bestStream = getBestStream(adativeStream);

                for(int i=0; i<bestStream.size();i++) addVideoToList(bestStream.get(i),videoTitle);

                continueinMainThread(audioLink);
            }

            @Override
            public void onExtractionGoesWrong(final ExtractorException e) {
                showAlert("Failed!", "Couldn't get the required audio stream. Try again!", true);
            }
        }).Extract(YTutils.getVideoID(yturl));
    }

    private List<YoutubeMedia> getBestStream(List<YoutubeMedia> streams) {
        List<YoutubeMedia> medias = new ArrayList<>();
        for(int i=0; i<streams.size();i++) {
            YoutubeMedia media = streams.get(i);
            if (!media.isAudioOnly()) {
                int j=0;
                while (j<streams.size()) {
                    YoutubeMedia media1 = streams.get(j);
                    if (media.getResolution().equals(media1.getResolution())) {
                        int m1 = Integer.parseInt(media.getBitrate());
                        int m2 = Integer.parseInt(media1.getBitrate());
                        if (m2>m1) {
                            media=media1;
                        }
                    }
                    j++;
                }
                if (!medias.contains(media)) medias.add(media);
            }else {
                medias.add(media);
            }
        }
        return medias;
    }

    private YtFile getBestStream(SparseArray<YtFile> ytFiles) {
        connectionQuality = ConnectionClassManager.getInstance().getCurrentBandwidthQuality();
        int[] itags = new int[]{251, 141, 140, 17};

        if (connectionQuality != null && connectionQuality != ConnectionQuality.UNKNOWN) {
            switch (connectionQuality) {
                case POOR:
                    itags = new int[]{17, 140, 251, 141};
                    break;
                case MODERATE:
                    itags = new int[]{251, 141, 140, 17};
                    break;
                case GOOD:
                case EXCELLENT:
                    itags = new int[]{141, 251, 140, 17};
                    break;
            }
        }

        if (ytFiles.get(itags[0]) != null) {
            return ytFiles.get(itags[0]);
        } else if (ytFiles.get(itags[1]) != null) {
            return ytFiles.get(itags[1]);
        } else if (ytFiles.get(itags[2]) != null) {
            return ytFiles.get(itags[2]);
        }
        return ytFiles.get(itags[3]);
    }

    private void setListener() {
        // Play or Pause listener
        Intent newintent = new Intent(PlayerActivity.this, PlayerActivity.class);
        newintent.putExtra("DO", "play");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, newintent, 0);

        expandedView.setOnClickPendingIntent(R.id.nPlay, pendingIntent);
        collpaseView.setOnClickPendingIntent(R.id.nPlay, pendingIntent);

        // Next song Listener
        newintent = new Intent(PlayerActivity.this, PlayerActivity.class);
        newintent.putExtra("DO", "next");
        pendingIntent = PendingIntent.getActivity(this, 1, newintent, 0);

        expandedView.setOnClickPendingIntent(R.id.nForward, pendingIntent);
        collpaseView.setOnClickPendingIntent(R.id.nForward, pendingIntent);

        // Previous song Listener
        newintent = new Intent(PlayerActivity.this, PlayerActivity.class);
        newintent.putExtra("DO", "previous");
        pendingIntent = PendingIntent.getActivity(this, 2, newintent, 0);

        expandedView.setOnClickPendingIntent(R.id.nPrevious, pendingIntent);
        collpaseView.setOnClickPendingIntent(R.id.nPrevious, pendingIntent);

        // Add to playlist Listener
        newintent = new Intent(PlayerActivity.this, PlayerActivity.class);
        newintent.putExtra("DO", "add");
        pendingIntent = PendingIntent.getActivity(this, 3, newintent, 0);

        expandedView.setOnClickPendingIntent(R.id.nAdd, pendingIntent);

        // Focus on Click Listener
        newintent = new Intent(PlayerActivity.this, PlayerActivity.class);
        newintent.putExtra("DO", "focus");
        pendingIntent = PendingIntent.getActivity(this, 4, newintent, 0);

        expandedView.setOnClickPendingIntent(R.id.mainlayout, pendingIntent);
        collpaseView.setOnClickPendingIntent(R.id.mainlayout, pendingIntent);
    }

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

    boolean isSameActivity() {
        ActivityManager m = (ActivityManager) getSystemService(ACTIVITY_SERVICE );
        List<ActivityManager.RunningTaskInfo> runningTaskInfoList =  m.getRunningTasks(1);
        ActivityManager.RunningTaskInfo runningTaskInfo = runningTaskInfoList.get(0);
        String topActivity = runningTaskInfo.topActivity.getShortClassName();
        String baseActivity = runningTaskInfo.baseActivity.getShortClassName();
        Log.e("TopActivity",topActivity+"");
        Log.e("BaseActivity",baseActivity+"");
        if (topActivity.equals(baseActivity)){
            return true;
        }else return false;
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel("channel_01");
        }
        notificationManagerCompat.cancel(1);
        if (datasync.getStatus() == AsyncTask.Status.RUNNING)
            datasync.cancel(true);
        player.stop();
        player.release();

        mHandler.removeCallbacks(mUpdateTimeTask);
        super.onDestroy();
    }

    void onClear() {
        mainlayout.setVisibility(View.GONE);
        player.stop();
        player.release();
        mHandler.removeCallbacks(mUpdateTimeTask);
        isplaying = false;
        total_duration = 0;
        total_seconds = 0;
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
            YTutils.addToPlayList(this, YouTubeUrl, total_duration / 1000);
        } else if (itemId == R.id.action_loop) {
            isLoop = !isLoop;
            item.setChecked(isLoop);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.e("OnSupportFinished","called");
        callFinish();
        return false;
    }

    public void changePlayBack(boolean isplay) {
        Log.e("PlayingState", "Playing State: " + isplaying + ", isPlay:" + isplay);
        if (isplay) {

            makePause();
            player.setPlayWhenReady(true);
            //   updateDuration();
        } else {
            makePlay();
            player.setPlayWhenReady(false);
            // mTimer.cancel();
        }
        Log.e("CurrentDur", player.getCurrentPosition() + "");
        isplaying = isplay;
    }

    void makePlay() {
        playFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
        collpaseView.setImageViewResource(R.id.nPlay, R.drawable.ic_play_notify);
        expandedView.setImageViewResource(R.id.nPlay, R.drawable.ic_play_notify);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            notificationManager.notify(1,builder.build());
        }
        notificationManagerCompat.notify(1, builder.build());
    }

    void makePause() {
        playFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
        collpaseView.setImageViewResource(R.id.nPlay, R.drawable.ic_pause_notify);
        expandedView.setImageViewResource(R.id.nPlay, R.drawable.ic_pause_notify);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            notificationManager.notify(1,builder.build());
        }
        notificationManagerCompat.notify(1, builder.build());
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

    void showAlert(String title, String message, boolean isalert) {
        int icon = android.R.drawable.ic_dialog_info;
        if (isalert) icon = android.R.drawable.ic_dialog_alert;
        new AlertDialog.Builder(PlayerActivity.this)
                .setTitle(title)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> callFinish())
                .setIcon(icon)
                .show();

    }

    private void addFormatToList(final String videoTitle, final YtFile ytfile) {
        Format ytFrVideo = ytfile.getFormat();

        String ytText;
        if (ytFrVideo.getHeight() == -1)
            ytText = "Audio " + ytFrVideo.getAudioBitrate() + " kbit/s";
        else {
            ytText = (ytFrVideo.getFps() == 60) ? "Video " + ytFrVideo.getHeight() + "p60" :
                    "Video " + ytFrVideo.getHeight() + "p";
            if (ytfile.getFormat().getAudioBitrate() == -1) {
                ytText += " (no audio)";
            }
        }
        if (ytText.contains("128 kbit/s"))
            audioLink = ytfile.getUrl();
        ytConfigs.add(new YTConfig(ytText, ytfile.getUrl(), ytfile.getFormat().getExt(), videoTitle));
    }

    void callFinish() {
        String toput = "true";
        if (!isplaying) toput = "false";
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("yturl",YouTubeUrl);
        i.putExtra("is_playing",toput);
        i.putExtra("b_title",mainTitle.getText().toString());
        Log.e("sendActivity",sendActivity+"");
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

        Log.e("YOUTUBEURL",YouTubeUrl);

        ArrayList<String> tmplist = new ArrayList<>();
        final ArrayList<YTConfig> configs = new ArrayList<>();

        for (int i = 0; i < ytConfigs.size(); i++) {
            String text = ytConfigs.get(i).getText();
            boolean isalreadyadded = false;
            for (int j = 0; j < tmplist.size(); j++) {
                if (tmplist.get(j).contains(text))
                    isalreadyadded = true;
            }
            if (!isalreadyadded) {
                tmplist.add(ytConfigs.get(i).getText());
                configs.add(ytConfigs.get(i));
            }
        }

        final String[] arrays = new String[configs.size()];
        for (int i = 0; i < configs.size(); i++) {
            arrays[i] = configs.get(i).getText();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity.this);
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
                final AlertDialog.Builder alert= new AlertDialog.Builder(PlayerActivity.this);
                alert.setIcon(icon);
                alert.setTitle("Merge");
                alert.setMessage("The current sample you selected does not contain audio stream.\n\nDo you want to merge the audio with it?");
                alert.setPositiveButton("Yes", (dialog1, which1) -> {
                    showAd();
                    mergeTask = new MergeAudioVideo(PlayerActivity.this,"/sdcard/Download/"+fileCurrent);
                    mergeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,audioLink,config.getUrl());
                });
                alert.setNegativeButton("No", (dialog12, which12) -> {
                    downloadFromUrl(config.getUrl(), config.getTitle(), fileCurrent);

                    Toast.makeText(PlayerActivity.this, "Download started",
                            Toast.LENGTH_SHORT).show();
                    showAd();
                });
                alert.setNeutralButton("Cancel",null);
                alert.show();
                return;
            } else if (arrays[which].contains("Audio ")) {
                int icon = android.R.drawable.ic_dialog_info;
                final AlertDialog.Builder alert= new AlertDialog.Builder(PlayerActivity.this);
                alert.setIcon(icon);
                alert.setTitle("Trim Sample");
                alert.setMessage("Do you want to download and cut sample in editor?\n\nIf so select \"Cut\" else \"Normal\" to begin usual download.");
                alert.setPositiveButton("Cut", (dialog1, which1) -> {
                    showAd();
                    cutTask = new cutTask(PlayerActivity.this,
                            "Download/"+fileCurrent);
                    cutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,config.getUrl());
                });
                alert.setNeutralButton("Cancel",null);
                alert.setNegativeButton("Normal", (dialog12, which12) -> {
                    downloadFromUrl(config.getUrl(), config.getTitle(), fileCurrent);
                    Toast.makeText(PlayerActivity.this, "Download started",
                            Toast.LENGTH_SHORT).show();
                    showAd();
                });
                alert.show();
                return;
            }
            downloadFromUrl(config.getUrl(), config.getTitle(), filename);

            Toast.makeText(PlayerActivity.this, "Download started",
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

    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    public static Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = player.getDuration();
            long currentDur = player.getCurrentPosition();

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
            AlertDialog.Builder alert = new AlertDialog.Builder(PlayerActivity.this);
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
                File root = android.os.Environment.getExternalStorageDirectory();

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
            AlertDialog.Builder alert = new AlertDialog.Builder(PlayerActivity.this);
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
                File root = android.os.Environment.getExternalStorageDirectory();

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
            Toast.makeText(PlayerActivity.this, "Saved at "+target, Toast.LENGTH_LONG).show();
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
