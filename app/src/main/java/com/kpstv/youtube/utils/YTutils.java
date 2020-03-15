package com.kpstv.youtube.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.util.IOUtils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.kpstv.youtube.AppInterface;
import com.kpstv.youtube.AppSettings;
import com.kpstv.youtube.BuildConfig;
import com.kpstv.youtube.CPlaylistActivity;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.PurchaseActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.ImportAdapter;
import com.kpstv.youtube.helper.BillingUtils;
import com.kpstv.youtube.models.ImportModel;
import com.kpstv.youtube.models.LocalModel;
import com.kpstv.youtube.models.MetaModel;
import com.kpstv.youtube.models.YTConfig;
import com.kpstv.youtube.services.IntentDownloadService;
import com.kpstv.youtube.services.MusicService;

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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class YTutils implements AppInterface {

    private static final String TAG = "YTutils";

    public static String[] ConvertToStringArray(ArrayList<String> list) {
        final String[] arrays = new String[list.size()];
        for (int i = 0; i < list.size(); i++)
            arrays[i] = list.get(i);
        return arrays;
    }

    public static String getImageUrl(String YtUrl) {
        String quality = MainActivity.activity.getSharedPreferences("appSettings", MODE_PRIVATE)
                .getString("pref_image_quality", "mq");
        return "https://i.ytimg.com/vi/" + getVideoID(YtUrl) + "/" + quality + "default.jpg";
       /* String imageUrl = "https://i.ytimg.com/vi/"+getVideoID(YtUrl)+"/mqdefault.jpg";
        if (MainActivity.activity.getSharedPreferences("appSettings",MODE_PRIVATE)
                .getString("pref_image_quality","mq").equals("hq"))
            imageUrl = "https://i.ytimg.com/vi/"+getVideoID(YtUrl)+"/hqdefault.jpg";
        return imageUrl;*/
    }
    static String link;

    public static void addADView(Activity activity, LinearLayout l) {
        l.setVisibility(View.VISIBLE);
        int number = ThreadLocalRandom.current().nextInt(1, 4);
        l.removeAllViews();
        int layout;// = R.layout.ad_banner;
        if (number==1) {
            layout = R.layout.ad_banner;
            link = "https://androdevkit.github.io";
        }else if (number==2) {
            layout = R.layout.ad_banner_2;
            link = "https://github.com/KaustubhPatange/Kling";
        }else {
            layout = R.layout.ad_banner_3;
            link = "https://kaustubhpatange.github.io/Iso2Usb";
        }
        View v = LayoutInflater.from(activity).inflate(layout,null);
        v.findViewById(R.id.ad_banner_click).setOnClickListener(view ->
                YTutils.StartURL(link,activity));
        l.addView(v);
    }

    public static String getImageUrlID_HQ(String videoID) {
        return "https://i.ytimg.com/vi/" + videoID + "/hqdefault.jpg";
    }

    public static String getImageUrlID_MAX(String videoID) {
        return "https://i.ytimg.com/vi/" + videoID + "/maxresdefault.jpg";
    }

    public static String getImageUrlID_MQ(String videoID) {
        return "https://i.ytimg.com/vi/" + videoID + "/mqdefault.jpg";
    }

    public static String getImageUrlID(String videoID) {
        String quality = MainActivity.activity.getSharedPreferences("appSettings", MODE_PRIVATE)
                .getString("pref_image_quality", "mq");
        return "https://i.ytimg.com/vi/" + videoID + "/" + quality + "default.jpg";
       /* String imageUrl = "https://i.ytimg.com/vi/"+videoID+"/mqdefault.jpg";
        try {
            if (MainActivity.activity.getSharedPreferences("appSettings",MODE_PRIVATE)
                    .getString("pref_image_quality","mq").equals("hq"))
                imageUrl = "https://i.ytimg.com/vi/"+videoID+"/hqdefault.jpg";
        }catch (Exception e){e.printStackTrace();}

        return imageUrl;*/
    }

    public static String getImageUrlID(Context context,String videoID) {
        String quality = context.getSharedPreferences("appSettings", MODE_PRIVATE)
                .getString("pref_image_quality", "mq");
        return "https://i.ytimg.com/vi/" + videoID + "/" + quality + "default.jpg";
       /* String imageUrl = "https://i.ytimg.com/vi/"+videoID+"/mqdefault.jpg";
        try {
            if (MainActivity.activity.getSharedPreferences("appSettings",MODE_PRIVATE)
                    .getString("pref_image_quality","mq").equals("hq"))
                imageUrl = "https://i.ytimg.com/vi/"+videoID+"/hqdefault.jpg";
        }catch (Exception e){e.printStackTrace();}

        return imageUrl;*/
    }
    /*public static String getImageUrlID(Context context,String videoID) {
        String imageUrl = "https://i.ytimg.com/vi/"+videoID+"/mqdefault.jpg";
        if (context.getSharedPreferences("pref_image_quality",MODE_PRIVATE)
                .getString("pref_image_quality","mq").equals("hq"))
            imageUrl = "https://i.ytimg.com/vi/"+videoID+"/hqdefault.jpg";

        Log.e(TAG, "getImageUrlID: Context ImageUrl: "+imageUrl );

        return imageUrl;
    }*/

    public static void openSong(Context context, Intent intent) {
        try {

                   /* if (MainActivity.activity==null) {
                        Intent im = new Intent(context, MainActivity.class);
                        im.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(im);
                    }*/

            Log.e(TAG, "onReceive: triggered");
            Log.e(TAG, "onReceive: " + intent.getData().toString());
            Uri uri = intent.getData();
            Log.e(TAG, "onReceive: " + uri.toString());
            File f = new File(uri.getPath());
            Log.e(TAG, "onReceive: File: " + f.getPath());
            if (!f.exists()) {
                f = new File(uri.toString().replace(".mp3", ".m4a"));
            }

            if (f.exists()) {

                if (uri.toString().contains(".mp4")) {
                    Intent i = new Intent();
                    i.setAction(Intent.ACTION_VIEW);
                    i.setDataAndType(uri, "video/*");
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                    return;
                }

                File downloads = YTutils.getFile(Environment.DIRECTORY_DOWNLOADS);
                File[] files = downloads.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return (file.getPath().endsWith(".mp3") || file.getPath().endsWith(".m4a")
                                || file.getPath().endsWith(".wav") || file.getPath().endsWith(".aac")
                                || file.getPath().endsWith(".ogg") || file.getPath().endsWith(".flac"));
                    }
                });
                if (files.length > 0) {
                    String[] ids = new String[files.length];
                    int position = 0;
                    for (int i = 0; i < files.length; i++) {
                        File id = files[i];
                        ids[i] = files[i].getPath();
                        Log.e(TAG, "onReceive: ID PATH: " + id.getPath());
                        if (id.getPath().equals(f.getPath())) {
                            position = i;
                            break;
                        }
                    }
                    MainActivity.PlayVideo_Local(context, ids, position);
                } else
                    Toast.makeText(context, "There seems to be an error in parsing downloads!", Toast.LENGTH_SHORT).show();

            } else Toast.makeText(context, "Error: File not found!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public static void commonBilling(Context context) {
        SharedPreferences pref = context.getSharedPreferences("appSettings", MODE_PRIVATE);
        if (!pref.getBoolean("pref_audioChange", true))
            AppSettings.listenAudioChange = false;

        if (pref.getBoolean("pref_purchase", false))
            BillingUtils.publishPremium();
    }

    public static String getYtUrl(String videoID) {
        if (videoID.contains("soundcloud.com"))
            return videoID;
        return "https://www.youtube.com/watch?v=" + videoID;
    }

    public static void showAlert(Activity activity, String title, String message, boolean isalert) {
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

    public static void Write(String string, File file) {
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
        Log.e("writeToSDFile3", "\n\nFile written to " + file);
    }

    public static File getFile(String targetfolder) {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/" + targetfolder);
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

    public static void writeContent(Context activity, String FILE_NAME, String content) {
        FileOutputStream fos = null;

        try {
            if (FILE_NAME.contains("/")) {
                fos = new FileOutputStream(new File(FILE_NAME));
            } else {
                fos = activity.openFileOutput(FILE_NAME, MODE_PRIVATE);
            }
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

    static String formatString(String t) {
        t = t.replace(",", "");
        t = t.replace("|", " ");
        return t;
    }

    static AlertDialog alertDialog;

    public static void addToPlayList(Context activity, String videoID, String videoTitle, String channelTitle, String imageUrl, long seconds) {

        Log.e(TAG, "addToPlayList: VideoId" + videoID + ",Seconds: " + seconds);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        // builder.setTitle("Add to playlist");
        ArrayList<String> configs = new ArrayList<>();

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.alert_playlist_layout, null);

        ListView listView = view.findViewById(R.id.recyclerView);
        ConstraintLayout constraintLayout = view.findViewById(R.id.mainLayout);

        constraintLayout.setOnClickListener(view1 -> {
            alertDialog.dismiss();
            Intent intent = new Intent(activity, CPlaylistActivity.class);
            activity.startActivity(intent);
        });
        builder.setView(view);
        String playlist_csv = YTutils.readContent(activity, "playlist.csv");
        if (playlist_csv != null && !playlist_csv.isEmpty()) {
            String[] allPlaylist = playlist_csv.split("\n|\r");

            for (String line : allPlaylist) {
                String[] playlist = line.split(",");
                configs.add(playlist[1]);
            }
            final String[] arrays = new String[configs.size()];
            for (int i = 0; i < configs.size(); i++) {
                arrays[i] = configs.get(i);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, configs);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((adapterView, view1, which, l) -> {
                boolean alreadPresent = false, added = false;
                String name = configs.get(which);
                for (int i = 0; i < configs.size(); i++) {
                    if (allPlaylist[i].contains("," + name) && !allPlaylist[i].contains(videoID + "|")) {
                        allPlaylist[i] += "," + videoID + "|" + seconds + "|" + formatString(videoTitle) + "|" + channelTitle + "|" + imageUrl;
                        writeContent(activity, "playlist.csv",
                                join(allPlaylist, '\n'));
                        added = true;
                        Toast.makeText(activity, "Added to playlist", Toast.LENGTH_SHORT).show();
                    } else alreadPresent = true;
                }
                if (alreadPresent && !added)
                    Toast.makeText(activity, "Already exist in playlist", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            });
           /* builder.setItems(arrays, (dialog, which) -> {

            });*/
        }


        alertDialog = builder.create();
        alertDialog.show();
    }

    /* public Long getTodayTimeString() {
         Date c = Calendar.getInstance().getTime();
         @SuppressLint("SimpleDateFormat")
         SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
         return Long.parseLong(df.format(c));
     }

     public String getLastSavedUrl(Context activity,String videoId) {
         String data = YTutils.readContent(activity,"lastSavedUrls.csv");
         if (data!=null && !data.isEmpty()) {
             long today_time=
             String[] items = data.spl
             it("\n|\r");
             for (int i=0;i<items.length;i++) {

                 if (items[0].split("\\|")[0])
             }
         }
         return null;
     }
 */
    public static String setVideoTitle(String title) {
        Log.e("VideoTitle", title);
        String t = title;
        if (t.contains("-")) {
            t = t.split("-")[1].trim();
            MusicService.channelTitle = title.split("-")[0];
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
            t = t.replace("&#39;", "'");
        if (t.contains("-")) {
            if (t.split("\\-").length < 3)
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
            if (!retVal) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
                builder.setTitle("Set ringtone");
                builder.setMessage("Allow YTPlayer to modify audio settings");
                builder.setNegativeButton("Cancel", null);
                builder.setPositiveButton("OK", (dialogInterface, i) -> {
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
        setRingtone(activity, f);
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
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void setRingtone(Activity activity, File f) {
        try {
            RingtoneManager.setActualDefaultRingtoneUri(activity, RingtoneManager.TYPE_RINGTONE, Uri.fromFile(f));
        } catch (Exception t) {
            t.printStackTrace();

        }
    }

    public static String getChannelTitle(String title, String defaulttitle) {
        if (title.contains("&#39;"))
            title = title.replace("&#39;", "'");
        if (defaulttitle.contains("-"))
            defaulttitle = defaulttitle.split("-")[0];
        if (title.contains("-")) {
            String t = title;
            if (t.split("\\-").length < 3)
                t = t.split("\\-")[0].trim();
            return t.trim();
        } else
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
            } else {
                File f = new File(activity.getFilesDir(), FILE_NAME);
                if (!f.exists()) return null;
                fis = activity.openFileInput(FILE_NAME);
            }
            if (fis == null) return null;
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
        if (spotifyurl.contains("open.spotify")) {
            url = spotifyurl.split("/")[4];
            if (url.contains("?"))
                return url.split("\\?")[0];
            else return url;
        }
        return null;
    }

    public static String dividePattern(float num1, float num2, String pattern) {
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(num1 / num2) + "";
    }

    public static String getSize(long size) {

        DecimalFormat df = new DecimalFormat("0.00");

        float sizeKb = 1024.0f;
        float sizeMb = sizeKb * sizeKb;
        float sizeGb = sizeMb * sizeKb;
        float sizeTerra = sizeGb * sizeKb;

        if (size < sizeMb)
            return df.format(size / sizeKb) + " KB";
        else if (size < sizeGb)
            return df.format(size / sizeMb) + " MB";
        else if (size < sizeTerra)
            return df.format(size / sizeGb) + " GB";

        return "";
    }

    public static String getSizeNoDecimal(long size) {

        DecimalFormat df = new DecimalFormat("0");

        float sizeKb = 1024.0f;
        float sizeMb = sizeKb * sizeKb;
        float sizeGb = sizeMb * sizeKb;
        float sizeTerra = sizeGb * sizeKb;

        if (size < sizeMb)
            return df.format(size / sizeKb) + " KB";
        else if (size < sizeGb)
            return df.format(size / sizeMb) + " MB";
        else if (size < sizeTerra)
            return df.format(size / sizeGb) + " GB";

        return "";
    }

    public static String getSize_withoutPostFix(long size) {

        DecimalFormat df = new DecimalFormat("0.00");

        float sizeKb = 1024.0f;
        float sizeMb = sizeKb * sizeKb;
        float sizeGb = sizeMb * sizeKb;
        float sizeTerra = sizeGb * sizeKb;

        if (size < sizeMb)
            return df.format(size / sizeKb);
        else if (size < sizeGb)
            return df.format(size / sizeMb);
        else if (size < sizeTerra)
            return df.format(size / sizeGb);

        return "";
    }

    public static String getViewCount(long number) {
        if (number > 1000000000) { // billion
            return number / 1000000000 + "B";
        } else if (number > 1000000) { // million
            return number / 1000000 + "M";
        } else if (number > 1000) {
            return number / 1000 + "K";
        }
        return number + "";
    }

    public static int progressToTimer(int progress, long totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }

    public static boolean isValidID(String url) {
        return (url.contains("youtube.com") || url.contains("youtu.be"));
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
        @SuppressLint("SimpleDateFormat") String month = new SimpleDateFormat("MM").format(c);
        @SuppressLint("SimpleDateFormat") String year = new SimpleDateFormat("yyyy").format(c);
        return year + "" + month + (Integer.parseInt(date) + 1);
    }

    public static String getYesterday_nogap() {
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("dd").format(c);
        @SuppressLint("SimpleDateFormat") String month = new SimpleDateFormat("MM").format(c);
        @SuppressLint("SimpleDateFormat") String year = new SimpleDateFormat("yyyy").format(c);
        return year + "" + month + (Integer.parseInt(date) - 1);
    }


    public static String getLocalArtworkImage(Activity activity, LocalModel model) {
        if (!YTutils.isInternetAvailable())
            return null;
        String data = YTutils.readContent(activity, "artistImages.csv");
        String imageUri = null;
        if (data != null && !data.isEmpty()) {
            String title = model.getTitle().trim();
            if (data.contains(title + "$")) {
                String[] items = data.split("\n|\r");
                for (String item : items) {
                    if (item.isEmpty()) continue;
                    if (item.contains(title + "$")) {
                        imageUri = item.split("\\$")[1];
                        return imageUri;
                    }
                }
            } else {
                ArtistImage artistImage = new ArtistImage(title);
                imageUri = artistImage.getImageUri();
                YTutils.writeContent(activity, "artistImages.csv", data +
                        title + "$" + imageUri);
            }
        } else {
            String title = model.getTitle().trim();
            ArtistImage artistImage = new ArtistImage(title);
            imageUri = artistImage.getImageUri();
            YTutils.writeContent(activity, "artistImages.csv", title + "$" + imageUri);
        }
        return imageUri;
    }

    public static void openPurchaseActivity(Context context) {
        Intent intent = new Intent(context, PurchaseActivity.class);
        context.startActivity(intent);
    }

    public static String getPlayBackstate(int state) {
        switch (state) {
            case ExoPlayer.STATE_IDLE:
                return "IDLE";
            case ExoPlayer.STATE_BUFFERING:
                return "BUFFERING";
            case ExoPlayer.STATE_READY:
                return "READY";
            case ExoPlayer.STATE_ENDED:
                return "ENDED";
        }
        return "-1";
    }

    public static String getAudioFormat(File f) {
        String ext = f.getPath().substring(f.getPath().lastIndexOf("."), f.getPath().length());
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

            if (data != null) {
                return BitmapFactory.decodeByteArray(data, 0, data.length);
            }
        } catch (Exception e) {
            e.getMessage();
        }
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

    public static boolean isProcessCompleted(Process process) {
        try {
            if (process == null) return true;
            process.exitValue();
            return true;
        } catch (IllegalThreadStateException e) {
            // do nothing
        }
        return false;
    }

    public static boolean isDebug(Context context) {
        return (0 != (context.getApplicationContext().getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
    }

    public static void close(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                // Do nothing
            }
        }
    }

    public static void close(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                // Do nothing
            }
        }
    }

    public static String convertInputStreamToString(InputStream inputStream) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            String str;
            StringBuilder sb = new StringBuilder();
            while ((str = r.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "error converting input stream to string" + e.getMessage());
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void destroyProcess(Process process) {
        if (process != null)
            process.destroyForcibly();
    }

    static boolean killAsync(AsyncTask asyncTask) {
        return asyncTask != null && !asyncTask.isCancelled() && asyncTask.cancel(true);
    }

    public static void shareFile(Activity context, File f) {
        try {
            Uri uri = Uri.fromFile(f);
            ShareCompat.IntentBuilder.from(context)
                    .setStream(uri)
                    .setType(URLConnection.guessContentTypeFromName(f.getName()))
                    .startChooser();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        @SuppressLint("SimpleDateFormat") String month = new SimpleDateFormat("MM").format(c);
        @SuppressLint("SimpleDateFormat") String year = new SimpleDateFormat("yyyy").format(c);
        return (Integer.parseInt(date) - 1) + "-" + month + "-" + year;
    }

    public static String getVideoID_ImageUri(String imageUrl) {
        // "https://i.ytimg.com/vi/"+videoID+"/mqdefault.jpg";
        String t = imageUrl;

        t = t.split("/")[4];

        return t;
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            java.net.URL url = new java.net.URL(src);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getVideoID(String youtube_url) {
        String t = youtube_url;
        if (t.contains("soundcloud.com"))
            return t;
        if (youtube_url.contains("youtube.com")) {
            t = youtube_url.split("=")[1];
        } else if (youtube_url.contains("youtu.be")) {
            t = youtube_url.replace("https://youtu.be/", "");
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

    public static void StartURL(String url, Context context) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
        CustomTabsIntent customTabsIntent = builder.build();
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo("com.android.chrome", 0);
            customTabsIntent.intent.setPackage("com.android.chrome");
            customTabsIntent.launchUrl(context, Uri.parse(url));
        } catch (Exception e) {
            Log.e("Chrome", "Chrome not installed: " + e.getMessage());
            StartURLIntent(url, context);
        }
    }

    public static String getUrlVideoRTSP(String urlYoutube) {
        try {
            String gdy = "http://gdata.youtube.com/feeds/api/videos/";
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            String id = getVideoID(urlYoutube);
            URL url = new URL(gdy + id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            Document doc = documentBuilder.parse(connection.getInputStream());
            Element el = doc.getDocumentElement();
            NodeList list = el.getElementsByTagName("media:content");///media:content
            String cursor = urlYoutube;
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node != null) {
                    NamedNodeMap nodeMap = node.getAttributes();
                    HashMap<String, String> maps = new HashMap<String, String>();
                    for (int j = 0; j < nodeMap.getLength(); j++) {
                        Attr att = (Attr) nodeMap.item(j);
                        maps.put(att.getName(), att.getValue());
                    }
                    if (maps.containsKey("yt:format")) {
                        String f = maps.get("yt:format");
                        if (maps.containsKey("url")) {
                            cursor = maps.get("url");
                        }
                        if (f.equals("1"))
                            return cursor;
                    }
                }
            }
            return cursor;
        } catch (Exception ex) {
            Log.e("RTSP_Video", ex.toString());
        }
        return urlYoutube;

    }

    public static long TimertoMilliseconds(String timer) {
        String[] idls = timer.split(":");
        Log.e("IDLS_Count", idls.length + "");
        if (idls.length > 2) {
            // It contains hour...
            // 3:02:11
            long hour = Long.parseLong(idls[0]) * 60 * 60 * 1000;
            long min = Long.parseLong(idls[1]) * 60 * 1000;
            long sec = Long.parseLong(idls[2]) * 1000;
            return hour + min + sec;
        } else {
            // It is only minutes...
            //3:04
            long min = Long.parseLong(idls[0]) * 60 * 1000;
            long sec = Long.parseLong(idls[1]) * 1000;
            return min + sec;
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

    public static int getRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    public static long getFileSize(URL url) {
        URLConnection conn = null;
        try {
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).setRequestMethod("HEAD");
            }
            conn.getInputStream();
            return conn.getContentLength();
        } catch (IOException e) {
            Log.e(TAG, "URL-FILE_SIZE ERROR: "+e.getMessage());
            e.printStackTrace();
            return 0;
        } finally {
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).disconnect();
            }
        }
    }

    public static class CheckForUpdates extends AsyncTask<Void, Void, Void> {
        boolean isAutomatic, permissionGranted;
        String json;
        @SuppressLint("StaticFieldLeak")
        Activity context;
        String updateName;
        long downloadID;
        AsyncTask<Void, Long, Void> updateTask;


        public CheckForUpdates(Activity context, boolean isAutomatic) {
            this.context = context;
            this.isAutomatic = isAutomatic;

            Log.e(TAG, "CheckForUpdates: Okay I am here...");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Dexter.withActivity(context)
                            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                    permissionGranted = true;
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    Toast.makeText(context, "Permission denied!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                    return;
                                }

                            }).check();

                } else permissionGranted = true;
            } else permissionGranted = true;
        }

        void runInstall(Context context, String updateName) {
            File f = getFile(Environment.DIRECTORY_DOWNLOADS + "/" + updateName);
            Uri data = Uri.fromFile(f);
            Log.e(TAG, "runInstall: Uri of FIle: " + data.toString());
            if (f.exists()) {

                Uri uri = getApkUri(f.getPath());

                Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.getApplicationInfo().packageName);

                if (context.getPackageManager().queryIntentActivities(intent, 0) != null) {// checked on start Activity

                    Activity act = (Activity) context;
                    act.startActivityForResult(intent, 100);

                }
            } else
                Toast.makeText(context, "Update file does not exist!", Toast.LENGTH_SHORT).show();
        }

        private Uri getApkUri(String path) {

            // Before N, a MODE_WORLD_READABLE file could be passed via the ACTION_INSTALL_PACKAGE
            // Intent. Since N, MODE_WORLD_READABLE files are forbidden, and a FileProvider is
            // recommended.
            boolean useFileProvider = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;

           // String tempFilename = "tmp.apk";
         //   byte[] buffer = new byte[16384];
            int fileMode = useFileProvider ? Context.MODE_PRIVATE : Context.MODE_WORLD_READABLE;
          /*  try (InputStream is = new FileInputStream(new File(path));
                 FileOutputStream fout = context.openFileOutput(tempFilename, fileMode)) {

                int n;
                while ((n = is.read(buffer)) >= 0) {
                    fout.write(buffer, 0, n);
                }

            } catch (IOException e) {
                Log.i(TAG + ":getApkUri", "Failed to write temporary APK file", e);
            }*/

            if (useFileProvider) {
             //   File toInstall = new File(context.getFilesDir(), tempFilename);
                File toInstall = new File(path);
                return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID+".provider", toInstall);
            } else {
                return Uri.fromFile(new File(path));
            }
        }

        @SuppressLint("StaticFieldLeak")
        ProgressBar progressBar;
        AlertDialog alertDialog;
        TextView progressText;

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!permissionGranted)
                return;
            if (json == null || json.isEmpty())
                return;
            try {
                JSONObject object = new JSONObject(json);
                String changelogHtml = object.getString("changelog");
                String downloadUri = object.getString("download");

                int newVer = Integer.parseInt(object.getString("newVersion")
                        .replace(".", ""));
                int deprecatedVersion = Integer.parseInt(object.getString("deprecated")
                        .replace(".", ""));
                updateName = "YTPlayer_v" + newVer + ".apk";
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                int curVer = Integer.parseInt(pInfo.versionName.replace(".", ""));
                Log.e("VersionLOG", "NewVersion: " + newVer + ", currVersion: " + curVer);
                LayoutInflater inflater = LayoutInflater.from(context);
                if (deprecatedVersion>=curVer) {
                    View v = inflater.inflate(R.layout.alert_deprecated_error,null);

                    alertDialog = new AlertDialog.Builder(context)
                            .setView(v)
                            .setCancelable(false)
                            .setPositiveButton("OK",null)
                            .setNeutralButton("Visit Website",(dialogInterface, i) -> {
                                StartURL(context.getResources().getString(R.string.weburl),context);
                            })
                            .create();
                    alertDialog.show();
                    return;
                }
                if (newVer > curVer) {
               //       if (true) {
                    View v = inflater.inflate(R.layout.alert_download, null);

                    TextView showTxt = v.findViewById(R.id.textTxt);
                    showTxt.setText(getHtml(changelogHtml));

                    progressBar = v.findViewById(R.id.progressBar);
                    progressText = v.findViewById(R.id.progressText);

                    alertDialog = new AlertDialog.Builder(context)
                            .setView(v)
                            .setCancelable(false)
                            .setPositiveButton("Download", null)
                            .setNegativeButton("Cancel", (dialogInterface, i) -> {
                                if (updateTask != null && updateTask.getStatus() == Status.RUNNING) {
                                    updateTask.cancel(true);
                                }
                            })
                            .create();

                    alertDialog.show();

                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                        progressBar.setVisibility(View.VISIBLE);
                        progressText.setVisibility(View.VISIBLE);
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
                        updateTask = new downloadTask(downloadUri);
                        updateTask.executeOnExecutor(THREAD_POOL_EXECUTOR);
                    });
                } else if (!isAutomatic) {
                    Toast.makeText(context, "No update available!", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException | PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }

        class downloadTask extends AsyncTask<Void, Long, Void> {
            String download_Uri;

            public downloadTask(String download_Uri) {
                this.download_Uri = download_Uri;
            }

            boolean isDownloaded = false;

            @Override
            protected void onPostExecute(Void aVoid) {
                // alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
                if (getFile(Environment.DIRECTORY_DOWNLOADS + "/" + updateName).exists()) {
                    progressText.setVisibility(View.GONE);
                    Toast.makeText(context, "Download complete, Click Install!", Toast.LENGTH_SHORT).show();
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setText("Install");
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                        alertDialog.dismiss();
                        runInstall(context, updateName);
                    });
                } else
                    Toast.makeText(context, "There is a problem with update package!", Toast.LENGTH_SHORT).show();

                super.onPostExecute(aVoid);
            }

            @Override
            protected void onProgressUpdate(Long... values) {
                int val = values[0].intValue();
                if (val <= 100)
                    progressBar.setProgress(val);
                progressText.setText((values[1] / 1000) + "/" + (values[2] / 1000));
                super.onProgressUpdate(values);
            }

            @Override
            protected Void doInBackground(Void... voids) {
                File f = new File(YTutils.getFile(Environment.DIRECTORY_DOWNLOADS).getPath(), updateName);
                if (f.exists()) return null;
                PRDownloader.download(download_Uri,
                        YTutils.getFile(Environment.DIRECTORY_DOWNLOADS).getPath(), updateName)
                        .build()
                        .setOnProgressListener(progress1 -> {
                            publishProgress((progress1.currentBytes * 100 / progress1.totalBytes), progress1.currentBytes, progress1.totalBytes);
                        })
                        .start(new OnDownloadListener() {
                            @Override
                            public void onDownloadComplete() {
                                isDownloaded = true;
                                Log.e(TAG, "onDownloadComplete: Completed");
                            }

                            @Override
                            public void onError(Error error) {
                                isDownloaded = true;
                            }
                        });

                /** Wait till download is complete... */
                while (!isDownloaded) ;
                return null;
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

    public static void showPurchaseDialog(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.alert_buy_premium, null);
        new AlertDialog.Builder(context)
                .setView(v).setPositiveButton("Purchase", (dialogInterface, i) -> {
            YTutils.openPurchaseActivity(context);
        }).setNegativeButton("Cancel", null)
                .show();
    }

    public static void downloadDialog(Context context, MetaModel meta) {

        if (AppSettings.downloadCount <= 0 && AppSettings.setDownloads) {
            YTutils.showPurchaseDialog(context);
            return;
        } else if (AppSettings.setDownloads) AppSettings.downloadCount--;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.alert_download_filter, null);

        RadioButton m4aRadio = v.findViewById(R.id.radio_m4a);
        RadioButton v1080Radio = v.findViewById(R.id.radio_1080p);
        RadioButton v720pRadio = v.findViewById(R.id.radio_720p);
        RadioButton v480pRadio = v.findViewById(R.id.radio_480p);

        alertDialog = new AlertDialog.Builder(context)
                .setView(v)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    String ext = "mp3";
                    if (m4aRadio.isChecked()) ext = "m4a";
                    else if (v1080Radio.isChecked()) ext = "1080p";
                    else if (v720pRadio.isChecked()) ext = "720p";
                    else if (v480pRadio.isChecked()) ext = "480p";

                    YTConfig config = new YTConfig("auto-generate", "auto-generate", ext
                            , meta.getTitle(), meta.getAuthor(), true, meta.getImgUrl());
                    config.setVideoID(meta.getVideoID());
                    config.setTargetName(YTutils.getTargetName(config));
                    config.setTaskExtra("autoTask");

                    if (AppSettings.showAds)
                        showAd(context);

                    Intent serviceIntent = new Intent(context, IntentDownloadService.class);
                    serviceIntent.putExtra("addJob", config);
                    ContextCompat.startForegroundService(context, serviceIntent);
                })
                .create();

        alertDialog.show();
    }

    public static void showAd(Context con) {

        try {
            InterstitialAd mInterstitialAd = new InterstitialAd(con);
            mInterstitialAd.setAdUnitId("ca-app-pub-1164424526503510/4801416648");
            mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("07153BA64BB64F7C3F726B71C4AE30B9").build());
            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);
                    Log.e(TAG, "onAdFailedToLoad: Ad failed to load: " + i);
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mInterstitialAd.show();
                }
            });
        } catch (Exception ignored) {
        }
    }

    public static String getTargetName(YTConfig config) {
        String filename;
        if (config.getText().length() > 55) {
            filename = config.getTitle().substring(0, 55).trim();
        } else {
            filename = config.getChannelTitle().trim() + " - " + config.getTitle().trim();
        }
        return filename.replaceAll("[\\\\><\"|*?%:#/]", "");
    }

    public static void showInterstitialAd(Context activity) {
        if (!AppSettings.showAds)
            return;
        //TODO: Change ad unit ID, Sample ca-app-pub-3940256099942544/1033173712
        InterstitialAd mInterstitialAd = new InterstitialAd(activity);
        mInterstitialAd.setAdUnitId("ca-app-pub-1164424526503510/4801416648");
        mInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice("07153BA64BB64F7C3F726B71C4AE30B9").build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.e(TAG, "onAdFailedToLoad: Ad failed to load: " + i);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
            }
        });
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

    public static String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // return timer string
        return finalTimerString;
    }

    public static int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        // return percentage
        return percentage.intValue();
    }

    public static String join(String[] arrays, char deliminator) {
        StringBuilder builder = new StringBuilder();
        for (String a : arrays)
            builder.append(a).append(deliminator);
        return builder.toString();
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
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
        String returnstring = "";
        long no = number / 1000;
        long mins = no / 60;
        long seconds = no % 60;
        if (mins > 9)
            returnstring += mins;
        else returnstring += "0" + mins;
        if (seconds > 9)
            returnstring += ":" + seconds;
        else returnstring += ":0" + seconds;
        return returnstring;
    }

    public static int getAvgBitRate(int bitrate) {
        return (bitrate / 1000);
    }

    public static void StartURLIntent(String url, Context context) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
    }



    public static void parseDataForPlaylist(Context context, String path) {
        parseDataForPlaylist(context, path, false,false);
    }

    public static void parseDataForPlaylist(Context context, String path, boolean playlist_export) {
        parseDataForPlaylist(context, path, playlist_export,true);
    }

    static AlertDialog alertDialog1;
    public static void parseDataForPlaylist(Context context, String path, boolean playlist_export,boolean export) {
        if (path != null) {

            String[] items_bef = Objects.requireNonNull(YTutils.readContent(context, path)).split("\n|\r");

            if (items_bef.length > 0) {
                String[] items;
                if (!playlist_export) {
                    items = new String[items_bef.length-1];
                    if (!items_bef[0].startsWith(context.getResources().getString(R.string.header_playlist))) {
                        Toast.makeText(context, "Invalid playlist file", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    System.arraycopy(items_bef, 1, items, 0, items_bef.length - 1);
                }else {
                    List<String> list = Arrays.asList(items_bef);

                    Collections.reverse(list);

                    items = (String[]) list.toArray();
                    //items = items_bef;

                }

                ArrayList<ImportModel> models = new ArrayList<>();

                for (String line : items) {
                    String[] childs = line.split(",");
                    int seconds = 0;
                    for (int i = 2; i < childs.length; i++) {
                        String playlist_item = childs[i];
                        seconds += Integer.parseInt(playlist_item.split("\\|")[1]);
                    }
                    models.add(new ImportModel(
                            childs[1], +childs.length - 2, seconds
                    ));
                }

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

                View view = inflater.inflate(R.layout.alert_import_playlist, null);

                RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

                ImportAdapter adapter = new ImportAdapter(context, models, items);
                adapter.setListener((model, position) -> {
                    model.setChecked(!model.isChecked());
                    adapter.notifyItemChanged(position);
                });


                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                recyclerView.setAdapter(adapter);

                String text = "Import";
                if (export) text = "Export";

                ((TextView) view.findViewById(R.id.titleTxt)).setText(text);

                alertDialog = new AlertDialog.Builder(context)
                        .setView(view)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton(text, (dialogInterface, i) -> {

                        })
                        .create();
                alertDialog.show();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view2 -> {

                    boolean atleast_one_check=false;

                    if (export) {
                        for (ImportModel importModel:models) {
                            if (importModel.isChecked()) {
                                atleast_one_check=true;
                                break;
                            }
                        }
                        if (!atleast_one_check) {
                            Toast.makeText(context, "Kindly make a selection", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        View view1 = inflater.inflate(R.layout.alert_export_name,null);
                        EditText editText = view1.findViewById(R.id.editText);

                        alertDialog1 = new AlertDialog.Builder(context)
                                .setView(view1)
                                .setPositiveButton("Export",(dialogInterface, i) -> {

                                })
                                .setNegativeButton("Cancel",null)
                                .create();

                        alertDialog1.show();
                        alertDialog1.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view3 -> {
                            if (editText.getText().toString().isEmpty()) {
                                Toast.makeText(context, "Playlist name cannot be empty", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            StringBuilder builder = new StringBuilder();
                            builder.append(context.getResources().getString(R.string.header_playlist)).append("\n");
                            for (int i=0;i<models.size();i++) {
                                if (models.get(i).isChecked()) {
                                    builder.append(items[i]).append("\n");
                                }
                            }
                            String path1 = getFile("Playlists/"+editText.getText().toString()+".txt").getPath();
                            YTutils.writeContent(context,path1,builder.toString());
                            Toast.makeText(context, "Saved to "+path1, Toast.LENGTH_SHORT).show();
                            alertDialog1.dismiss();
                            alertDialog.dismiss();
                        });

                    } else {
                        int added = 0, replaced = 0;
                        String playlist_csv = YTutils.readContent(context, "playlist.csv");
                        if (playlist_csv == null) playlist_csv = "";
                        StringBuilder playlistBuilder = new StringBuilder(playlist_csv);
                        for (int c = 0; c < models.size(); c++) {
                            if (models.get(c).isChecked()) {
                                atleast_one_check = true;
                                if (!playlist_csv.contains("," + models.get(c).getTitle() + ",")) {
                                    playlistBuilder.append(items[c]).append("\n");
                                    added++;
                                } else {
                                    String[] playlist_items = playlistBuilder.toString().split("\n|\r");
                                    for (int k = 0; k < playlist_items.length; k++) {
                                        if (playlist_items[k].contains("," + models.get(c).getTitle() + ",")) {
                                            playlist_items[k] = items[c];
                                        }
                                    }
                                    playlistBuilder = new StringBuilder(YTutils.join(playlist_items, '\n'));
                                    replaced++;
                                }
                            }
                        }
                        if (atleast_one_check) {
                            YTutils.writeContent(context, "playlist.csv", playlistBuilder.toString());
                            Toast.makeText(context, added + " new playlist added, " + replaced + " replaced!", Toast.LENGTH_SHORT).show();
                            alertDialog.dismiss();
                        } else
                            Toast.makeText(context, "Kindly Make a selection", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else
            Toast.makeText(context, "Error: Failed to parse playlist!", Toast.LENGTH_SHORT).show();
    }

    public static String[] getExternalStorageDirectories(Context context) {

        List<String> results = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //Method 1 for KitKat & above
            File[] externalDirs = context.getExternalFilesDirs(null);
            String internalRoot = Environment.getExternalStorageDirectory().getAbsolutePath().toLowerCase();

            for (File file : externalDirs) {
                if (file == null) //solved NPE on some Lollipop devices
                    continue;
                String path = file.getPath().split("/Android")[0];

                if (path.toLowerCase().startsWith(internalRoot))
                    continue;

                boolean addPath = false;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addPath = Environment.isExternalStorageRemovable(file);
                } else {
                    addPath = Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(file));
                }

                if (addPath) {
                    results.add(path);
                }
            }
        }

        if (results.isEmpty()) { //Method 2 for all versions
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
            if (!output.trim().isEmpty()) {
                String devicePoints[] = output.split("\n");
                for (String voldPoint : devicePoints) {
                    results.add(voldPoint.split(" ")[2]);
                }
            }
        }

        //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}")) {
                    Log.d("YTUtilsTAG", results.get(i) + " might not be extSDcard");
                    results.remove(i--);
                }
            }
        } else {
            for (int i = 0; i < results.size(); i++) {
                if (!results.get(i).toLowerCase().contains("ext") && !results.get(i).toLowerCase().contains("sdcard")) {
                    Log.d("YTUtilsTAG", results.get(i) + " might not be extSDcard");
                    results.remove(i--);
                }
            }
        }

        String[] storageDirectories = new String[results.size()];
        for (int i = 0; i < results.size(); ++i) storageDirectories[i] = results.get(i);

        return storageDirectories;
    }

    public static String getTimeString(Long time) {
        if (time > 60)
            return (time / 60) + " mins";
        else if (time == 60)
            return "1 min";
        else {
            return time + " secs";
        }
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
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
                } else {
                    // Trying to handle non-primary volumes
                    String[] volumes = getExternalStorageDirectories(context);
                    if (volumes.length > 0)
                        return getExternalStorageDirectories(context)[0] + "/" + split[1];
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
                final String[] selectionArgs = new String[]{
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
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
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


    public static class ContentProvider {
        public static String getFilePathFromURI(Context context, Uri contentUri) {
            //copy file and send new file path
            String fileName = getFileName(contentUri);
            if (!TextUtils.isEmpty(fileName)) {
                File copyFile = new File(context.getCacheDir() + File.separator + fileName);
                copy(context, contentUri, copyFile);
                return copyFile.getAbsolutePath();
            }
            return null;
        }

        private static String getFileName(Uri uri) {
            if (uri == null) return null;
            String fileName = null;
            String path = uri.getPath();
            int cut = path.lastIndexOf('/');
            if (cut != -1) {
                fileName = path.substring(cut + 1);
            }
            return fileName;
        }

        private static void copy(Context context, Uri srcUri, File dstFile) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
                if (inputStream == null) return;
                OutputStream outputStream = new FileOutputStream(dstFile);
                IOUtils.copyStream(inputStream, outputStream);
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

    public static boolean extractZip(String zipFile, String tooutPath) {
        InputStream is;
        ZipInputStream zis;
        try {
            String filename;
            is = new FileInputStream(zipFile);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(tooutPath + "/" + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(tooutPath + "/" + filename);

                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
