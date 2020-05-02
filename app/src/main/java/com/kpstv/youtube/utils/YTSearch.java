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

import android.graphics.PathEffect;
import android.os.AsyncTask;
import android.os.PatternMatcher;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YTSearch {

    ArrayList<String> videoIDs,channelImages,videoNewIDs; String TAG = "YTSearchThread";

    public YTSearch(String query) {

        videoIDs = new ArrayList<>();
        channelImages = new ArrayList<>();
        videoNewIDs = new ArrayList<>();
        try {
            String url = "https://www.youtube.com/results?search_query="+ URLEncoder.encode(query);
            URLConnection connection = (new URL(url)).openConnection();
            connection.connect();
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            StringBuilder builder = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null)
            {
                builder.append(line).append("\n");
                if (line.contains("src=\"//i.ytimg.com")) {
                    videoIDs.add(line.split("/")[4]);
                }
                if (line.contains("src=\"https://i.ytimg.com"))
                {
                    videoIDs.add(line.split("/")[4]);
                }
                if (line.contains("src=\"//yt3.ggpht.com") || line.contains("src=\"//lh3.googleusercontent.com")) {
                    String link = "https:"+line.replace("<img src=\"","").trim()
                            .replace("\">","");
                    channelImages.add(link);
                }
                if (line.contains("//i.ytimg.com/vi")) {
                    Pattern compile = Pattern.compile("//i.ytimg.com/vi/.*?/", Pattern.DOTALL);
                    Matcher matcher = compile.matcher(line);
                    if (matcher.find()) {
                        String videoId = matcher.group().split("/")[4];
                        Log.e(TAG, "YTSearch: VideoId: "+ videoId );
                        videoNewIDs.add(videoId);
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

    public ArrayList<String> getVideoIDs() {
        if (videoIDs.size() > 0)
        return videoIDs;
        else if (videoNewIDs.size() > 0)
            return videoNewIDs;
        else if (channelImages.size() > 0)
            return channelImages;
        else return new ArrayList<>();
    }
}
