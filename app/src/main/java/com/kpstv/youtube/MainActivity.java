package com.kpstv.youtube;

import android.app.ProgressDialog;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
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


import com.google.android.gms.ads.MobileAds;
import com.kpstv.youtube.fragments.HistoryFragment;
import com.kpstv.youtube.fragments.NCFragment;
import com.kpstv.youtube.fragments.PlaylistFragment;
import com.kpstv.youtube.fragments.SearchFragment;
import com.kpstv.youtube.utils.SpotifyTrack;
import com.kpstv.youtube.utils.YTutils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

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
    Fragment SearchFrag;
    Fragment PlaylistFrag;
    Fragment NCFrag;
    SharedPreferences preferences;
    LinearLayout bottom_player;
    ImageButton actionUp,actionPlay;
    TextView actionTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Remove this code afterwards...
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        Log.e("HeightMatrix",height+"");

        // Get required views...
        bottom_player = findViewById(R.id.bottom_player);
        actionPlay = findViewById(R.id.action_play);
        actionUp = findViewById(R.id.action_maximize);
        actionTitle = findViewById(R.id.action_title);

        // Check onComing links from YouTube or Spotify...
        CheckIntent(getIntent());

        preferences = getSharedPreferences("history",MODE_PRIVATE);
        String list = preferences.getString("urls","");
        ArrayList<String> urls = new ArrayList<>();
        if (!Objects.requireNonNull(list).isEmpty()) {
            urls.addAll(Arrays.asList(list.split(",")));
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Disabling URI exposure for lame android 8.0+ who can't do by themselves.
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

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

    @Override
    protected void onDestroy() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(1);
        super.onDestroy();
    }

    @Override
    public void onRemoveFromHistory(int position) {
        HistoryFragment.removeFromHistory(position);
    }

    @Override
    public void ReplaceFragment() {
        if (YTutils.isInternetAvailable()) {
            HistoryFrag = new HistoryFragment();
            loadFragment(HistoryFrag);
        } else Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
    }

    void openPlayer(boolean changePlayBack) {
        Intent i=new Intent(MainActivity.this,PlayerActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.putExtra("sendActivity","main");
        if (changePlayBack)
        i.putExtra("changePlayback","true");
        startActivity(i);
    }
    void CheckIntent(Intent incoming) {
        String playerCheck = incoming.getStringExtra("is_playing");
        if (playerCheck!=null && playerCheck.equals("true")) {
            bottom_player.setVisibility(View.VISIBLE);
            actionTitle.setSelected(true);
            actionTitle.setText(incoming.getStringExtra("b_title"));
            bottom_player.setOnClickListener(v -> openPlayer(false));
            actionUp.setOnClickListener(v -> openPlayer(false));
            actionPlay.setOnClickListener(v -> openPlayer(true));

        }else {
            actionTitle.setText(" ");
            bottom_player.setVisibility(View.GONE);
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


