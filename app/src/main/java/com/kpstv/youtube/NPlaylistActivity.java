package com.kpstv.youtube;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.kpstv.youtube.adapters.NPlayAdapter;
import com.kpstv.youtube.models.NPlayModel;
import com.kpstv.youtube.utils.EqualizerView;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTutils;

import org.mozilla.javascript.tools.jsc.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class NPlaylistActivity extends AppCompatActivity {

    Toolbar toolbar;

    NPlayAdapter adapter;
    ArrayList<NPlayModel> models;
    TextView cTitle,cAuthor;
    RecyclerView recyclerView;
    ImageView cImageView;
    LinearLayoutManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nplaylist);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(" ");

        cTitle = findViewById(R.id.cTitle);
        cAuthor = findViewById(R.id.cAuthor);
        recyclerView = findViewById(R.id.my_recycler_view);
        cImageView = findViewById(R.id.cImage);
        models = new ArrayList<>();

        // Set current song...

        cTitle.setText(MainActivity.videoTitle);
        cAuthor.setText(MainActivity.channelTitle);
        cImageView.setImageBitmap(MainActivity.bitmapIcon);

        // Set recycler view...

        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

        // Set Data and set which one is playing right now...

        adapter = new NPlayAdapter(models,NPlaylistActivity.this);

        setAdapterClicks();

        recyclerView.setAdapter(adapter);

        if (MainActivity.yturls.size()>0) {
            // Check for old data....
            if (MainActivity.nPlayModels.size()>0 && MainActivity.yturls.size() == MainActivity.nPlayModels.size()) {
                boolean sameData=true;
                for(int i=0;i<MainActivity.nPlayModels.size();i++) {
                    MainActivity.nPlayModels.get(i).set_playing(false);
                    String yturl = MainActivity.yturls.get(i);
                    String nurl = MainActivity.nPlayModels.get(i).getUrl();

                    String title = YTutils.getVideoTitle( MainActivity.nPlayModels.get(i).getModel()
                            .getVideMeta().getTitle());
                    if (title.equals(MainActivity.videoTitle)) {
                        MainActivity.nPlayModels.get(i).set_playing(true);
                    }

                    if (!yturl.equals(nurl)) {
                        sameData = false;
                    }
                }
                if (sameData) {
                    reloadAdapter();
                    return;
                }
            }
            for (String url : MainActivity.yturls)
            {
                new getData(url,this).execute();
            }
        }
    }

    void reloadAdapter() {
        models = MainActivity.nPlayModels;
        adapter = new NPlayAdapter(models,this);
        setAdapterClicks();
        recyclerView.setAdapter(adapter);
    }

    void setAdapterClicks() {
        adapter.setOnSingleClickListener((view, position ,model, holder) -> {
            // Remove current queue song and make it current...
            for(int i=0;i<models.size();i++) {
                models.get(i).set_playing(false);
            }
            models.get(position).set_playing(true);
            String url = model.getUrl();
            new setCurrentData(url).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            MainActivity.ChangeVideo(position);
            adapter.notifyDataSetChanged();
        });

        adapter.setOnCheckClickListener((view, position, model, holder) -> {
            CheckBox checkBox = (CheckBox) view;
            if (checkBox.isChecked()) {
                models.get(position).set_selected(true);
            }else models.get(position).set_selected(false);
            adapter.notifyItemChanged(position);
        });

        adapter.setOnLongClickListener((view, position ,model, holder) -> {
            setPopUPMenu(view,model,position);
        });

        adapter.setOnMoreClickListener((view, position , model, holder) -> {
          setPopUPMenu(view,model,position);
        });
    }

    void setPopUPMenu(View view, NPlayModel model, int position) {
        PopupMenu popupMenu = new PopupMenu(this,view);
        popupMenu.inflate(R.menu.nplaymenu);
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_remove:
                    Log.e("Determining","true");
                    int i=0;
                    for (NPlayModel model1 : models) {
                        if (model1.is_selected()) {
                            i++;
                        }
                    }
                    Log.e("TotalItemChecked",i+" Item checked");
                    if (i>1) {
                        Integer[] pos = new Integer[i];
                        int j=-1;
                        // Multi-remove
                        for (i=0;i<models.size();i++) {
                             if (models.get(i).is_selected()) {
                                 pos[++j] = i;
                                 Log.e("CheckDetected","position: " + i);
                            }
                        }
                        // Reversing list...
                        Arrays.sort(pos, Collections.reverseOrder());
                        for (int c : pos) {
                            Log.e("RemovingItem","position: " + c+", "+YTutils.getVideoTitle(
                                    models.get(c).getModel().getVideMeta().getTitle()
                            ));
                            removeItem(c);
                        }
                        reloadAdapter();
                        return true;
                    }else {
                        // Single-remove
                        removeItem(position);
                        reloadAdapter();
                        return true;
                    }
                case R.id.action_share:
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Listen to this "+ model.getUrl());
                    startActivity(Intent.createChooser(shareIntent, "Share using..."));
                    break;
            }
            return false;
        });
    }

    void removeItem(int position) {
        MainActivity.nPlayModels.remove(position);
        MainActivity.yturls.remove(position);
    }

    class getData extends AsyncTask<Void,Void,Void> {

        String url; Context context; YTMeta meta;

        public getData(String url, Context context) {
            this.url = url;
            this.context = context;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (meta.getVideMeta()!=null) {
                adapter.notifyDataSetChanged();
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            meta = new YTMeta(YTutils.getVideoID(url));
            if (meta.getVideMeta()!=null) {
                String title = YTutils.getVideoTitle(meta.getVideMeta().getTitle());
                if (title.equals(MainActivity.videoTitle)) {
                    Log.e("NPlaylistActivity",MainActivity.videoTitle+", "+title);
                    models.add(new NPlayModel(url,meta,true));
                }else
                    models.add(new NPlayModel(url,meta,false));
            }
            return null;
        }
    }

    class setCurrentData extends AsyncTask<Void,Void,Void> {

        String yturl;
        YTMeta meta;

        public setCurrentData(String yturl) {
            this.yturl = yturl;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (meta.getVideMeta()!=null) {
                cTitle.setText(YTutils.getVideoTitle(meta.getVideMeta().getTitle()));
                cAuthor.setText(YTutils.getChannelTitle(meta.getVideMeta().getTitle(),meta.getVideMeta().getAuthor()));
                Glide.with(NPlaylistActivity.this)
                        .asBitmap()
                        .load(meta.getVideMeta().getImgUrl())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Palette.generateAsync(resource, palette -> {
                                    cImageView.setImageBitmap(resource);
                                    MainActivity.bitmapIcon = resource;
                                    MainActivity.nColor = palette.getVibrantColor(NPlaylistActivity.this
                                            .getResources().getColor(R.color.light_white));
                                    MainActivity.rebuildNotification();
                                });

                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            meta = new YTMeta(YTutils.getVideoID(yturl));
            return null;
        }
    }

    @Override
    public boolean onNavigateUp() {
        finish();
        return super.onNavigateUp();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}


/*
            ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                    ItemTouchHelper.UP | ItemTouchHelper.DOWN,0) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged,
                                      @NonNull RecyclerView.ViewHolder target) {
                    int position_dragged = dragged.getAdapterPosition();
                    int position_target = target.getAdapterPosition();

                    Collections.swap(MainActivity.yturls,position_dragged,position_target);

                    adapter.notifyItemMoved(position_dragged,position_target);
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                }
            });*/