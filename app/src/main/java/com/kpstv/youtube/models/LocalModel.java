package com.kpstv.youtube.models;

import java.io.Serializable;
import java.util.ArrayList;

public class LocalModel implements Serializable {
    String title; int albumCount;
    ArrayList<String> songString;

    public LocalModel(String title, ArrayList<String> songPath, int albumCount) {
        this.title = title;
        this.songString = songPath;
        this.albumCount = albumCount;
    }

    public int getAlbumCount() {
        return albumCount;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getSongList() {
        return songString;
    }
}
