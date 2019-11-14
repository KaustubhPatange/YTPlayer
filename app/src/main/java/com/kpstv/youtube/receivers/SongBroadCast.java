package com.kpstv.youtube.receivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.PlayerActivity2;
import com.kpstv.youtube.R;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTStatistics;
import com.kpstv.youtube.utils.YTutils;

import org.json.JSONException;
import org.json.JSONObject;

public class SongBroadCast extends BroadcastReceiver {

    String[] apikeys = new String[]{"AIzaSyBYunDr6xBmBAgyQx7IW2qc770aoYBidLw", "AIzaSyBH8szUCt1ctKQabVeQuvWgowaKxHVjn8E"};

    AsyncTask<Void,Void,Void> setData;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action) {
            case "com.kpstv.youtube.ACTION_NEXT":
                MainActivity.playNext();
                if (setData!=null && setData.getStatus() == AsyncTask.Status.RUNNING)
                    setData.cancel(true);
                setData = new loadData(context);
                setData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case "com.kpstv.youtube.ACTION_PREVIOUS":
                MainActivity.playPrevious();
                if (setData!=null && setData.getStatus() == AsyncTask.Status.RUNNING)
                    setData.cancel(true);
                setData = new loadData(context);
                setData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case "com.kpstv.youtube.ACTION_PLAY":
                MainActivity.changePlayBack(!MainActivity.isplaying);
                break;
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
            String link = "https://www.googleapis.com/youtube/v3/videos?id=" + videoID + "&key=" + apikeys[apinumber] + "&part=statistics";
            return httpHandler.makeServiceCall(link);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String videoID = MainActivity.videoID;
            String json = jsonResponse(videoID, 0);

            YTMeta ytMeta = new YTMeta(videoID);
            if (ytMeta.getVideMeta() != null) {
                MainActivity.channelTitle = ytMeta.getVideMeta().getAuthor();
                MainActivity.videoTitle = YTutils.setVideoTitle(ytMeta.getVideMeta().getTitle());
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
                }
            }
            return null;
        }
    }
}
