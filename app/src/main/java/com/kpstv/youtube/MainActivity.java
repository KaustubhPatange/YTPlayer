package com.kpstv.youtube;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.kpstv.youtube.fragments.DiscoverFragment;
import com.kpstv.youtube.fragments.HistoryBottomSheet;
import com.kpstv.youtube.fragments.HistoryFragment;
import com.kpstv.youtube.fragments.LibraryFragment;
import com.kpstv.youtube.fragments.LocalMusicFragment;
import com.kpstv.youtube.fragments.LocalSearchFragment;
import com.kpstv.youtube.fragments.NCFragment;
import com.kpstv.youtube.fragments.OPlaylistFragment;
import com.kpstv.youtube.fragments.PlaylistFragment;
import com.kpstv.youtube.fragments.SFragment;
import com.kpstv.youtube.fragments.SearchFragment;
import com.kpstv.youtube.fragments.SleepBottomSheet;
import com.kpstv.youtube.fragments.basedOnApi.PopularFragment;
import com.kpstv.youtube.models.MetaModel;
import com.kpstv.youtube.models.NPlayModel;
import com.kpstv.youtube.models.spotify.Track;
import com.kpstv.youtube.models.spotify.Tracks;
import com.kpstv.youtube.services.MusicService;
import com.kpstv.youtube.utils.OnSwipeTouchListener;
import com.kpstv.youtube.utils.SoundCloud;
import com.kpstv.youtube.utils.SpotifyApi;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTSearch;
import com.kpstv.youtube.utils.YTutils;
import com.naveed.ytextractor.model.YTMedia;
import com.spyhunter99.supertooltips.ToolTipManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cat.ereza.customactivityoncrash.config.CaocConfig;

import static android.view.View.VISIBLE;
import static com.kpstv.youtube.services.MusicService.changePlayBack;
import static com.kpstv.youtube.services.MusicService.functionInMainActivity;
import static com.kpstv.youtube.services.MusicService.isplaying;
import static com.kpstv.youtube.services.MusicService.loadedFavFrag;
import static com.kpstv.youtube.services.MusicService.localPlayBack;
import static com.kpstv.youtube.services.MusicService.nPlayModels;
import static com.kpstv.youtube.services.MusicService.onClear;
import static com.kpstv.youtube.services.MusicService.onMake;
import static com.kpstv.youtube.services.MusicService.playNext;
import static com.kpstv.youtube.services.MusicService.playPrevious;
import static com.kpstv.youtube.services.MusicService.selectedItemText;
import static com.kpstv.youtube.services.MusicService.updateMediaSessionPlaybackState;
import static com.kpstv.youtube.services.MusicService.ytIndex;
import static com.kpstv.youtube.utils.SpotifyApi.AUTHORIZATION_REQUEST_CODE;

public class MainActivity extends AppCompatActivity implements AppInterface, SleepBottomSheet.ItemClickListener, HistoryBottomSheet.BottomSheetListener, NCFragment.NoConnectionListener {

    // https://www.googleapis.com/youtube/v3/videos?id=BDocp-VpCwY&key=AIzaSyBYunDr6xBmBAgyQx7IW2qc770aoYBidLw&part=snippet,statistics

    // API KEY= AIzaSyBMqerRAATEnrsfPnWYfeqDdqX0TbR0bEo

    /* https://www.googleapis.com/youtube/v3/search?part=snippet,id&q=tuto&type=video&key=AIzaSyBYunDr6xBmBAgyQx7IW2qc770aoYBidLw

    * Code to launch player
    *
    *           Intent intent = new Intent(MainActivity.this,PlayerActivity.class);
                intent.putExtra("youtubelink",stringarray);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
    *
    * */

    Fragment HistoryFrag;
    static Fragment SearchFrag;
    public static FragmentManager fragmentManager;
    public static Fragment PlaylistFrag, libraryFrag, FavouriteFrag, localMusicFrag, localSearchFrag, popularFrag;
    Fragment NCFrag;
    String ytLink;
    static SharedPreferences preferences;
    public static LinearLayout bottom_player, adViewLayout;
    public static ImageButton actionUp;
    public static ImageButton actionPlay;
    public static ProgressBar loadProgress;
    public static ProgressBar songProgress;
    public static TextView actionTitle;
    public static TextView actionChannelTitle;
    public static AdView adView;
    public static FragmentActivity activity;

    public SpotifyApi spotifyApi;

    static boolean shortCut_loadLocal = false, shortCut_loadSearch = false;

    LinearLayout swipeLayout;
    public static ToolTipManager toolTipManager;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG, "onCreate: MainActivity Started!");

        // Disabling URI exposure coz I don't want to take much efforts...
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();


        activity = MainActivity.this;

        YTutils.commonBilling(this);

        // Remove this code afterwards...
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        Log.e("HeightMatrix", height + "");

        // Set CrashActivity...
        CaocConfig.Builder.create()
                .errorActivity(ErrorActivity.class)
                .apply();

        //TODO: Change app unit id, Sample : ca-app-pub-3940256099942544~3347511713, ca-app-pub-1763645001743174~5602018181
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        // Get required views...
        adView = findViewById(R.id.adView);
        swipeLayout = findViewById(R.id.swipeLayout);
        adViewLayout = findViewById(R.id.adViewLayout);
        bottom_player = findViewById(R.id.bottom_player);
        actionPlay = findViewById(R.id.action_play);
        actionUp = findViewById(R.id.action_maximize);
        actionTitle = findViewById(R.id.action_title);
        actionChannelTitle = findViewById(R.id.action_channelTitle);
        songProgress = findViewById(R.id.songLayoutProgress);
        loadProgress = findViewById(R.id.song_progress);

        toolTipManager = new ToolTipManager(activity);

        if (MusicService.activity == null)
            functionInMainActivity(activity);
        else {
            /** MainActivity was destroyed so we will reinstate it.*/
            onMake();
        }

        // Check onComing links from YouTube or Spotify...
        CheckIntent(getIntent());

        String history = YTutils.readContent(this, "History");
        if (history != null && !history.isEmpty()) {
            Log.e("historyContents", history + "");

            SharedPreferences preferences = getSharedPreferences("history", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("urls", history.replace("&#10;", "").trim());
            editor.apply();
            new File(getFilesDir().toString() + "/History").delete();
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        swipeLayout.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                playPrevious();
            }

            @Override
            public void onSwipeTop() {
                super.onSwipeTop();
                openPlayer();
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                playNext();
            }

            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        actionPlay.setOnClickListener(v -> changePlayBack(!isplaying));
        // bottom_player.setOnClickListener(v-> openPlayer());
        actionUp.setOnClickListener(v -> openPlayer());

        fragmentManager = getSupportFragmentManager();
        localMusicFrag = new LocalMusicFragment();
        localSearchFrag = new LocalSearchFragment();
        HistoryFrag = new HistoryFragment();
        libraryFrag = new LibraryFragment();
        FavouriteFrag = new OPlaylistFragment();
        SearchFrag = new SearchFragment();
        PlaylistFrag = new PlaylistFragment();
        NCFrag = new NCFragment();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (YTutils.isInternetAvailable()) {
            if (getYTUrls("blank").length > 1) {
                if (shortCut_loadLocal) {
                    navigation.setSelectedItemId(R.id.navigation_playlist);
                    loadLocalMusicFrag();
                    bottom_player.setVisibility(View.GONE);
                } else if (shortCut_loadSearch) {
                    bottom_player.setVisibility(View.GONE);
                    navigation.setSelectedItemId(R.id.navigation_search);
                } else loadFragment(HistoryFrag);
            } else navigation.setSelectedItemId(R.id.navigation_search);
        } else {
            loadFragment(NCFrag);
        }
    }

    /**
     * YTUrls here will work as path to music file...
     * <p>
     * Background task will load all the details about music
     * and will set it to player and respective fields.
     */
    public static void PlayVideo_Local(String[] urls) {

        Intent intent = new Intent(activity, MusicService.class);
        intent.setAction("PlayVideo_Local");
        intent.putExtra("urls", urls);
        ContextCompat.startForegroundService(activity, intent);
    }

    public static void PlayVideo_Local(String[] urls, int position) {
        PlayVideo_Local(activity, urls, position);
    }

    public static void PlayVideo_Local(Context context, String[] urls, int position) {
        /** YTUrls here will work as path to music file...
         *
         *  Background task will load all the details about music
         *  and will set it to player and respective fields.
         */

        Intent intent = new Intent(context, MusicService.class);
        intent.setAction("PlayVideo_Local_pos");
        intent.putExtra("urls", urls);
        intent.putExtra("pos", position);
        ContextCompat.startForegroundService(context, intent);
    }

    public static void ChangeVideoOffline(int position) {
        Intent intent = new Intent(activity, MusicService.class);
        intent.setAction("ChangeVideoOffline");
        intent.putExtra("pos", position);
        ContextCompat.startForegroundService(activity, intent);
    }

    public static void PlayVideo(String[] ytUrls) {

        Intent intent = new Intent(activity, MusicService.class);
        intent.setAction("PlayVideo");
        intent.putExtra("urls", ytUrls);
        ContextCompat.startForegroundService(activity, intent);
    }

    public static void PlayVideo(String[] ytUrls, int position) {
        Intent intent = new Intent(activity, MusicService.class);
        intent.setAction("PlayVideo_pos");
        intent.putExtra("urls", ytUrls);
        intent.putExtra("pos", position);
        ContextCompat.startForegroundService(activity, intent);
    }

    public static void ChangeVideo(int position) {
        Intent intent = new Intent(activity, MusicService.class);
        intent.setAction("ChangeVideo");
        intent.putExtra("pos", position);
        ContextCompat.startForegroundService(activity, intent);
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return;
        }

        if (fragment instanceof PopularFragment) {
            loadFragment(SearchFrag);
            return;
        }

        if (fragment instanceof SFragment) {
            loadFragment(SearchFrag);
            return;
        }
        if (fragment instanceof DiscoverFragment) {
            loadFragment(SearchFrag);
            return;
        }

        if (fragment instanceof LocalSearchFragment) {
            loadFragment(localMusicFrag);
            return;
        }

        if (fragment instanceof OPlaylistFragment && fragment.getTag() != null && fragment.getTag().equals("localMusic")) {
            loadFragment(localMusicFrag);
            return;
        }

        if (fragment instanceof OPlaylistFragment && loadedFavFrag) {
            loadedFavFrag = false;
            loadFragment(libraryFrag);
            return;
        }

        if (fragment instanceof OPlaylistFragment) {
            loadFragment(PlaylistFrag);
            return;
        }

        if (fragment instanceof PlaylistFragment) {
            loadFragment(libraryFrag);
            return;
        }

        if (fragment instanceof LocalMusicFragment) {
            loadFragment(libraryFrag);
            return;
        }


        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back once more to exit.", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_history:
                    if (YTutils.isInternetAvailable()) {
                        loadFragment(HistoryFrag);
                    } else loadFragment(NCFrag);
                    return true;
                case R.id.navigation_search:
                    if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof SearchFragment)
                        SearchFrag.onActivityResult(100, 1, null);
                    else
                        loadFragment(SearchFrag);
                    return true;
                case R.id.navigation_playlist:
                    loadFragment(libraryFrag);
                    //                 loadFragment(PlaylistFrag);
                    return true;
            }

            return true;
        }
    };

    public static boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            Log.e("LoadingFragment", "");

            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
            ft.replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        CheckIntent(intent);
    }

    public static void loadPlayFrag() {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PlaylistFrag)
                .commit();
    }

    public static void loadLibraryFrag() {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, libraryFrag)
                .commit();
    }

    public static void loadLocalMusicFrag() {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, localMusicFrag)
                .commit();
    }

    public static void loadSearchFrag() {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SearchFrag)
                .commit();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mUpdateTimeTask);
        stopService(new Intent(this, MusicService.class));
        //    notificationManagerCompat.cancel(1);
        super.onDestroy();
    }

    @Override
    public void onRemoveFromHistory(int position) {
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container)
                instanceof HistoryFragment)
            HistoryFragment.removeFromHistory(position);
        if (MusicService.yturls.size() > 0) {
            try {
                MusicService.yturls.remove(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (nPlayModels.size() > 0) {
            try {
                nPlayModels.remove(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void ReplaceFragment() {
        if (YTutils.isInternetAvailable()) {
            HistoryFrag = new HistoryFragment();
            loadFragment(HistoryFrag);
        } else Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(TextView view) {
        selectedItemText = view.getText().toString();
        switch (selectedItemText) {
            case "5 minutes":
                MusicService.sleepSeconds = 5 * 60;
                break;
            case "10 minutes":
                MusicService.sleepSeconds = 10 * 60;
                break;
            case "15 minutes":
                MusicService.sleepSeconds = 15 * 60;
                break;
            case "30 minutes":
                MusicService.sleepSeconds = 30 * 60;
                break;
            case "45 minutes":
                MusicService.sleepSeconds = 45 * 60;
                break;
            case "60 minutes":
                MusicService.sleepSeconds = 60 * 60;
                break;
            case "End of track":
                MusicService.sleepSeconds = -2;
                break;
            case "None":
                selectedItemText = "";
                MusicService.sleepSeconds = 0;
                break;
        }
        libraryFrag.onActivityResult(100, 0, null);
    }

    void openPlayer() {
        Intent i = new Intent(MainActivity.this, PlayerActivity2.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
       /* i.putExtra("youtubelink",new String[]{ytLink});
        i.putExtra("isNewIntent","true");
        if (enablePlayback)
        i.putExtra("changePlayback",changePlayBack);*/
        startActivity(i);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
    }


    boolean CheckIntent(Intent incoming) {
        shortCut_loadSearch = shortCut_loadLocal = false;
        Log.e(TAG, "CheckIntent: Intent Gotcha...");
        String action = incoming.getAction();

        String data = incoming.getData() != null ? incoming.getData().toString() : null;
        if (data != null && data.equals("localmusic")) {
            shortCut_loadLocal = true;
            return true;
        } else if (data != null && data.equals("searchmusic")) {
            shortCut_loadSearch = true;
            return true;
        }

        if (action != null && action.equals("com.kpstv.youtube.OPEN_SONG")) {
            Log.e(TAG, "CheckIntent: Running in MainActivity...");
            YTutils.openSong(this, incoming);
            return false;
        }

        if (Intent.ACTION_SEND.equals(incoming.getAction())
                && incoming.getType() != null && "text/plain".equals(incoming.getType())) {
            String ytLink = incoming.getStringExtra(Intent.EXTRA_TEXT);
            commonIntentCheck(ytLink);
        } else if (incoming.getData() != null) {
            String url = incoming.getData().toString();
            commonIntentCheck(url);
        }
        return false;

    }

    private static final String TAG = "MainActivity";

    AlertDialog alertDialog;

    @SuppressLint("StaticFieldLeak")
    void commonIntentCheck(String yt) {
        if (yt.startsWith("file://") || yt.startsWith("content://")) {
            String path;
            try {
                path = YTutils.getPath(this, Uri.parse(yt));
            } catch (Exception ex) {
                Uri uri = Uri.parse(yt);
                path = YTutils.ContentProvider.getFilePathFromURI(this, uri);
                Log.e(TAG, "commonIntentCheck: Content-Provider: " + path);
                YTutils.parseDataForPlaylist(activity, path);
                return;
            }
            if (path.endsWith(".txt")) {
                Log.e(TAG, "commonIntentCheck: Path: " + path);
                YTutils.parseDataForPlaylist(activity, path);
                return;
            }
            if (path == null) {
                YTutils.showAlert(MainActivity.this, "Callback Error",
                        "Couldn't parse the path from uri", true);
                return;
            }
            File folder_path = new File(path).getParentFile();

            File[] files = folder_path.listFiles(file -> (file.getPath().endsWith(".mp3") || file.getPath().endsWith(".m4a")
                    || file.getPath().endsWith(".wav") || file.getPath().endsWith(".aac")
                    || file.getPath().endsWith(".ogg") || file.getPath().endsWith(".flac")));

            int position = 0;
            String[] urls = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                urls[i] = files[i].getPath();
                if (files[i].getPath().equals(path))
                    position = i;
            }
            if (files.length > 0)
                PlayVideo_Local(this, urls, position);
            return;
        }
        if (yt.contains("/playlist/") || yt.contains("/album/") || yt.contains("/playlist?")) {
            View v = getLayoutInflater().inflate(R.layout.alert_not_playlist, null);

            alertDialog = new AlertDialog.Builder(this)
                    .setView(v)
                    .setPositiveButton("OK", null)
                    .create();

            alertDialog.show();
            return;
        }
        ytLink = yt;
        if (YTutils.isValidID(yt) || (yt.contains("soundcloud.com"))) {
            if (yt.contains("soundcloud.com")) {
                String[] childs = yt.split("\\s");
                for (String child : childs) {
                    if (child.contains("soundcloud.com")) {
                        ytLink = child;
                        break;
                    }
                }
            } else ytLink = yt;
            if (MusicService.yturls.size() <= 0) {
                PlayVideo(getYTUrls(ytLink), 0);
            } else {
                int insert_pos = ytIndex;
                if (localPlayBack) {
                    Log.e(TAG, "CheckIntent: Running this one");
                    localPlayBack = false;
                    PlayVideo(getYTUrls(ytLink), 0);
                    return;
                }
                if (nPlayModels.size() > 0 && nPlayModels.size() == MusicService.yturls.size()) {
                    if (YTutils.isValidID(ytLink)) {
                        new AsyncTask<Void, Void, Void>() {
                            YTMeta ytMeta;

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                if (ytMeta.getVideMeta() != null) {
                                    NPlayModel model = new NPlayModel(ytLink, ytMeta, false);
                                    for (NPlayModel model1 : nPlayModels) {
                                        if (model1.getUrl().equals(model.getUrl())) {
                                            nPlayModels.remove(model1);
                                            break;
                                        }
                                    }
                                    nPlayModels.add(insert_pos, model);
                                } else
                                    Toast.makeText(activity, "Unexpected parsing error occurred!", Toast.LENGTH_SHORT).show();
                                super.onPostExecute(aVoid);
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                ytMeta = new YTMeta(YTutils.getVideoID(ytLink));
                                return null;
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else if (ytLink.contains("soundcloud.com")) {
                        new AsyncTask<Void, Void, Void>() {
                            SoundCloud.SoundCloudModel model;

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                if (model != null) {
                                    MetaModel metaModel = new MetaModel(model.getNormalUrl(), model.getTitle(), model.getAuthorName(),
                                            model.getImageUrl());
                                    NPlayModel nPlayModel = new NPlayModel(ytLink, new YTMeta(metaModel), false);
                                    for (NPlayModel model1 : nPlayModels) {
                                        if (model1.getUrl().equals(nPlayModel.getUrl())) {
                                            nPlayModels.remove(model1);
                                            break;
                                        }
                                    }
                                    nPlayModels.add(insert_pos, nPlayModel);
                                } else
                                    Toast.makeText(activity, "Unexpected parsing error occurred!", Toast.LENGTH_SHORT).show();
                                super.onPostExecute(aVoid);
                            }

                            @Override
                            protected Void doInBackground(Void... voids) {
                                SoundCloud soundCloud = new SoundCloud(ytLink);
                                if (soundCloud.getModel() != null)
                                    model = soundCloud.getModel();
                                return null;
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
                String ytID = YTutils.getVideoID(ytLink);
                for (String yturl : MusicService.yturls) {
                    if (YTutils.getVideoID(yturl).equals(ytID)) {
                        MusicService.yturls.remove(yturl);
                        break;
                    }
                }
                MusicService.yturls.add(insert_pos, ytLink);
                ChangeVideo(insert_pos);
            }
        } else if (ytLink.contains("open.spotify.com") && ytLink.contains("/track/")) {
            String SpotifyId = YTutils.getSpotifyID(ytLink);

            if (SpotifyId == null) {
                YTutils.showAlert(MainActivity.this, "Spotify Error",
                        "The requested url is not a supported url", true);
                return;
            }

            spotifyApi = new SpotifyApi(this);

            spotifyApi.getTrackDetail(SpotifyId, new SpotifyApi.ResponseAction<Track>() {
                @Override
                public void onComplete(Track track) {
                    Tracks tracks = track.getTracks().get(0);
                    if (tracks.getArtists() != null) {
                        new makeSpotifyData(tracks).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    } else
                        Toast.makeText(MainActivity.this, "Error: Track is invalid", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(@NotNull Exception e) {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


        } /*else if (ytLink.contains("soundcloud.com")) {
            Log.e(TAG, "commonIntentCheck: Working here..." );
          //  new soundCloudData(ytLink).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }*/ else {
            YTutils.showAlert(MainActivity.this, "Callback Error",
                    "The requested url is not a valid YouTube url", true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AUTHORIZATION_REQUEST_CODE)
            spotifyApi.processResponse(requestCode, resultCode, data,
                    new SpotifyApi.ResponseAction<SpotifyApi.AuthResponse>() {
                        @Override
                        public void onComplete(SpotifyApi.AuthResponse authResponse) {
                            Toast.makeText(MainActivity.this, "Response Successful", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(@NotNull Exception e) {
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Implementing a new player within main activity itself...
     */

    static Handler mHandler = new Handler();

    public static void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    public static Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = MusicService.player.getDuration();
            long currentDur = MusicService.player.getCurrentPosition();
            int progress = (YTutils.getProgressPercentage(currentDur, totalDuration));
            songProgress.setProgress(progress);

            updateMediaSessionPlaybackState();
            //mMediaSessionCallback.onSeekTo(currentDur);

            mHandler.postDelayed(this, 100);
        }
    };

    static String[] getYTUrls(String to_inject_yturl) {
        preferences = activity.getSharedPreferences("history", MODE_PRIVATE);
        String data = YTutils.readContent(activity, "history.csv");
        // String line = preferences.getString("urls","");
        if (data != null && !data.isEmpty()) {
            String[] lines = data.split("\n|\r");

            ArrayList<String> arrayList = new ArrayList<>();

            Log.e(TAG, "getYTUrls: Injected uri 0: " + to_inject_yturl);
            arrayList.add(to_inject_yturl);
            int i = 1;
            for (String l : lines) {
                l = l.split("\\|")[0];
                if (l.isEmpty()) continue;
                if (YTutils.getVideoID(l).equals(YTutils.getVideoID(to_inject_yturl))) continue;
                arrayList.add(l);
                Log.e(TAG, "getYTUrls: Injected uri " + i + ": " + l);
//                i++;
            }
            nPlayModels.clear();

            return YTutils.convertListToArrayMethod(arrayList);
        } else {
            String[] yt_urls = new String[1];
            yt_urls[0] = to_inject_yturl;
            return yt_urls;
        }
    }

    static class soundCloudData extends AsyncTask<Void, Void, Void> {
        String link, ytLink;
        SoundCloud.SoundCloudModel model;

        public soundCloudData(String link) {
            this.link = link;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.e(TAG, "onPostExecute: Comming now...");
           /* if (ytLink!=null) {
                if (yturls.size()<=0) {
                    PlayVideo(getYTUrls(ytLink),0);
                } else {
                    int insert_index = ytIndex;
                    if (nPlayModels.size()>0 && nPlayModels.size()==yturls.size()) {
                        MetaModel metaModel = new MetaModel(model.getTitle(),model.getAuthorName(),model.getImageUrl());
                        NPlayModel model = new NPlayModel(ytLink,new YTMeta(metaModel),true);
                        nPlayModels.add(insert_index,model);
                    }
                    yturls.remove(ytLink);
                    yturls.add(insert_index,ytLink);
                    ChangeVideo(insert_index);
                }
            }else {
                Toast.makeText(activity, "Couldn't parse this Spotify url", Toast.LENGTH_SHORT).show();
            }*/
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SoundCloud soundCloud = new SoundCloud(link);
            soundCloud.captureViews();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bottom_player.setVisibility(VISIBLE);
        }
    }

    static class makeSpotifyData extends AsyncTask<Void, Void, Void> {
        // SpotifyTrack track;
        YTSearch search;
        Tracks tracks;
        String ytLink, author = "unknown";

        public makeSpotifyData(Tracks tracks) {
            this.tracks = tracks;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (ytLink != null) {
                if (MusicService.yturls.size() <= 0) {
                    PlayVideo(getYTUrls(ytLink), 0);
                } else {

                    if (tracks.getArtists() != null && tracks.getArtists().size() > 0)
                        author = tracks.getArtists().get(0).getName();

                    int insert_index = ytIndex;
                    if (nPlayModels.size() > 0 && nPlayModels.size() == MusicService.yturls.size()) {
                        MetaModel metaModel = new MetaModel(YTutils.getVideoID(
                                ytLink),
                                tracks.getName(),
                                author,
                                YTutils.getImageUrl(ytLink)
                        );
                        NPlayModel model = new NPlayModel(ytLink, new YTMeta(metaModel), true);
                        nPlayModels.add(insert_index, model);
                    }
                    if (MusicService.yturls.contains(ytLink))
                        MusicService.yturls.remove(ytLink);
                    MusicService.yturls.add(insert_index, ytLink);
                    ChangeVideo(insert_index);
                }
            } else {
                Toast.makeText(activity, "Couldn't parse this Spotify url", Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onPreExecute() {
            onClear();
            bottom_player.setVisibility(VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String query = tracks.getName();
            if (tracks.getArtists() != null && tracks.getArtists().size() > 0)
                query = tracks.getName() +" by " + tracks.getArtists().get(0).getName();
            search = new YTSearch(query);
            if (search.getVideoIDs() != null && search.getVideoIDs().size() > 0)
                ytLink = YTutils.getYtUrl(search.getVideoIDs().get(0));
            return null;
        }
    }

    static int command = 0;


    static void makePlay() {
        actionPlay.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_play_circle));
        try {
            PlayerActivity2.makePlay();
        } catch (Exception ignored) {
            Log.e("PlayerActivity", "isnull");
        }
    }

    static void makePause() {
        actionPlay.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_pause_circle));
        try {
            PlayerActivity2.makePause();
        } catch (Exception ignored) {
            Log.e("PlayerActivity", "isnull");
        }
    }

    private static List<YTMedia> getBestStream(List<YTMedia> streams) {
        List<YTMedia> medias = new ArrayList<>();
        for (int i = 0; i < streams.size(); i++) {
            YTMedia media = streams.get(i);
            if (media.getHeight() != -1) {
                int j = 0;
                while (j < streams.size()) {
                    YTMedia media1 = streams.get(j);
                    if (media.getQuality().equals(media1.getQuality())) {
                        int m1 = media.getBitrate();
                        int m2 = media1.getBitrate();
                        if (m2 > m1) {
                            media = media1;
                        }
                    }
                    j++;
                }
                if (!medias.contains(media)) medias.add(media);
            } else {
                medias.add(media);
            }
        }
        return medias;
    }


    static void showAlert(String title, String message, boolean isalert) {
        int icon = android.R.drawable.ic_dialog_info;
        if (isalert) icon = android.R.drawable.ic_dialog_alert;
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    bottom_player.setVisibility(View.GONE);
                })
                .setIcon(icon)
                .show();

    }



   /* private void setListener() {
        // Play or Pause listener
        Intent newintent = new Intent(MainActivity.this, MainActivity.class);
        newintent.putExtra("DO", "play");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, newintent, 0);

        expandedView.setOnClickPendingIntent(R.id.nPlay, pendingIntent);
        collpaseView.setOnClickPendingIntent(R.id.nPlay, pendingIntent);

        // Next song Listener
        newintent = new Intent(MainActivity.this, MainActivity.class);
        newintent.putExtra("DO", "next");
        pendingIntent = PendingIntent.getActivity(this, 1, newintent, 0);

        expandedView.setOnClickPendingIntent(R.id.nForward, pendingIntent);
        collpaseView.setOnClickPendingIntent(R.id.nForward, pendingIntent);

        // Previous song Listener
        newintent = new Intent(MainActivity.this, MainActivity.class);
        newintent.putExtra("DO", "previous");
        pendingIntent = PendingIntent.getActivity(this, 2, newintent, 0);

        expandedView.setOnClickPendingIntent(R.id.nPrevious, pendingIntent);
        collpaseView.setOnClickPendingIntent(R.id.nPrevious, pendingIntent);

        // Add to playlist Listener
        newintent = new Intent(MainActivity.this, MainActivity.class);
        newintent.putExtra("DO", "add");
        pendingIntent = PendingIntent.getActivity(this, 3, newintent, 0);

        expandedView.setOnClickPendingIntent(R.id.nAdd, pendingIntent);

        // Focus on Click Listener
        newintent = new Intent(PlayerActivity.this, PlayerActivity.class);
        newintent.putExtra("DO", "focus");
        pendingIntent = PendingIntent.getActivity(this, 4, newintent, 0);

        expandedView.setOnClickPendingIntent(R.id.mainlayout, pendingIntent);
        collpaseView.setOnClickPendingIntent(R.id.mainlayout, pendingIntent);
    }*/
}


