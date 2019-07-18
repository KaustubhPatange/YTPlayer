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
import android.util.Log;
import android.view.MenuItem;


import com.kpstv.youtube.fragments.HistoryFragment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // https://www.googleapis.com/youtube/v3/videos?id=BDocp-VpCwY&key=AIzaSyBYunDr6xBmBAgyQx7IW2qc770aoYBidLw&part=snippet,contentDetails,statistics,status

    // API KEY= AIzaSyBYunDr6xBmBAgyQx7IW2qc770aoYBidLw

    /*
    * Code to launch player
    *
    *           Intent intent = new Intent(MainActivity.this,PlayerActivity.class);
                intent.putExtra("youtubelink",stringarray);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
    *
    * */

    Fragment HistoryFrag;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("history",MODE_PRIVATE);
        String list = preferences.getString("urls","");
        ArrayList<String> urls = new ArrayList<>();
        if (!Objects.requireNonNull(list).isEmpty()) {
            urls.addAll(Arrays.asList(list.split(",")));
        }

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

        loadFragment(HistoryFrag);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_history:
                    loadFragment(HistoryFrag);
                    return true;
                case R.id.navigation_search:
                    return true;
                case R.id.navigation_playlist:
                    return true;
            }
            return true;
        }
    };

    private boolean loadFragment(Fragment fragment) {
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


}


