package com.kpstv.youtube.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coremedia.iso.boxes.Container;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.ShellCommand;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import com.kpstv.youtube.DownloadActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.YTConfig;
import com.kpstv.youtube.receivers.SongBroadCast;
import com.kpstv.youtube.utils.FileUtils;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

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
import java.util.Random;

import static com.kpstv.youtube.MainActivity.supportFFmpeg;

public class IntentDownloadService extends IntentService {
    private static final String TAG = "IntentDownloadService";
    private boolean isDownloaded=false;
    private PowerManager.WakeLock wakeLock; NotificationManagerCompat notificationManagerCompat;
    private Context context; private String CHANNEL_ID = "channel_01"; Bitmap icon;
    private final int FOREGROUND_ID=109; Notification notification;
    PendingIntent contentIntent,cancelIntent; private long oldbytes;
    private Handler handler;

    /** Some static Declarations */
    public static YTConfig currentModel; public static int progress;
    public static long totalsize; public static ArrayList<YTConfig> pendingJobs;
    public static long currentsize; public static Process process;

    public IntentDownloadService() {
        super("IntentDownloadService");
        setIntentRedelivery(true);
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        pendingJobs = new ArrayList<>();

        /** Setting Power Manager and Wakelock */

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"app:Wakelock");
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

        notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setContentTitle("Download")
                .addAction(R.mipmap.ic_launcher,"Cancel",cancelIntent)
                .setContentText(pendingJobs.size()+" files downloading")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .build();

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            startForeground(FOREGROUND_ID,notification);
        }else {
            notificationManagerCompat.notify(FOREGROUND_ID,notification);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        /** Set notification update handler*/


        currentModel = (YTConfig) intent.getSerializableExtra("addJob");

        if (pendingJobs.size()>0)
            pendingJobs.remove(0);

        try {

            switch (currentModel.getTaskExtra()) {
                case "mp3task":

                    LOG("Task detected MP3");

                    /** Set Image Url first and save to BitMap ICON... */

                    String imageUri;
                    if (currentModel.getVideoID().contains("soundcloud.com"))
                        imageUri = currentModel.getImageUrl();
                    else imageUri = YTutils.getImageUrlID_HQ(currentModel.getVideoID());

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
                    File mp3 = YTutils.getFile("YTPlayer/" + prefixName + ".mp3");
                    if (mp3.exists()) mp3.delete();
                    File dst = YTutils.getFile(Environment.DIRECTORY_DOWNLOADS + "/" + currentModel.getTargetName());
                    if (dst.exists()) dst.delete();

                    /** Actually downloading it... */
                    PRDownloader.download(download_Uri,
                            YTutils.getFile("YTPlayer").getPath(), f.getName())
                            .build()
                            .setOnProgressListener(progress1 -> {
                                if (supportFFmpeg)
                                    totalsize = progress1.totalBytes * 2;
                                else {
                                    totalsize = progress1.totalBytes;
                                }
                                currentsize = progress1.currentBytes;
                                setProgress((int) (currentsize * 100 / totalsize), false);
                                //    publishProgress(());
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

                    LOG("Download Completed");

                    if (!supportFFmpeg) {
                        LOG("FFMPEG not supported");
                        String target = currentModel.getTargetName().replace(".mp3", ".m4a");
                        dst = YTutils.getFile(Environment.DIRECTORY_DOWNLOADS + "/" + target);
                        try {
                            YTutils.moveFile(f, YTutils.getFile(Environment.DIRECTORY_DOWNLOADS + "/" + target));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {

                        /** Setup ffmpeg commands... */
                        String[] cmd = new String[]{"-y", "-i", f.getPath(), mp3.getPath()};
                        String[] ffmpegBinary = new String[]{FileUtils.getFFmpeg(context)};
                        String[] command = FFmpeg.concatenate(ffmpegBinary, cmd);


                        ShellCommand shellCommand = new ShellCommand();

                        LOG("Running ffmpeg");
                        try {
                            /** Run ffmpeg... */
                            process = shellCommand.run(command);
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
                            } while (!YTutils.isProcessCompleted(process));
                        } catch (Exception e) {
                            LOG("Error: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            YTutils.destroyProcess(process);
                        }

                        LOG("FFMPEG Run complete");

                        YTMeta ytMeta = new YTMeta(currentModel.getVideoID());

                        /** Set mp3 tags... */
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

                        if (!dst.exists()) {
                            LOG("Overriding defaults");
                            File sf = new File(dst.getPath().replace(".mp3", ".m4a"));
                            dst = sf;
                            f.renameTo(sf);
                            Toast.makeText(context, "Failed to convert to mp3, overriding defaults!", Toast.LENGTH_SHORT).show();
                        }

                    }


                    /** Show notification... */

                    setFinalNotification(dst);

                    if (f.exists())
                        f.delete();
                    if (mp3.exists())
                        mp3.delete();
                    break;


                case "mergetask":

                    if (currentModel.getVideoID().contains("soundcloud.com"))
                        imageUri = currentModel.getImageUrl();
                    else imageUri = YTutils.getImageUrlID_HQ(currentModel.getVideoID());

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

                    File audio = YTutils.getFile("/YTPlayer/audio.m4a");
                    if (audio.exists()) audio.delete();
                    File video = YTutils.getFile("/YTPlayer/video.download");
                    if (video.exists()) video.delete();

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
                                if (totalsize==0)
                                    return;
                                setProgress((int) (progress.currentBytes * 100 / totalsize),false);
                                oldbytes = currentsize;
                            })
                            .start(new OnDownloadListener() {
                                @Override
                                public void onDownloadComplete() {
                                    Log.e(TAG, "onDownloadComplete: Audio Download Complete" );

                                    /** Download video file now... */
                                    PRDownloader.download(audioUrl,YTutils.getFile("YTPlayer").getPath(),"audio.m4a")
                                            .build()
                                            .setOnProgressListener(progress1 -> {
                                                currentsize = oldbytes+progress1.currentBytes;
                                                setProgress((int) ((progress1.currentBytes + oldbytes) * 100 / totalsize),false);
                                            })
                                            .start(new OnDownloadListener() {
                                                @Override
                                                public void onDownloadComplete() {
                                                    Log.e(TAG, "onDownloadComplete: Video Download Complete" );



                                                   /* muxing(YTutils.getFile("YTPlayer/video.mp4").getPath(),
                                                            YTutils.getFile("YTPlayer/audio.m4a").getPath(),
                                                            save.getPath());*/

                                                    /** Show notification... */


                                                    isDownloaded=true;
                                                }

                                                @Override
                                                public void onError(Error error) {
                                                  isDownloaded=true;
                                                }
                                            });
                                }

                                @Override
                                public void onError(Error error) {
                                   isDownloaded=true;
                                }

                            });

                    do {} while (!isDownloaded);

                    File save = YTutils.getFile(Environment.DIRECTORY_DOWNLOADS+"/"+currentModel.getTargetName());
                    if (save.exists()) save.delete();

                    mux(YTutils.getFile("YTPlayer/video.mp4").getPath(),
                            YTutils.getFile("YTPlayer/audio.m4a").getPath(),
                            save.getPath());

                    setFinalNotification(save);

                   /* if (video.exists())
                        video.delete();
                    if (audio.exists())
                        audio.delete();*/

                    Log.e(TAG, "doInBackground: Task Finished" );
                    break;

            }
        } catch (Exception e) {
            Log.e(TAG,"Error:"+e.getMessage());
        }
    }

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
        if (handler!=null)
            handler.removeCallbacks(updateNotificationTask);
        handler = new Handler();
        handler.post(updateNotificationTask);
    }

    public synchronized void setProgress(int progress, boolean indeterminate) {

        if (indeterminate)
            IntentDownloadService.progress = -1;
        else
            IntentDownloadService.progress = progress;
       /* notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setContentTitle("Download - "+currentModel.getTitle())
                .addAction(R.mipmap.ic_launcher,"Cancel",cancelIntent)
                .setContentText(pendingJobs.size()+1+" files downloading")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setLargeIcon(icon)
                .setContentIntent(contentIntent)
                .setProgress(100,progress,indeterminate)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .build();

        notificationManagerCompat.notify(FOREGROUND_ID,notification);*/
    }

    @Override
    public void onDestroy() {
        currentModel=null;
        handler.removeCallbacks(updateNotificationTask);
        notificationManagerCompat.cancel(FOREGROUND_ID);
        wakeLock.release();
        super.onDestroy();
    }

    private Runnable updateNotificationTask = new Runnable() {
        @Override
        public void run() {
            LOG("Running... "+progress);
            if (currentModel==null)
                return;
            boolean indeterminate=false;
            if (progress==-1)
                indeterminate=true;
            notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                    .setContentTitle("Download - "+currentModel.getTitle())
                    .addAction(R.mipmap.ic_launcher,"Cancel",cancelIntent)
                    .setContentText(pendingJobs.size()+1+" files downloading")
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setLargeIcon(icon)
                    .setContentIntent(contentIntent)
                    .setProgress(100,progress,indeterminate)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_LOW)
                    .build();

            notificationManagerCompat.notify(FOREGROUND_ID,notification);
            handler.postDelayed(this,1000);
        }
    };

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

            while (!sawEOS)
            {
                videoBufferInfo.offset = offset;
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);


                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
                {
                    // Log.d(TAG, "saw input EOS.");
                    sawEOS = true;
                    videoBufferInfo.size = 0;

                }
                else
                {
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
            int frameCount2 =0;
            while (!sawEOS2)
            {
                frameCount2++;

                audioBufferInfo.offset = offset;
                audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
                {
                    //  Log.d(TAG, "saw input EOS.");
                    sawEOS2 = true;
                    audioBufferInfo.size = 0;
                }
                else
                {
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

    public boolean mux(String videoFile, String audioFile, String outputFile) {
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
