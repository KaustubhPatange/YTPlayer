package com.kpstv.youtube.models;

public class YTConfig {

    String Text,Url,Ext,Title;

    public YTConfig(String text, String linkurl,String ext,String videoTitle) {
        Text = text;
        Url = linkurl;
        Ext = ext;
        Title = videoTitle;
    }

    public String getText() {
        return Text;
    }

    public String getUrl() {
        return Url;
    }

    public String getExt() {
        return Ext;
    }

    public String getTitle() {
        return Title;
    }
}
