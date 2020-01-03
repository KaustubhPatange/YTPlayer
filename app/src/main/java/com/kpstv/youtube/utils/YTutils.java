package com.kpstv.youtube.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.kpstv.youtube.AppInterface;
import com.kpstv.youtube.BuildConfig;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.PlayerActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.LocalModel;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class YTutils implements AppInterface {

    private static final String TAG = "YTutils";

    public static String[] ConvertToStringArray(ArrayList<String> list) {
        final String[] arrays = new String[list.size()];
        for(int i=0;i<list.size();i++)
            arrays[i]=list.get(i);
        return arrays;
    }

    public static String getImageUrl(String YtUrl) {
        String imageUrl = "https://i.ytimg.com/vi/"+getVideoID(YtUrl)+"/mqdefault.jpg";
        if (MainActivity.activity.getSharedPreferences("appSettings",MODE_PRIVATE)
                .getString("pref_image_quality","mq").equals("hq"))
            imageUrl = "https://i.ytimg.com/vi/"+getVideoID(YtUrl)+"/hqdefault.jpg";
        return imageUrl;
    }
    public static String getImageUrlID(String videoID) {
        String imageUrl = "https://i.ytimg.com/vi/"+videoID+"/mqdefault.jpg";
        if (MainActivity.activity.getSharedPreferences("appSettings",MODE_PRIVATE)
                .getString("pref_image_quality","mq").equals("hq"))
            imageUrl = "https://i.ytimg.com/vi/"+videoID+"/hqdefault.jpg";

        return imageUrl;
    }
    /*public static String getImageUrlID(Context context,String videoID) {
        String imageUrl = "https://i.ytimg.com/vi/"+videoID+"/mqdefault.jpg";
        if (context.getSharedPreferences("pref_image_quality",MODE_PRIVATE)
                .getString("pref_image_quality","mq").equals("hq"))
            imageUrl = "https://i.ytimg.com/vi/"+videoID+"/hqdefault.jpg";

        Log.e(TAG, "getImageUrlID: Context ImageUrl: "+imageUrl );

        return imageUrl;
    }*/

    public static String getYtUrl(String videoID) {
        if (videoID.contains("soundcloud.com"))
            return videoID;
        return "https://www.youtube.com/watch?v="+videoID;
    }

    public static void showAlert(Activity activity,String title, String message, boolean isalert) {
        int icon = android.R.drawable.ic_dialog_info;
        if (isalert) icon = android.R.drawable.ic_dialog_alert;
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       dialog.dismiss();
                    }
                })
                .setIcon(icon)
                .show();

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

    public static void CreateFile(File file) {
        /*File root = android.os.Environment.getExternalStorageDirectory();
        Log.e("CreateDir",targetfolder);*/
        File dir = file;
        try {
            dir.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeContent(Activity activity, String FILE_NAME, String content) {
        FileOutputStream fos = null;

        try {
            if (FILE_NAME.contains("/")) {
                fos = new FileOutputStream(new File(FILE_NAME));
            }else
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

    public static void addToPlayList(Activity activity,String videoID, String videoTitle, String channelTitle,
                                     String imageUrl, long seconds) {
        String playlist_csv = YTutils.readContent(activity,"playlist.csv");
        if (playlist_csv==null||playlist_csv.isEmpty()) {
            Toast.makeText(activity, "No playlist found!", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<String> configs = new ArrayList<>();

        String[] allPlaylist = playlist_csv.split("\n|\r");

        for(String line : allPlaylist) {
            String[] playlist = line.split(",");
            configs.add(playlist[1]);
        }
        final String[] arrays = new String[configs.size()];
        for(int i=0;i<configs.size();i++) {
            arrays[i]=configs.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Add to playlist");

        builder.setItems(arrays, (dialog, which) -> {
            boolean alreadPresent=false,added=false;
            String name = configs.get(which);
            for (int i=0;i<configs.size();i++) {
                if (allPlaylist[i].contains(","+name) && !allPlaylist[i].contains(videoID+"|")) {
                    allPlaylist[i]+=","+videoID+"|"+seconds+"|"+videoTitle+"|"+channelTitle+"|"+imageUrl;
                    writeContent(activity,"playlist.csv",
                            join(allPlaylist,'\n'));
                    added=true;
                    Toast.makeText(activity, "Added to playlist", Toast.LENGTH_SHORT).show();
                }else alreadPresent=true;
            }
            if (alreadPresent&&!added)
                Toast.makeText(activity, "Already exist in playlist", Toast.LENGTH_SHORT).show();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public static String setVideoTitle(String title) {
        Log.e("VideoTitle",title);
        String t = title;
        if (t.contains("-")) {
            t = t.split("\\-")[1].trim();
            MainActivity.channelTitle = title.split("-")[0];
        } // (Mark orision)
        if (t.contains("(")) {
            t = t.split("\\(")[0].trim();
        }
        if (t.contains("["))
            t = t.split("\\[")[0].trim();
        return t;
    }
    public static String getVideoTitle(String title) {
        String t = title;
        if (t.contains("&#39;"))
            t = t.replace("&#39;","'");
        if (t.contains("-")) {
            if (t.split("\\-").length<3)
                t = t.split("\\-")[1].trim();
        }
        if (t.contains("(")) {
            t = t.split("\\(")[0].trim();
        }
        if (t.contains("["))
            t = t.split("\\[")[0].trim();
        return t.trim();
    }

    public static void setDefaultRingtone(Activity activity, File f) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean retVal = Settings.System.canWrite(activity);
            if(!retVal){
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
                builder.setTitle("Set ringtone");
                builder.setMessage("Allow YTPlayer to modify audio settings");
                builder.setNegativeButton("Cancel",null);
                builder.setPositiveButton("OK",(dialogInterface, i) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setData(Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                });
                android.app.AlertDialog dialog = builder.create();
                dialog.show();
                return;
            }
        }
        Toast.makeText(activity, "New ringtone set!", Toast.LENGTH_SHORT).show();
        setRingtone(activity,f);
    }

    public static String getRedirectAppUrl(String urlOriginal) {
        if (StringUtils.isEmpty(urlOriginal)) {
            return null;
        }
        URL u = null;
        try {
            u = new URL(urlOriginal);
            HttpURLConnection huc = (HttpURLConnection) u.openConnection();
            huc.setInstanceFollowRedirects(false);
            huc.setReadTimeout(5000);
            boolean redirect = false;
            int status = huc.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {
                    redirect = true;
                }
            }
            if (redirect) {
                String newUrl = huc.getHeaderField("Location");
                return newUrl;
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    static void setRingtone(Activity activity,File f) {
        try {
            RingtoneManager.setActualDefaultRingtoneUri(activity, RingtoneManager.TYPE_RINGTONE, Uri.fromFile(f));
        } catch (Exception t) {
            t.printStackTrace();

        }
    }

    public static String getChannelTitle(String title, String defaulttitle) {
        if (title.contains("&#39;"))
            title = title.replace("&#39;","'");
        if (defaulttitle.contains("-"))
            defaulttitle = defaulttitle.split("-")[0];
        if (title.contains("-")) {
            String t = title;
            if (t.split("\\-").length<3)
                t = t.split("\\-")[0].trim();
            return t.trim();
        }else
        return defaulttitle;
    }

    public static void Vibrate(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(100);
        }
    }

    public static ArrayList<String> convertArrayToArrayList(String[] arr) {
        ArrayList<String> l = new ArrayList<>(Arrays.asList(arr));
        return l;
    }

    public static String readContent(Context activity, String FILE_NAME) {
        FileInputStream fis = null;
        StringBuilder sb = new StringBuilder();
        try {
            if (FILE_NAME.contains("/")) {
                File f = new File(FILE_NAME);
                if (!f.exists()) return null;
                fis = new FileInputStream(f);
            }else{
                File f = new File(activity.getFilesDir(),FILE_NAME);
                if (!f.exists()) return null;
                fis = activity.openFileInput(FILE_NAME);
            }
            if (fis==null) return null;
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

    public static String dividePattern(float num1, float num2, String pattern) {
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(num1/num2)+"";
    }

    public static String getSize(long size) {

        DecimalFormat df = new DecimalFormat("0.00");

        float sizeKb = 1024.0f;
        float sizeMb = sizeKb * sizeKb;
        float sizeGb = sizeMb * sizeKb;
        float sizeTerra = sizeGb * sizeKb;

        if(size < sizeMb)
            return df.format(size / sizeKb)+ " KB";
        else if(size < sizeGb)
            return df.format(size / sizeMb) + " MB";
        else if(size < sizeTerra)
            return df.format(size / sizeGb) + " GB";

        return "";
    }

    public static String getSizeNoDecimal(long size) {

        DecimalFormat df = new DecimalFormat("0");

        float sizeKb = 1024.0f;
        float sizeMb = sizeKb * sizeKb;
        float sizeGb = sizeMb * sizeKb;
        float sizeTerra = sizeGb * sizeKb;

        if(size < sizeMb)
            return df.format(size / sizeKb)+ " KB";
        else if(size < sizeGb)
            return df.format(size / sizeMb) + " MB";
        else if(size < sizeTerra)
            return df.format(size / sizeGb) + " GB";

        return "";
    }

    public static String getSize_withoutPostFix(long size) {

        DecimalFormat df = new DecimalFormat("0.00");

        float sizeKb = 1024.0f;
        float sizeMb = sizeKb * sizeKb;
        float sizeGb = sizeMb * sizeKb;
        float sizeTerra = sizeGb * sizeKb;

        if(size < sizeMb)
            return df.format(size / sizeKb);
        else if(size < sizeGb)
            return df.format(size / sizeMb);
        else if(size < sizeTerra)
            return df.format(size / sizeGb);

        return "";
    }

    public static String getViewCount(long number) {
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
    public static String[] convertListToArrayMethod(ArrayList<String> strArray) {
        String[] arr = new String[strArray.size()];
        arr = strArray.toArray(arr);
        return arr;
    }
    public static String getTodayDate() {
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        return df.format(c);
    }
    public static String getDate(Date c) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        return df.format(c);
    }
    public static String getTodayDate_nogaps() {
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        return df.format(c);
    }
    public static String getTomorrowDate_nogap() {
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("dd").format(c);
        @SuppressLint("SimpleDateFormat") String month =  new SimpleDateFormat("MM").format(c);
        @SuppressLint("SimpleDateFormat") String year =  new SimpleDateFormat("yyyy").format(c);
        return  year+""+month+(Integer.parseInt(date)+1);
    }
    public static String getYesterday_nogap() {
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("dd").format(c);
        @SuppressLint("SimpleDateFormat") String month =  new SimpleDateFormat("MM").format(c);
        @SuppressLint("SimpleDateFormat") String year =  new SimpleDateFormat("yyyy").format(c);
        return  year+""+month+(Integer.parseInt(date)-1);
    }



    public static String getLocalArtworkImage(Activity activity, LocalModel model) {
        String data = YTutils.readContent(activity,"artistImages.csv");
        String imageUri=null;
        if (data!=null && !data.isEmpty()) {
            if (data.contains(model.getTitle().trim()+"$")) {
                String[] items = data.split("\n|\r");
                for (String item : items) {
                    if (item.isEmpty()) continue;
                    if (item.contains(model.getTitle().trim()+"$")) {
                        imageUri = item.split("\\$")[1];
                        return imageUri;
                    }
                }
            }else {
                ArtistImage artistImage = new ArtistImage(model.getTitle().trim());
                YTutils.writeContent(activity,"artistImages.csv",data+
                        model.getTitle().trim()+"$"+imageUri);
                imageUri = artistImage.getImageUri();
            }
        }else {
            ArtistImage artistImage = new ArtistImage(model.getTitle().trim());
            YTutils.writeContent(activity,"artistImages.csv",model.getTitle().trim()+"$"+imageUri);
            imageUri = artistImage.getImageUri();
        }
        return imageUri;
    }

    public static String getAudioFormat(File f) {
        String ext =  f.getPath().substring(f.getPath().lastIndexOf("."),f.getPath().length());
        switch (ext) {
            case ".mp3":
                return "MPEG-1 Audio Layer 3";
            case ".m4a":
            case ".ogg":
            case ".aac":
                return "HE-AAC";
        }
        return null;
    }

    public static Bitmap getArtworkFromFile(Activity activity, File f) {
       try {
           MediaMetadataRetriever mmr = new MediaMetadataRetriever();
           mmr.setDataSource(activity, Uri.fromFile(f));

           byte[] data = mmr.getEmbeddedPicture();

           if (data!=null) {
               return BitmapFactory.decodeByteArray(data, 0, data.length);
           }
       }catch (Exception e){e.getMessage();}
       return null;
    }

    /*public static String getArtistImage(String query) {
        // https://www.googleapis.com/youtube/v3/search?part=snippet&q=Ashley%20O&type=channel&key=[YOUR_API_KEY]
        HttpHandler handler
    }*/

    public static byte[] getByteFromBitmap(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bmp.recycle();
        return byteArray;
    }

    public static void shareFile(Activity context, File f) {
        try {
            Uri uri = Uri.fromFile(f);
            ShareCompat.IntentBuilder.from(context)
                    .setStream(uri)
                    .setType(URLConnection.guessContentTypeFromName(f.getName()))
                    .startChooser();
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(context, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static String getTodayDate_Time() {
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("ddMMyyyy_HHmmss");
        return df.format(c);
    }

    public static String getYesterdayDate() {
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("dd").format(c);
        @SuppressLint("SimpleDateFormat") String month =  new SimpleDateFormat("MM").format(c);
        @SuppressLint("SimpleDateFormat") String year =  new SimpleDateFormat("yyyy").format(c);
        return  (Integer.parseInt(date)-1)+"-"+month+"-"+year;
    }

    public static String getVideoID_ImageUri(String imageUrl) {
        // "https://i.ytimg.com/vi/"+videoID+"/mqdefault.jpg";
        String t = imageUrl;

        t = t.split("/")[4];

        return t;
    }

    public static String getVideoID(String youtube_url) {
        String t = youtube_url;
        if (t.contains("soundcloud.com"))
            return t;
        if (youtube_url.contains("youtube.com")) {
            t = youtube_url.split("=")[1];
        }else if (youtube_url.contains("youtu.be")) {
         t = youtube_url.replace("https://youtu.be/","");
        }
        if (t.contains("&"))
            t = t.split("&")[0];
        return t;
    }

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
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

    public static Spanned getHtml(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT);
        } else {
            return Html.fromHtml(text);
        }
    }

    public static int getRandomColor(){
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    public static class CheckForUpdates extends AsyncTask<Void,Void,Void> {
        boolean isAutomatic;
        String json;
        @SuppressLint("StaticFieldLeak")
        Context context;
        String updateName;
        long downloadID;


        public CheckForUpdates(Activity context, boolean isAutomatic) {
            this.context = context;
            this.isAutomatic = isAutomatic;

            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Dexter.withActivity(context)
                            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(new PermissionListener() {
                                @Override public void onPermissionGranted(PermissionGrantedResponse response) { }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    return;
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                    return;
                                }

                            }).check();

                }
            }
        }

        void runInstall(String updateName) {
            Intent install = new Intent(Intent.ACTION_VIEW)
                    .setDataAndType(Uri.fromFile(getFile(Environment.DIRECTORY_DOWNLOADS+"/"+updateName)),
                            "application/vnd.android.package-archive");
            install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(install);
        }

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadID == id) {
                    Log.e("FileDownloadLink",Uri.fromFile(getFile(Environment.DIRECTORY_DOWNLOADS+"/"+updateName)).toString());

                    if (getFile(Environment.DIRECTORY_DOWNLOADS+"/"+updateName).exists())
                    {
                        runInstall(updateName);
                    }
                    else
                        Toast.makeText(ctxt, "There is a problem with update package!", Toast.LENGTH_SHORT).show();

                    context.unregisterReceiver(this);
                }
            }
        };

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (json==null||json.isEmpty())
                return;
            try {
                JSONObject object = new JSONObject(json);
                String changelogHtml = object.getString("changelog");
                String downloadUri = object.getString("download");
                int newVer = Integer.parseInt(object.getString("new")
                        .replace(".",""));
                updateName = "YTPlayer_v"+newVer+".apk";
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                int curVer = Integer.parseInt(pInfo.versionName.replace(".",""));
                Log.e("VersionLOG","NewVersion: "+newVer+", currVersion: "+curVer);
                if (newVer>curVer) {
                    new AlertDialog.Builder(context)
                            .setTitle("Update Available")
                            .setMessage(getHtml(changelogHtml))
                            .setPositiveButton("Update", (dialog, which) -> {
                                DownloadManager downloadManager= (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                                Uri Download_Uri = Uri.parse(downloadUri);
                                DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                                request.setTitle("Downloading "+updateName);
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,updateName);
                                context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                                downloadID = downloadManager.enqueue(request);
                                Toast.makeText(context, "Download Started! Check notification", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("Cancel",null)
                            .show();
                }else if (!isAutomatic) {
                    Toast.makeText(context, "No update available!", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException | PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (!isInternetAvailable()) {
                return null;
            }
            String updateLink = context.getResources().getString(R.string.updateUri);
            HttpHandler handler = new HttpHandler();
            json = handler.makeServiceCall(updateLink);
            return null;
        }
    }

    public static void moveFile(File file, File dest) throws IOException {
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(dest).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            file.delete();
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }

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

    public static int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    public static String join(String[] arrays,char deliminator) {
        StringBuilder builder = new StringBuilder();
        for (String a: arrays)
            builder.append(a).append(deliminator);
        return builder.toString();
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


    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }

    public static String getDuration(long number) {
        String returnstring="";
        long no = number/1000;
        long mins = no/60;
        long seconds = no%60;
        if (mins>9)
            returnstring+=mins;
        else  returnstring+="0"+mins;
        if (seconds>9)
            returnstring+=":"+seconds;
        else  returnstring+=":0"+seconds;
        return returnstring;
    }

    public static int getAvgBitRate(int bitrate) {
        return (bitrate/1000);
    }

    public  static void StartURLIntent(String url,Context context) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }


    public static String[] getExternalStorageDirectories(Context context) {

        List<String> results = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
            File[] externalDirs = context.getExternalFilesDirs(null);
            String internalRoot = Environment.getExternalStorageDirectory().getAbsolutePath().toLowerCase();

            for (File file : externalDirs) {
                if(file==null) //solved NPE on some Lollipop devices
                    continue;
                String path = file.getPath().split("/Android")[0];

                if(path.toLowerCase().startsWith(internalRoot))
                    continue;

                boolean addPath = false;

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addPath = Environment.isExternalStorageRemovable(file);
                }
                else{
                    addPath = Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file));
                }

                if(addPath){
                    results.add(path);
                }
            }
        }

        if(results.isEmpty()) { //Method 2 for all versions
            // better variation of: http://stackoverflow.com/a/40123073/5002496
            String output = "";
            try {
                final Process process = new ProcessBuilder().command("mount | grep /dev/block/vold")
                        .redirectErrorStream(true).start();
                process.waitFor();
                final InputStream is = process.getInputStream();
                final byte[] buffer = new byte[1024];
                while (is.read(buffer) != -1) {
                    output = output + new String(buffer);
                }
                is.close();
            } catch (final Exception e) {
                e.printStackTrace();
            }
            if(!output.trim().isEmpty()) {
                String devicePoints[] = output.split("\n");
                for(String voldPoint: devicePoints) {
                    results.add(voldPoint.split(" ")[2]);
                }
            }
        }

        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    Log.d("YTUtilsTAG", results.get(i) + " might not be extSDcard");
                    results.remove(i--);
                }
            }
        } else {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().contains("ext") && !results.get(i).toLowerCase().contains("sdcard")) {
                    Log.d("YTUtilsTAG", results.get(i)+" might not be extSDcard");
                    results.remove(i--);
                }
            }
        }

        String[] storageDirectories = new String[results.size()];
        for(int i=0; i<results.size(); ++i) storageDirectories[i] = results.get(i);

        return storageDirectories;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }else {
                    // Trying to handle non-primary volumes
                    String[] volumes = getExternalStorageDirectories(context);
                    if (volumes.length>0)
                        return getExternalStorageDirectories(context)[0] + "/" +split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /*
     *
     * Zips a file at a location and places the resulting zip file at the toLocation
     * Example: zipFileAtPath("downloads/myfolder", "downloads/myFolder.zip");
     */

    public static boolean zipFileAtPath(String sourcePath, String toLocation) {
        final int BUFFER = 2048;

        File sourceFile = new File(sourcePath);
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                entry.setTime(sourceFile.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*
     *
     * Zips a subfolder
     *
     */

    private static void zipSubFolder(ZipOutputStream out, File folder,
                              int basePathLength) throws IOException {

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(file.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    /*
     * gets the last path component
     *
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     */
    public static String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }

    public static boolean extractZip(String zipFile, String tooutPath)
    {
        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(zipFile);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(tooutPath+ "/" + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(tooutPath+ "/" + filename);

                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
