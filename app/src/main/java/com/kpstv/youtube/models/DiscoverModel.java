package com.kpstv.youtube.models;

public class DiscoverModel {
    String Title,Author,ImgUrl,ytUrl;

    public DiscoverModel(String title, String author, String imgUrl, String ytUrl) {
        Title = title;
        Author = author;
        ImgUrl = imgUrl;
        this.ytUrl = ytUrl;
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
