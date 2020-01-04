package com.kpstv.youtube.models;

import java.io.Serializable;

public class MetaModel implements Serializable {
    String Title, Author, ImgUrl;
    String videoID;
    public MetaModel(String videoID, String title, String author, String imgUrl) {
        this.videoID = videoID;
        Title = title;
        Author = author;
        ImgUrl = imgUrl;
    }

    public String getVideoID() {
        return videoID;
    }

    public void setVideoID(String videoID) {
        this.videoID = videoID;
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
}
