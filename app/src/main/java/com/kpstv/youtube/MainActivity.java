package com.kpstv.youtube;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.kpstv.youtube.fragments.DiscoverFragment;
import com.kpstv.youtube.fragments.HistoryFragment;
import com.kpstv.youtube.fragments.NCFragment;
import com.kpstv.youtube.fragments.OPlaylistFragment;
import com.kpstv.youtube.fragments.PlaylistFragment;
import com.kpstv.youtube.fragments.SearchFragment;
import com.kpstv.youtube.utils.SpotifyTrack;
import com.kpstv.youtube.utils.YTSearch;
import com.kpstv.youtube.utils.YTutils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import cat.ereza.customactivityoncrash.config.CaocConfig;

import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements HistoryBottomSheet.BottomSheetListener, NCFragment.NoConnectionListener {

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
    static FragmentManager fragmentManager;
    static Fragment PlaylistFrag;
    Fragment NCFrag; String ytLink;
    SharedPreferences preferences;
    LinearLayout bottom_player,adViewLayout;
    ImageButton actionUp,actionPlay;
    TextView actionTitle; AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Disabling URI exposure coz I don't want to take much efforts...
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        // Remove this code afterwards...
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        Log.e("HeightMatrix",height+"");

        // Set CrashActivity...
        CaocConfig.Builder.create()
                .errorActivity(ErrorActivity.class)
                .apply();

        //TODO: Change app unit id, Sample : ca-app-pub-3940256099942544~3347511713, ca-app-pub-1763645001743174~5602018181
        MobileAds.initialize(this, "ca-app-pub-1763645001743174~5602018181");

        // Get required views...
        adView = findViewById(R.id.adView);
        adViewLayout = findViewById(R.id.adViewLayout);
        bottom_player = findViewById(R.id.bottom_player);
        actionPlay = findViewById(R.id.action_play);
        actionUp = findViewById(R.id.action_maximize);
        actionTitle = findViewById(R.id.action_title);

        // Check onComing links from YouTube or Spotify...
        CheckIntent(getIntent());

        String history = YTutils.readContent(this,"History");
        if (history !=null && !history.isEmpty()) {
            Log.e("historyContents",history+"");

            SharedPreferences preferences = getSharedPreferences("history",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("urls",history.replace("&#10;","").trim());
            editor.apply();
            new File(getFilesDir().toString()+"/History").delete();
        }

        preferences = getSharedPreferences("history",MODE_PRIVATE);
        String list = preferences.getString("urls","");
        ArrayList<String> urls = new ArrayList<>();
        if (!Objects.requireNonNull(list).isEmpty()) {
            urls.addAll(Arrays.asList(list.split(",")));
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        /*// Disabling URI exposure for lame android 7.0+ who can't do by themselves.
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }*/

        fragmentManager = getSupportFragmentManager();
        HistoryFrag = new HistoryFragment();
        SearchFrag = new SearchFragment();
        PlaylistFrag = new PlaylistFragment();
        NCFrag = new NCFragment();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (YTutils.isInternetAvailable())
            loadFragment(HistoryFrag);
        else {
            loadFragment(NCFrag);
        }

    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof DiscoverFragment) {
            loadFragment(SearchFrag);
            return;
        }
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof OPlaylistFragment) {
            loadFragment(PlaylistFrag);
            return;
        }
        finish();
    }

    Uri getFileUri(Context context, File file) {
        return FileProvider.getUriForFile(context,
                context.getApplicationContext().getPackageName() + ".authorityStr"
                , file);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_history:
                    if (YTutils.isInternetAvailable()) {
                        loadFragment(HistoryFrag);
                    }else loadFragment(NCFrag);
                    return true;
                case R.id.navigation_search:
                    loadFragment(SearchFrag);
                    return true;
                case R.id.navigation_playlist:
                    loadFragment(PlaylistFrag);
                    return true;
            }

            return true;
        }
    };

    public boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            Log.e("LoadingFragment","");

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
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

    public static void loadSearchFrag() {
       fragmentManager.beginTransaction()
               .replace(R.id.fragment_container, SearchFrag)
               .commit();
    }

    @Override
    protected void onDestroy() {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.deleteNotificationChannel("channel_01");
        }
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(1);
       try {
           if (PlayerActivity.datasync.getStatus() == AsyncTask.Status.RUNNING)
               PlayerActivity.datasync.cancel(true);
           PlayerActivity.player.stop();
           PlayerActivity.player.release();

           PlayerActivity.mHandler.removeCallbacks(PlayerActivity.mUpdateTimeTask);
       }catch (Exception e) { e.printStackTrace(); }
        super.onDestroy();
    }

    @Override
    public void onRemoveFromHistory(int position) {
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container)
                instanceof HistoryFragment)
        HistoryFragment.removeFromHistory(position);
    }

    @Override
    public void ReplaceFragment() {
        if (YTutils.isInternetAvailable()) {
            HistoryFrag = new HistoryFragment();
            loadFragment(HistoryFrag);
        } else Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
    }

    void openPlayer(boolean enablePlayback,String changePlayBack) {
        Intent i=new Intent(MainActivity.this,PlayerActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra("youtubelink",new String[]{ytLink});
        i.putExtra("isNewIntent","true");
        if (enablePlayback)
        i.putExtra("changePlayback",changePlayBack);
        startActivity(i);
        overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
    }

    void CheckIntent(Intent incoming) {
        ytLink = incoming.getStringExtra("yturl");
        Log.e("YouTubeUrl",ytLink+"");
        String playerCheck = incoming.getStringExtra("is_playing");
        if (playerCheck!=null) {
            bottom_player.setVisibility(View.VISIBLE);
            actionTitle.setSelected(true);
            actionTitle.setText(incoming.getStringExtra("b_title"));
            bottom_player.setOnClickListener(v -> openPlayer(false,null));
            actionUp.setOnClickListener(v -> openPlayer(false,null));
            if (playerCheck.equals("true")) {
                actionPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_circle));
                actionPlay.setOnClickListener(v -> openPlayer(true,"true"));
            }else {
                actionPlay.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_circle));
                actionPlay.setOnClickListener(v -> openPlayer(true,"false"));
            }
            adViewLayout.setVisibility(VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        }

        if (incoming.getData()!=null) {
            String ytLink = incoming.getData().toString();
            Log.e("IntentYTLink",ytLink+"");
            if (YTutils.isValidID(ytLink)){
                Intent intent = new Intent(MainActivity.this,PlayerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("youtubelink",new String[] {ytLink});
                startActivityForResult(intent,200);
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
            }else if (ytLink.contains("open.spotify.com")&&ytLink.contains("/track/")) {
                new MainActivity.getData(ytLink).execute();
            }else {
                YTutils.showAlert(MainActivity.this,"Callback Error",
                        "The requested url is not a valid YouTube url", true);
            }
        }
    }

    class getData extends AsyncTask<Void,Void,Void> {

        String spotifyUrl,ytLink;
        ProgressDialog dialog;
        public getData(String yturl) {
            this.spotifyUrl = yturl;
            dialog = new ProgressDialog(MainActivity.this);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            if (ytLink!=null) {
                Intent intent = new Intent(MainActivity.this,PlayerActivity.class);
                intent.putExtra("youtubelink",new String[] {ytLink});
                startActivityForResult(intent,200);
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onPreExecute() {
            dialog.setCancelable(false);
            dialog.setMessage("Parsing spotify url...");
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.e("Original_URL",spotifyUrl+"");
            SpotifyTrack track = new SpotifyTrack(YTutils.getSpotifyID(spotifyUrl));
            ytLink = track.getYtUrl();
            Log.e("GOTURL_Here",ytLink+"");
            return null;
        }
    }
}


