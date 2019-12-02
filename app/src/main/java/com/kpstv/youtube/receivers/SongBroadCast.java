package com.kpstv.youtube.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.kpstv.youtube.AppSettings;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.PlayerActivity2;
import com.kpstv.youtube.R;
import com.kpstv.youtube.services.DownloadService;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTStatistics;
import com.kpstv.youtube.utils.YTutils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URLConnection;

public class SongBroadCast extends BroadcastReceiver implements AppSettings {

    AsyncTask<Void,Void,Void> setData;
    private static final String TAG = "SongBroadCast";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case "com.kpstv.youtube.ACTION_NEXT":
                MainActivity.playNext();
                if (setData!=null && setData.getStatus() == AsyncTask.Status.RUNNING)
                    setData.cancel(true);

                /** For local playback stuff */
                if (MainActivity.localPlayBack)
                    setData = new loadData_Offline(context);
                else
                    setData = new loadData(context);

                setData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case "com.kpstv.youtube.ACTION_PREVIOUS":
                MainActivity.playPrevious();
                if (setData!=null && setData.getStatus() == AsyncTask.Status.RUNNING)
                    setData.cancel(true);

                /** For local playback stuff */
                if (MainActivity.localPlayBack)
                    setData = new loadData_Offline(context);
                else
                    setData = new loadData(context);

                setData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case "com.kpstv.youtube.ACTION_PLAY":
                MainActivity.changePlayBack(!MainActivity.isplaying);
                break;
            case "com.kpstv.youtube.STOP_SERVICE":
                Log.e("SongBroadCast", "onReceive: STOP_SERVICE called" );
                Intent serviceIntent = new Intent(context, DownloadService.class);
                context.stopService(serviceIntent);
                break;
            case "com.kpstv.youtube.OPEN_SONG":
                Log.e(TAG, "onReceive: triggered" );
                //TODO: Change this OPEN_SONG reciever to in-built player if possible
                try {
                    Uri uri = Uri.parse(intent.getStringExtra("filePath"));
                    String mime = context.getContentResolver().getType(uri);

                    // Open file with user selected app
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_VIEW);
                    i.setDataAndType(uri, mime);
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                   // ContextCompat.startActivity(context,i,null);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                break;
            case "com.kpstv.youtube.OPEN_SHARE_SONG":
                try {
                    Uri uri = Uri.parse(intent.getStringExtra("filePath"));
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
        }
    }

    private String getMimeType(String url)
    {
        String parts[]=url.split("\\.");
        String extension=parts[parts.length-1];
        String type = null;
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    class loadData_Offline extends AsyncTask<Void,Void,Void> {
        Context context;

        public loadData_Offline(Context context) {
            this.context = context;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Palette.generateAsync(MainActivity.bitmapIcon, new Palette.PaletteAsyncListener() {
                public void onGenerated(Palette palette) {
                    MainActivity.nColor = palette.getVibrantColor(context.getResources().getColor(R.color.light_white));
                    MainActivity.rebuildNotification();
                    try {
                        PlayerActivity2.loadAgain();
                    }catch (Exception e) {
                        Log.e("PlayerActivity","is_still_null");
                    }
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

    class loadData extends AsyncTask<Void,Void,Void> {

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
                            Palette.generateAsync(resource, new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette palette) {
                                    MainActivity.nColor = palette.getVibrantColor(context.getResources().getColor(R.color.light_white));
                                    MainActivity.bitmapIcon = resource;
                                    MainActivity.rebuildNotification();
                                    try {
                                        PlayerActivity2.loadAgain();
                                    }catch (Exception e) {
                                        Log.e("PlayerActivity","is_still_null");
                                    }
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

        @Override
        protected Void doInBackground(Void... voids) {
            String videoID = MainActivity.videoID;

            int i=0;
            int apiLength = API_KEYS.length;
            String json;
            do {
                json = jsonResponse(videoID, i);
                i++;
            }while (json.contains("\"error\":") && i<apiLength);

            YTMeta ytMeta = new YTMeta(videoID);
            if (ytMeta.getVideMeta() != null) {
                MainActivity.channelTitle = ytMeta.getVideMeta().getAuthor();
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
    }
}
