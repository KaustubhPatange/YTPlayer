package com.kpstv.youtube.models;

public class MetaModel {
    String Title, Author, ImgUrl;

    public MetaModel(String title, String author, String imgUrl) {
        Title = title;
        Author = author;
        ImgUrl = imgUrl;
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
