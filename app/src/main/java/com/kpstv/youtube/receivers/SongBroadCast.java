package com.kpstv.youtube.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.jakewharton.processphoenix.ProcessPhoenix;
import com.kpstv.youtube.AppInterface;
import com.kpstv.youtube.AppSettings;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.PlayerActivity2;
import com.kpstv.youtube.R;
//import com.kpstv.youtube.services.DownloadService;
import com.kpstv.youtube.SplashActivity;
import com.kpstv.youtube.services.IntentDownloadService;
import com.kpstv.youtube.services.MusicService;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.SoundCloud;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTStatistics;
import com.kpstv.youtube.utils.YTutils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.net.URLConnection;

public class SongBroadCast extends BroadcastReceiver implements AppInterface {

    AsyncTask<Void,Void,Void> setData;
    private static final String TAG = "SongBroadCast";
    private InterstitialAd mInterstitialAd;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case "com.kpstv.youtube.ACTION_NEXT":
                try {
                    MusicService.playNext();
                }catch (Exception ignored){disableContext(context);}
                break;
            case "com.kpstv.youtube.ACTION_PREVIOUS":
                try {
                MusicService.playPrevious();
                }catch (Exception ignored){disableContext(context);}
                break;
            case "com.kpstv.youtube.ACTION_PLAY":
                try {
                MusicService.changePlayBack(!MusicService.isplaying);
                try {
                    if (MusicService.isplaying) {
                        PlayerActivity2.makePause();
                    } else {
                        PlayerActivity2.makePlay();
                    }
                }catch (Exception ignored){}
                }catch (Exception ignored){disableContext(context);}
                break;
            case "com.kpstv.youtube.STOP_SERVICE":
                Intent serviceIntent = new Intent(context, IntentDownloadService.class);
                context.stopService(serviceIntent);
                break;
            case "com.kpstv.youtube.OPEN_SONG":
                Log.e(TAG, "onReceive: Coming Right here..." );
                if (MainActivity.activity==null) {
                    Log.e(TAG, "onReceive: Let's launch mainactivity" );
                    Intent intent1 = new Intent(context,MainActivity.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent1.setAction("com.kpstv.youtube.OPEN_SONG");
                    intent1.setData(intent.getData());
                    context.startActivity(intent1);
                }else
                    YTutils.openSong(context,intent);
                break;
            case "com.kpstv.youtube.FAVOURITE_SONG":
                try {
                    MusicService.actionFavouriteClicked();
                }catch (Exception ignored){disableContext(context);}
                break;
            case "com.kpstv.youtube.OPEN_SHARE_SONG":
                try {
                    Uri uri = Uri.parse(intent.getStringExtra("filePath"));
                    Log.e(TAG, "onReceive: Uri: "+uri.toString() );
                    File f = new File(uri.getPath());
                    ShareCompat.IntentBuilder.from(MainActivity.activity)
                            .setStream(uri)
                            .setType(URLConnection.guessContentTypeFromName(f.getName()))
                            .startChooser();
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case "com.kpstv.youtube.SHOW_UPDATE":
                new YTutils.CheckForUpdates(MainActivity.activity,false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
        }
        showAd(context);
    }

    public void disableContext(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(1);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.deleteNotificationChannel("channel_01");
        }
        Process.killProcess(Process.myPid());
    }

    void showAd(Context con) {
        if (AppSettings.playAdCount%AppSettings.adOffset==0 && AppSettings.playAdCount!=0 && AppSettings.showAds) {
            Log.e(TAG, "showAd: Showing Ad..." );
            //TODO: Change ad unit ID, Sample ca-app-pub-3940256099942544/1033173712
            mInterstitialAd = new InterstitialAd(con);
            mInterstitialAd.setAdUnitId("ca-app-pub-1164424526503510/4801416648");
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
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
        }
    }

    class loadData_Offline extends AsyncTask<Void,Void,Void> {
        Context context;

        public loadData_Offline(Context context) {
            this.context = context;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Palette.generateAsync(MusicService.bitmapIcon, palette -> {
                MusicService.nColor = palette.getVibrantColor(context.getResources().getColor(R.color.light_white));
                Log.e(TAG, "loadData_OFFLINE Changing nColor: "+MusicService.nColor +
                        ", ImageUri: "+MusicService.imgUrl);
                MusicService.rebuildNotification();
                try {
                    PlayerActivity2.loadAgain();
                }catch (Exception e) {
                    Log.e("PlayerActivity","is_still_null");
                }
            });
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            File f = new File(MusicService.videoID);
            Uri uri = Uri.fromFile(f);
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(context,uri);
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                byte [] data = mmr.getEmbeddedPicture();

                if(data != null)
                    MusicService.bitmapIcon = BitmapFactory.decodeByteArray(data, 0, data.length);
                else
                    MusicService.bitmapIcon = YTutils.drawableToBitmap(ContextCompat.getDrawable(context,R.drawable.ic_pulse));

                if (artist==null) artist ="Unknown artist";
                if (title==null) title = YTutils.getVideoTitle(f.getName());

                if (title.contains("."))
                    title = title.split("\\.")[0];

                MusicService.videoTitle = title;
                MusicService.channelTitle = artist;
                MusicService.likeCounts = -1; MusicService.dislikeCounts = -1;
                MusicService.viewCounts = "-1";

                MusicService.total_seconds = Integer.parseInt(durationStr);

            }catch (Exception e) {
                // TODO: Do something when cannot played...
            }
            return null;
        }
    }

    /*class loadData extends AsyncTask<Void,Void,Void> {

        Context context;

        public loadData(Context context) {
            this.context = context;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Glide.with(context)
                    .asBitmap()
                    .load(MusicService.imgUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Palette.generateAsync(resource, palette -> {
                                MusicService.nColor = palette.getVibrantColor(context.getResources().getColor(R.color.light_white));
                                MusicService.bitmapIcon = resource;
                                Log.e(TAG, "loadData: Changing nColor: "+MusicService.nColor +
                                        ", ImageUri: "+MusicService.imgUrl );
                                MusicService.rebuildNotification();
                                try {
                                    PlayerActivity2.loadAgain();
                                }catch (Exception ignored) {

                                }
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
            super.onPostExecute(aVoid);
        }

        String jsonResponse(String videoID, int apinumber) {
            HttpHandler httpHandler = new HttpHandler();
            String link = "https://www.googleapis.com/youtube/v3/videos?id=" + videoID + "&key=" + API_KEYS[apinumber] + "&part=statistics";
            return httpHandler.makeServiceCall(link);
        }

        SoundCloud soundCloud;
        @Override
        protected Void doInBackground(Void... voids) {
            String videoID = MusicService.videoID;

            if (videoID.contains("soundcloud.com")) {
                soundCloud = new SoundCloud(videoID);
                if (soundCloud.getModel()==null || soundCloud.getModel().getStreamUrl()==null) {
                    return null;
                }
                // soundCloud.captureViews();
                MusicService.soundCloudPlayBack=true;
                MusicService.videoTitle = soundCloud.getModel().getTitle();
                MusicService.channelTitle = soundCloud.getModel().getAuthorName();
                MusicService.imgUrl = soundCloud.getModel().getImageUrl();
                MusicService.likeCounts = -1; MusicService.dislikeCounts = -1;
                MusicService.viewCounts = "-1";
                return null;
            }

            int i=0;
            int apiLength = API_KEYS.length;
            String json;
            do {
                json = jsonResponse(videoID, i);
                i++;
            }while (json.contains("\"error\":") && i<apiLength);
            YTMeta ytMeta = new YTMeta(videoID);
            if (ytMeta.getVideMeta() != null) {
                MusicService.channelTitle = YTutils.getChannelTitle(ytMeta.getVideMeta().getTitle(),
                        ytMeta.getVideMeta().getAuthor());
                MusicService.videoTitle = YTutils.setVideoTitle(ytMeta.getVideMeta().getTitle());
                MusicService.imgUrl = ytMeta.getVideMeta().getImgUrl();
            }


            if (json.contains("\"error\":")) {
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
                    MusicService.viewCounts = YTutils.getViewCount(Long.parseLong(statistics.getString("viewCount")));
                    MusicService.likeCounts = 100;
                    MusicService.dislikeCounts = 0;
                    try {
                        MusicService.likeCounts = Integer.parseInt(statistics.getString("likeCount"));
                        MusicService.dislikeCounts = Integer.parseInt(statistics.getString("dislikeCount"));
                    }catch (Exception e){e.printStackTrace();}

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }*/
}
