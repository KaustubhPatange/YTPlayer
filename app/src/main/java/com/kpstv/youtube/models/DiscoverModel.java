package com.kpstv.youtube.models;

public class DiscoverModel {
    String Title,Author,ImgUrl,ytUrl;
    long seconds;

    public DiscoverModel(String title, String author, String imgUrl, String ytUrl) {
        Title = title;
        Author = author;
        ImgUrl = imgUrl;
        this.ytUrl = ytUrl;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    public String getTitle() {
        return Title;
    }

    public String getAuthor() {
        return Author;
    }

    public String getImgUrl() {
        return ImgUrl;
    }

    public String getYtUrl() {
        return ytUrl;
    }
}
