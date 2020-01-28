
package com.kpstv.youtube.services;
/*
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coremedia.iso.boxes.Container;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.kpstv.libffmpeg.ExecuteBinaryResponseHandler;
import com.kpstv.libffmpeg.FFmpeg;

import com.kpstv.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.kpstv.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.android.exoplayer2.util.NotificationUtil;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.kpstv.youtube.DownloadActivity;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.YTConfig;
import com.kpstv.youtube.receivers.SongBroadCast;
import com.kpstv.youtube.utils.FileUtils;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
import java.util.Random;

import static com.kpstv.youtube.MainActivity.notificationManagerCompat;
import static com.kpstv.youtube.MainActivity.supportFFmpeg;

public class DownloadService extends Service {

    public static String TAG = "DownloadService";
    public static ArrayList<YTConfig> pendingJobs;
    private static String CHANNEL_ID = "channel_01";
    public static long totalsize;
    public static long currentsize; NotificationManagerCompat notificationManager;
    public static Process process; Bitmap icon;
    public static YTConfig currentModel; long oldbytes;
    boolean isDownloaded=false;
    Context context; YTMeta ytMeta;long total=0; public static int progress;
    Notification notification; Handler handler;
    public static AsyncTask<Void,Integer,Void> downloadTask;PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

        if (pendingJobs==null) pendingJobs = new ArrayList<>();

        icon = MainActivity.bitmapIcon;

        notificationManager = NotificationManagerCompat.from(context);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "YTPlayer:wakelock");
        wakeLock.acquire();

        Intent notificationIntent = new Intent(this, DownloadActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Intent newintent = new Intent(context, SongBroadCast.class);
        newintent.setAction("com.kpstv.youtube.STOP_SERVICE");

        PendingIntent stopservicePending =
                PendingIntent.getBroadcast(context, 5, newintent, 0);


        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Download")
                .addAction(R.mipmap.ic_launcher,"Cancel",stopservicePending)
                .setContentText(pendingJobs.size()+1+" files downloading")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_LOW)
                .build();

        startForeground(105, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        YTConfig model = (YTConfig) intent.getSerializableExtra("addJob");
        if (model!=null) {
            pendingJobs.add(model);

            if (pendingJobs.size()==1 && currentModel==null) {
                // Start the first job...
                downloadTask = new parseData(pendingJobs.get(0));
                pendingJobs.remove(0);
                downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }

        handler = new Handler();
        handler.postDelayed(updateTask,550);

        return START_NOT_STICKY;
    }

    public Runnable updateTask = new Runnable() {
        @Override
        public void run() {

            if (currentModel==null) stopSelf();

          */
/*  if (pendingJobs.size()>0) {
                Log.e(TAG, "doInBackground: Next JOB: "+pendingJobs.get(0).getTaskExtra());
            }//else Log.e(TAG, "doInBackground: NO JOB" );*//*


            if (pendingJobs.size()>0 && (downloadTask!=null && downloadTask.getStatus() != AsyncTask.Status.RUNNING))
            {
                downloadTask = new parseData(pendingJobs.get(0));
                pendingJobs.remove(0);
                downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }

            if (currentModel!=null) {

                Intent notificationIntent = new Intent(context, DownloadActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context,
                        0, notificationIntent, 0);

                Intent newintent = new Intent(context, SongBroadCast.class);
                newintent.setAction("com.kpstv.youtube.STOP_SERVICE");

                PendingIntent stopservicePending =
                        PendingIntent.getBroadcast(context, 5, newintent, 0);

                int max=100,incr;
                if (progress==-1) {
                    max=0;incr=0;
                }else {
                    incr = progress;
                }

                notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSubText(pendingJobs.size()+1+" files downloading")
                        .setContentTitle(currentModel.getTitle()+" - "+currentModel.getChannelTitle())
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setOngoing(true)
                        .setContentIntent(pendingIntent)
                        .setLargeIcon(icon)
                        .addAction(R.mipmap.ic_launcher,"Cancel",stopservicePending)
                        .setProgress(max,incr,false)
                        .build();

                notificationManager.notify(105,notification);
              //  appnotification.notify(105,notification);
            }
            handler.postDelayed(updateTask,1000);
        }
    };

    void checkforpending() {
        if (pendingJobs.size()<=0)
            currentModel=null;
        progress=0;
        currentsize=0;
        totalsize=0;
    }

    class parseData extends AsyncTask<Void,Integer,Void> {
        YTConfig model;
        public parseData(YTConfig model) {
            this.model = model;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progress = values[0];
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            String filePath = YTutils.getFile(Environment.DIRECTORY_DOWNLOADS+"/"+model.getTargetName()).getPath();

            Intent openSong = new Intent(context, SongBroadCast.class);
            openSong.setAction("com.kpstv.youtube.OPEN_SONG");
            openSong.putExtra("fx_path",filePath);

            Intent openShare = new Intent(context, SongBroadCast.class);
            openShare.setAction("com.kpstv.youtube.OPEN_SHARE_SONG");
            openShare.putExtra("fx_path",filePath);

            PendingIntent opensongService =
                    PendingIntent.getBroadcast(context, 6, openSong, 0);

            PendingIntent openshareService =
                    PendingIntent.getBroadcast(context, 7, openShare, 0);

            Notification not = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Download Complete")
                    .setContentText(model.getTitle()+" - "+model.getChannelTitle())
                    .setSmallIcon(R.drawable.ic_check)
                    .setContentIntent(opensongService)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_LOW)
                    .addAction(R.mipmap.ic_launcher,"Share",openshareService)
                    .build();

            Log.e(TAG, "onPostExecute: Intent Extra: "+openSong.getStringExtra("fx_path") );

            int nos = new Random().nextInt(400)+150;
            Log.e(TAG, "onPostExecute: Notification number: "+ nos);
            notificationManagerCompat.notify(nos,not);
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onPreExecute() {

            PRDownloader.initialize(context);

            // Setting timeout globally for the download network requests:
            PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                    .setDatabaseEnabled(true)
                    .setReadTimeout(30_000)
                    .setConnectTimeout(30_000)
                    .build();
            PRDownloader.initialize(context, config);

            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            switch (model.getTaskExtra()) {
                case "mp3Task":
                    String imageUri=null;
                    if (model.getVideoID().contains("soundcloud.com"))
                        imageUri = model.getImageUrl();
                    else imageUri = YTutils.getImageUrlID_HQ(model.getVideoID());
                    try {
                        Glide.with(context).asBitmap().load(imageUri).into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                icon = resource;
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });

                        currentModel = model;
                        String prefixName = (model.getTitle().trim()+"_"+model.getChannelTitle().trim())
                                .replace( " ","_").replace("]","").replace("[","")
                                .replace("{","").replace("}","").replace("/","");
                        //prefixName = "audio";
                        File mp3 = YTutils.getFile("YTPlayer/"+prefixName+".mp3");
                        if (mp3.exists()) mp3.delete();
                        File f = YTutils.getFile("YTPlayer/"+prefixName+".file");
                        if (f.exists()) f.delete();

                        String audioUrl = model.getUrl(); // This is basically a DownloadUrl...

                        */
/** Download audio file first...*//*


                        isDownloaded=false;

                        PRDownloader.download(audioUrl,YTutils.getFile("YTPlayer").getPath(),f.getName())
                                .build()
                                .setOnProgressListener(progress1 -> {
                                    if (supportFFmpeg)
                                        totalsize = progress1.totalBytes*2;
                                    else {
                                        totalsize = progress1.totalBytes;
                                    }
                                    currentsize = progress1.currentBytes;
                                    publishProgress(((int) (currentsize * 100 / totalsize)));
                                })
                                .start(new OnDownloadListener() {
                                    @Override
                                    public void onDownloadComplete() {
                                        isDownloaded=true;
                                        Log.e(TAG, "onDownloadComplete: Completed" );
                                    }

                                    @Override
                                    public void onError(Error error) {
                                        isDownloaded=true;
                                    }
                                });

                        Log.e(TAG, "doInBackground: Just before this" );
                        do {
                            //  Log.e(TAG, "doInBackground: Doing in background" );
                        } while (!isDownloaded);

                        Log.e(TAG, "doInBackground: Loading before");

                        if (!supportFFmpeg) {
                            Log.e(TAG, "doInBackground: Not supported ffmpeg");
                            // Change extension...
                            String target = model.getTargetName().replace(".mp3",".m4a");
                            try {
                                YTutils.moveFile(f,YTutils.getFile(Environment.DIRECTORY_DOWNLOADS+"/"+target));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            checkforpending();
                            return null;
                        }

                        Log.e(TAG, "doInBackground: About to convert");

                        */
/** Convert the audio file to mp3...*//*


                        String cmd[] = new String[] { "-y","-i",f.getPath(),mp3.getPath() };
                        String[] ffmpegBinary = new String[] { FileUtils.getFFmpeg(context) };
                        String[] command = concatenate(ffmpegBinary, cmd);

                        Log.e(TAG, "doInBackground: Command: ");
                        for(int i=0;i<command.length;i++)
                            Log.e(TAG, command[i]+" ");

                        ShellCommand shellCommand = new ShellCommand();

                        try {
                            Log.e(TAG, "doInBackground: Executing command" );
                            process = shellCommand.run(command);

                            ytMeta = new YTMeta(model.getVideoID());

                            do {
                                if (mp3.exists()) {
                                    try {
                                        Thread.sleep(550);
                                    }catch (Exception e){}
                                    total = mp3.length()+f.length();
                                    currentsize = total;
                                    publishProgress(((int) (total * 100 / totalsize)));
                                }
                            }while (!YTutils.isProcessCompleted(process));
                            Log.e(TAG, "doInBackground: Execution done" );
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        finally {
                            YTutils.destroyProcess(process);
                        }

                        Log.e(TAG, "onSuccess: ONCompleted" );

                        File dst = YTutils.getFile(Environment.DIRECTORY_DOWNLOADS+"/"+model.getTargetName());
                        MusicMetadataSet src_set = null;
                        try {
                            src_set = new MyID3().read(mp3);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        if (src_set == null)
                        {
                            Log.i("NULL", "NULL");
                            mp3.renameTo(dst);
                        }
                        else
                        {
                            URL uri = null; ImageData imageData=null;

                            try {
                                Log.e(TAG, "doInBackground: ImageUri: "+imageUri );
                                uri = new URL(imageUri);
                                Bitmap bitmap = BitmapFactory.decodeStream(uri.openConnection().getInputStream());
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                byte[] bitmapdata = stream.toByteArray();
                                imageData = new ImageData(bitmapdata,"image/jpeg","background",1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            MusicMetadata meta = new MusicMetadata(YTutils.getVideoTitle(model.getTitle()));
                            if (imageData!=null) {
                                meta.addPicture(imageData);
                            }
                            meta.setAlbum(ytMeta.getVideMeta().getAuthor());
                            meta.setArtist(YTutils.getChannelTitle(model.getTitle(),model.getChannelTitle()));
                            try {
                                new MyID3().write(mp3, dst, src_set, meta);
                                mp3.delete();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }

                        if (!dst.exists()) {
                            File sf = new File(dst.getPath().replace(".mp3",".m4a"));
                            f.renameTo(sf);
                            Toast.makeText(context, "Failed to convert to mp3, overriding defaults!", Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "doInBackground: Error: "+e.getMessage() );
                        e.printStackTrace();
                    }

                    checkforpending();

                    Log.e(TAG, "doInBackground: Task ended" );
                    break;

                case "mergeTask":
                    try {
                        currentModel = model;

                        String audioUrl = model.getAudioUrl();
                        String videoUrl = model.getUrl();

                        File audio = YTutils.getFile("/YTPlayer/audio.download");
                        if (audio.exists()) audio.delete();
                        File video = YTutils.getFile("/YTPlayer/video.download");
                        if (video.exists()) video.delete();

                        */
/** Calculate total file size... *//*

                        URL url = new URL(videoUrl);
                        URLConnection connection = url.openConnection();
                        connection.connect();

                        long fileLength = connection.getContentLength();

                        */
/** Download audio file first... *//*

                        url = new URL(audioUrl);
                        connection = url.openConnection();
                        connection.connect();

                        fileLength += connection.getContentLength();
                        totalsize = fileLength;

                        PRDownloader.download(videoUrl, YTutils.getFile("YTPlayer").getPath(), "video.download")
                                .build()
                                .setOnProgressListener(progress -> {
                                    currentsize = progress.currentBytes;
                                    if (totalsize==0)
                                        return;
                                    publishProgress((int) (progress.currentBytes * 100 / totalsize));
                                    oldbytes = currentsize;
                                })
                                .start(new OnDownloadListener() {
                                    @Override
                                    public void onDownloadComplete() {
                                        Log.e(TAG, "onDownloadComplete: Audio Download Complete" );

                                        */
/** Download video file now... *//*

                                        PRDownloader.download(audioUrl,YTutils.getFile("YTPlayer").getPath(),"audio.download")
                                                .build()
                                                .setOnProgressListener(progress1 -> {
                                                    currentsize = oldbytes+progress1.currentBytes;
                                                    publishProgress((int) ((progress1.currentBytes + oldbytes) * 100 / totalsize));
                                                })
                                                .start(new OnDownloadListener() {
                                                    @Override
                                                    public void onDownloadComplete() {
                                                        File save = YTutils.getFile(Environment.DIRECTORY_DOWNLOADS+"/"+model.getTargetName());
                                                        if (save.exists()) save.delete();

                                                        mux("/sdcard/YTPlayer/video.download","/sdcard/YTPlayer/audio.download",
                                                                save.getPath());

                                                        checkforpending();

                                                        isDownloaded=true;
                                                    }

                                                    @Override
                                                    public void onError(Error error) {
                                                        checkforpending();isDownloaded=true;
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onError(Error error) {
                                        checkforpending();isDownloaded=true;
                                    }

                                });

                        do {} while (!isDownloaded);

                        Log.e(TAG, "doInBackground: Task Finished" );

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }

            return null;
        }

        public boolean mux(String videoFile, String audioFile, String outputFile) {
            Movie video;
            try {
                video = new MovieCreator().build(videoFile);
            } catch (RuntimeException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            Movie audio;
            try {

                audio = new MovieCreator().build(audioFile);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (NullPointerException e) {
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
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: Service Stopped" );
        currentModel=null;
        if (downloadTask!=null && downloadTask.getStatus() == AsyncTask.Status.RUNNING)
            downloadTask.cancel(true);
        if (!Util.isProcessCompleted(process))
            process.destroy();
        wakeLock.release();
        handler.removeCallbacks(updateTask);
        notificationManager.cancel(105);
       // appnotification.cancel(105);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
*/
