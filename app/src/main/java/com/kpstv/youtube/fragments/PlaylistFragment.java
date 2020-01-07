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
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.googlecode.mp4parser.boxes.AC3SpecificBox;
import com.kpstv.youtube.CPlaylistActivity;
import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.PlaylistAdapter;
import com.kpstv.youtube.models.PlaylistModel;
import com.kpstv.youtube.utils.YTutils;

import java.util.ArrayList;
import java.util.Arrays;

public class PlaylistFragment extends Fragment {

    View v; FragmentActivity activity;ProgressBar progressBar;
    boolean networkCreated;static String playlist_csv;
    ArrayList<PlaylistModel> data; FloatingActionButton fabCreate;
    RecyclerView recyclerView; PlaylistAdapter adapter;
    RecyclerView.LayoutManager layoutManager; Fragment OPlayFrag;
    NestedScrollView nestedScrollView; FragmentManager fragmentManager;

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
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(view -> MainActivity.loadLibraryFrag());

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

    private static final String TAG = "PlaylistFragment";

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


    void setAdapterClicks() {
        adapter.setSingleClickListener((v1, model, position) -> {
            OPlayFrag = new OPlaylistFragment();
            Bundle args = new Bundle();
            args.putSerializable("model",model);
            if (model.getData().size()>0) {
                Log.e(TAG, "Data1: " + model.getData().get(0));
                OPlayFrag.setArguments(args);
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.setCustomAnimations(R.anim.fade_in,
                        R.anim.fade_out);
                ft.replace(R.id.fragment_container, OPlayFrag);
                ft.commit();
            }else Toast.makeText(activity, "Playlist is empty!", Toast.LENGTH_SHORT).show();
        });

        adapter.setLongClickListener((v1, model, position) -> {
            String playlist_csv = YTutils.readContent( activity,"playlist.csv");
            PopupMenu popupMenu = new PopupMenu(activity,v1);
            popupMenu.inflate(R.menu.playlist_context);
            popupMenu.show();
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                switch (itemId) {
                    case R.id.action_open:
                        adapter.performSingleClick(v,model,position);
                        break;
                    case R.id.action_modify:
                        if (playlist_csv!=null&&!playlist_csv.isEmpty()) {
                            String[] lines = playlist_csv.split("\r|\n");
                            for(int i=0;i<lines.length;i++) {
                                if (lines[i].contains(","+model.getTitle())) {
                                    Intent intent = new Intent(activity,CPlaylistActivity.class);
                                    intent.putExtra("line",lines[i]);
                                    activity.startActivity(intent);
                                }
                            }
                        }

                        break;
                    case R.id.action_delete:
                        if (playlist_csv!=null&&!playlist_csv.isEmpty()) {
                            ArrayList<String> lines = new ArrayList<>(Arrays.asList(playlist_csv.split("\r|\n")));
                            for(int i=0;i<lines.size();i++) {
                                if (lines.get(i).contains(","+model.getTitle())) {
                                    data.remove(data.size()-1-i);
                                    lines.remove(i);
                                    YTutils.writeContent(activity,"playlist.csv",
                                            YTutils.convertListToStringMethod(lines));
                                    adapter.notifyDataSetChanged();
                                    break;
                                }
                            }
                        }
                        break;
                }
                return false;
            });
        });
    }

    class getData extends AsyncTask<Void,Void,Void> {

        @SuppressLint("StaticFieldLeak")
        Activity activity;
        public getData(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (data.size()>0) {
                adapter = new PlaylistAdapter(data,activity);
                setAdapterClicks();
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

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            super.onPreExecute();
        }
    }

}
