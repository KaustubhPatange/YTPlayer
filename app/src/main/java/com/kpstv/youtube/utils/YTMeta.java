/**
 * The api is created by KP for YTPlayer app.
 * https://github.com/KaustubhPatange/YTPlayer/
 *
 * If you want to use it in your project use it without changing the name
 * or this header info.
 *
 * Support the work!
 *
 **/

package com.kpstv.youtube.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.util.Log;

import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.models.MetaModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class YTMeta {

    MetaModel model;

    public YTMeta(MetaModel model) {
        this.model = model;
    }

    public YTMeta(String videoID) {
        load(MainActivity.activity,videoID);
    }

    public YTMeta(Context context, String videoID) {
       load(context, videoID);
    }

    public void load(Context context, String videoID) {
        if (videoID==null) return;
        if (videoID.contains("soundcloud.com")) {
            SoundCloud soundCloud = new SoundCloud(videoID);
            if (soundCloud.getModel()!=null) {
                model = new MetaModel(
                        videoID,
                        soundCloud.getModel().getTitle(),
                        soundCloud.getModel().getAuthorName(),
                        soundCloud.getModel().getImageUrl()
                );
                model.setVideoID(videoID);
            }
            return;
        }
        try {
            HttpHandler handler = new HttpHandler();
            String json = handler.makeServiceCall(
                    "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v="+videoID+"&format=json");
            if (json==null) {
                Log.e("YTMetaResponse","Null response on: "+videoID);
                return;
            }

            try {
                JSONObject object = new JSONObject(json);
                model = new MetaModel(
                        videoID,
                        object.getString("title"),
                        object.getString("author_name"),
                        YTutils.getImageUrlID(context,videoID)
                );
                model.setVideoID(videoID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MetaModel getVideMeta() {
        return model;
    }

}
