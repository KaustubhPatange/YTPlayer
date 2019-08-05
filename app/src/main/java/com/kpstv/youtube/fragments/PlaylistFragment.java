package com.kpstv.youtube.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.kpstv.youtube.CPlaylistActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.PlaylistAdapter;
import com.kpstv.youtube.models.PlaylistModel;
import com.kpstv.youtube.utils.YTutils;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment {

    View v; Activity activity;
    boolean networkCreated; String playlist_csv;
    ArrayList<PlaylistModel> data; FloatingActionButton fabCreate;
    RecyclerView recyclerView; PlaylistAdapter adapter;
    static RecyclerView.LayoutManager layoutManager;

    public PlaylistFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (!networkCreated) {
            v = inflater.inflate(R.layout.fragment_playlist, container, false);

            data = new ArrayList<>();

            Toolbar toolbar = v.findViewById(R.id.toolbar);

            toolbar.setTitle("Playlist");

            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

            activity = getActivity();

            recyclerView = v.findViewById(R.id.my_recycler_view);
            fabCreate = v.findViewById(R.id.fab_create);
            layoutManager = new LinearLayoutManager(activity.getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);

            fabCreate.setOnClickListener(view -> {
                Intent intent = new Intent(activity,CPlaylistActivity.class);
                activity.startActivity(intent);
            });

            new getData(activity).execute();

            networkCreated=true;
        }
        return v;
    }

    @Override
    public void onResume() {
        new getData(activity).execute();
        super.onResume();
    }

    class getData extends AsyncTask<Void,Void,Void> {

        Activity activity;
        public getData(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (data.size()>0) {
                adapter = new PlaylistAdapter(data,getActivity());
                recyclerView.setAdapter(adapter);
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            playlist_csv = YTutils.readContent(activity,"playlist.csv");
            if (playlist_csv !=null && !playlist_csv.isEmpty()) {
                data.clear();
                for (String line : playlist_csv.split("\n|\r")) {
                    if (line!=null&&!line.isEmpty()) {
                        ArrayList<String> list = new ArrayList<>();
                        String[] lines = line.split(",");
                        for(int i=2;i<lines.length;i++) {
                            list.add(lines[i]);
                        }
                        data.add(0,new PlaylistModel(
                                lines[0],
                                lines[1],
                                list
                        ));
                    }
                }
            }
            return null;
        }
    }

}
