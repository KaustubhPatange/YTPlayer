package com.kpstv.youtube.utils;

import android.util.Log;

import com.kpstv.youtube.AppInterface;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

public class ArtistImage implements AppInterface {
    String query;
    private static final String TAG = "ArtistImage";
    String imageLocation = null;

    public ArtistImage(String query) {
        this.query = query;

        int i=0;
        int apiLength = API_KEYS.length;
        String json;
        do {
            json = jsonResponse(i);
            i++;
        }while (json.contains("\"error\":") && i<apiLength);

        if (!json.contains("\"error\":")) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONArray array = jsonObject.getJSONArray("items");
                imageLocation = array.getJSONObject(0).getJSONObject("snippet")
                        .getJSONObject("thumbnails").getJSONObject("medium").getString("url");
                Log.e(TAG, "ArtistImage: "+imageLocation);
            }catch (Exception e) {}
        }
    }

    String jsonResponse(int apinumber) {
        HttpHandler httpHandler = new HttpHandler();
        String link = "https://www.googleapis.com/youtube/v3/search?part=snippet&q="
                + URLEncoder.encode(query) +"&type=channel&key="+API_KEYS[apinumber];
        return httpHandler.makeServiceCall(link);
    }
}
