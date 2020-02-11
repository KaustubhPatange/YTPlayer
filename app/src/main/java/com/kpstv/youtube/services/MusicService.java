package com.kpstv.youtube.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
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
import com.google.android.gms.ads.InterstitialAd;
import com.kpstv.youtube.AppInterface;
import com.kpstv.youtube.AppSettings;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.PlayerActivity2;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.NPlayModel;
import com.kpstv.youtube.models.YTConfig;
import com.kpstv.youtube.receivers.SongBroadCast;
import com.kpstv.youtube.utils.APIResponse;
import com.kpstv.youtube.utils.DataUtils;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.LyricsApi;
import com.kpstv.youtube.utils.SoundCloud;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.view.View.VISIBLE;
import static com.kpstv.youtube.MainActivity.actionChannelTitle;
import static com.kpstv.youtube.MainActivity.actionPlay;
import static com.kpstv.youtube.MainActivity.actionTitle;
import static com.kpstv.youtube.MainActivity.actionUp;
import static com.kpstv.youtube.MainActivity.adView;
import static com.kpstv.youtube.MainActivity.adViewLayout;
import static com.kpstv.youtube.MainActivity.bottom_player;
import static com.kpstv.youtube.MainActivity.libraryFrag;
import static com.kpstv.youtube.MainActivity.loadProgress;
import static com.kpstv.youtube.MainActivity.mUpdateTimeTask;
import static com.kpstv.youtube.MainActivity.songProgress;
import static com.kpstv.youtube.MainActivity.toolTipManager;
import static com.kpstv.youtube.MainActivity.updateProgressBar;

public class MusicService extends Service implements AppInterface {

    @Override
    public void onCreate() {
        super.onCreate();
        functionInMainActivity(getApplicationContext());

        createNotification(getApplicationContext());

        Intent intent = new Intent(this,MusicService.class);
        intent.setAction("CLOSE_SERVICE");
        PendingIntent pendingIntent = PendingIntent.getService(this,0,intent,0);

        Notification notification = new NotificationCompat.Builder(this,"channel_01")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentTitle("Loading")
                .addAction(R.drawable.ic_close,"Cancel",pendingIntent)
                .setShowWhen(false)
                .setContentText("Please wait...")
                .setSmallIcon(R.drawable.ic_audio_pulse2)
                .build();

        startForeground(1,notification);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "app:wakelockTag");
        wakeLock.acquire();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();
        if (action==null) return START_NOT_STICKY;

        switch (action) {
            case "CLOSE_SERVICE":
                stopSelf();
                break;


            case "PlayVideo_Local":

                String[] urls = intent.getStringArrayExtra("urls");

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
                break;

            case "PlayVideo_Local_pos":

                urls = intent.getStringArrayExtra("urls");
                int position = intent.getIntExtra("pos",0);

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
                break;

            case "ChangeVideoOffline":

                localPlayBack=true;

                position = intent.getIntExtra("pos",0);

                if (LoadOffline !=null && LoadOffline.getStatus() == AsyncTask.Status.RUNNING)
                {
                    player.stop(); player.release();
                    LoadOffline.cancel(true);
                }
                ytIndex = position;
                LoadOffline = new loadVideo_Local(yturls.get(ytIndex));
                LoadOffline.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;

            case "PlayVideo":

                urls = intent.getStringArrayExtra("urls");

                localPlayBack=false;
                yturls.clear();
                if (LoadVideo !=null && LoadVideo.getStatus() == AsyncTask.Status.RUNNING)
                {
                    player.stop(); player.release();
                    LoadVideo.cancel(true);
                }
                yturls.addAll(Arrays.asList(urls));
                videoID = YTutils.getVideoID(yturls.get(0));
                Log.e("VideoID_ToPLAY",videoID+"");
                LoadVideo = new loadVideo();
                LoadVideo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,videoID);
                break;

            case "PlayVideo_pos":

                urls = intent.getStringArrayExtra("urls");
                position = intent.getIntExtra("pos",0);

                localPlayBack=false;
                yturls.clear();
                if (LoadVideo !=null && LoadVideo.getStatus() == AsyncTask.Status.RUNNING)
                {
                    player.stop(); player.release();
                    LoadVideo.cancel(true);
                }
                yturls.addAll(Arrays.asList(urls));
                ytIndex = position;
                videoID = YTutils.getVideoID(yturls.get(position));
                LoadVideo = new loadVideo();
                LoadVideo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,videoID);
                break;

            case "ChangeVideo":

                position = intent.getIntExtra("pos",0);

                localPlayBack=false;

                if (LoadVideo !=null && LoadVideo.getStatus() == AsyncTask.Status.RUNNING)
                {
                    player.stop(); player.release();
                    LoadVideo.cancel(true);
                }
                ytIndex = position;
                videoID = YTutils.getVideoID(yturls.get(position));
                LoadVideo = new loadVideo();
                LoadVideo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,videoID);
                break;
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.deleteNotificationChannel("channel_01");
            notificationManager.deleteNotificationChannel("channel_02");
        }

        if (PlayerActivity2.activity!=null) {
            PlayerActivity2.activity.finish();
        }

        try {
            bottom_player.setVisibility(View.GONE);
            adView.setVisibility(View.GONE);
            onClear();
        }catch (Exception ignored){}

        notificationManagerCompat.cancel(1);
        try {

            if (mEqualizer!=null) mEqualizer.release();
            if (bassBoost!=null) bassBoost.release();
            if (loudnessEnhancer!=null) loudnessEnhancer.release();
            if (virtualizer!=null) virtualizer.release();

            player.stop();
            player.release();

        }catch (Exception e) { e.printStackTrace(); }
        wakeLock.release();
        activity = null;
        super.onDestroy();
    }














    static AsyncTask<String,String,Void> LoadVideo; static AsyncTask<Void,Void,Void> LoadOffline;
    public static ArrayList<NPlayModel> nPlayModels; public static List<String> playListItems;
    public static SimpleExoPlayer player;  public static boolean supportFFmpeg=false,loadedFavFrag=false;
    public static MediaSource mediaSource; private static final String TAG = "MusicService";
    public static DefaultDataSourceFactory dataSourceFactory;
    public static DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    public static TrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
    public static TrackSelector trackSelector = new DefaultTrackSelector(trackSelectionFactory);
    public static String selectedItemText=""; public static int sleepSeconds;
    private static MediaSessionCompat mediaSession;
    PowerManager.WakeLock wakeLock;
    static SharedPreferences preferences;
    public static SharedPreferences settingPref;
    public static Context activity;

    public static void functionInMainActivity(Context activity) {

       // Toast.makeText(activity, "Function in MainActivity...!", Toast.LENGTH_SHORT).show();

        MusicService.activity = activity;

        settingPref = activity.getSharedPreferences("settings",MODE_PRIVATE);
        isEqualizerEnabled = settingPref.getBoolean("equalizer_enabled",false);

        dataSourceFactory = new DefaultDataSourceFactory(activity,
                Util.getUserAgent(activity,
                        activity.getResources().getString(R.string.app_name)), BANDWIDTH_METER);

        player = ExoPlayerFactory.newSimpleInstance(activity, trackSelector);

        createNotification(activity);

        playListItems = new ArrayList<>();
        yturls = new ArrayList<>();
        nPlayModels = new ArrayList<>();

        ComponentName mediaButtonReceiverComponentName = new ComponentName(
                activity,
                SongBroadCast.class);
        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mediaButtonReceiverComponentName);
        PendingIntent mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(
                activity,
                0,
                mediaButtonIntent,
                0);
        mediaSession = new MediaSessionCompat(activity,
                "MusicPlayer",
                mediaButtonReceiverComponentName,
                mediaButtonReceiverPendingIntent);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );
        mediaSession.setCallback(mMediaSessionCallback);
        mediaSession.setActive(true);
        mediaSession.setMediaButtonReceiver(mediaButtonReceiverPendingIntent);

        preferences = activity.getSharedPreferences("history",MODE_PRIVATE);

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


    public static String videoTitle = "", channelTitle = "", viewCounts, imgUrl, videoID,audioLink;
    public static int likeCounts,dislikeCounts;
    public static NotificationManagerCompat notificationManagerCompat;
    static NotificationManager notificationManager;
    static NotificationChannel notificationChannel;
    static PendingIntent prevPendingIntent,pausePendingIntent,nextPendingIntent,clickPendingIntent,favouritePendingIntent;
    public static Bitmap bitmapIcon; public static ArrayList<YTConfig> ytConfigs = new ArrayList<>();
    static NotificationCompat.Builder builder;
    public static boolean isplaying, sleepEndTrack=false,localPlayBack=false, soundCloudPlayBack,isFavourite=false,isEqualizerEnabled=false;
    public static boolean isEqualizerSet=false,loadedFromData=false; // isLoop=false,
    public static Spanned lyricText;static boolean isFirstLaunch=true;
    public static long total_duration = 0; public static PresetReverb presetReverb;
    public static BassBoost bassBoost; public static Virtualizer virtualizer;
    public static LoudnessEnhancer loudnessEnhancer;static int trials=3;
    public static int total_seconds; public static int nColor;
    public static ArrayList<String> yturls; static boolean dontAllowToPlay=false;
    public static int ytIndex = 0; public static Visualizer visualizer;
    public static Equalizer mEqualizer;static int command=0;
    public static int repeatMode = 0; /** 0 - No repeat, 1 - Repeat playlist, 2 - Repeat current song */


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
                    Log.e(TAG, "loadData_ OFFLINE Changing nColor: "+MusicService.nColor
                            +", ImageUri:"+MusicService.imgUrl);
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
                {
                    bitmapIcon = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Log.e(TAG, "BITMAP DATA: HERE...." );
                }
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
                    Log.e(TAG, "loadVideo: Changing nColor: "+MusicService.nColor +
                            ", ImageUri: "+MusicService.imgUrl);
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
                MusicService.videoTitle = soundCloud.getModel().getTitle();
                MusicService.channelTitle = soundCloud.getModel().getAuthorName();
                MusicService.imgUrl = soundCloud.getModel().getImageUrl();
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
                    MusicService.channelTitle = YTutils.getChannelTitle(ytMeta.getVideMeta().getTitle(),
                            ytMeta.getVideMeta().getAuthor());
                    MusicService.videoTitle = YTutils.setVideoTitle(ytMeta.getVideMeta().getTitle());
                    MusicService.imgUrl = ytMeta.getVideMeta().getImgUrl();
                }

                if (json.contains("\"error\"")) {
                    YTStatistics ytStatistics = new YTStatistics(videoID);
                    MusicService.viewCounts = ytStatistics.getViewCount();
                    MusicService.likeCounts = Integer.parseInt(ytStatistics.getLikeCount());
                    MusicService.dislikeCounts = Integer.parseInt(ytStatistics.getDislikeCount());
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

        @Override
        protected void onPreExecute() {
            commonPreExecute();
            super.onPreExecute();
        }
    }

    static void commonPreExecute() {
        try {
            if (!localPlayBack) {
                PlayerActivity2.progressBar.setVisibility(VISIBLE);
                PlayerActivity2.hidePlayButton();
            }
        }catch (Exception e){
            Log.e(TAG, "commonPreExecute: Error: "+e.getMessage());
        }
        try {
            bottom_player.setVisibility(VISIBLE);
            onClear();

            if (AppSettings.showAds) {
                AdRequest adRequest = new AdRequest.Builder().addTestDevice("07153BA64BB64F7C3F726B71C4AE30B9").build();
                MainActivity.adView.loadAd(adRequest);
                MainActivity.adView.setAdListener(new AdListener(){
                    @Override
                    public void onAdLoaded() {
                        MainActivity.adView.setVisibility(VISIBLE);
                        super.onAdLoaded();
                    }

                    @Override
                    public void onAdFailedToLoad(int i) {
                        adViewLayout.setVisibility(View.GONE);
                        Log.e(TAG, "onAdFailedToLoad: Failed to load Ad"+i );
                        super.onAdFailedToLoad(i);
                    }
                });
            }else MainActivity.adView.setVisibility(View.GONE);

        }catch (Exception e){
            Log.e(TAG, "commonPreExecute: Error: "+e.getMessage() );
        }
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
        if (MusicService.total_seconds==0)
        {
            Toast.makeText(activity, "Player is still processing!", Toast.LENGTH_SHORT).show();
        }else{
            try {
                if (!MusicService.isFavourite)
                    PlayerActivity2.favouriteButton.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_favorite_full));
                else PlayerActivity2.favouriteButton.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_favorite));
            }catch (Exception ignored){}

            MusicService.write_Favourite();
            rebuildNotification();
        }
    }

    public static void write_Favourite() {
        String t = YTutils.readContent(activity,"favourite.csv");
        if (t!=null && !t.contains(MusicService.videoID)) {
            t += "\n"+MusicService.videoID+"|"+MusicService.total_seconds+"|"+MusicService.videoTitle+"|"+MusicService.channelTitle
                    +"|"+MusicService.imgUrl;
            Toast.makeText(activity, "Added to favourites!", Toast.LENGTH_SHORT).show();
            MusicService.isFavourite=true;
        }else if (t!=null && t.contains(MusicService.videoID)) {

            String[] lines = t.split("\n|\r");
            StringBuilder builder = new StringBuilder();
            for (String line : lines) {
                if (!line.contains(MusicService.videoID) && !line.isEmpty()) {
                    builder.append("\n").append(line);
                }
            }

            t = builder.toString().trim();

            Toast.makeText(activity, "Removed from favourites!", Toast.LENGTH_SHORT).show();
            MusicService.isFavourite=false;
        }else {
            t =MusicService.videoID+"|"+MusicService.total_seconds+"|"+MusicService.videoTitle+"|"+MusicService.channelTitle
                    +"|"+MusicService.imgUrl;
            Toast.makeText(activity, "Added to favourites!", Toast.LENGTH_SHORT).show();
            MusicService.isFavourite=true;
        }
        YTutils.writeContent(activity,"favourite.csv",t.trim());
    }

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
                if (!playWhenReady && playbackState== ExoPlayer.STATE_READY && AppSettings.listenAudioChange) {
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
                        if (repeatMode!=2) {
                            makePlay();
                            isplaying = false;
                            playNext();
                            if (sleepEndTrack) {
                                Log.e(TAG, "onPlayerStateChanged: tiggered");
                                sleepEndTrack = false;
                                dontAllowToPlay = true;
                            }
                        }else playNext();
                        break;
                    case ExoPlayer.STATE_READY:

                        if(isFirstLaunch) {
                            isFirstLaunch=false;
                            setDefaultEqualizerValues();
                        }

                        try {
                            loadProgress.setVisibility(View.GONE);
                            actionTitle.setVisibility(VISIBLE);
                            actionChannelTitle.setVisibility(View.VISIBLE);
                            songProgress.setVisibility(View.VISIBLE);
                            actionPlay.setVisibility(View.VISIBLE);
                            songProgress.setProgress(0);
                            actionUp.setVisibility(View.VISIBLE);
                            actionTitle.setText(videoTitle);
                            actionChannelTitle.setText(" "+ Html.fromHtml("&#8226")+" "+channelTitle);
                        }catch (Exception e){
                            Log.e(TAG, "onPlayerStateChanged: Error: "+e.getMessage());
                        }
                        total_duration = MusicService.player.getDuration();
                        total_seconds = (int) total_duration / 1000;
                        try {
                            if (!localPlayBack) {
                                PlayerActivity2.progressBar.setVisibility(View.GONE);
                                PlayerActivity2.showPlayButton();
                            }
                            PlayerActivity2.loadAgain();
                            PlayerActivity2.totalDuration.setText(YTutils.milliSecondsToTimer(MusicService.total_duration));
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

                        if (MainActivity.activity!=null)
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

    private static void addVideoToList(final YTMedia media, final String videoTitle, final String channelTitle) {

        String ytText=""; boolean isaudio=false;
        String ext = "m4a";
        try {
            if (media.getAudioSampleRate() != 0)
            {
                isaudio = true;
                ytText = media.getBitrate()/1000 + " kbps";
            }
            else {
                ext = "mp4";
                ytText = (media.getFps() == 60) ?  media.getHeight() + "p60" : media.getHeight() + "p";
                for(YTConfig config: ytConfigs) {
                    if (config.getText().equals(ytText))
                        return;
                }
            }
            Log.e(TAG, "addVideoToList: Title: "+ytText);
        }catch (Exception e){e.printStackTrace();}
        String audioSet=null;
        if (isaudio) {
            audioSet = media.getUrl();
            if (media.getMimeType().contains("audio/mp4")) {
                Log.e(TAG, "addVideoToList: AudioUrlSet true" );
                audioLink = media.getUrl();
            }
        }
        if (audioLink==null) audioLink = audioSet;

        YTConfig ytConfig = new YTConfig(ytText, media.getUrl(), ext, videoTitle, channelTitle,isaudio,imgUrl);
        if (ytConfig.isAudio()) {
            ytConfig.setBitRate(media.getBitrate()/1000);
        }else ytConfig.setExt("mp4");
        ytConfigs.add(ytConfig);
    }

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


        Intent intent = new Intent(activity,MusicService.class);
        intent.setAction("CLOSE_SERVICE");
        PendingIntent pendingIntent = PendingIntent.getService(activity,0,intent,0);

        builder = new NotificationCompat.Builder(activity, "channel_01")
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSmallIcon(R.drawable.ic_audio_pulse2)
                .addAction(R.drawable.ic_close,"Close",pendingIntent)
                .addAction(R.drawable.ic_previous_notify, "Previous", prevPendingIntent)
                .addAction(icon, "Pause", pausePendingIntent)
                .addAction(R.drawable.ic_next_notify, "Next", nextPendingIntent)
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1,2,3)
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

    public static void createNotification(Context con) {

        /** Next song Listener */
        Intent newintent = new Intent(con, SongBroadCast.class);
        newintent.setAction("com.kpstv.youtube.ACTION_NEXT");
        nextPendingIntent = PendingIntent.getBroadcast(con, 1, newintent, 0);
        /** Previous song Listener */
        newintent = new Intent(con, SongBroadCast.class);
        newintent.setAction("com.kpstv.youtube.ACTION_PREVIOUS");
        prevPendingIntent = PendingIntent.getBroadcast(con, 2, newintent, 0);
        /** Play or Pause listener */
        newintent = new Intent(con, SongBroadCast.class);
        newintent.setAction("com.kpstv.youtube.ACTION_PLAY");
        pausePendingIntent = PendingIntent.getBroadcast(con, 3, newintent, 0);
        /** Focus on Click Listener */
        newintent = new Intent(con, MainActivity.class);
        newintent.putExtra("DO", "focus");
        clickPendingIntent = PendingIntent.getActivity(con, 4, newintent, 0);
        /** Favourite Pending intent */
        newintent = new Intent(con,SongBroadCast.class);
        newintent.setAction("com.kpstv.youtube.FAVOURITE_SONG");
        favouritePendingIntent = PendingIntent.getBroadcast(con,10,newintent,0);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = con.getString(R.string.channel_name);
            String description = con.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            notificationChannel = new NotificationChannel("channel_01", name, importance);
            notificationChannel.setDescription(description);
            notificationManager = con.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

            NotificationChannel channel = new NotificationChannel("channel_02","Download",importance);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManagerCompat = NotificationManagerCompat.from(con);
    }

    public static void changePlayBack(boolean isplay) {
        Log.e("PlayingState", "Playing State: " + player.isPlayingAd() + ", isPlay:" + isplay);
        if (isplay) {
            makePause();
            MusicService.player.setPlayWhenReady(true);
            //   updateDuration();
        } else {
            makePlay();
            MusicService.player.setPlayWhenReady(false);
            // mTimer.cancel();
        }
        Log.e("CurrentDur", MusicService.player.getCurrentPosition() + "");
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
        isplaying = false;
        total_duration = 0;
        total_seconds = 0;
    }

    public static void onMake() {

        loadProgress.setVisibility(View.GONE);
        actionTitle.setVisibility(VISIBLE);
        actionChannelTitle.setVisibility(View.VISIBLE);
        songProgress.setVisibility(View.VISIBLE);
        actionPlay.setVisibility(View.VISIBLE);
        songProgress.setProgress(0);
        actionUp.setVisibility(View.VISIBLE);
        actionTitle.setText(videoTitle);
        actionChannelTitle.setText(" "+ Html.fromHtml("&#8226")+" "+channelTitle);

        commonPreExecute();
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

    public static void playSame() {
        player.seekTo(0);
    }

    public static void playNext() {
        switch (repeatMode) {
            case 0:
                if ((ytIndex + 1) == yturls.size()) {
                    Toast.makeText(activity, "No new song in playlist", Toast.LENGTH_SHORT).show();
                    return;
                }
            case 1:
                if ((ytIndex + 1) == yturls.size()) {
                    ytIndex=-1;
                }
                break;
            case 2:
                playSame();
                return;
        }
           /* if (isLoop) {
                ytIndex=-1;
            }else {
                Toast.makeText(activity, "No new song in playlist", Toast.LENGTH_SHORT).show();
                //     bottom_player.setVisibility(View.GONE);
                return;
            }*/

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

    static void makePlay() {
        try {
            actionPlay.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_play_circle));
            PlayerActivity2.makePlay();
        }catch (Exception ignored) {
            Log.e("PlayerActivity","isnull");
        }
    }

    static void makePause() {
        try {
            actionPlay.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_pause_circle));
            PlayerActivity2.makePause();
        }catch (Exception ignored) {
            Log.e("PlayerActivity","isnull");
        }
    }

    public static void showAd(Context con) {
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

            String videoTitle = MusicService.videoTitle.replace("|","");
            String channelTitle = MusicService.channelTitle.replace("|","");

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

}
