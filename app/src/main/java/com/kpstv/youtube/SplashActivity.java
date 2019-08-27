package com.kpstv.youtube;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.ads.MobileAds;
import com.kpstv.youtube.utils.YTutils;

import java.io.File;

public class SplashActivity extends AppCompatActivity {

    // A perfect example of splash screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: Change app unit id, Sample : ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

        File f = YTutils.getFile("YTPlayer");
        f.mkdirs();

        SharedPreferences preferences = getSharedPreferences("settings",MODE_PRIVATE);
        if (!preferences.getBoolean("intro",false)) {
            startActivity(new Intent(this,IntroActivity.class));
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            finish();
        }else {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }
    }
}
