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
        /*if (!YTutils.isInternetAvailable()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this)
                    .setIcon(getDrawable(android.R.drawable.ic_dialog_alert))
                    .setTitle("No connection")
                    .setCancelable(false)
                    .setMessage("Looks like you are not connected to internet")
                    .setPositiveButton("OK",(dialog, which) -> finish());
            alert.show();
        }*/
        startActivity(new Intent(SplashActivity.this,MainActivity.class));
        finish();
    }
}
