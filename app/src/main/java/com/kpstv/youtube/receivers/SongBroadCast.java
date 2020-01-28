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
import com.kpstv.youtube.services.IntentDownloadService;
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
                MainActivity.playNext();
                }catch (Exception ignored){disableContext(context);}
                break;
            case "com.kpstv.youtube.ACTION_PREVIOUS":
                try {
                MainActivity.playPrevious();
                }catch (Exception ignored){disableContext(context);}
                break;
            case "com.kpstv.youtube.ACTION_PLAY":
                try {
                MainActivity.changePlayBack(!MainActivity.isplaying);
                try {
                    if (MainActivity.isplaying) {
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
                try {
                    Log.e(TAG, "onReceive: triggered" );
                    Log.e(TAG, "onReceive: "+intent.getData().toString());
                    Uri uri = intent.getData();
                    Log.e(TAG, "onReceive: "+uri.toString() );
                    File f = new File(uri.getPath());
                    if (!f.exists()) {
                        f = new File(uri.toString().replace(".mp3",".m4a"));
                    }

                    if (f.exists()) {

                        if (uri.toString().contains(".mp4")) {
                            Intent i = new Intent();
                            i.setAction(Intent.ACTION_VIEW);
                            i.setDataAndType(uri, "video/*");
                            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(i);
                            return;
                        }

                        File downloads = YTutils.getFile(Environment.DIRECTORY_DOWNLOADS);
                        File[] files = downloads.listFiles(new FileFilter() {
                            @Override
                            public boolean accept(File file)
                            {
                                return (file.getPath().endsWith(".mp3")||file.getPath().endsWith(".m4a")
                                        ||file.getPath().endsWith(".wav")||file.getPath().endsWith(".aac")
                                        ||file.getPath().endsWith(".ogg")||file.getPath().endsWith(".flac"));
                            }
                        });
                        if (files.length>0) {
                            String[] ids = new String[files.length];
                            int position = 0;
                            for (int i=0;i<files.length;i++) {
                                File id = files[i];
                                ids[i] = files[i].getPath();
                                if (id.getPath().equals(f.getPath())) {
                                    position=0;
                                }
                            }
                            MainActivity.PlayVideo_Local(ids,position);
                        }else
                            Toast.makeText(context, "There seems to be an error in parsing downloads!", Toast.LENGTH_SHORT).show();

                    }else Toast.makeText(context, "Error: File not found!", Toast.LENGTH_SHORT).show();

                   /* String mime = context.getContentResolver().getType(uri);

                    // Open file with user selected app
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_VIEW);
                    i.setDataAndType(uri, "audio/*");
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                   // ContextCompat.startActivity(context,i,null);*/
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case "com.kpstv.youtube.FAVOURITE_SONG":
                try {
                    MainActivity.actionFavouriteClicked();
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
            Palette.generateAsync(MainActivity.bitmapIcon, palette -> {
                MainActivity.nColor = palette.getVibrantColor(context.getResources().getColor(R.color.light_white));
                Log.e(TAG, "loadData_OFFLINE Changing nColor: "+MainActivity.nColor +
                        ", ImageUri: "+MainActivity.imgUrl);
                MainActivity.rebuildNotification();
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
            File f = new File(MainActivity.videoID);
            Uri uri = Uri.fromFile(f);
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(context,uri);
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                byte [] data = mmr.getEmbeddedPicture();

                if(data != null)
                    MainActivity.bitmapIcon = BitmapFactory.decodeByteArray(data, 0, data.length);
                else
                    MainActivity.bitmapIcon = YTutils.drawableToBitmap(ContextCompat.getDrawable(context,R.drawable.ic_pulse));

                if (artist==null) artist ="Unknown artist";
                if (title==null) title = YTutils.getVideoTitle(f.getName());

                if (title.contains("."))
                    title = title.split("\\.")[0];

                MainActivity.videoTitle = title;
                MainActivity.channelTitle = artist;
                MainActivity.likeCounts = -1; MainActivity.dislikeCounts = -1;
                MainActivity.viewCounts = "-1";

                MainActivity.total_seconds = Integer.parseInt(durationStr);

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
                    .load(MainActivity.imgUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Palette.generateAsync(resource, palette -> {
                                MainActivity.nColor = palette.getVibrantColor(context.getResources().getColor(R.color.light_white));
                                MainActivity.bitmapIcon = resource;
                                Log.e(TAG, "loadData: Changing nColor: "+MainActivity.nColor +
                                        ", ImageUri: "+MainActivity.imgUrl );
                                MainActivity.rebuildNotification();
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
            String videoID = MainActivity.videoID;

            if (videoID.contains("soundcloud.com")) {
                soundCloud = new SoundCloud(videoID);
                if (soundCloud.getModel()==null || soundCloud.getModel().getStreamUrl()==null) {
                    return null;
                }
                // soundCloud.captureViews();
                MainActivity.soundCloudPlayBack=true;
                MainActivity.videoTitle = soundCloud.getModel().getTitle();
                MainActivity.channelTitle = soundCloud.getModel().getAuthorName();
                MainActivity.imgUrl = soundCloud.getModel().getImageUrl();
                MainActivity.likeCounts = -1; MainActivity.dislikeCounts = -1;
                MainActivity.viewCounts = "-1";
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
                MainActivity.channelTitle = YTutils.getChannelTitle(ytMeta.getVideMeta().getTitle(),
                        ytMeta.getVideMeta().getAuthor());
                MainActivity.videoTitle = YTutils.setVideoTitle(ytMeta.getVideMeta().getTitle());
                MainActivity.imgUrl = ytMeta.getVideMeta().getImgUrl();
            }


            if (json.contains("\"error\":")) {
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
                    MainActivity.viewCounts = YTutils.getViewCount(Long.parseLong(statistics.getString("viewCount")));
                    MainActivity.likeCounts = 100;
                    MainActivity.dislikeCounts = 0;
                    try {
                        MainActivity.likeCounts = Integer.parseInt(statistics.getString("likeCount"));
                        MainActivity.dislikeCounts = Integer.parseInt(statistics.getString("dislikeCount"));
                    }catch (Exception e){e.printStackTrace();}

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }*/
}
