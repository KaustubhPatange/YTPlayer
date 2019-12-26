package com.kpstv.youtube.models;

import android.text.Spanned;

import java.io.Serializable;

public class LyricModel implements Serializable {
    String title;
    Spanned text;

    public LyricModel(String title, Spanned text) {
        this.title = title;
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public Spanned getText() {
        return text;
    }
}
