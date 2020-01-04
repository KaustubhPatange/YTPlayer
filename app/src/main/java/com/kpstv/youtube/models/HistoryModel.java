package com.kpstv.youtube.models;

public class HistoryModel {
    String VideoId,Title,ChannelTitle,ImageUrl,Date,Percent;

    public HistoryModel(String videoId, String title, String channelTitle, String imageUrl, String date, String percent) {
        VideoId = videoId;
        Title = title;
        ChannelTitle = channelTitle;
        ImageUrl = imageUrl;
        Date = date;
        Percent = percent;
    }

    public String getVideoId() {
        return VideoId;
    }

    public String getTitle() {
        return Title;
    }

    public String getChannelTitle() {
        return ChannelTitle;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public String getDate() {
        return Date;
    }

    public String getPercent() {
        return Percent;
    }
}
