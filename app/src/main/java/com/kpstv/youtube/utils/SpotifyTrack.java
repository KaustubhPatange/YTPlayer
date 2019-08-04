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
import android.widget.ImageView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyTrack {

    private String Title, Author, ImageUrl, YtUrl;

    public SpotifyTrack(String title,String author) {
        String search_text = title.replace(" ","+")
                + "+by+" + author.replace(" ","+");
        YTSearch ytSearch = new YTSearch(search_text);

        String videoId = ytSearch.getVideoIDs().get(0);
        YtUrl = "https://www.youtube.com/watch?v="+videoId;
        ImageUrl = "https://i.ytimg.com/vi/"+videoId+"/mqdefault.jpg";
        Title = title;
        Author = author;
    }

    public SpotifyTrack(String id) {
        String stringUrl = "https://open.spotify.com/track/"+id+"?nd=1";
        try {
            URL url = new URL(stringUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            try {
                boolean first=false, second=false;
                while ((line = reader.readLine()) != null) {
                    if (first&&second) {
                        String search_text = Title.replace(" ","+")
                                + "+by+" + Author.replace(" ","+");
                        YTSearch ytSearch = new YTSearch(search_text);
                        String videoId = ytSearch.getVideoIDs().get(0);
                        YtUrl = "https://www.youtube.com/watch?v="+videoId;
                        ImageUrl = "https://i.ytimg.com/vi/"+videoId+"/mqdefault.jpg";
                        return;
                    }
                    if (line.contains("\"og:title\"")) {
                        Title = parseString(line,"og:title");
                        first=true;
                    }
                    if (line.contains("\"music:musician\"")) {
                        String musicianUrl = parseString(line,"music:musician")+"?nd=1";
                        Author = getMusicianName(musicianUrl);
                        second=true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    String parseString(String line,String tag) {
        Matcher matcher1 = Pattern.compile(
                "<meta property=\""+tag+"\" content=\"(.*?)\">").matcher(line);
        if (matcher1.find()) {
            return matcher1.group().split("\"")[3];
        }
        return null;
    }

    String getMusicianName(String stringUrl) {
        try {
            URL url = new URL(stringUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    if (line.contains("\"og:title\"")) {
                        return parseString(line,"og:title");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getYtUrl() {
        return YtUrl;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public String getTitle() {
        return Title;
    }

    public String getAuthor() {
        return Author;
    }
}
