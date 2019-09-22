package com.kpstv.youtube.ytextractor.model;


public class YoutubeMedia {
	
	String Url;
	String Resolution;
	String ResSize;
	String Extension;
	String itag;
	String Bitrate;
	String dechiperedSig;
	String Codec;
	boolean isMuxed=false;
	boolean isVideoOnly;
	boolean isAudioOnly;
	
	public YoutubeMedia() {

	}

	public void setIsMuxed(boolean isMuxed) {
		this.isMuxed = isMuxed;
	}

	public boolean isMuxed() {
		return isMuxed;
	}

	public void setIsVideoOnly(boolean isVideoOnly) {
		this.isVideoOnly = isVideoOnly;
	}

	public boolean isVideoOnly() {
		return isVideoOnly;
	}

	public void setIsAudioOnly(boolean isAudioOnly) {
		this.isAudioOnly = isAudioOnly;
	}

	public boolean isAudioOnly() {
		return isAudioOnly;
	}
	
	

	public void setCodec(String codec) {
		Codec = codec;
	}

	public String getCodec() {
		return Codec;
	}

	public void setDechiperedSig(String dechiperedSig) {
		this.dechiperedSig = dechiperedSig;
	}

	public String getDechiperedSig() {
		return dechiperedSig;
	}

	public void setItag(String itag) {
		this.itag = itag;
	}

	public String getItag() {
		return itag;
	}

	public void setBitrate(String bitrate) {
		Bitrate = bitrate;
	}

	public String getBitrate() {
		return Bitrate;
	}

	public void setUrl(String url) {
		Url = url;
	}

	public String getUrl() {
		if (dechiperedSig != null) {
			if (Url.contains("&lsig=")) {
				return Url + "&sig=" + dechiperedSig;
			} else {
				return Url + "&signature=" + dechiperedSig;
			}
		} else {
			return Url;
		}
	}

	public void setResolution(String resolution) {
		Resolution = resolution;
	}

	public String getResolution() {
		return Resolution;
	}

	public void setResSize(String resSize) {
		ResSize = resSize;
	}

	public String getResSize() {
		return ResSize;
	}

	

	public void setExtension(String extension) {
		Extension = extension;
	}

	public String getExtension() {
		return Extension;
	}}
