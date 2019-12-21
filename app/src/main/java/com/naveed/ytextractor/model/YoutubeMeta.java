package com.naveed.ytextractor.model;

import java.util.ArrayList;
import java.util.List;

public class YoutubeMeta {

	private boolean allowRatings;
	private String author;
	private Float averageRating;
	private String channelId;
	private boolean isCrawlable;
	private boolean isLiveContent;
	private boolean isLive;
	private boolean isLowLatencyLiveStream;
	private boolean isOwnerViewing;
	private boolean isPrivate;
	private boolean useCipher;
	private boolean isUnpluggedCorpus;
	private String latencyClass;
	private String lengthSeconds;
	private String shortDescription;
	private Thumbnail thumbnail;
	private String title;
	private String videoId;
	private String viewCount;

	public void setIsLive(boolean isLive) {
		this.isLive = isLive;
	}

	public boolean getisLive() {
		return isLive;
	}

	public void setUseChiper(boolean useChiper) {
		this.useCipher = useChiper;
	}

	public boolean getUseChiper() {
		return useCipher;
	}

	public boolean getAllowRatings() {
		return allowRatings;
	}

	public void setAllowRatings(boolean allowRatings) {
		this.allowRatings = allowRatings;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Float getAverageRating() {
		return averageRating;
	}

	public void setAverageRating(Float averageRating) {
		this.averageRating = averageRating;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public boolean getIsCrawlable() {
		return isCrawlable;
	}

	public void setIsCrawlable(boolean isCrawlable) {
		this.isCrawlable = isCrawlable;
	}

	public boolean getIsLiveContent() {
		return isLiveContent;
	}

	public void setIsLiveContent(boolean isLiveContent) {
		this.isLiveContent = isLiveContent;
	}

	public boolean getIsLowLatencyLiveStream() {
		return isLowLatencyLiveStream;
	}

	public void setIsLowLatencyLiveStream(boolean isLowLatencyLiveStream) {
		this.isLowLatencyLiveStream = isLowLatencyLiveStream;
	}

	public boolean getIsOwnerViewing() {
		return isOwnerViewing;
	}

	public void setIsOwnerViewing(boolean isOwnerViewing) {
		this.isOwnerViewing = isOwnerViewing;
	}

	public boolean getIsPrivate() {
		return isPrivate;
	}

	public void setIsPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public boolean getIsUnpluggedCorpus() {
		return isUnpluggedCorpus;
	}

	public void setIsUnpluggedCorpus(boolean isUnpluggedCorpus) {
		this.isUnpluggedCorpus = isUnpluggedCorpus;
	}

	public String getLatencyClass() {
		return latencyClass;
	}

	public void setLatencyClass(String latencyClass) {
		this.latencyClass = latencyClass;
	}

	public String getLengthSeconds() {
		return lengthSeconds;
	}

	public void setLengthSeconds(String lengthSeconds) {
		this.lengthSeconds = lengthSeconds;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public Thumbnail getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(Thumbnail thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}

	public String getViewCount() {
		return viewCount;
	}

	public void setViewCount(String viewCount) {
		this.viewCount = viewCount;
	}


public class Thumbnail {

	private List<Thumbnail_> thumbnails = new ArrayList<Thumbnail_>();

	public List<Thumbnail_> getThumbnails() {
		return thumbnails;
	}

	public void setThumbnails(List<Thumbnail_> thumbnails) {
		this.thumbnails = thumbnails;
	}

}

public class Thumbnail_ {

	private Integer height;
	private String url;
	private Integer width;

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

}
}
	
	

	

