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

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyPlaylist {
    static String TAG = "SpotifyPlaylist_api";
    ArrayList<String> spotifyUrls = new ArrayList<>();

    public ArrayList<String> getSpotifyUrls() {
        return spotifyUrls;
    }

    public SpotifyPlaylist(String id) {
        try {
            String url = "https://open.spotify.com/playlist/"+id+"?nd=1";
            URL uri = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)uri.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent","Opera/9.80 (X11; Linux x86_64) Presto/2.12.388 Version/12.11");
            InputStream in = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while((line = reader.readLine()) != null)
            {
              if (line.contains("\"music:song\"")) {
                  Pattern p = Pattern.compile("<meta property=\"music:song\" content=\"(.*?)\"");
                  Matcher matcher = p.matcher(line);
                  while (matcher.find()) {
                      String spotifyurl = matcher.group(0).split("\"")[3];
                      spotifyUrls.add(spotifyurl);
                  }
              }
            }
            in.close();

        } catch (MalformedURLException e) {
            Log.e(TAG, "MalformedURLException: " + e.getMessage());
        } catch (ProtocolException e) {
            Log.e(TAG, "ProtocolException: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }
}
