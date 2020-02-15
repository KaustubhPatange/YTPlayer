package com.kpstv.youtube.models;

import java.io.Serializable;

public class ImportModel implements Serializable {
    private String title; int songCount; int duration;
    private boolean checked=false;

    public ImportModel(String title, int songCount, int duration) {
        this.title = title;
        this.songCount = songCount;
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getTitle() {
        return title;
    }

    public int getSongCount() {
        return songCount;
    }

    public boolean isChecked() {
        return checked;
    }
}
