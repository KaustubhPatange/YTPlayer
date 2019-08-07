package com.kpstv.youtube;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.kpstv.youtube.fragments.HistoryFragment;
import com.kpstv.youtube.utils.SpotifyTrack;
import com.kpstv.youtube.utils.YTutils;

public class HiddenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hidden);
        setTitle(" ");
        CheckIntent(true);

    }

    void CheckIntent(boolean callFinish) {
        if (Intent.ACTION_SEND.equals(getIntent().getAction())
                && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {
            String ytLink = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (YTutils.isValidID(ytLink)){
                Intent intent = new Intent(HiddenActivity.this,PlayerActivity.class);
                intent.putExtra("youtubelink",new String[] {ytLink});
                startActivityForResult(intent,200);
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                finish();
            }else if (ytLink.contains("open.spotify.com")&&ytLink.contains("/track/")) {
                new getData(ytLink).execute();
            }else {
                showAlert(callFinish);
            }
        }
    }

    class getData extends AsyncTask<Void,Void,Void> {

        String spotifyUrl,ytLink;
        ProgressDialog dialog;
        public getData(String yturl) {
            this.spotifyUrl = yturl;
            dialog = new ProgressDialog(HiddenActivity.this);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
         //   dialog.dismiss();
            if (ytLink!=null) {
                Intent intent = new Intent(HiddenActivity.this,PlayerActivity.class);
                intent.putExtra("youtubelink",new String[] {ytLink});
                startActivityForResult(intent,200);
                overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                finish();
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onPreExecute() {
            dialog.setCancelable(false);
            dialog.setMessage("Parsing spotify url...");
           // dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SpotifyTrack track = new SpotifyTrack(spotifyUrl);
            ytLink = track.getYtUrl();
            Log.e("GOTURL_Here",ytLink+"");
            return null;
        }
    }

    void showAlert(final boolean callFinish) {
        int icon =android.R.drawable.ic_dialog_alert;
        new AlertDialog.Builder(HiddenActivity.this)
                .setTitle("Callback Error")
                .setMessage("The requested url is not a valid YouTube url")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callFinish)
                            finish();
                    }
                })
                .setIcon(icon)
                .show();
    }
}
