package com.kpstv.youtube;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.audio.AudioFocusManager;
import com.google.android.exoplayer2.audio.AudioListener;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.kpstv.youtube.fragments.DiscoverFragment;
import com.kpstv.youtube.fragments.HistoryBottomSheet;
import com.kpstv.youtube.fragments.HistoryFragment;
import com.kpstv.youtube.fragments.LibraryFragment;
import com.kpstv.youtube.fragments.LocalMusicFragment;
import com.kpstv.youtube.fragments.LocalSearchFragment;
import com.kpstv.youtube.fragments.NCFragment;
import com.kpstv.youtube.fragments.OPlaylistFragment;
import com.kpstv.youtube.fragments.PlaylistFragment;
import com.kpstv.youtube.fragments.SFragment;
import com.kpstv.youtube.fragments.SearchFragment;
import com.kpstv.youtube.fragments.SleepBottomSheet;
import com.kpstv.youtube.fragments.basedOnApi.PopularFragment;
import com.kpstv.youtube.models.MetaModel;
import com.kpstv.youtube.models.NPlayModel;
import com.kpstv.youtube.models.YTConfig;
import com.kpstv.youtube.receivers.SongBroadCast;
import com.kpstv.youtube.utils.APIResponse;
import com.kpstv.youtube.utils.DataUtils;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.LyricsApi;
import com.kpstv.youtube.utils.OnSwipeTouchListener;
import com.kpstv.youtube.utils.SoundCloud;
import com.kpstv.youtube.utils.SpotifyTrack;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTStatistics;
import com.kpstv.youtube.utils.YTutils;
import com.naveed.ytextractor.ExtractorException;
import com.naveed.ytextractor.YoutubeStreamExtractor;
import com.naveed.ytextractor.model.YTMedia;
import com.naveed.ytextractor.model.YoutubeMeta;
import com.spyhunter99.supertooltips.ToolTip;
import com.spyhunter99.supertooltips.ToolTipManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.security.auth.login.LoginException;

import at.huber.youtubeExtractor.Format;
import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YouTubeUriExtractor;
import at.huber.youtubeExtractor.YtFile;
import cat.ereza.customactivityoncrash.config.CaocConfig;

import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements AppInterface, SleepBottomSheet.ItemClickListener, HistoryBottomSheet.BottomSheetListener, NCFragment.NoConnectionListener{

    // https://www.googleapis.com/youtube/v3/videos?id=BDocp-VpCwY&key=AIzaSyBYunDr6xBmBAgyQx7IW2qc770aoYBidLw&part=snippet,statistics

    // API KEY= AIzaSyBMqerRAATEnrsfPnWYfeqDdqX0TbR0bEo

    /* https://www.googleapis.com/youtube/v3/search?part=snippet,id&q=tuto&type=video&key=AIzaSyBYunDr6xBmBAgyQx7IW2qc770aoYBidLw

    * Code to launch player
    *
    *           Intent intent = new Intent(MainActivity.this,PlayerActivity.class);
                intent.putExtra("youtubelink",stringarray);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
    *
    * */

    Fragment HistoryFrag;
    static Fragment SearchFrag;
    public static FragmentManager fragmentManager;
    public static Fragment PlaylistFrag, libraryFrag, FavouriteFrag,localMusicFrag, localSearchFrag,popularFrag;
    Fragment NCFrag; String ytLink;
    public static List<String> playListItems;
    static SharedPreferences preferences,settingPref;
    public static LinearLayout bottom_player, adViewLayout; static boolean isFirstLaunch=true;
    static ImageButton actionUp,actionPlay;static ProgressBar loadProgress,songProgress;
    static TextView actionTitle, actionChannelTitle; static AdView adView;
    static AsyncTask<String,String,Void> LoadVideo; public static FragmentActivity activity;
    static AsyncTask<Void,Void,Void> LoadOffline;

    public static ArrayList<NPlayModel> nPlayModels;
    public static SimpleExoPlayer player;  public static boolean supportFFmpeg=false,loadedFavFrag=false;
    public static MediaSource mediaSource; private static final String TAG = "MainActivity";
    public static DefaultDataSourceFactory dataSourceFactory;
    public static DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    public static TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
    public static TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
    public static String selectedItemText=""; public static int sleepSeconds;
    private static MediaSessionCompat mediaSession; LinearLayout swipeLayout;
    PowerManager.WakeLock wakeLock; static ToolTipManager toolTipManager;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Disabling URI exposure coz I don't want to take much efforts...
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();


      /*  if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }*/

        settingPref = getSharedPreferences("settings",MODE_PRIVATE);
        isEqualizerEnabled = settingPref.getBoolean("equalizer_enabled",false);

        dataSourceFactory = new DefaultDataSourceFactory(MainActivity.this,
                Util.getUserAgent(MainActivity.this,
                        getResources().getString(R.string.app_name)), BANDWIDTH_METER);

        player = ExoPlayerFactory.newSimpleInstance(MainActivity.this, trackSelector);

      //  AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        activity = MainActivity.this;

        createNotification();

        ytConfigs = new ArrayList<>();
        playListItems = new ArrayList<>();
        yturls = new ArrayList<>();
        nPlayModels = new ArrayList<>();

        // Remove this code afterwards...
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        Log.e("HeightMatrix",height+"");

        // Set CrashActivity...
        CaocConfig.Builder.create()
                .errorActivity(ErrorActivity.class)
                .apply();

        //TODO: Change app unit id, Sample : ca-app-pub-3940256099942544~3347511713, ca-app-pub-1763645001743174~5602018181
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        // Get required views...
        adView = findViewById(R.id.adView);
        swipeLayout = findViewById(R.id.swipeLayout);
        adViewLayout = findViewById(R.id.adViewLayout);
        bottom_player = findViewById(R.id.bottom_player);
        actionPlay = findViewById(R.id.action_play);
        actionUp = findViewById(R.id.action_maximize);
        actionTitle = findViewById(R.id.action_title);
        actionChannelTitle = findViewById(R.id.action_channelTitle);
        songProgress = findViewById(R.id.songLayoutProgress);
        loadProgress = findViewById(R.id.song_progress);

        toolTipManager = new ToolTipManager(activity);


        // Check onComing links from YouTube or Spotify...
        CheckIntent(getIntent());

        String history = YTutils.readContent(this,"History");
        if (history !=null && !history.isEmpty()) {
            Log.e("historyContents",history+"");

            SharedPreferences preferences = getSharedPreferences("history",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("urls",history.replace("&#10;","").trim());
            editor.apply();
            new File(getFilesDir().toString()+"/History").delete();
        }

      //  mediaSession = new MediaSessionCompat(this,"MediaSessionCompat");

      /*  ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), SongBroadCast.class);
        mediaSession = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);
        mediaSession.setCallback(mMediaSessionCallback);
        mediaSession.setFlags( MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS );
        mediaSession.setActive(true);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, SongBroadCast.class);
        PendingIntent pendingIntent = PendingIntent.
                getBroadcast(this, 0, mediaButtonIntent, 0);
        mediaSession.setMediaButtonReceiver(pendingIntent);*/

        ComponentName mediaButtonReceiverComponentName = new ComponentName(
                getApplicationContext(),
                SongBroadCast.class);
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mediaButtonReceiverComponentName);
        PendingIntent mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                0,
                mediaButtonIntent,
                0);
        mediaSession = new MediaSessionCompat(this,
                "MusicPlayer",
                mediaButtonReceiverComponentName,
                mediaButtonReceiverPendingIntent);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );
        mediaSession.setCallback(mMediaSessionCallback);
        mediaSession.setActive(true);
        mediaSession.setMediaButtonReceiver(mediaButtonReceiverPendingIntent);

        preferences = getSharedPreferences("history",MODE_PRIVATE);
        /*String list = preferences.getString("urls","");
        ArrayList<String> urls = new ArrayList<>();
        if (!Objects.requireNonNull(list).isEmpty()) {
            urls.addAll(Arrays.asList(list.split(",")));
        }*/

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        swipeLayout.setOnTouchListener(new OnSwipeTouchListener(this){
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                playPrevious();
            }

            @Override
            public void onSwipeTop() {
                super.onSwipeTop();
                openPlayer();
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                playNext();
            }
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        actionPlay.setOnClickListener(v -> changePlayBack(!isplaying));
       // bottom_player.setOnClickListener(v-> openPlayer());
        actionUp.setOnClickListener(v->openPlayer());

        fragmentManager = getSupportFragmentManager();
        localMusicFrag = new LocalMusicFragment();
        localSearchFrag = new LocalSearchFragment();
        HistoryFrag = new HistoryFragment();
        libraryFrag = new LibraryFragment();
        FavouriteFrag = new OPlaylistFragment();
        SearchFrag = new SearchFragment();
        PlaylistFrag = new PlaylistFragment();
        NCFrag = new NCFragment();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (YTutils.isInternetAvailable())
        {
            if (getYTUrls("blank").length>1)
                loadFragment(HistoryFrag);
            else navigation.setSelectedItemId(R.id.navigation_search);
        }
        else {
            loadFragment(NCFrag);
        }

        try {
            FFmpeg ffmpeg = FFmpeg.getInstance(this);
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onFailure() {}

                @Override
                public void onSuccess() {
                    supportFFmpeg=true;
                    Log.e(TAG, "onSuccess: FFMPEG Loaded");
                }

                @Override
                public void onFinish() {}
            });
        } catch (Exception ignored) {
            Log.e(TAG, "onCreate: FFMpeg not supported");
        }


        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "app:wakelockTag");
        wakeLock.acquire();

       /* FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
*//*
                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);*//*
                        Log.e(TAG, "Got token: "+token);
                      //  Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });*/

    }

    static void setDefaultEqualizerValues() {
       try {
           boolean isSet = settingPref.getBoolean("equalizer_default",false);
           if (!isSet) {
               SharedPreferences.Editor editor = settingPref.edit();
               int sessionId = player.getAudioComponent().getAudioSessionId();

               mEqualizer = new Equalizer(1000,sessionId);
               short numberFrequencyBands = mEqualizer.getNumberOfBands();
               final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];
               final short upperEqualizerBandLevel = mEqualizer.getBandLevelRange()[1];

               editor.putInt("bandLength",numberFrequencyBands);
               editor.putInt("lowerBand",lowerEqualizerBandLevel);
               editor.putInt("higherBand",upperEqualizerBandLevel);
               editor.putBoolean("equalizer_default",true);
               editor.apply();

               mEqualizer.release();
           }
       }catch (Exception ignored){}
    }

    private static final long MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_SEEK_TO;

    public static void updateMediaSessionPlaybackState() {
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(MEDIA_SESSION_ACTIONS)
                .setState(isplaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                        player.getCurrentPosition(), 1);
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    public static MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlay() {
            super.onPlay();
        }

        @Override
        public void onPause() {
            super.onPause();
        }

        @Override
        public void onSeekTo(long pos) {
            Log.e(TAG, "onSeekTo: Playing"+pos );
            player.seekTo(pos);
            super.onSeekTo(pos);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
        }
    };

    public static void PlayVideo_Local(String[] urls) {
        /** YTUrls here will work as path to music file...
         *
         *  Background task will load all the details about music
         *  and will set it to player and respective fields.
         */

        localPlayBack=true;
        yturls.clear();
        if (LoadOffline !=null && LoadOffline.getStatus() == AsyncTask.Status.RUNNING)
        {
            player.stop(); player.release();
            LoadOffline.cancel(true);
        }
        yturls.addAll(Arrays.asList(urls));
        ytIndex = 0;
        LoadOffline = new loadVideo_Local(urls[ytIndex]);
        LoadOffline.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void PlayVideo_Local(String[] urls,int position) {
        /** YTUrls here will work as path to music file...
         *
         *  Background task will load all the details about music
         *  and will set it to player and respective fields.
         */

        localPlayBack=true;
        yturls.clear();
        if (LoadOffline !=null && LoadOffline.getStatus() == AsyncTask.Status.RUNNING)
        {
            player.stop(); player.release();
            LoadOffline.cancel(true);
        }
        yturls.addAll(Arrays.asList(urls));
        ytIndex = position;
        LoadOffline = new loadVideo_Local(urls[ytIndex]);
        LoadOffline.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void ChangeVideoOffline(int position) {
        if (LoadOffline !=null && LoadOffline.getStatus() == AsyncTask.Status.RUNNING)
        {
            player.stop(); player.release();
            LoadOffline.cancel(true);
        }
        ytIndex = position;
        LoadOffline = new loadVideo_Local(yturls.get(ytIndex));
        LoadOffline.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void PlayVideo(String[] ytUrls) {
        localPlayBack=false;
        yturls.clear();
        if (LoadVideo !=null && LoadVideo.getStatus() == AsyncTask.Status.RUNNING)
        {
            player.stop(); player.release();
            LoadVideo.cancel(true);
        }
        yturls.addAll(Arrays.asList(ytUrls));
        videoID = YTutils.getVideoID(yturls.get(0));
        Log.e("VideoID_ToPLAY",videoID+"");
        LoadVideo = new loadVideo();
        LoadVideo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,videoID);
    }

    public static void PlayVideo(String[] ytUrls, int position) {
        localPlayBack=false;
        yturls.clear();
        if (LoadVideo !=null && LoadVideo.getStatus() == AsyncTask.Status.RUNNING)
        {
            player.stop(); player.release();
            LoadVideo.cancel(true);
        }
        yturls.addAll(Arrays.asList(ytUrls));
        ytIndex = position;
        videoID = YTutils.getVideoID(yturls.get(position));
        LoadVideo = new loadVideo();
        LoadVideo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,videoID);
    }

    public static void ChangeVideo(int position) {
        if (LoadVideo !=null && LoadVideo.getStatus() == AsyncTask.Status.RUNNING)
        {
            player.stop(); player.release();
            LoadVideo.cancel(true);
        }
        ytIndex = position;
        videoID = YTutils.getVideoID(yturls.get(position));
        LoadVideo = new loadVideo();
        LoadVideo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,videoID);
    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (getSupportFragmentManager().getBackStackEntryCount()>0) {
            getSupportFragmentManager().popBackStack();
            return;
        }

        if (fragment instanceof PopularFragment) {
            loadFragment(SearchFrag);
            return;
        }

        if (fragment instanceof SFragment) {
            loadFragment(SearchFrag);
            return;
        }
        if (fragment instanceof DiscoverFragment) {
            loadFragment(SearchFrag);
            return;
        }

        if (fragment instanceof LocalSearchFragment) {
            loadFragment(localMusicFrag);
            return;
        }

        if (fragment instanceof OPlaylistFragment && fragment.getTag()!=null && fragment.getTag().equals("localMusic")) {
            loadFragment(localMusicFrag);
            return;
        }

        if (fragment instanceof OPlaylistFragment && loadedFavFrag) {
            loadedFavFrag=false;
            loadFragment(libraryFrag);
            return;
        }

        if (fragment instanceof OPlaylistFragment) {
            loadFragment(PlaylistFrag);
            return;
        }

        if (fragment instanceof PlaylistFragment) {
            loadFragment(libraryFrag);
            return;
        }

        if (fragment instanceof LocalMusicFragment) {
            loadFragment(libraryFrag);
            return;
        }


        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back once more to exit.", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_history:
                    if (YTutils.isInternetAvailable()) {
                        loadFragment(HistoryFrag);
                    }else loadFragment(NCFrag);
                    return true;
                case R.id.navigation_search:
                    if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof SearchFragment)
                        SearchFrag.onActivityResult(100,1,null);
                    else
                        loadFragment(SearchFrag);
                    return true;
                case R.id.navigation_playlist:
                    loadFragment(libraryFrag);
   //                 loadFragment(PlaylistFrag);
                    return true;
            }

            return true;
        }
    };

    public static boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            Log.e("LoadingFragment","");

            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.setCustomAnimations(R.anim.fade_in,R.anim.fade_out);
            ft.replace(R.id.fragment_container,fragment)
                    .commit();
            return true;
        }
        return false;
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        CheckIntent(intent);
    }

    public static void loadPlayFrag() {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PlaylistFrag)
                .commit();
    }

    public static void loadLibraryFrag() {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, libraryFrag)
                .commit();
    }

    public static void loadLocalMusicFrag() {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, localMusicFrag)
                .commit();
    }

    public static void loadSearchFrag() {
       fragmentManager.beginTransaction()
               .replace(R.id.fragment_container, SearchFrag)
               .commit();
    }

    @Override
    protected void onDestroy() {
        wakeLock.release();
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.deleteNotificationChannel("channel_01");
        }
        notificationManagerCompat.cancel(1);
       try {

           if (mEqualizer!=null) mEqualizer.release();
           if (bassBoost!=null) bassBoost.release();
           if (loudnessEnhancer!=null) loudnessEnhancer.release();
           if (virtualizer!=null) virtualizer.release();

           player.stop();
           player.release();

          // PlayerActivity.mHandler.removeCallbacks(PlayerActivity.mUpdateTimeTask);
       }catch (Exception e) { e.printStackTrace(); }
        super.onDestroy();
    }

    @Override
    public void onRemoveFromHistory(int position) {
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container)
                instanceof HistoryFragment)
        HistoryFragment.removeFromHistory(position);
        if (yturls.size()>0) {
            try {
                yturls.remove(position);
            }catch (Exception e) {e.printStackTrace();}
        }if (nPlayModels.size()>0) {
           try {
               nPlayModels.remove(position);
           }catch (Exception e){ e.printStackTrace(); }
        }
    }

    @Override
    public void ReplaceFragment() {
        if (YTutils.isInternetAvailable()) {
            HistoryFrag = new HistoryFragment();
            loadFragment(HistoryFrag);
        } else Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(TextView view) {
        selectedItemText = view.getText().toString();
        switch (selectedItemText) {
            case "5 minutes":
                sleepSeconds = 5*60;
                break;
            case "10 minutes":
                sleepSeconds = 10*60;
                break;
            case "15 minutes":
                sleepSeconds = 15*60;
                break;
            case "30 minutes":
                sleepSeconds = 30*60;
                break;
            case "45 minutes":
                sleepSeconds = 45*60;
                break;
            case "60 minutes":
                sleepSeconds = 60*60;
                break;
            case "End of track":
                sleepSeconds = -2;
                break;
            case "None":
                selectedItemText="";
                sleepSeconds=0;
                break;
        }
        libraryFrag.onActivityResult(100,0,null);
    }

    void openPlayer() {
        Intent i=new Intent(MainActivity.this,PlayerActivity2.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
       /* i.putExtra("youtubelink",new String[]{ytLink});
        i.putExtra("isNewIntent","true");
        if (enablePlayback)
        i.putExtra("changePlayback",changePlayBack);*/
        startActivity(i);
        overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
    }


    @SuppressLint("StaticFieldLeak")
    boolean CheckIntent(Intent incoming) {
        if (Intent.ACTION_SEND.equals(incoming.getAction())
                && incoming.getType() != null && "text/plain".equals(incoming.getType())) {
            String ytLink = incoming.getStringExtra(Intent.EXTRA_TEXT);
            commonIntentCheck(ytLink);
        }else if (incoming.getData()!=null) {
            String url = incoming.getData().toString();
            commonIntentCheck(url);
        }
        return false;

    }

    AlertDialog alertDialog;
    @SuppressLint("StaticFieldLeak")
    void commonIntentCheck(String yt) {
        if (yt.contains("/playlist/")||yt.contains("/album/")||yt.contains("/playlist?")) {
            View v = getLayoutInflater().inflate(R.layout.alert_not_playlist,null);

            alertDialog = new AlertDialog.Builder(this)
                    .setView(v)
                    .setPositiveButton("OK",null)
                    .create();

            alertDialog.show();
            return;
        }
        ytLink = yt;
        if (YTutils.isValidID(yt) || (yt.contains("soundcloud.com"))) {
            if (yt.contains("soundcloud.com")) {
                String[] childs = yt.split("\\s");
                for (String child : childs) {
                    if (child.contains("soundcloud.com")) {
                        ytLink = child;
                        break;
                    }
                }
            } else ytLink = yt;
            if (yturls.size()<=0) {
                PlayVideo(getYTUrls(ytLink),0);
            }else {
                int insert_pos = ytIndex;
                if (localPlayBack) {
                    Log.e(TAG, "CheckIntent: Running this one" );
                    localPlayBack=false;
                    PlayVideo(getYTUrls(ytLink),0);
                    return;
                }
                if (nPlayModels.size()>0 && nPlayModels.size()==yturls.size()) {
                    if (YTutils.isValidID(ytLink)) {
                        new AsyncTask<Void, Void, Void>() {
                            YTMeta ytMeta;

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                if (ytMeta.getVideMeta() != null) {
                                    NPlayModel model = new NPlayModel(ytLink, ytMeta, false);
                                    for (NPlayModel model1 : nPlayModels) {
                                        if (model1.getUrl().equals(model.getUrl())) {
                                            nPlayModels.remove(model1);
                                            break;
                                        }
                                    }
                                    nPlayModels.add(insert_pos, model);
                                } else
                                    Toast.makeText(activity, "Unexpected parsing error occurred!", Toast.LENGTH_SHORT).show();
                                super.onPostExecute(aVoid);
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                ytMeta = new YTMeta(YTutils.getVideoID(ytLink));
                                return null;
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }else if (ytLink.contains("soundcloud.com")) {
                        new AsyncTask<Void,Void,Void>() {
                            SoundCloud.SoundCloudModel model;

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                if (model!=null) {
                                    MetaModel metaModel = new MetaModel(model.getNormalUrl(),model.getTitle(),model.getAuthorName(),
                                            model.getImageUrl());
                                    NPlayModel nPlayModel = new NPlayModel(ytLink,new YTMeta(metaModel),false);
                                    for (NPlayModel model1: nPlayModels) {
                                        if (model1.getUrl().equals(nPlayModel.getUrl())) {
                                            nPlayModels.remove(model1);
                                            break;
                                        }
                                    }
                                    nPlayModels.add(insert_pos, nPlayModel);
                                } else
                                    Toast.makeText(activity, "Unexpected parsing error occurred!", Toast.LENGTH_SHORT).show();
                                super.onPostExecute(aVoid);
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                SoundCloud soundCloud = new SoundCloud(ytLink);
                                if (soundCloud.getModel()!=null)
                                    model = soundCloud.getModel();
                                return null;
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
                String ytID = YTutils.getVideoID(ytLink);
                for (String yturl: yturls) {
                    if (YTutils.getVideoID(yturl).equals(ytID)) {
                        yturls.remove(yturl);
                        break;
                    }
                }
                yturls.add(insert_pos,ytLink);
                ChangeVideo(insert_pos);
            }
        }else if (ytLink.contains("open.spotify.com")&&ytLink.contains("/track/")) {
            new makeSpotifyData(ytLink).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } /*else if (ytLink.contains("soundcloud.com")) {
            Log.e(TAG, "commonIntentCheck: Working here..." );
          //  new soundCloudData(ytLink).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }*/else {
            YTutils.showAlert(MainActivity.this,"Callback Error",
                    "The requested url is not a valid YouTube url", true);
        }
    }



    /**
    * Implementing a new player within main activity itself...
    */

    public static String videoTitle = "", channelTitle = "", viewCounts, imgUrl, videoID,audioLink;
    public static int likeCounts,dislikeCounts;
    public static NotificationManagerCompat notificationManagerCompat;
    static NotificationManager notificationManager;
    static NotificationChannel notificationChannel;
    static PendingIntent prevPendingIntent,pausePendingIntent,nextPendingIntent,clickPendingIntent,favouritePendingIntent;
    public static Bitmap bitmapIcon; static ArrayList<YTConfig> ytConfigs;
    static NotificationCompat.Builder builder;
    public static boolean isplaying, sleepEndTrack=false,localPlayBack=false, soundCloudPlayBack,isFavourite=false,isEqualizerEnabled=false;
    public static boolean isLoop=false,isEqualizerSet=false,loadedFromData=false;
    static Handler mHandler = new Handler();
    static long total_duration = 0; public static PresetReverb presetReverb;
    public static BassBoost bassBoost; public static Virtualizer virtualizer;
    public static LoudnessEnhancer loudnessEnhancer;
    public static int total_seconds; public static int nColor;
    public static ArrayList<String> yturls;
    public static int ytIndex = 0; public static Visualizer visualizer;
    public static Equalizer mEqualizer;

    static class loadVideo_Local extends AsyncTask<Void,Void,Void> {
        String filePath;
        boolean notExist=false;

        public loadVideo_Local(String filePath) {
            this.filePath = filePath;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (notExist) {
                playNext();
                return;
            }

            if (YTutils.isInternetAvailable())
                setLyricData();

            try {
                Palette.generateAsync(bitmapIcon, palette -> {
                    nColor = palette.getVibrantColor(activity.getResources().getColor(R.color.light_white));
                    Log.e(TAG, "loadData_ OFFLINE Changing nColor: "+MainActivity.nColor
                            +", ImageUri:"+MainActivity.imgUrl);
                    continueinMainThread("isPath:"+filePath);
                });
            }catch (Exception e){
                Log.e(TAG, "onPostExecute: "+e.getMessage());
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            File f = new File(filePath);
            if (!f.exists()) {
                notExist=true;
                return null;
            }
            Uri uri = Uri.fromFile(f);
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(activity,uri);
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                byte [] data = mmr.getEmbeddedPicture();

                if(data != null)
                    bitmapIcon = BitmapFactory.decodeByteArray(data, 0, data.length);
                else
                    bitmapIcon = YTutils.drawableToBitmap(ContextCompat.getDrawable(activity,R.drawable.ic_pulse));

                if (artist==null) artist ="Unknown artist";
                if (title==null) title = YTutils.getVideoTitle(f.getName());

                if (title.contains("."))
                    title = title.split("\\.")[0];

                videoTitle = title;
                channelTitle = artist;
                likeCounts = -1; dislikeCounts = -1;
                viewCounts = "-1";

                videoID = f.getPath();

                total_seconds = Integer.parseInt(durationStr);

            }catch (Exception e) {
                notExist=true;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            commonPreExecute();
            super.onPreExecute();
        }
    }

    public static Spanned lyricText;

    static void setLyricData() {
        new LyricsApi(activity,videoTitle,channelTitle){
            @Override
            public void onLyricFound(Spanned data) {
                super.onLyricFound(data);
                lyricText = data;
                try {
                    PlayerActivity2.setLyricData(lyricText);
                }catch (Exception e){}
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    static class loadVideo extends AsyncTask<String,String,Void> {

        @SuppressLint("StaticFieldLeak")
        @Override
        protected void onPostExecute(Void aVoid) {
            if (noInternet) {
                return;
            }
            if (skipSong) {
                if (souncloudFailed)
                    Toast.makeText(activity, "Error: Parsing song "+videoTitle, Toast.LENGTH_SHORT).show();
                if (command==1)
                    playNext();
                else if (command==2)
                    playPrevious();
                return;
            }

            if (bitmapIcon!=null) {
                Palette.generateAsync(bitmapIcon, palette -> {
                    Log.e(TAG, "loadVideo: Changing nColor: "+MainActivity.nColor +
                            ", ImageUri: "+MainActivity.imgUrl);
                    nColor = palette.getVibrantColor(activity.getResources().getColor(R.color.light_white));
                });
            }

            setLyricData();

            if (soundCloudPlayBack) {
                ytConfigs.clear();
                ytConfigs.add(new YTConfig("Audio 128 kbit/s",soundCloud.getModel().getStreamUrl(),
                        ".mp3",videoTitle,channelTitle,true,soundCloud.getModel().getImageUrl()));
                audioLink = soundCloud.getModel().getStreamUrl();
                continueinMainThread(audioLink);
                return;
            }

            if (!loadedFromData)
                parseVideoNewMethod(YTutils.getYtUrl(videoID),videoTitle);
            else continueinMainThread(audioLink);
          /*  new YouTubeExtractor(activity) {

                @Override
                protected void onPostExecute(SparseArray<YtFile> ytFiles) {

                    if (ytFiles == null) {
                        parseVideoNewMethod(YTutils.getYtUrl(videoID),videoTitle);
                        return;
                    }

                    YtFile ytaudioFile = getBestStream(ytFiles);
                    if (ytaudioFile.getUrl() == null) {
                        parseVideoNewMethod(YTutils.getYtUrl(videoID), videoTitle);
                        return;
                    } else {
                        audioLink = ytaudioFile.getUrl();
                        audioLink = audioLink.replace("\\", "");

                        Log.e("PlayerActivity", "videoTitle: " + videoTitle + ", channelTitle: " + channelTitle);

                        Log.e("PlayerActivity", "Stream: " + audioLink);
                        ytConfigs.clear();
                        for (int i = 0, itag; i < ytFiles.size(); i++) {
                            itag = ytFiles.keyAt(i);
                            YtFile ytFile = ytFiles.get(itag);

                            if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                                addFormatToList(videoTitle, ytFile,channelTitle);
                            }
                        }

                        continueinMainThread(audioLink);
                    }

                    super.onPostExecute(ytFiles);
                }

                @Override
                protected void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta videoMeta) {
                    if (videoTitle.isEmpty() && videoMeta.getTitle() != null) {
                        channelTitle = videoMeta.getAuthor();
                        videoTitle = YTutils.setVideoTitle(videoMeta.getTitle());
                    }
                }
            }.execute(YTutils.getYtUrl(videoID));*/
            super.onPostExecute(aVoid);
        }

        boolean skipSong, noInternet,souncloudFailed=false;
        SoundCloud soundCloud;
        @Override
        protected Void doInBackground(String... arg0) {
            trials=3; loadedFromData=false;
            soundCloudPlayBack=false;souncloudFailed=false;
            if (!YTutils.isInternetAvailable()) {
                Toast.makeText(activity, "No active internet connection", Toast.LENGTH_SHORT).show();
                noInternet=true;
                return null;
            }

            videoID = arg0[0];
            MainActivity.playListItems = new ArrayList<>();

            if (!localPlayBack) {
                String data = YTutils.readContent(activity,"removedList.csv");
                if (data!=null && !data.isEmpty()) {
                    if (data.contains(","))
                        MainActivity.playListItems = new ArrayList<>(Arrays.asList(data.split(",")));
                    else  MainActivity.playListItems.add(data.trim());
                }
                if (playListItems.contains("ytID:"+videoID)||playListItems.contains("sd:"+videoID)) {
                    skipSong =true;
                    return null;
                }
            }

            if (videoID.contains("soundcloud.com")) {
                soundCloud = new SoundCloud(videoID);
                Log.e(TAG, "doInBackground: Likely here..." );
                videoTitle = soundCloud.getModel().getTitle();
                if (soundCloud.getModel()==null || soundCloud.getModel().getStreamUrl()==null) {
                    Log.e(TAG, "doInBackground: Skipping soundcloud" );
                    skipSong=true; souncloudFailed=true;
                    command=1;
                    return null;
                }
             //   soundCloud.captureViews();
                soundCloudPlayBack=true;
                MainActivity.videoTitle = soundCloud.getModel().getTitle();
                MainActivity.channelTitle = soundCloud.getModel().getAuthorName();
                MainActivity.imgUrl = soundCloud.getModel().getImageUrl();
                likeCounts = -1; dislikeCounts = -1;
                viewCounts = "-1";
               /* if (soundCloud.getViewCount()!=null && !soundCloud.getViewCount().isEmpty())
                    viewCounts =YTutils.getViewCount( Long.parseLong(soundCloud.getViewCount()));
                Log.e(TAG, "doInBackground: Here I am: " +soundCloud.getModel().getStreamUrl());*/
            } else {


                String link = "https://www.googleapis.com/youtube/v3/videos?id=" + videoID + "&part=statistics";
                APIResponse response = new APIResponse(link);
                String json = response.getJson();

                /*int i = 0;
                int apiLength = API_KEYS.length;
                String json;
                do {
                    json = jsonResponse(videoID, i);
                    i++;
                } while (json.contains("\"error\":") && i < apiLength);*/

                YTMeta ytMeta = new YTMeta(videoID);
                if (ytMeta.getVideMeta() != null) {
                    MainActivity.channelTitle = YTutils.getChannelTitle(ytMeta.getVideMeta().getTitle(),
                            ytMeta.getVideMeta().getAuthor());
                    MainActivity.videoTitle = YTutils.setVideoTitle(ytMeta.getVideMeta().getTitle());
                    MainActivity.imgUrl = ytMeta.getVideMeta().getImgUrl();
                }

                if (json.contains("\"error\"")) {
                    YTStatistics ytStatistics = new YTStatistics(videoID);
                    MainActivity.viewCounts = ytStatistics.getViewCount();
                    MainActivity.likeCounts = Integer.parseInt(ytStatistics.getLikeCount());
                    MainActivity.dislikeCounts = Integer.parseInt(ytStatistics.getDislikeCount());
                    json = null;
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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("PlayerActivity_JSON", e.getMessage());
                    }
                }

                DataUtils.DataModel dataModel = DataUtils.getSavedUrl(activity,videoID,videoTitle,channelTitle);
                if (dataModel!=null) {
                    loadedFromData=true;
                    audioLink = dataModel.getAudioLink();
                    ytConfigs = dataModel.getConfigs();
                }
            }
            if (imgUrl!=null) {
                Log.e(TAG, "doInBackground: Downloading Image..." );
                bitmapIcon = YTutils.getBitmapFromURL(imgUrl);
            }
            return null;
        }

        String jsonResponse(String videoID, int apinumber) {
            HttpHandler httpHandler = new HttpHandler();
            String link = "https://www.googleapis.com/youtube/v3/videos?id=" + videoID + "&key=" + API_KEYS[apinumber] + "&part=statistics";
            Log.e(TAG, "jsonResponse: Link: "+link);
            return httpHandler.makeServiceCall(link);
        }

        @Override
        protected void onPreExecute() {
            commonPreExecute();
            super.onPreExecute();
        }
    }

    static int trials=3;

    static void commonPreExecute() {
        try {
            if (!localPlayBack) {
                PlayerActivity2.progressBar.setVisibility(VISIBLE);
                PlayerActivity2.hidePlayButton();
            }
        }catch (Exception ignored){}
        if (AppSettings.showAds) {
            AdRequest adRequest = new AdRequest.Builder().addTestDevice("07153BA64BB64F7C3F726B71C4AE30B9").build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    adView.setVisibility(VISIBLE);
                    super.onAdLoaded();
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    adViewLayout.setVisibility(View.GONE);
                    Log.e(TAG, "onAdFailedToLoad: Failed to load Ad"+i );
                    super.onAdFailedToLoad(i);
                }
            });
        }else adView.setVisibility(View.GONE);
        bottom_player.setVisibility(VISIBLE);
        onClear();
    }


    private static void parseVideoNewMethod(String yturl, String videoTitle) {
        Log.e(TAG, "parseVideoNewMethod: Starting here...." );
        new YoutubeStreamExtractor(new YoutubeStreamExtractor.ExtractorListner(){
            @Override
            public void onExtractionGoesWrong(ExtractorException e) {
                if (trials>0) {
                    trials--;
                    Log.e(TAG, "onExtractionGoesWrong: Trial Count: "+trials );
                    parseVideoNewMethod(yturl,videoTitle);
                    return;
                }
                playNext();
                Toast.makeText(activity, videoTitle+": Couldn't get the required audio stream!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onExtractionDone(List<YTMedia> adativeStream, List<YTMedia> muxedStream, YoutubeMeta meta) {
                Log.e(TAG, "onExtractionDone: Parsing Audio using second method" );
                if (adativeStream.isEmpty()) {
                    if (trials>0) {
                        trials--;
                        Log.e(TAG, "onExtractionGoesWrong: Trial Count: "+trials );
                        parseVideoNewMethod(yturl,videoTitle);
                        return;
                    }
                    playNext();
                    Toast.makeText(activity, videoTitle+": Couldn't get the required audio stream!", Toast.LENGTH_SHORT).show();
                    // showAlert("Failed!", "Couldn't get the required audio stream. Try again!", true);
                    return;
                }

                Log.e("Method2","Extracted using new method");
                if (muxedStream.size()>0)
                {
                    Log.e(TAG, "onExtractionDone: Muxed exist" );
                    for (int i=0;i<muxedStream.size();i++) {
                        Log.e(TAG, "onExtractionDone: Muxed ("+i+"): "+muxedStream.get(i).getUrl() );
                    }
                }
                else Log.e(TAG, "onExtractionDone: Muxed error" );

                ytConfigs.clear();
            //    List<YTMedia> bestStream = getBestStream(adativeStream);

                Log.e(TAG, "onExtractionDone: Expires in: "+meta.getExpiresInSeconds());
                Log.e(TAG, "onExtractionDone: Media Size: " +adativeStream.size());

                for(int i=0; i<adativeStream.size();i++) addVideoToList(adativeStream.get(i),videoTitle,channelTitle);

                Log.e(TAG, "parseVideoNewMethod: Ending here...." );
                continueinMainThread(audioLink);
            }

        }).useDefaultLogin().Extract(YTutils.getVideoID(yturl));
    }

    public static void actionFavouriteClicked() {
        if (MainActivity.total_seconds==0)
        {
            Toast.makeText(activity, "Player is still processing!", Toast.LENGTH_SHORT).show();
        }else{
            try {
                if (!MainActivity.isFavourite)
                    PlayerActivity2.favouriteButton.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_favorite_full));
                else PlayerActivity2.favouriteButton.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_favorite));
            }catch (Exception ignored){}

            MainActivity.write_Favourite();
            rebuildNotification();
        }
    }

    public static void write_Favourite() {
        String t = YTutils.readContent(activity,"favourite.csv");
        if (t!=null && !t.contains(MainActivity.videoID)) {
            t += "\n"+MainActivity.videoID+"|"+MainActivity.total_seconds+"|"+MainActivity.videoTitle+"|"+MainActivity.channelTitle
                    +"|"+MainActivity.imgUrl;
            Toast.makeText(activity, "Added to favourites!", Toast.LENGTH_SHORT).show();
            MainActivity.isFavourite=true;
        }else if (t!=null && t.contains(MainActivity.videoID)) {

            String[] lines = t.split("\n|\r");
            StringBuilder builder = new StringBuilder();
            for (String line : lines) {
                if (!line.contains(MainActivity.videoID) && !line.isEmpty()) {
                    builder.append("\n").append(line);
                }
            }

            t = builder.toString().trim();

            Toast.makeText(activity, "Removed from favourites!", Toast.LENGTH_SHORT).show();
            MainActivity.isFavourite=false;
        }else {
            t =MainActivity.videoID+"|"+MainActivity.total_seconds+"|"+MainActivity.videoTitle+"|"+MainActivity.channelTitle
                    +"|"+MainActivity.imgUrl;
            Toast.makeText(activity, "Added to favourites!", Toast.LENGTH_SHORT).show();
            MainActivity.isFavourite=true;
        }
        YTutils.writeContent(activity,"favourite.csv",t.trim());
    }

    static boolean dontAllowToPlay=false;

    private static void continueinMainThread(String link) {

        Log.e(TAG, "onExtractionDone: AudioLink: "+link );

        player.stop();
        player.release();
        if (link.startsWith("isPath:"))
        {
            link = link.split(":")[1];
            File f = new File(link);
            mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.fromFile(f));
        }else
        mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(link));
        player = ExoPlayerFactory.newSimpleInstance(activity, trackSelector);
        player.prepare(mediaSource);
        player.setPlayWhenReady(true);

        makePause();
        isplaying = true;

        if (AppSettings.listenAudioChange) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.CONTENT_TYPE_MOVIE)
                    .build();

            player.setAudioAttributes(audioAttributes, true);
        }

        player.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                Log.e(TAG, "onPlayerStateChanged: playWhenReady: "+playWhenReady+", playbackState: "
                        +YTutils.getPlayBackstate(playbackState));

                /** A logic to listen to audio focus change listener */
                if (!playWhenReady && playbackState==ExoPlayer.STATE_READY && AppSettings.listenAudioChange) {
                    Log.e(TAG, "onPlayerStateChanged: Here I am..." );
                    makePlay();
                    isplaying = false;
                    rebuildNotification();
                }
                switch (playbackState) {
                    case ExoPlayer.STATE_IDLE:
                        break;
                    case ExoPlayer.STATE_BUFFERING:
                        try {
                            if (!localPlayBack) {
                                PlayerActivity2.progressBar.setVisibility(VISIBLE);
                                PlayerActivity2.hidePlayButton();
                            }
                        }catch (Exception ignored){}
                        break;
                    case ExoPlayer.STATE_ENDED:
                        makePlay();
                        isplaying = false;
                        playNext();
                       if (sleepEndTrack) {
                            Log.e(TAG, "onPlayerStateChanged: tiggered" );
                            sleepEndTrack=false;
                            dontAllowToPlay=true;
                        }
                        break;
                    case ExoPlayer.STATE_READY:

                        if(isFirstLaunch) {
                            isFirstLaunch=false;
                            setDefaultEqualizerValues();
                        }

                        actionTitle.setVisibility(VISIBLE);
                        actionChannelTitle.setVisibility(VISIBLE);
                        actionTitle.setText(videoTitle);
                        actionChannelTitle.setText(" "+ Html.fromHtml("&#8226")+" "+channelTitle);
                        loadProgress.setVisibility(View.GONE);
                        songProgress.setVisibility(VISIBLE);
                        actionPlay.setVisibility(VISIBLE);
                        actionUp.setVisibility(VISIBLE);
                        total_duration = MainActivity.player.getDuration();
                        total_seconds = (int) total_duration / 1000;
                        try {
                            if (!localPlayBack) {
                                PlayerActivity2.progressBar.setVisibility(View.GONE);
                                PlayerActivity2.showPlayButton();
                            }
                            PlayerActivity2.loadAgain();
                            PlayerActivity2.totalDuration.setText(YTutils.milliSecondsToTimer(MainActivity.total_duration));
                        }catch (Exception e) { Log.e("PlayerActivity","not loaded yet!"); }

                        try {
                            addEqualizer();
                        }catch (Exception e) {}

                        /** Setting mediaSession metadata */
                        final MediaMetadataCompat.Builder metaData = new MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, channelTitle)
                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, channelTitle)
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, videoTitle)
                                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player.getDuration())
                                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null);
                        mediaSession.setMetadata(metaData.build());

                        rebuildNotification();
                        updateProgressBar();

                        // Store video into history
                        if (!localPlayBack)
                            new saveToHistory().execute(YTutils.getYtUrl(videoID));

                        if (dontAllowToPlay)
                        {
                            dontAllowToPlay=false;
                            libraryFrag.onActivityResult(101,0,null);
                            changePlayBack(false);
                        }


                        if (!settingPref.getBoolean("showPlayerTip",false)) {
                            ToolTip toolTip = new ToolTip()
                                    .withText("Swipe player right and left to change song.")
                                    .withTextColor(activity.getResources().getColor(R.color.black))
                                    .withColor(activity.getResources().getColor(R.color.colorAccent)) //or whatever you want
                                    .withAnimationType(ToolTip.AnimationType.FROM_MASTER_VIEW)
                                    .withShadow();
                            toolTipManager.showToolTip(toolTip,bottom_player);
                            SharedPreferences.Editor editor = settingPref.edit();
                            editor.putBoolean("showPlayerTip",true);
                            editor.apply();
                        }

                        break;
                }
            }
        });
    }

    public static void setAudioFocusListener() {
       /* AudioManager mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build();

            AudioFocusRequest focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attributes)
                    .setOnAudioFocusChangeListener(i -> {
                        Log.e(TAG, "onAudioFocusChange: Focus Change OREO: "+i );
                        if (i==AudioManager.AUDIOFOCUS_LOSS) {
                            changePlayBack(false);
                        }
                    })
                    .build();

            mAudioManager.requestAudioFocus(focusRequest);

        }else {
            mAudioManager.requestAudioFocus(i -> {
                Log.e(TAG, "onAudioFocusChange: Focus Change "+i );
                if (i==AudioManager.AUDIOFOCUS_LOSS) {
                    changePlayBack(false);
                }
                },AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        }*/
    }

   /* @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mPlaybackDelayed || mResumeOnFocusGain) {
                    synchronized(mFocusLock) {
                        mPlaybackDelayed = false;
                        mResumeOnFocusGain = false;
                    }
                    playbackNow();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                synchronized(mFocusLock) {
                    mResumeOnFocusGain = false;
                    mPlaybackDelayed = false;
                }
                pausePlayback();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                synchronized(mFocusLock) {
                    mResumeOnFocusGain = true;
                    mPlaybackDelayed = false;
                }
                pausePlayback();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // ... pausing or ducking depends on your application
                break;
        }
    }*/

    public static void addEqualizer() {
        try {
            int audioSessionId = player.getAudioComponent().getAudioSessionId();
            Log.e(TAG, "onAudioSessionId: AudioSessionID: "+audioSessionId );
            if (mEqualizer!=null) mEqualizer.release();
            if (presetReverb!=null) presetReverb.release();
            if (bassBoost!=null) bassBoost.release();
            if (virtualizer!=null) virtualizer.release();
            if (loudnessEnhancer!=null) loudnessEnhancer.release();
            mEqualizer = new Equalizer(1000, audioSessionId);
            mEqualizer.setEnabled(isEqualizerEnabled);

            presetReverb = new PresetReverb(0,audioSessionId);
            presetReverb.setEnabled(AppSettings.enableEqualizer);

            bassBoost = new BassBoost(0,audioSessionId);
            bassBoost.setEnabled(AppSettings.enableEqualizer);

            virtualizer = new Virtualizer(0,audioSessionId);
            virtualizer.setEnabled(AppSettings.enableEqualizer);

            loudnessEnhancer = new LoudnessEnhancer(audioSessionId);
            loudnessEnhancer.setEnabled(AppSettings.enableEqualizer);

            if (AppSettings.enableEqualizer) {
                int default_reverb = preferences.getInt("selected_reverb", 0);
                presetReverb.setPreset((short)default_reverb);

                int default_bass = preferences.getInt("selected_bass", 0);
                bassBoost.setStrength((short)default_bass);

                int default_virtualizer = preferences.getInt("selected_virtualizer", 0);
                virtualizer.setStrength((short)default_virtualizer);

                int default_loudness = preferences.getInt("selected_loudness", 0);
                loudnessEnhancer.setTargetGain((short)default_loudness);
            }

            isEqualizerSet=true;
            int current = settingPref.getInt("position", 0);
            if (current == 0) {
                for (short seek_id = 0; seek_id < mEqualizer.getNumberOfBands(); seek_id++) {
                    int progressBar = settingPref.getInt("seek_" + seek_id, 1500);
                    short equalizerBandIndex = seek_id;
                    final short lowerEqualizerBandLevel = mEqualizer.getBandLevelRange()[0];
                    Log.i("seek_" + seek_id, ":" + progressBar);
                    if (progressBar != 1500) {
                        mEqualizer.setBandLevel(equalizerBandIndex,
                                (short) (progressBar + lowerEqualizerBandLevel));
                    } else {
                        mEqualizer.setBandLevel(equalizerBandIndex,
                                (short) (progressBar + lowerEqualizerBandLevel));
                    }
                }
            }
        }catch (Exception ignored) {}
    }

    private static void addFormatToList(final String videoTitle, final YtFile ytfile, final String channelTitle) {
        Format ytFrVideo = ytfile.getFormat();

        String ytText; boolean isaudio=false;
        if (ytFrVideo.getHeight() == -1)
        {
            isaudio = true;
            ytText = "Audio " + ytFrVideo.getAudioBitrate() + " kbit/s";
        }
        else {
            ytText = (ytFrVideo.getFps() == 60) ? "Video " + ytFrVideo.getHeight() + "p60" :
                    "Video " + ytFrVideo.getHeight() + "p";
            if (ytfile.getFormat().getAudioBitrate() == -1) {
                ytText += " (no audio)";
            }
        }
        if (ytText.contains("128 kbit/s"))
            audioLink = ytfile.getUrl();

        ytConfigs.add(new YTConfig(ytText, ytfile.getUrl(), ytfile.getFormat().getExt(), videoTitle, channelTitle,isaudio,imgUrl));
    }

    private static void addVideoToList(final YTMedia media, final String videoTitle, final String channelTitle) { ;

        String ytText=""; boolean isaudio=false;
        String ext = "m4a";
        try {

            if (media.getAudioSampleRate() != 0)
            {
                isaudio = true;
                ytText = "Audio " + media.getBitrate()/1000 + " kbit/s";
            }
            else {
                ext = "mp4";
                ytText = (media.getFps() == 60) ? "Video " + media.getHeight() + "p60" :
                        "Video " + media.getHeight() + "p";
                ytText += " + merge audio";
               /* Log.e(TAG, "addVideoToList: "+ytText+" sampleRate:"+media.getAudioSampleRate()+", Bitrate: "+media.getBitrate());
                if (media.getBitrate() == -1) {
                    ytText += " (no audio)";
                }*/
            }
            Log.e(TAG, "addVideoToList: MediaSampleRate: "+media.getAudioSampleRate() );
        }catch (Exception e){e.printStackTrace();}
        String audioSet=null;
        if (isaudio) {
            /*Log.e(TAG, "addVideoToList: Sample Rate: "+media.getAudioSampleRate()+", Average bit: "+media.getAverageBitrate()
            +", Mime/type: "+media.getMimeType()+", Audio Quality: "+media.getAudioQuality()+", ProjectionType: "+media.getProjectionType()
            +", Quality: "+media.getQuality());*/

            audioSet = media.getUrl();
            if (media.getMimeType().contains("audio/mp4")) {
                Log.e(TAG, "addVideoToList: AudioUrlSet true" );
                audioLink = media.getUrl();
            }
        }
        if (audioLink==null) audioLink = audioSet;

        ytConfigs.add(new YTConfig(ytText, media.getUrl(), ext, videoTitle, channelTitle,isaudio,imgUrl));
    }

   /* private static void addVideoToList(final YTMedia media, final String videoTitle, final String channelTitle) {

        String ytText;boolean isaudio=false;
        if (media.getResSize()!=null) {
            ytText = "Video "+media.getResolution();
            if (media.isVideoOnly()) {
                ytText+=" (no audio)";
            }
            Log.e("VideoUrlFound",media.getUrl()+"");
        }else {
            isaudio = true;
            ytText = "Audio "+YTutils.getAvgBitRate(Integer.parseInt(media.getBitrate()))+" kbit/s";
            if (media.getCodec().contains("mp4a")){
                audioLink = media.getUrl();
                media.setExtension("m4a");
                Log.e("AudioSpecialLink",media.getUrl()+"");
            }else {
                Log.e("AudioURLFOUND",media.getUrl()+"");
            }
        }
        ytConfigs.add(new YTConfig(ytText, media.getUrl(), media.getExtension(), videoTitle,channelTitle,isaudio));
    }

*/
    public static void rebuildNotification() {
        boolean setgoing = true;
        int icon = R.drawable.ic_pause_notify;
        int favicon = R.drawable.ic_favorite;
        if (!isplaying) {
            icon = R.drawable.ic_play_notify;
            setgoing = false;
        }
        if (isFavourite)
            favicon = R.drawable.ic_favorite_full;

        builder = new NotificationCompat.Builder(activity, "channel_01")
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_audio_pulse2)
                .addAction(R.drawable.ic_previous_notify, "Previous", prevPendingIntent)
                .addAction(icon, "Pause", pausePendingIntent)
                .addAction(R.drawable.ic_next_notify, "Next", nextPendingIntent)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1,2)
                .setMediaSession(mediaSession.getSessionToken()))
                .setShowWhen(false)
                .setContentTitle(videoTitle)
                .setOngoing(setgoing)
                .setSound(null)
                .setContentIntent(clickPendingIntent)
                .setContentText(channelTitle);
        try {
            if (bitmapIcon.getWidth()>1700) {
                double multiply_factor = 1700.0/(double)bitmapIcon.getWidth();
                int height = (int)(bitmapIcon.getHeight() * multiply_factor);
                bitmapIcon = Bitmap.createScaledBitmap(bitmapIcon,1700,height,false);
            }
            builder.setLargeIcon(bitmapIcon);
        }catch (Exception ignored) { }

        if (!localPlayBack)
            builder.addAction(favicon,"Favourite",favouritePendingIntent);

        notificationManagerCompat.notify(1, builder.build());
    }



    public void createNotification() {

        /** Next song Listener */
        Intent newintent = new Intent(this, SongBroadCast.class);
        newintent.setAction("com.kpstv.youtube.ACTION_NEXT");
        nextPendingIntent = PendingIntent.getBroadcast(this, 1, newintent, 0);
        /** Previous song Listener */
        newintent = new Intent(this, SongBroadCast.class);
        newintent.setAction("com.kpstv.youtube.ACTION_PREVIOUS");
        prevPendingIntent = PendingIntent.getBroadcast(this, 2, newintent, 0);
        /** Play or Pause listener */
        newintent = new Intent(this, SongBroadCast.class);
        newintent.setAction("com.kpstv.youtube.ACTION_PLAY");
        pausePendingIntent = PendingIntent.getBroadcast(this, 3, newintent, 0);
        /** Focus on Click Listener */
        newintent = new Intent(MainActivity.this, MainActivity.class);
        newintent.putExtra("DO", "focus");
        clickPendingIntent = PendingIntent.getActivity(this, 4, newintent, 0);
        /** Favourite Pending intent */
        newintent = new Intent(this,SongBroadCast.class);
        newintent.setAction("com.kpstv.youtube.FAVOURITE_SONG");
        favouritePendingIntent = PendingIntent.getBroadcast(this,10,newintent,0);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            notificationChannel = new NotificationChannel("channel_01", name, importance);
            notificationChannel.setDescription(description);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManagerCompat = NotificationManagerCompat.from(this);
    }

    public static void changePlayBack(boolean isplay) {
        Log.e("PlayingState", "Playing State: " + player.isPlayingAd() + ", isPlay:" + isplay);
        if (isplay) {
            makePause();
            MainActivity.player.setPlayWhenReady(true);
            //   updateDuration();
        } else {
            makePlay();
            MainActivity.player.setPlayWhenReady(false);
            // mTimer.cancel();
        }
        Log.e("CurrentDur", MainActivity.player.getCurrentPosition() + "");
        isplaying = isplay;
        rebuildNotification();
    }

    public static void onClear() {
        loadProgress.setVisibility(VISIBLE);
        actionTitle.setVisibility(View.GONE);
        actionChannelTitle.setVisibility(View.GONE);
        songProgress.setVisibility(View.GONE);
        actionPlay.setVisibility(View.GONE);
        songProgress.setProgress(0);
        actionUp.setVisibility(View.GONE);
        player.stop();
        player.release();
        mHandler.removeCallbacks(mUpdateTimeTask);
        isplaying = false;
        total_duration = 0;
        total_seconds = 0;
    }

    public static void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    public static Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = MainActivity.player.getDuration();
            long currentDur = MainActivity.player.getCurrentPosition();
            int progress = (YTutils.getProgressPercentage(currentDur, totalDuration));
            songProgress.setProgress(progress);

            updateMediaSessionPlaybackState();
            //mMediaSessionCallback.onSeekTo(currentDur);

            mHandler.postDelayed(this, 100);
        }
    };

    static String[] getYTUrls(String to_inject_yturl) {
        preferences = activity.getSharedPreferences("history",MODE_PRIVATE);
        String data = YTutils.readContent(activity,"history.csv");
        // String line = preferences.getString("urls","");
        if (data!=null && !data.isEmpty()) {
            String[] lines = data.split("\n|\r");

            ArrayList<String> arrayList = new ArrayList<>();

             Log.e(TAG, "getYTUrls: Injected uri 0: "+to_inject_yturl );
            arrayList.add(to_inject_yturl);
            int i=1;
            for(String l: lines) {
                l = l.split("\\|")[0];
                if (l.isEmpty()) continue;
                if (YTutils.getVideoID(l).equals(YTutils.getVideoID(to_inject_yturl))) continue;
                arrayList.add(l);
                Log.e(TAG, "getYTUrls: Injected uri "+i+": "+l);
//                i++;
            }
            nPlayModels.clear();

            return YTutils.convertListToArrayMethod(arrayList);
        }else {
            String[] yt_urls = new String[1];
            yt_urls[0] = to_inject_yturl;
            return yt_urls;
        }
    }

    static class soundCloudData extends AsyncTask<Void,Void,Void> {
        String link,ytLink;
        SoundCloud.SoundCloudModel model;
        public soundCloudData(String link) {
            this.link = link;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e(TAG, "onPostExecute: Comming now..." );
           /* if (ytLink!=null) {
                if (yturls.size()<=0) {
                    PlayVideo(getYTUrls(ytLink),0);
                } else {
                    int insert_index = ytIndex;
                    if (nPlayModels.size()>0 && nPlayModels.size()==yturls.size()) {
                        MetaModel metaModel = new MetaModel(model.getTitle(),model.getAuthorName(),model.getImageUrl());
                        NPlayModel model = new NPlayModel(ytLink,new YTMeta(metaModel),true);
                        nPlayModels.add(insert_index,model);
                    }
                    yturls.remove(ytLink);
                    yturls.add(insert_index,ytLink);
                    ChangeVideo(insert_index);
                }
            }else {
                Toast.makeText(activity, "Couldn't parse this Spotify url", Toast.LENGTH_SHORT).show();
            }*/
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SoundCloud soundCloud = new SoundCloud(link);
            soundCloud.captureViews();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bottom_player.setVisibility(VISIBLE);
        }
    }

    static class makeSpotifyData extends AsyncTask<Void,Void,Void> {
        SpotifyTrack track;
        String spotifyUrl,ytLink;
        public makeSpotifyData(String yturl) {
            this.spotifyUrl = yturl;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (ytLink!=null) {
                if (yturls.size()<=0) {
                    PlayVideo(getYTUrls(ytLink),0);
                } else {
                    int insert_index = ytIndex;
                    if (nPlayModels.size()>0 && nPlayModels.size()==yturls.size()) {
                        MetaModel metaModel = new MetaModel(YTutils.getVideoID(track.getYtUrl()),track.getTitle(),track.getAuthor(),track.getImageUrl());
                        NPlayModel model = new NPlayModel(ytLink,new YTMeta(metaModel),true);
                        nPlayModels.add(insert_index,model);
                    }
                    if (yturls.contains(ytLink))
                        yturls.remove(ytLink);
                    yturls.add(insert_index,ytLink);
                    ChangeVideo(insert_index);
                }
            }else {
                Toast.makeText(activity, "Couldn't parse this Spotify url", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onPreExecute() {
            onClear();
            bottom_player.setVisibility(VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            track = new SpotifyTrack(YTutils.getSpotifyID(spotifyUrl));
            ytLink = track.getYtUrl();
            return null;
        }
    }

    public static void playPrevious() {
        if (ytIndex <= 0) {
            Toast.makeText(activity, "No previous song in playlist", Toast.LENGTH_SHORT).show();
            return;
        }
        command=2;
        onClear();
        videoID = YTutils.getVideoID(yturls.get(ytIndex-1));
        ytIndex--;
        AppSettings.playAdCount++;
        showAd(activity);
        if (!localPlayBack) {
            LoadVideo = new loadVideo();
            LoadVideo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,videoID);
        } else {
            videoID = yturls.get(ytIndex);
            LoadOffline = new loadVideo_Local(yturls.get(ytIndex));
            LoadOffline.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }
    static int command=0;
    public static void playNext() {
        if ((ytIndex + 1) == yturls.size()) {
            if (isLoop) {
                ytIndex=-1;
            }else {
                Toast.makeText(activity, "No new song in playlist", Toast.LENGTH_SHORT).show();
           //     bottom_player.setVisibility(View.GONE);
                return;
            }
        }
        command=1;
        onClear();
        videoID = YTutils.getVideoID(yturls.get(ytIndex+1));
        ytIndex++;
        AppSettings.playAdCount++;
        showAd(activity);
        if (!localPlayBack) {
            LoadVideo = new loadVideo();
            LoadVideo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,videoID);
        }else {
            videoID = yturls.get(ytIndex);
            LoadOffline = new loadVideo_Local(yturls.get(ytIndex));
            LoadOffline.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    static void showAd(Context con) {
        if (AppSettings.playAdCount%AppSettings.adOffset==0 && AppSettings.playAdCount!=0 && AppSettings.showAds && !localPlayBack) {
            Log.e(TAG, "showAd: Showing Ad..." );
            //TODO: Change ad unit ID, Sample ca-app-pub-3940256099942544/1033173712
           try {
               PlayerActivity2.showAd();
           }catch (Exception ex){
              try {
                  InterstitialAd mInterstitialAd = new InterstitialAd(con);
                  mInterstitialAd.setAdUnitId("ca-app-pub-1164424526503510/4801416648");
                  mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("07153BA64BB64F7C3F726B71C4AE30B9").build());
                  mInterstitialAd.setAdListener(new AdListener() {
                      @Override
                      public void onAdFailedToLoad(int i) {
                          super.onAdFailedToLoad(i);
                          Log.e(TAG, "onAdFailedToLoad: Ad failed to load: " + i);
                      }

                      @Override
                      public void onAdLoaded() {
                          super.onAdLoaded();
                          mInterstitialAd.show();
                      }
                  });
              } catch (Exception ignored){}
           }
        }
    }

    static void makePlay() {
        actionPlay.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_play_circle));
        try {
            PlayerActivity2.makePlay();
        }catch (Exception ignored) {
            Log.e("PlayerActivity","isnull");
        }
    }

    static void makePause() {
        actionPlay.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_pause_circle));
        try {
            PlayerActivity2.makePause();
        }catch (Exception ignored) {
            Log.e("PlayerActivity","isnull");
        }
       /* playFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
        collpaseView.setImageViewResource(R.id.nPlay, R.drawable.ic_pause_notify);
        expandedView.setImageViewResource(R.id.nPlay, R.drawable.ic_pause_notify);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            notificationManager.notify(1,builder.build());
        }
        notificationManagerCompat.notify(1, builder.build());*/
    }
    private static YtFile getBestStream(SparseArray<YtFile> ytFiles) {
        ConnectionQuality connectionQuality = ConnectionQuality.MODERATE;
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
                    itags = new int[]{141, 251, 140, 17};
                    break;
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

    private static List<YTMedia> getBestStream(List<YTMedia> streams) {
        List<YTMedia> medias = new ArrayList<>();
        for(int i=0; i<streams.size();i++) {
            YTMedia media = streams.get(i);
             if (media.getHeight()!=-1) {
                int j=0;
                while (j<streams.size()) {
                    YTMedia media1 = streams.get(j);
                    if (media.getQuality().equals(media1.getQuality())) {
                        int m1 = media.getBitrate();
                        int m2 = media1.getBitrate();
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

    private static class saveToHistory extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(String... strings) {
            String url_link = strings[0];
            String ytID = YTutils.getVideoID(url_link);

            String formattedDate = YTutils.getTodayDate();
            int percent = 100;
            if (!url_link.contains("soundcloud.com")) {
                try {
                    percent = likeCounts*100/(likeCounts+dislikeCounts);
                }catch (Exception e){e.printStackTrace();}
            }

            String videoTitle = MainActivity.videoTitle.replace("|","");
            String channelTitle = MainActivity.channelTitle.replace("|","");

            String insert_data = ytID+"|"+1+"|"+videoTitle+"|"+channelTitle+"|"+imgUrl+ "|"
                    + formattedDate+"|"+percent;

            String historyData = YTutils.readContent(activity,"history.csv");
            if (historyData!=null && !historyData.isEmpty()) {
                if (historyData.contains(ytID+"|")) {
                    String[] childs = historyData.split("\n|\r");
                    StringBuilder builder = new StringBuilder();
                    builder.append(insert_data);
                    for (int i=0;i<childs.length;i++) {
                        if (!childs[i].startsWith(ytID))
                            builder.append("\n").append(childs[i]);
                    }
                    YTutils.writeContent(activity,"history.csv",builder.toString().trim());
                }else YTutils.writeContent(activity,"history.csv",
                        insert_data+"\n"+historyData.trim());
            }else YTutils.writeContent(activity,"history.csv",
                    insert_data);

            /** A logic to calculate no of times a song is played and saved to list */
            String data = YTutils.readContent(activity,"library.csv");

            if (data!=null && !data.isEmpty()) {
                boolean ifExist=false;
                if (data.contains(ytID)) {
                    String[] items = data.split("\n|\r");
                    for (int i=0;i<items.length;i++) {
                        if (items[i].contains(ytID)) {
                            ifExist=true;
                            int count = Integer.parseInt(items[i].split("\\|")[1]);
                            items[i] = ytID+"|"+ (++count)+"|"+videoTitle+"|"+channelTitle+"|"+imgUrl+ "|"
                                    + formattedDate+"|"+percent;
                            String lines = YTutils.join(items,'\n');
                            YTutils.writeContent(activity,"library.csv",lines.trim());
                            break;
                        }
                    }
                }
                if (!ifExist) {
                    YTutils.writeContent(activity,"library.csv",
                            data.trim()+"\n"+insert_data);
                }
            }else YTutils.writeContent(activity,"library.csv",insert_data);

            if (!loadedFromData)
                DataUtils.saveUrl(activity,videoID,audioLink,ytConfigs);

            /*// Get playlist
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

            Log.e("StringtoAdd", url_link + "|" + formattedDate+"|"+percent);
            urls.add(0, url_link + "|" + formattedDate+"|"+percent);

            // Save playlist
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < urls.size(); i++) {
                sb.append(urls.get(i)).append(",");
            }
            SharedPreferences.Editor prefsEditor = preferences.edit();
            prefsEditor.putString("urls", sb.toString());
            prefsEditor.apply();*/
            return null;
        }
    }



    static void showAlert(String title, String message, boolean isalert) {
        int icon = android.R.drawable.ic_dialog_info;
        if (isalert) icon = android.R.drawable.ic_dialog_alert;
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    bottom_player.setVisibility(View.GONE);
                })
                .setIcon(icon)
                .show();

    }



   /* private void setListener() {
        // Play or Pause listener
        Intent newintent = new Intent(MainActivity.this, MainActivity.class);
        newintent.putExtra("DO", "play");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, newintent, 0);

        expandedView.setOnClickPendingIntent(R.id.nPlay, pendingIntent);
        collpaseView.setOnClickPendingIntent(R.id.nPlay, pendingIntent);

        // Next song Listener
        newintent = new Intent(MainActivity.this, MainActivity.class);
        newintent.putExtra("DO", "next");
        pendingIntent = PendingIntent.getActivity(this, 1, newintent, 0);

        expandedView.setOnClickPendingIntent(R.id.nForward, pendingIntent);
        collpaseView.setOnClickPendingIntent(R.id.nForward, pendingIntent);

        // Previous song Listener
        newintent = new Intent(MainActivity.this, MainActivity.class);
        newintent.putExtra("DO", "previous");
        pendingIntent = PendingIntent.getActivity(this, 2, newintent, 0);

        expandedView.setOnClickPendingIntent(R.id.nPrevious, pendingIntent);
        collpaseView.setOnClickPendingIntent(R.id.nPrevious, pendingIntent);

        // Add to playlist Listener
        newintent = new Intent(MainActivity.this, MainActivity.class);
        newintent.putExtra("DO", "add");
        pendingIntent = PendingIntent.getActivity(this, 3, newintent, 0);

        expandedView.setOnClickPendingIntent(R.id.nAdd, pendingIntent);

        // Focus on Click Listener
        newintent = new Intent(PlayerActivity.this, PlayerActivity.class);
        newintent.putExtra("DO", "focus");
        pendingIntent = PendingIntent.getActivity(this, 4, newintent, 0);

        expandedView.setOnClickPendingIntent(R.id.mainlayout, pendingIntent);
        collpaseView.setOnClickPendingIntent(R.id.mainlayout, pendingIntent);
    }*/
}


