package com.kpstv.youtube.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kpstv.youtube.MainActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.adapters.DownloadAdpater2;
import com.kpstv.youtube.models.YTConfig;
import com.kpstv.youtube.services.IntentDownloadService;

import java.util.ArrayList;

public class DownloadBottomSheet extends BottomSheetDialogFragment {
    private View v;
    private ArrayList<YTConfig> audioConfigs, videoConfigs;
    private RecyclerView mAudioRecyclerview;
    private RecyclerView mVideoRecyclerview;
    private LinearLayoutManager layoutManager1, layoutManager2;
    private static final String TAG = "DownloadBottomSheet";
    private TextView mAudiotxt;
    private TextView mVideotxt;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.bottom_sheet_download, container, false);
        initViews(v);

        audioConfigs = new ArrayList<>();
        videoConfigs = new ArrayList<>();
        layoutManager1 = new LinearLayoutManager(v.getContext(), LinearLayoutManager.HORIZONTAL, false);
        layoutManager2 = new LinearLayoutManager(v.getContext(), LinearLayoutManager.HORIZONTAL, false);

        mAudioRecyclerview.setLayoutManager(layoutManager1);
        mVideoRecyclerview.setLayoutManager(layoutManager2);

        for (YTConfig config : MainActivity.ytConfigs) {
            config.setTargetName(getTargetName(config));
            config.setVideoID(MainActivity.videoID);
            config.setAudioUrl(MainActivity.audioLink);
            if (config.isAudio()) {
                Log.e(TAG, "Audio: " + config.getText());
                config.setTaskExtra("mp3Task");
                int rate = config.getBitRate();
                if (rate > 131) {
                    config.setExt("m4a");
                } else
                    config.setExt("mp3");
                audioConfigs.add(config);

            } else {
                Log.e(TAG, "Video: " + config.getText());
                config.setTaskExtra("mergeTask");
                config.setExt("mp4");
                videoConfigs.add(config);
            }
        }

        if (videoConfigs.isEmpty()) {
            mVideotxt.setVisibility(View.GONE);
            mVideoRecyclerview.setVisibility(View.GONE);
        }

        if (audioConfigs.isEmpty()) {
            mAudioRecyclerview.setVisibility(View.GONE);
            mAudiotxt.setVisibility(View.GONE);
        }

        DownloadAdpater2 audioAdapter = new DownloadAdpater2(audioConfigs, v.getContext());
        audioAdapter.setListener((model, pos) -> {
            startService(model);
            Toast.makeText(v.getContext(), "Download started, check notification", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        DownloadAdpater2 videoAdapter = new DownloadAdpater2(videoConfigs, v.getContext());
        videoAdapter.setListener((model, pos) -> {
            startService(model);
            Toast.makeText(v.getContext(), "Download started, check notification", Toast.LENGTH_SHORT).show();
            dismiss();
        });
        mAudioRecyclerview.setAdapter(audioAdapter);
        mVideoRecyclerview.setAdapter(videoAdapter);

        return v;
    }

    public void startService(YTConfig model) {
        Intent serviceIntent = new Intent(v.getContext(), IntentDownloadService.class);
        serviceIntent.putExtra("addJob", model);

        ContextCompat.startForegroundService(v.getContext(), serviceIntent);
    }

    public static String getTargetName(YTConfig config) {
        String filename;
        if (config.getText().length() > 55) {
            filename = config.getTitle().substring(0, 55).trim();
        } else {
            filename = config.getChannelTitle().trim() + " - " + config.getTitle().trim();
        }
        return filename.replaceAll("[\\\\><\"|*?%:#/]", "");
    }

    private void initViews(View view) {
        mAudioRecyclerview = view.findViewById(R.id.audio_recyclerView);
        mVideoRecyclerview = view.findViewById(R.id.video_recyclerView);
        mAudiotxt = view.findViewById(R.id.audioTxt);
        mVideotxt = view.findViewById(R.id.videoTxt);
    }
}
