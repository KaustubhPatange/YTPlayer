package com.kpstv.youtube.models;

import android.util.Log;

public class DModel {
    private String title, subtitle, imageUrl, videoId;
    private int seconds;
    private boolean isChecked = false;

    public DModel(String videoId, String title, String subtitle, String imageUrl, String seconds) {
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.videoId = videoId;
        this.seconds = Integer.parseInt(seconds);
    }

    public int getSeconds() {
        return seconds;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isChecked() {
        return isChecked;
    }
}
