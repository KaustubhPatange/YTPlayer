package com.kpstv.youtube;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kpstv.youtube.adapters.SongAdapter;
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.models.PlaylistModel;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

import java.util.ArrayList;

public class OPlaylistActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    SongAdapter adapter; ArrayList<DiscoverModel> models;
    ArrayList<String> yturls;
    RecyclerView.LayoutManager layoutManager;
    TextView TitleText,SongCountText,TimeText,songText;
    ProgressBar progressBar; PlaylistModel playlistModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oplayist);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        models = new ArrayList<>();
        yturls = new ArrayList<>();
        recyclerView = findViewById(R.id.my_recycler_view);
        TitleText = findViewById(R.id.oTitle);
        SongCountText = findViewById(R.id.oSongText);
        TimeText = findViewById(R.id.oTime);
        songText = findViewById(R.id.songText);
        progressBar = findViewById(R.id.progressBar);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        Intent intent = getIntent();
        playlistModel = (PlaylistModel) intent.getSerializableExtra("model");

        ArrayList<String> videos = playlistModel.getData();
        TitleText.setText(playlistModel.getTitle());
        SongCountText.setText(String.format("%s songs", yturls.size()));
        TimeText.setText(String.format("  %s", YTutils.milliSecondsToTimer(
                playlistModel.getTimeseconds()*1000)));

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(playlistModel.getTitle());
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//careful there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });

        if (videos.size()>0) {
            for (int i = 0; i < videos.size(); i++) {
                String videoID = videos.get(i).split("\\|")[0];
                yturls.add( YTutils.getYtUrl(videoID));
            }

            new getData().execute();
        }else {
            progressBar.setVisibility(View.GONE);
            songText.setText("NO SONG DATA");
            songText.setVisibility(View.VISIBLE);
        }
    }

    private View.OnClickListener recyclerItemListener = view -> {
        int position = (int)view.getTag();
        String[] videos =YTutils.ConvertToStringArray(yturls);
        Intent intent = new Intent(OPlaylistActivity.this,PlayerActivity.class);
        intent.putExtra("playfromIndex",position);
        intent.putExtra("youtubelink",videos);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
    };

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    class getData extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter = new SongAdapter(models,OPlaylistActivity.this,true,recyclerItemListener);
            recyclerView.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
            songText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (String yturl : yturls) {
                YTMeta ytMeta = new YTMeta(YTutils.getVideoID(yturl));
                models.add(new DiscoverModel(
                        ytMeta.getVideMeta().getTitle(),
                        ytMeta.getVideMeta().getAuthor(),
                        ytMeta.getVideMeta().getImgUrl(),
                        yturl
                ));
            }
            return null;
        }

    }


}
