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
        this.data = data;

        // Calculation for time seconds from videoIDs

        for (String line : data) {
            if (line.isEmpty()) continue;
            try {
                Timeseconds+=Integer.parseInt(line.split("\\|")[1]);
            }catch (Exception ignored) { Timeseconds=0; }
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
