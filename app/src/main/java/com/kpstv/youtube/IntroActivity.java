package com.kpstv.youtube;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.LinearLayout;

import com.googlecode.mp4parser.authoring.Edit;

public class IntroActivity extends AppCompatActivity {

    AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        Log.e("DisplayMatrix",height+"");

        // Using this fuzzy logic to support screen sizes since there is no flexibility using
        // default method, can't work for custom resolutions!
        if (height>2770) {
            setContentView(R.layout.activity_intro_2000);
        }else if (height>1920&&height<2000) {
            setContentView(R.layout.activity_intro_1920);
        } else if (height>2000&&height<2770) {
            setContentView(R.layout.activity_intro_2000);
        }else
            setContentView(R.layout.activity_intro);

        SharedPreferences preferences = getSharedPreferences("settings",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("intro",true);
        editor.putInt("sort_order",0);
        editor.putInt("sort_type",2);
        editor.apply();


        LinearLayout buttonLayout = findViewById(R.id.buttonLayout);
        buttonLayout.setOnClickListener(v -> {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent();
                String packageName = getPackageName();
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    alertDialog = new AlertDialog.Builder(this)
                            .setTitle("Battery Optimization")
                            .setMessage("You need to disable battery optimization in order to prevent unexpected app shutdowns.")
                            .setPositiveButton("OK",(dialogInterface, i) -> {
                                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                                intent.setData(Uri.parse("package:" + packageName));
                                startActivity(intent);
                            })
                            .setCancelable(false)
                            .create();
                    alertDialog.show();
                }else loadAct();
            }else loadAct();


        });
    }

    void loadAct() {

        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        finish();
    }
}
