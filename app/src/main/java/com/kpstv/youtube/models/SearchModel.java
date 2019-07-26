package com.kpstv.youtube.models;

public class SearchModel {
    String Title, ImageUrl, Yturl;

    public SearchModel(String title, String imgurl, String yturl) {
        Title = title;
        ImageUrl = imgurl;
        Yturl = yturl;
    }

    public String getYturl() {
        return Yturl;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public String getTitle() {
        return Title;
    }

}
