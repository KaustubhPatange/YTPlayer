package com.kpstv.youtube.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.kpstv.youtube.models.MetaModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class YTMeta {

    MetaModel model;

    public YTMeta(String videoID) {
        try {
            HttpHandler handler = new HttpHandler();
            String json = handler.makeServiceCall(
                    "https://www.youtube.com/oembed?url=https://www.youtube.com/watch?v="+videoID+"&format=json");

            try {
                JSONObject object = new JSONObject(json);
                model = new MetaModel(
                        object.getString("title"),
                        object.getString("author_name"),
                        "https://i.ytimg.com/vi/"+videoID+"/mqdefault.jpg"
                );
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
