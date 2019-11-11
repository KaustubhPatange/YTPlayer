package com.kpstv.youtube.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.googlecode.mp4parser.boxes.AC3SpecificBox;
import com.kpstv.youtube.CPlaylistActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.PlaylistAdapter;
import com.kpstv.youtube.models.PlaylistModel;
import com.kpstv.youtube.utils.YTutils;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment {

    View v; static FragmentActivity activity; static ProgressBar progressBar;
    boolean networkCreated;static String playlist_csv;
    static ArrayList<PlaylistModel> data; FloatingActionButton fabCreate;
    static RecyclerView recyclerView; static PlaylistAdapter adapter;
    static RecyclerView.LayoutManager layoutManager; static Fragment OPlayFrag;
    NestedScrollView nestedScrollView; static FragmentManager fragmentManager;

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
            fragmentManager = activity.getSupportFragmentManager();

            recyclerView = v.findViewById(R.id.my_recycler_view);
            progressBar = v.findViewById(R.id.progressBar);
            nestedScrollView = v.findViewById(R.id.nestedScrollView);
            fabCreate = v.findViewById(R.id.fab_create);
            layoutManager = new LinearLayoutManager(activity.getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);

            fabCreate.setOnClickListener(view -> {
                Intent intent = new Intent(activity,CPlaylistActivity.class);
                activity.startActivity(intent);
            });

            nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                    if (scrollY > oldScrollY) {
                        fabCreate.hide();
                    }
                    if (scrollY < oldScrollY) {
                       fabCreate.show();
                    }
                }
            });

            if (YTutils.isInternetAvailable())
            new getData(activity).execute();

            networkCreated=true;
        }
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.player_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private static View.OnClickListener listener = v1 -> {
        OPlayFrag = new OPlaylistFragment();
        PlaylistModel playlistModel = (PlaylistModel) v1.getTag();
        Bundle args = new Bundle();
        args.putSerializable("model",playlistModel);
        OPlayFrag.setArguments(args);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in,
                android.R.anim.fade_out);
        ft.replace(R.id.fragment_container, OPlayFrag);
        ft.commit();
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        /*switch (itemID) {
            case R.id.action_removeAll:
                int icon = android.R.drawable.ic_dialog_alert;
                new AlertDialog.Builder(activity)
                        .setTitle("Remove All")
                        .setMessage("Are you sure to remove all playlist?")
                        .setPositiveButton("OK", (dialog, which) -> {
                          YTutils.writeContent(activity,"playlist.csv","");
                          new getData(activity).execute();
                        })
                        .setIcon(icon)
                        .show();
                break;
        }*/
        return super.onOptionsItemSelected(item);
    }

    public static void loadRecyclerAgain() {
        new getData(activity).execute();
    }

    @Override
    public void onResume() {
        Log.e("onResume","true");
        new getData(activity).execute();
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.e("onPause","true");
        super.onPause();
    }


    static class getData extends AsyncTask<Void,Void,Void> {

        @SuppressLint("StaticFieldLeak")
        Activity activity;
        public getData(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (data.size()>0) {
                adapter = new PlaylistAdapter(data,activity,listener);
                recyclerView.setAdapter(adapter);
                recyclerView.setVisibility(View.VISIBLE);
            }
            progressBar.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            playlist_csv = YTutils.readContent(activity,"playlist.csv");
            if (playlist_csv !=null && !playlist_csv.isEmpty()) {
                data.clear();
                for (String line : playlist_csv.split("[\n\r]")) {
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

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            super.onPreExecute();
        }
    }

}
