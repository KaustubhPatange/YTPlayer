package com.naveed.ytextractor.model;

public class YTMedia
{
	int itag;
	String mimeType;
	int bitrate;
	int width;
	int height;
	double lastModified;
	double contentLength;
	String quality;
	String qualityLabel;
	String projectionType;
	double averageBitrate;
	double approxDurationMs;
	int audioChannels;
	int audioSampleRate;
	String cipher;
	String audioQuality;
	String url;
	int fps;

	public void setFps(int fps) {
		this.fps = fps;
	}

	public int getFps() {
		return fps;
	}
	
	


	public void setItag(int itag)
	{
		this.itag = itag;
	}

	public int getItag()
	{
		return itag;
	}

	public void setMimeType(String mimeType)
	{
		this.mimeType = mimeType;
	}

	public String getMimeType()
	{
		return mimeType;
	}

	public void setBitrate(int bitrate)
	{
		this.bitrate = bitrate;
	}

	public int getBitrate()
	{
		return bitrate;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getWidth()
	{
		return width;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public int getHeight()
	{
		return height;
	}

	public void setLastModified(double lastModified)
	{
		this.lastModified = lastModified;
	}

	public double getLastModified()
	{
		return lastModified;
	}

	public void setContentLength(double contentLength)
	{
		this.contentLength = contentLength;
	}

	public double getContentLength()
	{
		return contentLength;
	}

	public void setQuality(String quality)
	{
		this.quality = quality;
	}

	public String getQuality()
	{
		return quality;
	}

	public void setQualityLabel(String qualityLabel)
	{
		this.qualityLabel = qualityLabel;
	}

	public String getQualityLabel()
	{
		return qualityLabel;
	}

	public void setProjectionType(String projectionType)
	{
		this.projectionType = projectionType;
	}

	public String getProjectionType()
	{
		return projectionType;
	}

	public void setAverageBitrate(double averageBitrate)
	{
		this.averageBitrate = averageBitrate;
	}

	public double getAverageBitrate()
	{
		return averageBitrate;
	}

	public void setApproxDurationMs(double approxDurationMs)
	{
		this.approxDurationMs = approxDurationMs;
	}

	public double getApproxDurationMs()
	{
		return approxDurationMs;
	}

	public void setAudioChannels(int audioChannels)
	{
		this.audioChannels = audioChannels;
	}

	public int getAudioChannels()
	{
		return audioChannels;
	}

	public void setAudioSampleRate(int audioSampleRate)
	{
		this.audioSampleRate = audioSampleRate;
	}

	public int getAudioSampleRate()
	{
		return audioSampleRate;
	}

	public void setCipher(String cipher)
	{
		this.cipher = cipher;
	}

	public String getCipher()
	{
		return cipher;
	}

	public void setAudioQuality(String audioQuality)
	{
		this.audioQuality = audioQuality;
	}

	public String getAudioQuality()
	{
		return audioQuality;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	public String getUrl()
	{
		return url;
	}
	
	public boolean useCipher(){
		return cipher!=null && url==null;
		
	}
	
}
