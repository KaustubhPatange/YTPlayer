package com.naveed.ytextractor.model;

public class PlayerResponse {
	private PlayabilityStatus playabilityStatus;
	private StreamingData streamingData;
	private YoutubeMeta videoDetails;

	public void setPlayabilityStatus(PlayabilityStatus playabilityStatus) {
		this.playabilityStatus = playabilityStatus;
	}

	public PlayabilityStatus getPlayabilityStatus() {
		return playabilityStatus;
	}
	
	public void setStreamingData(StreamingData streamingData) {
		this.streamingData = streamingData;
	}

	public StreamingData getStreamingData() {
		return streamingData;
	}

	public void setVideoDetails(YoutubeMeta videoDetails) {
		this.videoDetails = videoDetails;
	}

	public YoutubeMeta getVideoDetails() {
		return videoDetails;
	}
	
	
	
	



	public class PlayabilityStatus{
		private String status;
		private boolean playableInEmbed;

		public void setStatus(String status) {
			this.status = status;
		}

		public String getStatus() {
			return status;
		}

		public void setPlayableInEmbed(boolean playableInEmbed) {
			this.playableInEmbed = playableInEmbed;
		}

		public boolean isPlayableInEmbed() {
			return playableInEmbed;
		}
	}
}


