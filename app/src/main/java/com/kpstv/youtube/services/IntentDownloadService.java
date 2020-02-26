package com.kpstv.youtube.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coremedia.iso.boxes.Container;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import com.kpstv.youtube.DownloadActivity;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.OFModel;
import com.kpstv.youtube.models.YTConfig;
import com.kpstv.youtube.receivers.SongBroadCast;
import com.kpstv.youtube.utils.FileUtils;
import com.kpstv.youtube.utils.SoundCloud;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;
import com.naveed.ytextractor.ExtractorException;
import com.naveed.ytextractor.YoutubeStreamExtractor;
import com.naveed.ytextractor.model.YTMedia;
import com.naveed.ytextractor.model.YoutubeMeta;

import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.FFmpeg.RETURN_CODE_SUCCESS;

public class IntentDownloadService extends IntentService {
    private static final String TAG = "IntentDownloadService";
    private boolean isDownloaded = false;
    private PowerManager.WakeLock wakeLock;
    NotificationManagerCompat notificationManagerCompat;
    private Context context;
    private String CHANNEL_ID = "channel_02";
    Bitmap icon;
    private final int FOREGROUND_ID = 109;
    Notification notification;
    PendingIntent contentIntent, cancelIntent;
    private long oldbytes; boolean useFFMPEGmuxer=true;
    private Handler handler;

    /**
     * Some static Declarations
     */
    public static YTConfig currentModel;
    public static int progress;
    public static long totalsize;
    public static ArrayList<YTConfig> pendingJobs;
    public static long currentsize;
  //  public static Process process;
   // FFmpeg ffmpeg;

    public IntentDownloadService() {
        super("IntentDownloadService");
        setIntentRedelivery(true);
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        pendingJobs = new ArrayList<>();

        SharedPreferences preferences = getSharedPreferences("appSettings",MODE_PRIVATE);
        useFFMPEGmuxer = preferences.getBoolean("pref_muxer",true);

        PRDownloader.initialize(context);

        /** Create notification channel if not present */
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel("channel_01", name, importance);
            notificationChannel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"Download",importance);
            notificationManager.createNotificationChannel(channel);
        }


        /** Setting Power Manager and Wakelock */

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "app:Wakelock");
        wakeLock.acquire();

        Intent notificationIntent = new Intent(context, DownloadActivity.class);
        contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent, 0);

        Intent newintent = new Intent(context, SongBroadCast.class);
        newintent.setAction("com.kpstv.youtube.STOP_SERVICE");
        cancelIntent =
                PendingIntent.getBroadcast(context, 5, newintent, 0);

        setUpdateNotificationTask();
        super.onCreate();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        YTConfig config = (YTConfig) intent.getSerializableExtra("addJob");
        pendingJobs.add(config);

        /** Setting Notification */

        notificationManagerCompat = NotificationManagerCompat.from(context);

        notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Download")
                .addAction(R.mipmap.ic_launcher, "Cancel", cancelIntent)
                .setContentText(pendingJobs.size() + " files downloading")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(FOREGROUND_ID, notification);
        } else {
            notificationManagerCompat.notify(FOREGROUND_ID, notification);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@NonNull Intent intent) {
        isDownloaded=false;

        currentModel = (YTConfig) intent.getSerializableExtra("addJob");

        if (pendingJobs.size() > 0)
            pendingJobs.remove(0);
        switch (currentModel.getTaskExtra()) {

            case "autoTask":
                autoTask();
                break;
            case "mp3Task":
                mp3Task();
                break;
            case "mergeTask":
                mergeTask();
                break;
        }
    }

    /*@Override
    protected void onHandleIntent(@Nullable Intent intent) {

        *//** Set notification update handler*//*


        currentModel = (YTConfig) intent.getSerializableExtra("addJob");

        if (pendingJobs.size() > 0)
            pendingJobs.remove(0);
        switch (currentModel.getTaskExtra()) {
            case "mp3Task":
                mp3Task();
                break;
            case "mergeTask":
                mergeTask();
                break;
        }
    }*/

    public void setFinalNotification(File dst) {
        Notification not;
        String contentText = currentModel.getTitle() + " - " + currentModel.getChannelTitle();
        if (dst.exists()) {
            Intent openSong = new Intent(context, SongBroadCast.class);
            openSong.setAction("com.kpstv.youtube.OPEN_SONG");
            openSong.setData(Uri.fromFile(dst));

            Intent openShare = new Intent(context, SongBroadCast.class);
            openShare.setAction("com.kpstv.youtube.OPEN_SHARE_SONG");
            openShare.setData(Uri.fromFile(dst));

            PendingIntent opensongService =
                    PendingIntent.getBroadcast(context, 6, openSong, 0);

            PendingIntent openshareService =
                    PendingIntent.getBroadcast(context, 7, openShare, 0);

            not = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Download Complete")
                    .setContentText(contentText)
                    .setSmallIcon(R.drawable.ic_check)
                    .setContentIntent(opensongService)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_LOW)
                    .addAction(R.mipmap.ic_launcher, "Share", openshareService)
                    .build();
        } else {
            not = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setSmallIcon(R.drawable.ic_error_outline)
                    .setContentTitle("Download Failed")
                    .setContentText(contentText)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_LOW)
                    .build();
        }

        int nos = new Random().nextInt(400) + 150;
        notificationManagerCompat.notify(nos, not);
    }

    public static void LOG(String message) {
        Log.e(TAG, message);
    }

    public void setUpdateNotificationTask() {
        if (handler != null)
            handler.removeCallbacks(updateNotificationTask);
        handler = new Handler();
        handler.post(updateNotificationTask);
    }

    private void addFiletoLocalDevice(File f, YTConfig model) {
        if (f.exists()) {
            /** Generate file name */
            File fileList = new File(getFilesDir(), "fileList.csv");
            String fileName = f.getParent().replace("/", "_") + ".csv";
            File file = new File(getFilesDir(), "locals/" + fileName);
            Log.e(TAG, "FileList: " + fileList.getPath());
            Log.e(TAG, "File: " + file.getPath());
            if (fileList.exists()) {
                String author = model.getChannelTitle();
                String album = "Unknown album";
                String durationStr = "0";
                String lastModified = YTutils.getDate(new Date(f.lastModified()));
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    mmr.setDataSource(context, Uri.fromFile(f));
                    durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    String album_local = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                    if (album_local != null)
                        album = album_local;
                } catch (Exception ignored) {

                }
                int s = Integer.parseInt(durationStr) / 1000;

                String line = f.getPath() + "|" + author + "|" + album + "|" + s + "|" + lastModified;

                if (!file.exists()) {
                    YTutils.writeContent(context, file.getPath(), line);
                    String data = YTutils.readContent(context, "fileList.csv");
                    String fileContent = data + "\n" + f.getParent() + "|1|" + s;
                    YTutils.writeContent(context, fileList.getPath(), "\n" + fileContent);
                } else {

                    /** Check if file already exist in main File...*/
                    String mainData = YTutils.readContent(context,file.getPath());
                    if (mainData != null && mainData.contains(f.getPath()+"|"))
                        return;

                    /** Modify fileList */
                    String fileData = YTutils.readContent(context, "fileList.csv");
                    if (fileData != null && !fileData.isEmpty()) {
                        String[] items = fileData.split("\n|\r");
                        StringBuilder builder = new StringBuilder();
                        LOG("Parent: " + f.getParent());
                        for (int i = 0; i < items.length; i++) {
                            String l = items[i];
                            if (l.isEmpty()) continue;
                            LOG("Line: " + l);
                            if (l.contains(f.getParent() + "|")) {
                                LOG("I came here...");
                                String[] childs = l.split("\\|");
                                int numberOfSong = Integer.parseInt(childs[1]) + 1;
                                int duration = Integer.parseInt(childs[2]) + s;
                                builder.append("\n").append(f.getParent()).append("|").append(numberOfSong).append("|")
                                        .append(duration);
                            } else builder.append("\n").append(l);
                        }
                        YTutils.writeContent(context, "fileList.csv", builder.toString());
                    }

                    String data = YTutils.readContent(context, file.getPath()) + "\n" + line;
                    YTutils.writeContent(context, file.getPath(), data);
                }
            }
        }
    }

    public synchronized void setProgress(int progress, boolean indeterminate) {

        if (indeterminate)
            IntentDownloadService.progress = -1;
        else
            IntentDownloadService.progress = progress;
    }

    private void autoTask() {
        /** Generate a download link */

        if (currentModel.getVideoID().contains("soundcloud.com")) {
            SoundCloud soundCloud = new SoundCloud(currentModel.getVideoID());
            if (soundCloud.getModel()!=null) {
                if (currentModel.getTitle().equals("auto-generate")) {
                    String title = soundCloud.getModel().getTitle();
                    String author = soundCloud.getModel().getAuthorName();
                    String imgUrl = soundCloud.getModel().getImageUrl();
                    currentModel.setExt("mp3");
                    currentModel.setTitle(title);
                    currentModel.setChannelTitle(author);
                    currentModel.setImageUrl(imgUrl);
                    currentModel.setTargetName(YTutils.getTargetName(currentModel));

                    Log.e(TAG, "autoTask: CurrentModel: " +currentModel.getTitle());
                }
                currentModel.setExt("mp3");
                currentModel.setUrl(soundCloud.getModel().getStreamUrl());
            }else
                Toast.makeText(context, "Failed to extract soundcloud link!", Toast.LENGTH_SHORT).show();
        }else {
            isDownloaded=false;

            new YoutubeStreamExtractor(new YoutubeStreamExtractor.ExtractorListner() {
                @Override
                public void onExtractionGoesWrong(ExtractorException e) {
                    LOG("Stream Error");
                    isDownloaded = true;
                }

                @Override
                public void onExtractionDone(List<YTMedia> adativeStream, List<YTMedia> muxedStream, YoutubeMeta meta) {
                    /** Find the download link for suitable format */
                    if (adativeStream.isEmpty()) {
                        LOG("AdativeStream Empty");
                        setFinalNotification(new File("null"));
                        isDownloaded = true;
                        return;
                    }
                    Log.e(TAG, "onExtractionDone: Current Ext: "+currentModel.getExt() );
                    switch (currentModel.getExt()) {
                        case "mp3":
                            currentModel.setExt("mp3");
                            currentModel.setUrl(getAudioStream(adativeStream));
                            break;
                        case "m4a":
                            currentModel.setExt("m4a");
                            currentModel.setUrl(getAudioStream(adativeStream));
                            break;
                        case "1080p":
                            currentModel.setExt("mp4");
                            currentModel.setAudioUrl(getAudioStream(adativeStream));
                            currentModel.setUrl(getVideoStream(adativeStream, 1080));
                            break;
                        case "720p":
                            currentModel.setExt("mp4");
                            currentModel.setAudioUrl(getAudioStream(adativeStream));
                            currentModel.setUrl(getVideoStream(adativeStream, 720));

                            break;
                        case "480p":
                            currentModel.setExt("mp4");
                            currentModel.setAudioUrl(getAudioStream(adativeStream));
                            currentModel.setUrl(getVideoStream(adativeStream, 480));
                            break;
                    }
                    isDownloaded = true;
                }
            }).useDefaultLogin().Extract(currentModel.getVideoID());

            do { LOG("isDownloaded="+isDownloaded); }while (!isDownloaded);

            LOG("Normal Url: "+currentModel.getUrl());
            LOG("Audio Url: "+currentModel.getAudioUrl());
        }

        isDownloaded=false;

        if (currentModel.getUrl()!=null && !currentModel.getUrl().equals("auto-generate")) {
            if (currentModel.getExt().equals("mp3")||currentModel.getExt().equals("m4a")) {
                mp3Task();
            }else if (currentModel.getExt().equals("mp4")) {
                mergeTask();
            }
        }else LOG("Failed autoTask for: "+currentModel.getTargetName());
    }

    private String getAudioStream(List<YTMedia> adativeStream) {
        for (YTMedia media : adativeStream) {
            if (media.getMimeType().contains("audio/mp4")) {
                return media.getUrl();
            }
        }
        return null;
    }

    private String getVideoStream(List<YTMedia> adativeStream,int quality) {
        String backupUri=null; boolean setBackupUri=false;

        for (YTMedia media : adativeStream) {
            if (media.getAudioSampleRate() == 0) {
                if (!setBackupUri) {
                    backupUri = media.getUrl();
                    setBackupUri=true;
                }
                if (media.getHeight()==quality) {
                    return media.getUrl();
                }
            }
        }
        return backupUri;
    }

    @SuppressLint("StaticFieldLeak")
    private void mp3Task() {
        try {
            LOG("Task detected MP3");

            /** Set Image Url first and save to BitMap ICON... */

            String imageUri;
            if (currentModel.getVideoID().contains("soundcloud.com"))
                imageUri = currentModel.getImageUrl();
            else imageUri = YTutils.getImageUrlID_MAX(currentModel.getVideoID());
            setProgress(0, true);
            icon = YTutils.getBitmapFromURL(imageUri);
            if (icon==null && !currentModel.getVideoID().contains("soundcloud.com")) {
                String refreshedUri = YTutils.getImageUrlID_MQ(YTutils.getVideoID_ImageUri(imageUri));
                Log.e(TAG, "mp3Task: Error Image, Refreshing... "+refreshedUri );
                icon = YTutils.getBitmapFromURL(refreshedUri);
                if (icon!=null) {
                    imageUri = refreshedUri;
                }
            }
            setProgress(0, false);

            /*Glide.with(context).asBitmap().load(imageUri).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    icon = resource;
                    setProgress(0, true);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });*/

            /** Set Prefix for path... */
            String prefixName = (currentModel.getTitle().trim() + "_" + currentModel.getChannelTitle().trim())
                    .replace(" ", "_").replace("]", "").replace("[", "")
                    .replace("{", "").replace("}", "").replace("/", "");

            /** This is our download stream... */
            String download_Uri = currentModel.getUrl();

            isDownloaded = false;

            /** Setting some file info... */
            File f = YTutils.getFile("YTPlayer/" + prefixName + ".file");
            if (f.exists()) f.delete();
            File mp3 = YTutils.getFile("YTPlayer/" + prefixName + "." +currentModel.getExt());
            if (mp3.exists()) mp3.delete();
            File dst = YTutils.getFile(Environment.DIRECTORY_DOWNLOADS + "/" + currentModel.getTargetName() + "." + currentModel.getExt());
            if (dst.exists()) dst.delete();

            /** Actually downloading it... */
            PRDownloader.download(download_Uri,
                    YTutils.getFile("YTPlayer").getPath(), f.getName())
                    .build()
                    .setOnProgressListener(progress1 -> {
                        if (currentModel==null) {
                            return;
                        }
                        if (currentModel.getExt().equals("mp3")||currentModel.getExt().equals("m4a"))
                            totalsize = progress1.totalBytes * 2;
                        else {
                            totalsize = progress1.totalBytes;
                        }
                        currentsize = progress1.currentBytes;
                        setProgress((int) (currentsize * 100 / totalsize), false);
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

            Log.e(TAG, "mp3Task: Download Running");

            do { LOG("isDownloaded="+isDownloaded);} while (!isDownloaded);

            LOG("Download Completed");

            YTMeta ytMeta = new YTMeta(context, currentModel.getVideoID());

            Log.e(TAG, "mp3Task: Extension="+currentModel.getExt());

            switch (currentModel.getExt()) {
                case "m4a":
                    processM4A(ytMeta,f,dst);

                    /*dst = YTutils.getFile(Environment.DIRECTORY_DOWNLOADS + "/" + target);
                    try {
                        YTutils.moveFile(f, YTutils.getFile(Environment.DIRECTORY_DOWNLOADS + "/" + target));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                break;

                case "mp3":

                    /** Run ffmpeg... */

                    LOG("Executing FFMPEG");

                    isDownloaded = false;
                    new AsyncTask<File,Void,Void>() {

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            isDownloaded=true;
                            super.onPostExecute(aVoid);
                        }

                        @Override
                        protected Void doInBackground(File... files) {
                            LOG("Running FFMPEG now...");
                            String[] cmd = new String[]{"-i", files[0].getPath(), "-y" ,files[1].getPath()};
                            int rc = FFmpeg.execute(cmd);
                            processRC(rc);
                            return null;
                        }
                    }.execute(f,mp3);
                    try {
                        /** Wait for ffmpeg ffmpeg... */
                        do {
                            if (mp3.exists()) {
                                try {
                                    Thread.sleep(550);
                                } catch (Exception e) {

                                }
                                currentsize = mp3.length() + f.length();
                                setProgress((int) (currentsize * 100 / totalsize), false);
                            }
                        } while (!isDownloaded);
                    } catch (Exception e) {
                        LOG("Error: " + e.getMessage());
                        e.printStackTrace();
                    }

                    LOG("FFMPEG Run complete");

                    /** Set mp3 tags... */

                    setID3Tags(ytMeta,mp3,dst,imageUri);

                    if (!dst.exists()) {
                        LOG("Overriding defaults");
                        File sf = new File(dst.getPath().replace(".mp3", ".m4a"));
                        dst = sf;
                        f.renameTo(sf);
                        Toast.makeText(context, "Failed to convert to mp3, overriding defaults!", Toast.LENGTH_SHORT).show();
                    }
                    addFiletoLocalDevice(dst, currentModel);
                    break;
            }

            /** Show notification... */

            setFinalNotification(dst);

            if (f.exists())
                f.delete();
            if (mp3.exists())
                mp3.delete();
        } catch (Exception e) {
            e.printStackTrace();
            LOG("Error: " + e.getMessage());
        }
    }

    private void setID3Tags(YTMeta ytMeta,File mp3, File dst, String imageUri) {
        MusicMetadataSet src_set = null;
        try {
            src_set = new MyID3().read(mp3);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        if (src_set == null) {
            Log.i("NULL", "NULL");
            mp3.renameTo(dst);
        } else {
            URL uri = null;
            ImageData imageData = null;

            try {
                Log.e(TAG, "doInBackground: ImageUri: " + imageUri);
                uri = new URL(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(uri.openConnection().getInputStream());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] bitmapdata = stream.toByteArray();
                imageData = new ImageData(bitmapdata, "image/jpeg", "background", 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

            MusicMetadata meta = new MusicMetadata(YTutils.getVideoTitle(currentModel.getTitle()));
            if (imageData != null) {
                meta.addPicture(imageData);
            }
            meta.setAlbum(ytMeta.getVideMeta().getAuthor());
            meta.setArtist(YTutils.getChannelTitle(currentModel.getTitle(), currentModel.getChannelTitle()));

            try {
                new MyID3().write(mp3, dst, src_set, meta);

                LOG("MP3 Saved!");

                mp3.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @SuppressLint("StaticFieldLeak")
    private void processM4A(YTMeta ytMeta, File f, File dst) {
        /** Add meta data tags to m4a.. */

        Log.e(TAG, "processM4A: Processing: "+f.getPath()+", Dest: "+dst.getPath() );

        isDownloaded=false;
        new AsyncTask<File,Void,Void>() {
            @Override
            protected void onPostExecute(Void aVoid) {
                isDownloaded=true;
                super.onPostExecute(aVoid);
            }

            @Override
            protected Void doInBackground(File... files) {
                String album = ytMeta.getVideMeta().getAuthor();
                String artist = YTutils.getChannelTitle(currentModel.getTitle(), currentModel.getChannelTitle());

                String[] command = new String[] {"-i",files[0].getPath(),"-metadata","artist="+artist,
                        "-metadata","album="+album,"-y",files[1].getPath()};
                int rc = FFmpeg.execute(command);
                processRC(rc);
                return null;
            }
        }.execute(f,dst);
        try {
            /** Wait for ffmpeg ffmpeg... */
            do {
                if (dst.exists()) {
                    try {
                        Thread.sleep(550);
                    } catch (Exception e) {

                    }
                    currentsize = dst.length() + f.length();
                    setProgress((int) (currentsize * 100 / totalsize), false);
                }
            } while (!isDownloaded);
        } catch (Exception e) {
            LOG("Error: " + e.getMessage());
            e.printStackTrace();
        }
        addFiletoLocalDevice(dst,currentModel);
        //do { LOG("isDownload="+isDownloaded); }while (!isDownloaded);
    }

    private void processRC(int rc) {
        if (rc == RETURN_CODE_SUCCESS) {
            Log.e(Config.TAG, "Command execution completed successfully.");
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.e(Config.TAG, "Command execution cancelled by user.");
        } else {
            Log.e(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        LOG("Task removed");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {

        LOG("ON Destroyed");

        FFmpeg.cancel();
        PRDownloader.cancelAll();
        PRDownloader.shutDown();
        isDownloaded=true;
        pendingJobs.clear();
        totalsize=0;
        currentsize=0;
        progress=0;
        currentModel = null;
        handler.removeCallbacks(updateNotificationTask);
        notificationManagerCompat.cancel(FOREGROUND_ID);
        wakeLock.release();
        super.onDestroy();
    }

    private Runnable updateNotificationTask = new Runnable() {
        @Override
        public void run() {
            LOG("Running... " + progress);
            if (currentModel == null)
                return;
            boolean indeterminate = false;
            if (progress == -1)
                indeterminate = true;
            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Download - " + currentModel.getTitle())
                    .addAction(R.mipmap.ic_launcher, "Cancel", cancelIntent)
                    .setContentText(pendingJobs.size() + 1 + " files downloading")
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setLargeIcon(icon)
                    .setContentIntent(contentIntent)
                    .setProgress(100, progress, indeterminate)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_LOW)
                    .build();

            notificationManagerCompat.notify(FOREGROUND_ID, notification);
            handler.postDelayed(this, 1000);
        }
    };

    private void mergeTask() {

        File audio = YTutils.getFile("/YTPlayer/audio.m4a");
        if (audio.exists()) audio.delete();
        File video = YTutils.getFile("/YTPlayer/video.mp4");
        if (video.exists()) video.delete();
        try {

            isDownloaded=false;

            String imageUri;
            if (currentModel.getVideoID().contains("soundcloud.com"))
                imageUri = currentModel.getImageUrl();
            else imageUri = YTutils.getImageUrlID_MAX(currentModel.getVideoID());

            Glide.with(context).asBitmap().load(imageUri).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    icon = resource;
                    setProgress(0, true);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });

            String audioUrl = currentModel.getAudioUrl();
            String videoUrl = currentModel.getUrl();

            /** Calculate total file size... */
            URL url = new URL(videoUrl);
            URLConnection connection = url.openConnection();
            connection.connect();

            long fileLength = connection.getContentLength();

            /** Download audio file first... */
            url = new URL(audioUrl);
            connection = url.openConnection();
            connection.connect();

            fileLength += connection.getContentLength();
            totalsize = fileLength;

            PRDownloader.download(videoUrl, YTutils.getFile("YTPlayer").getPath(), "video.mp4")
                    .build()
                    .setOnProgressListener(progress -> {
                        currentsize = progress.currentBytes;
                        if (totalsize == 0)
                            return;
                        setProgress((int) (progress.currentBytes * 100 / totalsize), false);
                        oldbytes = currentsize;
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            Log.e(TAG, "onDownloadComplete: Audio Download Complete");

                            /** Download video file now... */
                            PRDownloader.download(audioUrl, YTutils.getFile("YTPlayer").getPath(), "audio.m4a")
                                    .build()
                                    .setOnProgressListener(progress1 -> {
                                        currentsize = oldbytes + progress1.currentBytes;
                                        setProgress((int) ((progress1.currentBytes + oldbytes) * 100 / totalsize), false);
                                    })
                                    .start(new OnDownloadListener() {
                                        @Override
                                        public void onDownloadComplete() {
                                            Log.e(TAG, "onDownloadComplete: Video Download Complete");



                                                   /* muxing(YTutils.getFile("YTPlayer/video.mp4").getPath(),
                                                            YTutils.getFile("YTPlayer/audio.m4a").getPath(),
                                                            save.getPath());*/

                                            /** Show notification... */


                                            isDownloaded = true;
                                        }

                                        @Override
                                        public void onError(Error error) {
                                            isDownloaded = true;
                                        }
                                    });
                        }

                        @Override
                        public void onError(Error error) {
                            isDownloaded = true;
                        }

                    });

            do { LOG("isDownload="+isDownloaded); } while (!isDownloaded);

            File save = YTutils.getFile(Environment.DIRECTORY_DOWNLOADS + "/" + currentModel.getTargetName() + "." + currentModel.getExt());
            if (save.exists()) save.delete();

            if (useFFMPEGmuxer) {
                mux_ffmpeg(YTutils.getFile("YTPlayer/video.mp4").getPath(),
                        YTutils.getFile("YTPlayer/audio.m4a").getPath(),
                        save.getPath());
            }else {
                mux(YTutils.getFile("YTPlayer/video.mp4").getPath(),
                        YTutils.getFile("YTPlayer/audio.m4a").getPath(),
                        save.getPath());
            }

            setFinalNotification(save);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error: " + e.getMessage());
        }

        IntentDownloadService.currentsize=0;
        IntentDownloadService.totalsize=0;
        IntentDownloadService.progress=0;
        IntentDownloadService.currentModel = null;
        isDownloaded=false;

        if (video.exists())
            video.delete();
        if (audio.exists())
            audio.delete();

        Log.e(TAG, "doInBackground: Task Finished");
    }

    private void muxing(String videoFile, String audioFile, String outFile) {

        String outputFile = "";

        try {

            File file = new File(outFile);
            file.createNewFile();
            outputFile = file.getAbsolutePath();
            MediaExtractor videoExtractor = new MediaExtractor();
            videoExtractor.setDataSource(videoFile);
            MediaExtractor audioExtractor = new MediaExtractor();
            audioExtractor.setDataSource(audioFile);
            MediaMuxer muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            videoExtractor.selectTrack(0);
            MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
            int videoTrack = muxer.addTrack(videoFormat);

            audioExtractor.selectTrack(0);
            MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
            int audioTrack = muxer.addTrack(audioFormat);
            boolean sawEOS = false;
            int frameCount = 0;
            int offset = 100;
            int sampleSize = 256 * 1024;
            ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
            ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();


            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            muxer.start();

            while (!sawEOS) {
                videoBufferInfo.offset = offset;
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);


                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                    // Log.d(TAG, "saw input EOS.");
                    sawEOS = true;
                    videoBufferInfo.size = 0;

                } else {
                    videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                    videoBufferInfo.flags = videoExtractor.getSampleFlags();
                    muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                    videoExtractor.advance();


                    frameCount++;

                }
            }

            //  Toast.makeText(getApplicationContext() ,  , Toast.LENGTH_SHORT).show();

            LOG("frame:" + frameCount);


            boolean sawEOS2 = false;
            int frameCount2 = 0;
            while (!sawEOS2) {
                frameCount2++;

                audioBufferInfo.offset = offset;
                audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                    //  Log.d(TAG, "saw input EOS.");
                    sawEOS2 = true;
                    audioBufferInfo.size = 0;
                } else {
                    audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                    audioBufferInfo.flags = audioExtractor.getSampleFlags();
                    muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                    audioExtractor.advance();

                }
            }
            LOG("frame:" + frameCount2);

            muxer.stop();
            muxer.release();


        } catch (IOException e) {
            Log.d(TAG, "Mixer Error 1 " + e.getMessage());
        } catch (Exception e) {
            Log.d(TAG, "Mixer Error 2 " + e.getMessage());
        }
    }

    @SuppressLint("StaticFieldLeak")
    public boolean mux_ffmpeg(String videoFile, String audioFile, String outputFile) {
        isDownloaded = false;
        File video = new File(videoFile);
        File audio = new File(audioFile);
        File outFile = new File(outputFile);
        new AsyncTask<File,Void,Void>() {

            @Override
            protected void onPostExecute(Void aVoid) {
                isDownloaded=true;
                super.onPostExecute(aVoid);
            }

            @Override
            protected Void doInBackground(File... files) {
                // ffmpeg -i audio.acc -i video.h264  -c:v copy -c:a copy -f mp4 -y out.mp4
                String[] cmd = new String[]{"-i", files[0].getPath(),"-i" ,files[1].getPath(),"-c:v","copy","-c:a","copy","-f","mp4","-y",files[2].getPath()};
                int rc = FFmpeg.execute(cmd);
                processRC(rc);
                return null;
            }
        }.execute(video,audio,outFile);
        do {
            Log.e(TAG, "isDownloaded="+isDownloaded );
        } while (!isDownloaded);
        return outFile.exists();
    }

    public boolean mux(String videoFile, String audioFile, String outputFile) {

       try {
           if (!new File(videoFile).exists())
               return false;
           if (!new File(audioFile).exists())
               return false;
       }catch (Exception e) {
           return false;
       }

        Movie video;
        try {
            video = MovieCreator.build(videoFile);
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            return false;
        }

        Movie audio;
        try {

            audio = MovieCreator.build(audioFile);
        } catch (IOException e) {
            LOG("IO Exception");
            e.printStackTrace();
            return false;
        } catch (NullPointerException e) {
            LOG("Null Exception");
            e.printStackTrace();
            return false;
        }
        Track audioTrack = audio.getTracks().get(0);
        video.addTrack(audioTrack);
        Container out = new DefaultMp4Builder().build(video);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        BufferedWritableFileByteChannel byteBufferByteChannel = new BufferedWritableFileByteChannel(fos);
        try {
            out.writeContainer(byteBufferByteChannel);
            byteBufferByteChannel.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    class BufferedWritableFileByteChannel implements WritableByteChannel {
        //    private static final int BUFFER_CAPACITY = 1000000;
        private static final int BUFFER_CAPACITY = 10000000;

        private boolean isOpen = true;
        private final OutputStream outputStream;
        private final ByteBuffer byteBuffer;
        private final byte[] rawBuffer = new byte[BUFFER_CAPACITY];

        private void dumpToFile() {
            try {
                outputStream.write(rawBuffer, 0, byteBuffer.position());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private BufferedWritableFileByteChannel(OutputStream outputStream) {
            this.outputStream = outputStream;
            this.byteBuffer = ByteBuffer.wrap(rawBuffer);
        }

        @Override
        public int write(ByteBuffer inputBuffer) {
            int inputBytes = inputBuffer.remaining();

            if (inputBytes > byteBuffer.remaining()) {
                dumpToFile();
                byteBuffer.clear();

                if (inputBytes > byteBuffer.remaining()) {
                    throw new BufferOverflowException();
                }
            }

            byteBuffer.put(inputBuffer);

            return inputBytes;
        }

        @Override
        public boolean isOpen() {
            return isOpen;
        }

        @Override
        public void close() throws IOException {
            dumpToFile();
            isOpen = false;
        }
    }
}
