package com.kpstv.youtube.models;

import java.io.Serializable;
import java.util.ArrayList;

public class PlaylistModel implements Serializable {
    String Title, Date;
    int Timeseconds;
    ArrayList<String> data;

    public PlaylistModel(String date,String title, ArrayList<String> data) {
        Title = title;
        this.Date = date;
        this.data = new ArrayList<>();

        // Calculation for time seconds from videoIDs

        for (String line : data) {
            this.data.add(line.split("\\|")[0]);
            Timeseconds+=Integer.parseInt(line.split("\\|")[1]);
        }
    }


    public String getDate() {
        return Date;
    }

    public int getTimeseconds() {
        return Timeseconds;
    }

    public String getSongCountText() {
        return data.size()+" songs";
    }

    public String getTitle() {
        return Title;
    }

    public ArrayList<String> getData() {
        return data;
    }
}
