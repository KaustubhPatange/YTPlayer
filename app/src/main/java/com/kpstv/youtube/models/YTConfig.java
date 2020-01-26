package com.kpstv.youtube.models;

import java.io.Serializable;

public class YTConfig implements Serializable {

    String Text,Url,Ext,Title,ChannelTitle,taskExtra;
    String targetName, videoID, audioUrl, imageUrl;
    boolean audio; long length; int bitRate;

    public YTConfig(String text, String linkurl,String ext,String videoTitle, String channelTitle,boolean audio,String imageUrl) {
        Text = text;
        Url = linkurl;
        Ext = ext;
        Title = videoTitle;
        ChannelTitle = channelTitle;
        this.imageUrl = imageUrl;
        this.audio = audio;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getVideoID() {
        return videoID;
    }

    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }

    public String getTaskExtra() {
        return taskExtra;
    }

    public void setTaskExtra(String taskExtra) {
        this.taskExtra = taskExtra;
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

    public void setExt(String ext) {
        Ext = ext;
    }

    public String getExt() {
        return Ext;
    }

    public String getTitle() {
        return Title;
    }
}
