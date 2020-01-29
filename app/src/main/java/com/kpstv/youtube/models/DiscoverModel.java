package com.kpstv.youtube.models;

public class DiscoverModel {
    private String Title,Author,ImgUrl,ytUrl;
    private long seconds=0; private boolean isDisabled=false;

    public DiscoverModel(String title, String author, String imgUrl, String ytUrl) {
        Title = title;
        Author = author;
        ImgUrl = imgUrl;
        this.ytUrl = ytUrl;
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
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
