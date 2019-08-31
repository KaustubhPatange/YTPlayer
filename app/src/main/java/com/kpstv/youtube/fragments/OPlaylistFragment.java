package com.kpstv.youtube.fragments;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.PlayerActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.SongAdapter;
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.models.PlaylistModel;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

import java.util.ArrayList;

public class OPlaylistFragment extends Fragment {

    View v;
    boolean isnetworkCreated; Toolbar toolbar;
    RecyclerView recyclerView; ArrayList<String> yturls;
    SongAdapter adapter; ArrayList<DiscoverModel> models;
    FragmentActivity activity;
    RecyclerView.LayoutManager layoutManager;
    TextView TitleText,SongCountText,TimeText,songText;
    ProgressBar progressBar; PlaylistModel playlistModel;
    FloatingActionButton playFab;

    public OPlaylistFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!isnetworkCreated) {
            v = inflater.inflate(R.layout.fragment_oplaylist, container, false);
            activity = getActivity();

            toolbar = v.findViewById(R.id.toolbar);
            toolbar.setNavigationOnClickListener(v1 -> MainActivity.loadPlayFrag());

            models = new ArrayList<>();
            yturls = new ArrayList<>();
            recyclerView = v.findViewById(R.id.my_recycler_view);
            TitleText = v.findViewById(R.id.oTitle);
            SongCountText = v.findViewById(R.id.oSongText);
            TimeText = v.findViewById(R.id.oTime);
            songText = v.findViewById(R.id.songText);
            playFab = v.findViewById(R.id.oPlayFAB);
            progressBar = v.findViewById(R.id.progressBar);
            layoutManager = new LinearLayoutManager(activity);
            recyclerView.setLayoutManager(layoutManager);

            Bundle args = getArguments();
            playlistModel = (PlaylistModel) args.getSerializable("model");

            ArrayList<String> videos = playlistModel.getData();
            TitleText.setText(playlistModel.getTitle());
            SongCountText.setText(String.format("%s songs", videos.size()));
            TimeText.setText(String.format("  %s", YTutils.milliSecondsToTimer(
                    playlistModel.getTimeseconds()*1000)));

            final CollapsingToolbarLayout collapsingToolbarLayout = v.findViewById(R.id.toolbar_layout);
            AppBarLayout appBarLayout = v.findViewById(R.id.app_bar);
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                boolean isShow = true;
                int scrollRange = -1;

                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (scrollRange == -1) {
                        scrollRange = appBarLayout.getTotalScrollRange();
                    }
                    if (scrollRange + verticalOffset == 0) {
                        toolbar.setTitle(playlistModel.getTitle());
                        collapsingToolbarLayout.setTitle(playlistModel.getTitle());
                        isShow = true;
                    } else if(isShow) {
                        toolbar.setTitle(" "); //careful there should a space between double quote otherwise it wont work
                        collapsingToolbarLayout.setTitle(" "); //careful there should a space between double quote otherwise it wont work
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
            
            isnetworkCreated = true;
        }
        return v;
    }

    private View.OnClickListener recyclerItemListener = view -> {
        int position = (int)view.getTag();
        PlayMusic(position);
    };

    void PlayMusic(int position) {
        if (yturls.size()==0) return;
        String[] videos =YTutils.ConvertToStringArray(yturls);
        Intent intent = new Intent(activity,PlayerActivity.class);
        intent.putExtra("playfromIndex",position);
        intent.putExtra("youtubelink",videos);
        startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
    }

    class getData extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            adapter = new SongAdapter(models,activity,true,recyclerItemListener);
            recyclerView.setAdapter(adapter);
            progressBar.setVisibility(View.GONE);
            songText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

            playFab.setOnClickListener(v -> PlayMusic(0));

            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (String yturl : yturls) {
                YTMeta ytMeta = new YTMeta(YTutils.getVideoID(yturl));
                if (ytMeta.getVideMeta()!=null) {
                    models.add(new DiscoverModel(
                            ytMeta.getVideMeta().getTitle(),
                            ytMeta.getVideMeta().getAuthor(),
                            ytMeta.getVideMeta().getImgUrl(),
                            yturl
                    ));
                }
            }
            return null;
        }

    }


}
