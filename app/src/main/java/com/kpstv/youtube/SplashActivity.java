package com.kpstv.youtube;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.ads.MobileAds;
import com.kpstv.youtube.helper.BillingUtils;
import com.kpstv.youtube.utils.YTutils;

import java.io.File;

public class SplashActivity extends AppCompatActivity {

    // A perfect example of splash screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*

        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M) {
            setContentView(R.layout.splash_lollipop);
        }else setTheme(R.style.SplashTheme);
*/

        File f = YTutils.getFile("YTPlayer");
        f.mkdirs();

        if (getSharedPreferences("appSettings",MODE_PRIVATE).getBoolean("pref_purchase",false))
            BillingUtils.publishPremium();

        SharedPreferences preferences = getSharedPreferences("settings",MODE_PRIVATE);
        if (!preferences.getBoolean("intro",false)) {
            startActivity(new Intent(this,IntroActivity.class));
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            finish();
        } else {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }
    }
}
