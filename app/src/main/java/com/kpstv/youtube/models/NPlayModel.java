package com.kpstv.youtube.models;

import android.graphics.Bitmap;

import com.kpstv.youtube.utils.YTMeta;

public class NPlayModel {
    YTMeta model;
    String url;
    boolean selected=false;
    boolean playing=false;
    Bitmap icon;

    public NPlayModel(String url, YTMeta model, boolean isplaying) {
        this.url = url;
        this.model = model;
        this.playing = isplaying;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public YTMeta getModel() {
        return model;
    }

    public String getUrl() {
        return url;
    }

    public boolean is_selected() {
        return selected;
    }

    public boolean is_playing() {
        return playing;
    }

    public void set_selected(boolean is_selected) {
        this.selected = is_selected;
    }

    public void set_playing(boolean playing) {
        this.playing = playing;
    }
}
