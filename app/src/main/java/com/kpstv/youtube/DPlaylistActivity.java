package com.kpstv.youtube;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kpstv.youtube.adapters.DAdapter;
import com.kpstv.youtube.fragments.DownloadBottomSheet;
import com.kpstv.youtube.models.DModel;
import com.kpstv.youtube.models.YTConfig;
import com.kpstv.youtube.services.IntentDownloadService;

import java.util.ArrayList;

public class DPlaylistActivity extends AppCompatActivity {

    private FloatingActionButton mFab;
    private RecyclerView mRecyclerview;
    private DAdapter adapter;
    private ArrayList<DModel> models;
    private ArrayList<YTConfig> ytConfigs;
    private String ext = "mp3";
    private RelativeLayout mPurchaselayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dplaylist);
        initViews();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Download (Mp3)");

        // gxE4ngu78Ro|241|Blackbear - Idfc (Tarro Remix)|Chill Nation|https://i.ytimg.com/vi/gxE4ngu78Ro/mqdefault.jpg
        ArrayList<String> list = getIntent().getStringArrayListExtra("list");
        for (String item : list) {
            String[] childs = item.split("\\|");
            models.add(new DModel(childs[0], childs[2], childs[3], childs[4], childs[1]));
        }

        setRecyclerView();

        mFab.setOnClickListener(view -> {
            ytConfigs.clear();

            int count = 0;

            if (!AppSettings.contentActivated) {
                for (DModel model : models) {
                    if (model.isChecked()) {
                        count++;
                    }
                }
            }

            if (AppSettings.contentActivated || count <= 7) {
                for (DModel dModel : models) {
                    if (dModel.isChecked()) {

                        String title = dModel.getTitle();
                        String author = dModel.getSubtitle();

                        if (title.contains("-")) {
                            title = dModel.getTitle().split("-")[1];
                            author = dModel.getTitle().split("-")[0];
                        }

                        YTConfig config = new YTConfig("auto-generate", "auto-generate",
                                ext, title, author, true, dModel.getImageUrl());
                        config.setVideoID(dModel.getVideoId());
                        config.setTargetName(DownloadBottomSheet.getTargetName(config));
                        config.setTaskExtra("autoTask");

                        Intent serviceIntent = new Intent(this, IntentDownloadService.class);
                        serviceIntent.putExtra("addJob", config);

                        ContextCompat.startForegroundService(this, serviceIntent);

                        ytConfigs.add(config);
                    }
                }
            } else {
                Toast.makeText(this, "Selection limit is 7, upgrade to remove this!", Toast.LENGTH_LONG).show();
                return;
            }

            if (ytConfigs.size() <= 0) {
                Toast.makeText(this, "Select some item first!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Downloading started, check notification", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.action_selectAll:
                for (DModel dModel : models) {
                    dModel.setChecked(true);
                }
                adapter.notifyDataSetChanged();
                break;
            case R.id.action_unselectAll:
                for (DModel dModel : models) {
                    dModel.setChecked(false);
                }
                adapter.notifyDataSetChanged();
                break;
            case R.id.action_m4a:
                setTitle("Download (M4A)");
                ext = "m4a";
                break;
            case R.id.action_mp3:
                setTitle("Download (MP3)");
                ext = "mp3";
                break;
            case R.id.action_720p:
                setTitle("Download (720P)");
                ext = "720p";
                break;
            case R.id.action_1080p:
                setTitle("Download (1080P)");
                ext = "1080p";
                break;
            case R.id.action_480p:
                setTitle("Download (480P)");
                ext = "480p";
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dplaylist_menu, menu);
        menu.getItem(2).getSubMenu().getItem(1).setChecked(true);
        MenuCompat.setGroupDividerEnabled(menu, true);
        return super.onCreateOptionsMenu(menu);
    }

    private void setRecyclerView() {

        if (AppSettings.contentActivated) {
            mPurchaselayout.setVisibility(View.GONE);
        }

        LinearLayoutManager manager = new LinearLayoutManager(this);
        mRecyclerview.setLayoutManager(manager);
        adapter = new DAdapter(this, models);
        adapter.setListener((model, pos) -> {
            model.setChecked(!model.isChecked());
            adapter.notifyItemChanged(pos);
        });

        mRecyclerview.setAdapter(adapter);
    }


    private void initViews() {
        ytConfigs = new ArrayList<>();
        models = new ArrayList<>();
        mFab = findViewById(R.id.fab);
        mRecyclerview = findViewById(R.id.recyclerView);
        mPurchaselayout = findViewById(R.id.purchaseLayout);
    }
}
