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

import android.os.AsyncTask;
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
import java.util.ArrayList;

public class YTSearch {

    ArrayList<String> videoIDs; String TAG = "YTSearchThread";

    public YTSearch(String query) {

        String tosearch = query.replace(" ","+")
                .replace("&#039;","'")
                .replace("&#038;","%26")
                .replace(",","%2");

        videoIDs = new ArrayList<>();
        try {
            String url = "https://www.youtube.com/results?search_query="+tosearch;
            URLConnection connection = (new URL(url)).openConnection();
            connection.connect();
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            StringBuilder builder = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null)
            {
                builder.append(line).append("\n");
                if (line.contains("src=\"https://i.ytimg.com"))
                {
                    videoIDs.add(line.split("/")[4]);
                }
            }
          //  YTutils.Write(builder.toString(),YTutils.getFile("Documents/search.txt"));
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
        return videoIDs;
    }
}
