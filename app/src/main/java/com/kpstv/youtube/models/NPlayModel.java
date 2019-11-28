package com.kpstv.youtube.models;

import com.kpstv.youtube.utils.YTMeta;

public class NPlayModel {
    YTMeta model;
    String url;
    boolean selected=false;
    boolean playing=false;

    public NPlayModel(String url, YTMeta model, boolean isplaying) {
        this.url = url;
        this.model = model;
        this.playing = isplaying;
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
