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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YTLength {
    String TAG = "YTLength_Util";
    long seconds;

    public long getSeconds() {
        return seconds;
    }

    public YTLength(String videoID) {
        try {
            String url = "https://www.youtube.com/watch?v="+videoID;
            URLConnection connection = (new URL(url)).openConnection();
            connection.connect();
            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = null;
            while((line = reader.readLine()) != null)
            {
                Pattern mPattern = Pattern.compile(",\"lengthText\"(.*?)\\}");
                Matcher matcher = mPattern.matcher(line);
                if(matcher.find())
                {
                    String length = matcher.group();
                    length = length.split("\"")[7];
                    Log.e("FoundText",length+"");
                    seconds = YTutils.TimertoMilliseconds(length)/1000;
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
