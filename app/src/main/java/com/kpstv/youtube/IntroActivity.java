package com.kpstv.youtube;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.LinearLayout;

import com.googlecode.mp4parser.authoring.Edit;

public class IntroActivity extends AppCompatActivity {

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
        editor.apply();

        LinearLayout buttonLayout = findViewById(R.id.buttonLayout);
        buttonLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            finish();
        });
    }
}
