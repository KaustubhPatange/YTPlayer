package com.kpstv.youtube;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kpstv.youtube.adapters.SongAdapter;
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.utils.YTLength;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

import java.util.ArrayList;

public class OPlaylistActivity extends Activity {

    RecyclerView recyclerView;
    SongAdapter adapter; ArrayList<DiscoverModel> models;
    ArrayList<String> videoIDs;
    RecyclerView.LayoutManager layoutManager;
    String playlistLine;
    TextView TitleText,SongCountText,TimeText,songText;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oplayist);

        setTitle("");

        models = new ArrayList<>();
        videoIDs = new ArrayList<>();
        recyclerView = findViewById(R.id.my_recycler_view);
        TitleText = findViewById(R.id.oTitle);
        SongCountText = findViewById(R.id.oSongText);
        TimeText = findViewById(R.id.oTime);
        songText = findViewById(R.id.songText);
        progressBar = findViewById(R.id.progressBar);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        Intent intent = getIntent();
        playlistLine = intent.getStringExtra("pline");

        String[] line = playlistLine.split(",");
        int totalseconds=0;
        if (line.length>1) {
            for (int i = 2; i < line.length; i++) {
                String videoID = line[i].split("\\|")[0];
                totalseconds += Integer.parseInt(line[i].split("\\|")[1]);
                videoIDs.add(videoID);
            }
            TitleText.setText(line[1]);
            SongCountText.setText(String.format("%s songs", videoIDs.size()));
            TimeText.setText(String.format("  %s", YTutils.milliSecondsToTimer(totalseconds * 1000)));

            new getData().execute();
        }else {

        }
    }

    class getData extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter = new SongAdapter(models,OPlaylistActivity.this,false,true);
            recyclerView.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
            songText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (String videoID : videoIDs) {
                YTMeta ytMeta = new YTMeta(videoID);
                models.add(new DiscoverModel(
                        ytMeta.getVideMeta().getTitle(),
                        ytMeta.getVideMeta().getAuthor(),
                        ytMeta.getVideMeta().getImgUrl(),
                        YTutils.getYtUrl(videoID)
                ));
            }
            return null;
        }

    }


}
