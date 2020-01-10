package com.kpstv.youtube.utils;

import android.util.Log;

import com.kpstv.youtube.AppInterface;

public class APIResponse implements AppInterface {
    String url;
    String json;
    private static final String TAG = "APIResponse";
    public APIResponse(String url) {
        this.url = url;
        int i=0;
        int apiLength = API_KEYS.length;
        String json;
        do {
            json = jsonResponse(url, i);
            i++;
        }while (json.contains("\"error\"") && i<apiLength);
        if (json.contains("\"error\"")) {
            this.json= null;
        }
        this.json = json;
    }


    public String getJson() {
        return json;
    }

    String jsonResponse(String url, int apinumber) {
        HttpHandler httpHandler = new HttpHandler();
        String link = url + "&key=" + API_KEYS[apinumber];
        Log.e(TAG, "jsonResponse: "+link );
        String json=null;
        try {
            json = httpHandler.makeServiceCall(link);
        }catch (Exception e){}
        if (json==null) return "\"error\"";
        return json;
    }
}
