package com.kpstv.youtube;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;


import com.kpstv.youtube.fragments.HistoryFragment;
import com.kpstv.youtube.fragments.NCFragment;
import com.kpstv.youtube.fragments.PlaylistFragment;
import com.kpstv.youtube.fragments.SearchFragment;
import com.kpstv.youtube.utils.YTutils;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        Log.e("HeightMatrix",height+"");

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
}


