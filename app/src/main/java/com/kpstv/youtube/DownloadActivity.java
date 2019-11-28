package com.kpstv.youtube;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kpstv.youtube.adapters.DownloadAdapter;
import com.kpstv.youtube.services.DownloadService;
import com.kpstv.youtube.utils.YTutils;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class DownloadActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    LinearLayoutManager manager;
    CircularProgressBar currentProgress;
    ImageView currentImageView, moreButton;
    TextView txtTitle, txtSize, txtPercent,pendingText;
    DownloadAdapter adapter; int total;
    Context context;

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Downloads");

        context = DownloadActivity.this;

        recyclerView = findViewById(R.id.recyclerView);
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

        currentProgress = findViewById(R.id.DProgress);
        pendingText = findViewById(R.id.pendingText);
        currentImageView = findViewById(R.id.DImage);
        moreButton = findViewById(R.id.DMore);
        txtTitle = findViewById(R.id.DTitle);
        txtPercent = findViewById(R.id.DPercentText);
        txtSize = findViewById(R.id.DSizeText);

        if (DownloadService.currentModel !=null) {
            runTask();
            adapter = new DownloadAdapter(DownloadService.pendingJobs,this);
            recyclerView.setAdapter(adapter);
            mHandler.postDelayed(mUpdateTimeTask, 1000);
        }else finish();
    }

    void runTask() {
        total = DownloadService.pendingJobs.size();
        if (DownloadService.currentModel !=null) {
            txtTitle.setText(DownloadService.currentModel.getTitle()+" - "+DownloadService.currentModel.getChannelTitle());
           switch (DownloadService.currentModel.getTaskExtra()) {
               case "mp3task":
                   currentImageView.setImageDrawable(getDrawable(R.drawable.ic_audio_download));
                   break;
               case "mergetask":
                   currentImageView.setImageDrawable(getDrawable(R.drawable.ic_movie_download));
                   break;
            }
            String currentSize = YTutils.getSize(DownloadService.currentsize);
            String totalSIze = YTutils.getSize(DownloadService.totalsize);
            //int percent = ((int) DownloadService.currentsize*100 / (int)DownloadService.totalsize);

            int percent = DownloadService.progress;

            if (percent==-1) {
                txtPercent.setText(percent+"%");
                currentProgress.setIndeterminateMode(true);
            }else {
                currentProgress.setIndeterminateMode(false);
                currentProgress.setProgressWithAnimation(percent);
                txtPercent.setText(percent+"%");
            }

            txtSize.setText(String.format("%s / %s", currentSize, totalSIze));

            if (DownloadService.pendingJobs.size()<=0)
            {
                recyclerView.setVisibility(View.GONE);
                pendingText.setVisibility(View.GONE);
                pendingText.setText(" ");
            }else {
                recyclerView.setVisibility(View.VISIBLE);
                pendingText.setVisibility(View.VISIBLE);
                pendingText.setText("Pending");
            }

            if (total != DownloadService.pendingJobs.size()) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    public Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            total = DownloadService.pendingJobs.size();
            if (DownloadService.currentModel !=null) {
                runTask();
                mHandler.postDelayed(this,1000);
            }else {
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mUpdateTimeTask);

        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
