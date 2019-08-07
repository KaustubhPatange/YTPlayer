package com.kpstv.youtube.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.widget.FrameLayout;

import com.kpstv.youtube.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.content.Context.MODE_PRIVATE;

public class YTutils {

    public static String[] ConvertToStringArray(ArrayList<String> list) {
        final String[] arrays = new String[list.size()];
        for(int i=0;i<list.size();i++)
            arrays[i]=list.get(i);
        return arrays;
    }

    public static String getImageUrl(String YtUrl) {
        return "https://i.ytimg.com/vi/"+getVideoID(YtUrl)+"/mqdefault.jpg";
    }

    public static String getYtUrl(String videoID) {
        return "https://www.youtube.com/watch?v="+videoID;
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static void Write(String string,File file) {
        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.write(string);
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("writeToSDFile2", "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e("writeToSDFile3","\n\nFile written to "+file);
    }

    public static File getFile(String targetfolder) {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/" + targetfolder);
        return dir;
    }

    public static void CreateDir(String targetfolder) {
        File root = android.os.Environment.getExternalStorageDirectory();
        Log.e("CreateDir",targetfolder);
        File dir = new File (root.getAbsolutePath() + "/" + targetfolder);
        dir.mkdirs();
    }

    public static void writeContent(Activity activity, String FILE_NAME, String content) {
        FileOutputStream fos = null;

        try {
            fos = activity.openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String readContent(Activity activity, String FILE_NAME) {
        FileInputStream fis = null;
        StringBuilder sb = new StringBuilder();
        try {
            fis = activity.openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String text;

            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
       return null;
    }


    public static String getSpotifyID(String spotifyurl) {
        // https://open.spotify.com/track/4hPpVbbakQNv8YTHYaOJP4
        String url;
        if (spotifyurl.contains("open.spotify")){
            url = spotifyurl.split("/")[4];
            if (url.contains("?"))
                return url.split("\\?")[0];
            else return url;
        }
        return null;
    }

    public static String getViewCount(int number) {
        if (number > 1000000000) { // billion
            return number/1000000000+"B";
        } else if (number > 1000000) { // million
            return number/1000000 +"M";
        }else if (number > 1000) {
            return number/1000 +"K";
        }
        return number+"";
    }

    public static int progressToTimer(int progress, long totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    public static boolean isValidID(String url) {
        return (url.contains("youtube.com")||url.contains("youtu.be"));
    }
    public static String convertArrayToStringMethod(String[] strArray) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < strArray.length; i++) {
            stringBuilder.append(strArray[i]).append("\n");
        }
        return stringBuilder.toString();
    }
    public static String convertListToStringMethod(List<String> strArray) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < strArray.size(); i++) {
            stringBuilder.append(strArray.get(i)).append("\n");
        }
        return stringBuilder.toString();
    }
    public static String getTodayDate() {
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        return df.format(c);
    }

    public static String getYesterdayDate() {
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("dd").format(c);
        @SuppressLint("SimpleDateFormat") String month =  new SimpleDateFormat("MM").format(c);
        @SuppressLint("SimpleDateFormat") String year =  new SimpleDateFormat("yyyy").format(c);
        return  (Integer.parseInt(date)-1)+"-"+month+"-"+year;
    }

    public static String getVideoID(String youtube_url) {
        if (youtube_url.contains("youtube.com")) {
            return youtube_url.split("=")[1];
        }else if (youtube_url.contains("youtu.be")) {
         return youtube_url.replace("https://youtu.be/","");
        }
        return null;
    }

    public static void StartURL(String url, Context context)
    {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        CustomTabsIntent customTabsIntent = builder.build();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo("com.android.chrome",0);
            customTabsIntent.intent.setPackage("com.android.chrome");
            customTabsIntent.launchUrl(context, Uri.parse(url));
        } catch (Exception e) {
            Log.e("Chrome","Chrome not installed: "+e.getMessage());
            StartURLIntent(url,context);
        }
    }

    public static String getUrlVideoRTSP(String urlYoutube)
    {
        try
        {
            String gdy = "http://gdata.youtube.com/feeds/api/videos/";
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            String id = getVideoID(urlYoutube);
            URL url = new URL(gdy + id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            Document doc = documentBuilder.parse(connection.getInputStream());
            Element el = doc.getDocumentElement();
            NodeList list = el.getElementsByTagName("media:content");///media:content
            String cursor = urlYoutube;
            for (int i = 0; i < list.getLength(); i++)
            {
                Node node = list.item(i);
                if (node != null)
                {
                    NamedNodeMap nodeMap = node.getAttributes();
                    HashMap<String, String> maps = new HashMap<String, String>();
                    for (int j = 0; j < nodeMap.getLength(); j++)
                    {
                        Attr att = (Attr) nodeMap.item(j);
                        maps.put(att.getName(), att.getValue());
                    }
                    if (maps.containsKey("yt:format"))
                    {
                        String f = maps.get("yt:format");
                        if (maps.containsKey("url"))
                        {
                            cursor = maps.get("url");
                        }
                        if (f.equals("1"))
                            return cursor;
                    }
                }
            }
            return cursor;
        }
        catch (Exception ex)
        {
            Log.e("RTSP_Video", ex.toString());
        }
        return urlYoutube;

    }

    public static long TimertoMilliseconds(String timer) {
        String[] idls = timer.split(":");
        Log.e("IDLS_Count",idls.length+"");
        if (idls.length>2) {
            // It contains hour...
            // 3:02:11
            long hour = Long.parseLong(idls[0])*60*60*1000;
            long min = Long.parseLong(idls[1])*60*1000;
            long sec =  Long.parseLong(idls[2])*1000;
            return hour+min+sec;
        }else {
            // It is only minutes...
            //3:04
            long min = Long.parseLong(idls[0])*60*1000;
            long sec =  Long.parseLong(idls[1])*1000;
            return min+sec;
        }
    }

    public static void setBackroundTint(FrameLayout frameLayout, int color) {
        Drawable buttonDrawable = frameLayout.getBackground();
        buttonDrawable = DrawableCompat.wrap(buttonDrawable);
        DrawableCompat.setTint(buttonDrawable, color);
        frameLayout.setBackground(buttonDrawable);
    }

    public static int getRandomColor(){
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }


    public static String milliSecondsToTimer(long milliseconds){
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int)( milliseconds / (1000*60*60));
        int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
        int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
        // Add hours if there
        if(hours > 0){
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if(seconds < 10){
            secondsString = "0" + seconds;
        }else{
            secondsString = "" + seconds;}

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    /**
     * Function to get Progress percentage
     * @param currentDuration
     * @param totalDuration
     * */
    public static int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }



    public static String getImageUri(String videoID) {
        String url = "https://www.googleapis.com/youtube/v3/videos?id="+videoID+"&key=AIzaSyBMqerRAATEnrsfPnWYfeqDdqX0TbR0bEo&part=snippet,contentDetails,statistics,status";
        HttpHandler sh = new HttpHandler();
        String json = sh.makeServiceCall(url);
        String imageuri=null;
        try {
            JSONObject snippets = new JSONObject(json).getJSONArray("items")
                    .getJSONObject(0).getJSONObject("snippet");
            imageuri = snippets.getJSONObject("thumbnails").getJSONObject("medium").getString("url");

        } catch (JSONException e) {
            Log.e("PlayerActivity_JSON",e.getMessage());
        }
        return imageuri;
    }

    public static String getDuration(int number) {
        String returnstring="";
        int no = number/1000;
        int mins = no/60;
        int seconds = no%60;
        if (mins>9)
            returnstring+=mins;
        else  returnstring+="0"+mins;
        if (seconds>9)
            returnstring+=":"+seconds;
        else  returnstring+=":0"+seconds;
        return returnstring;
    }

    public  static void StartURLIntent(String url,Context context) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }
}
