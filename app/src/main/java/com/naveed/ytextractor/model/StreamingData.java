package com.naveed.ytextractor.model;

public class StreamingData
{
	private String hlsManifestUrl;
	private long expiresInSeconds;
	private YTMedia[] formats;
	private YTMedia[] adaptiveFormats;

	public void setFormats(YTMedia[] formats)
	{
		this.formats = formats;
	}

	public YTMedia[] getFormats()
	{
		return formats;
	}

	public void setAdaptiveFormats(YTMedia[] adaptiveFormats)
	{
		this.adaptiveFormats = adaptiveFormats;
	}

	public YTMedia[] getAdaptiveFormats()
	{
		return adaptiveFormats;
	}

	public void setExpiresInSeconds(long expiresInSeconds) {
		this.expiresInSeconds = expiresInSeconds;
	}

	public long getExpiresInSeconds() {
		return expiresInSeconds;
	}
	public void setHlsManifestUrl(String hlsManifestUrl) {
		this.hlsManifestUrl = hlsManifestUrl;
	}
	public String getHlsManifestUrl() {
		return hlsManifestUrl;
	}
}
