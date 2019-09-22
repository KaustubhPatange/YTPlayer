package com.kpstv.youtube.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.kpstv.youtube.BuildConfig;
import com.kpstv.youtube.PlayerActivity;
import com.kpstv.youtube.R;

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
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static android.content.Context.DOWNLOAD_SERVICE;
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
    public static String getImageUrlID(String videoID) {
        return "https://i.ytimg.com/vi/"+videoID+"/mqdefault.jpg";
    }

    public static String getYtUrl(String videoID) {
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

    public static void addToPlayList(Activity activity,String ytUrl, long seconds) {
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
                String videoID = YTutils.getVideoID(ytUrl);
                if (allPlaylist[i].contains(","+name) && !allPlaylist[i].contains(videoID+"|")) {
                    allPlaylist[i]+=","+videoID+"|"+seconds;
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
    public static String getTodayDate() {
        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        return df.format(c);
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
                                Toast.makeText(context, "Download Started!", Toast.LENGTH_SHORT).show();
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

    public static int getAvgBitRate(int bitrate) {
        return (bitrate/1000);
    }

    public  static void StartURLIntent(String url,Context context) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
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
