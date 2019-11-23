package com.kpstv.youtube.models;

public class YTConfig {

    String Text,Url,Ext,Title,ChannelTitle;
    boolean audio; long length;

    public YTConfig(String text, String linkurl,String ext,String videoTitle, String channelTitle,boolean audio) {
        Text = text;
        Url = linkurl;
        Ext = ext;
        Title = videoTitle;
        ChannelTitle = channelTitle;
        this.audio = audio;
    }
    public boolean isAudio() {
        return audio;
    }

    public String getChannelTitle() {
        return ChannelTitle;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
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
