package com.kpstv.youtube;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.kpstv.youtube.helper.BillingUtils;
import com.kpstv.youtube.utils.YTutils;

import java.io.File;

public class SplashActivity extends AppCompatActivity {

    // A perfect example of splash screen
    private static final String TAG = "SplashActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M) {
            setContentView(R.layout.splash_lollipop);
        }

        File app = new File(getFilesDir(),"tmp.apk");
        if (app.exists())
            app.delete();

        File f = YTutils.getFile("YTPlayer");
        f.mkdirs();

        SharedPreferences preferences = getSharedPreferences("settings",MODE_PRIVATE);
        if (!preferences.getBoolean("intro",false)) {
            startActivity(new Intent(this,IntroActivity.class));
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            finish();
        } else {
            CommonIntentCheck(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        CommonIntentCheck(intent);
        super.onNewIntent(intent);
    }

    private void CommonIntentCheck(Intent intent) {
        String data  = intent.getData()!=null ? intent.getData().toString() : null;
        if (data==null) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }else if (data.equals("localmusic") || data.equals("searchmusic")) {
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            i.setData(Uri.parse(data));
            startActivity(i);
            finish();
        }
    }

}
