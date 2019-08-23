package com.kpstv.youtube;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.kpstv.youtube.utils.YTutils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // A perfect example of splash screen
        startActivity(new Intent(SplashActivity.this,MainActivity.class));
        finish();
    }
}
