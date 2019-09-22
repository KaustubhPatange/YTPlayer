package com.kpstv.youtube.ytextractor;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.kpstv.youtube.ytextractor.model.PlayerResponse;
import com.kpstv.youtube.ytextractor.model.Response;
import com.kpstv.youtube.ytextractor.model.YoutubeMedia;
import com.kpstv.youtube.ytextractor.model.YoutubeMeta;
import com.kpstv.youtube.ytextractor.utils.HTTPUtility;
import com.kpstv.youtube.ytextractor.utils.LogUtils;
import com.kpstv.youtube.ytextractor.utils.RegexUtils;
import com.kpstv.youtube.ytextractor.utils.Utils;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YoutubeStreamExtractor extends AsyncTask<String, Void, Void> {




	Map<String, String> Headers=new HashMap<>();



	List<YoutubeMedia> adaptiveMedia=new ArrayList<>();
	List<YoutubeMedia> muxedMedia=new ArrayList<>();
	
	String regexItag=("(?<=itag=).*");
	String regexUrl=("(?<=url=).*");
	String regexQualitylabel="(?<=quality_label=).*";
	String regexType="(?<=type=).*";
	String regexResSize="(?<=size=).*";
	String regexBitrate="(?<=bitrate=).*";
	String regexExtension="(?<=(video|audio)/).*?(?=\\;)";
	String regexCodec="(?<=(codecs=)\").*?(?=\")";
	String regextypeVideoAudio=".*?(?=/)";
	String regexYtshortLink="(http|https)://(www\\.|)youtu.be/.*";
	String regexPageLink = ("(http|https)://(www\\.|m.|)youtube\\.com/watch\\?v=(.+?)( |\\z|&)");
	String regexFindReason="(?<=(class=\"message\">)).*?(?=<)";
	String regexPlayerJson="(?<=ytplayer.config\\s=).*?((\\}(\n|)\\}(\n|))|(\\}))(?=;)";
	ExtractorListner listener;
	private ExtractorException Ex;
	List<String> reasonUnavialable= Arrays.asList(new String[]{"This video is unavailable on this device.","Content Warning","who has blocked it on copyright grounds."});
	Handler han=new Handler(Looper.getMainLooper());
	private YoutubeMeta YTVideoMeta;
	private Response response;

	private boolean isLive;
	private boolean useCipher;


	public YoutubeStreamExtractor(ExtractorListner EL) {
		this.listener = EL;
		Headers.put("Accept-Language", "en");
	}

	public void setHeaders(Map<String, String> headers) {
		Headers = headers;
	}

	public Map<String, String> getHeaders() {
		return Headers;
	}

	public void Extract(String VideoId) {
		this.execute(VideoId);
	}
	
	

	@Override
	protected void onPostExecute(Void result) {
		if (Ex != null) {
			listener.onExtractionGoesWrong(Ex);
		} else
			listener.onExtractionDone(adaptiveMedia, muxedMedia, YTVideoMeta);

	}

	@Override
	protected void onPreExecute() {
		Ex = null;
		adaptiveMedia.clear();
		muxedMedia.clear();

	}

	@Override
	protected void onCancelled() {
		if (Ex != null) {
			listener.onExtractionGoesWrong(Ex);
		}	
	}



	@Override
	protected Void doInBackground(String[] ids)  {

		String Videoid=Utils.extractVideoID(ids[0]);
		
        String jsonBody = null;

        try {
			String body = HTTPUtility.downloadPageSource("https://www.youtube.com/watch?v=" + Videoid + "&has_verified=1&bpctr=9999999999", Headers);
			jsonBody = parsePlayerConfig(body);
			parseJson(jsonBody);
			parseUrls();
		}

        catch (Exception e) {
			LogUtils.log(Arrays.toString(e.getStackTrace()));// e.toString());
			Ex = new ExtractorException("Error While getting Youtube Data:" + e.getMessage());
			this.cancel(true);
		}

		return null;
	}

	private String parsePlayerConfig(String body) throws ExtractorException {

		if(Utils.isListContain(reasonUnavialable,RegexUtils.matchGroup(regexFindReason,body))){
			throw new ExtractorException(RegexUtils.matchGroup(regexFindReason,body));
		}
		
		if(body.contains("ytplayer.config")){
			return RegexUtils.matchGroup(regexPlayerJson,body);
		}else{
			throw new ExtractorException("This Video is unavialable");
		}
	
	}





	private void parseUrls() {

		try {
			if (!isLive) {
				parseAdaptiveUrls();	
				parseMuxedUrls();

			}
		}
		catch (IOException e) {
			Ex = new ExtractorException(e.getMessage());
			this.cancel(true);
		}
	}

	private void parseMuxedUrls() throws IOException {
		String url_encoded_fmt_stream_map=response.getArgs().getUrlEncodedFmtStreamMap();
		if (url_encoded_fmt_stream_map.equals("") | url_encoded_fmt_stream_map == null)return;
		String[] rawUrls=url_encoded_fmt_stream_map.split(",");


		for (int x=0;x < rawUrls.length;x++) {

			YoutubeMedia media=new YoutubeMedia();

			String[] Decodedurl= rawUrls[x].split("&");
			for (String part:Decodedurl) {

				if (part.startsWith("url=")) {
					media.setUrl(URLDecoder.decode(RegexUtils.matchGroup(regexUrl, part)));
				}
				if (part.startsWith("s=") & useCipher) {		
					media.setDechiperedSig(CipherManager.dechiperSig(URLDecoder.decode(part.replace("s=", "")), response.getAssets().getJs()));
				}
				if (part.startsWith("size=")) {		
					media.setResSize(RegexUtils.matchGroup(regexResSize, part));
				}
				if (part.startsWith("bitrate=")) {		
					media.setBitrate(RegexUtils.matchGroup(regexBitrate, part));
				}
				if (part.startsWith("itag=")) {		
					media.setItag(RegexUtils.matchGroup(regexItag, part));
				}
				if (part.startsWith("quality_label=")) {		
					media.setResolution(RegexUtils.matchGroup(regexQualitylabel, part));
				}
				if (part.startsWith("type=")) {		
					String type= URLDecoder.decode(RegexUtils.matchGroup(regexType, part));

					media.setExtension(RegexUtils.matchGroup(regexExtension, type));
					media.setCodec(RegexUtils.matchGroup(regexCodec, type));
					media.setIsAudioOnly(false);
					media.setIsVideoOnly(false);					
					media.setIsMuxed(true);
				}		

			}
			muxedMedia.add(media);

		}

		muxedMedia =Utils.	filterInvalidLinks(muxedMedia);
	}




	private void parseAdaptiveUrls() throws IOException {

		String adaptive_fmts=response.getArgs().getAdaptiveFmts();
		String[] rawUrls=adaptive_fmts.split(",");


		for (int x=0;x < rawUrls.length;x++) {

			YoutubeMedia media=new YoutubeMedia();

			String[] Decodedurl= rawUrls[x].split("&");
			for (String part:Decodedurl) {

				if (part.startsWith("url=")) {
					media.setUrl(URLDecoder.decode(RegexUtils.matchGroup(regexUrl, part)));
				}
				if (part.startsWith("s=") & useCipher) {		
					media.setDechiperedSig(CipherManager.dechiperSig(URLDecoder.decode(part.replace("s=", "")), response.getAssets().getJs()));
				}
				if (part.startsWith("size=")) {		
					media.setResSize(RegexUtils.matchGroup(regexResSize, part));
				}
				if (part.startsWith("bitrate=")) {		
					media.setBitrate(RegexUtils.matchGroup(regexBitrate, part));
				}
				if (part.startsWith("itag=")) {		
					media.setItag(RegexUtils.matchGroup(regexItag, part));
				}
				if (part.startsWith("quality_label=")) {		
					media.setResolution(RegexUtils.matchGroup(regexQualitylabel, part));
				}
				if (part.startsWith("type=")) {		
					String type= URLDecoder.decode(RegexUtils.matchGroup(regexType, part));
					media.setExtension(RegexUtils.matchGroup(regexExtension, type));
					media.setCodec(RegexUtils.matchGroup(regexCodec, type));
					if (RegexUtils.matchGroup(regextypeVideoAudio, type).equals("audio")) {
						media.setIsAudioOnly(true);
						media.setIsVideoOnly(false);					
					} else {
						media.setIsAudioOnly(false);media.setIsVideoOnly(true);				
					}

				}		

			}
			adaptiveMedia.add(media);

		}

		adaptiveMedia = Utils.filterInvalidLinks(adaptiveMedia);
	}



	private void parseJson(String body) throws Exception {
		JsonParser parser=new JsonParser();
		
		response=new GsonBuilder().serializeNulls().create().fromJson(parser.parse(body), Response.class);
		PlayerResponse playerResponse=new GsonBuilder().serializeNulls().create().fromJson(response.getArgs().getPlayerResponse(),PlayerResponse.class);
		YTVideoMeta=playerResponse.getVideoDetails();
		LogUtils.log(response.getAssets().getJs());
		if (YTVideoMeta.getisLive() ||  ( YTVideoMeta.getIsLiveContent()&& playerResponse.getStreamingData().getHlsManifestUrl()!=null)) {
			isLive=true;
		}
		useCipher=YTVideoMeta.getUseChiper();
		if(isLive)parseLiveUrls(playerResponse.getStreamingData());
		
}
	

	private void parseLiveUrls( PlayerResponse.StreamingData data) throws Exception {
		
		if(data.getHlsManifestUrl()==null){
			throw new ExtractorException("No link for hls video");
		}
		
		LogUtils.log(data.getHlsManifestUrl());
		String hlsPageSource= HTTPUtility.downloadPageSource(data.getHlsManifestUrl());

		String regexhlsLinks="(https://manifest.googlevideo.com/).*?((?=\\#)|\\z| )";
		List<String> Livelinks= RegexUtils.getAllMatches(regexhlsLinks, hlsPageSource);
		//List<String> linksInfo=re
		for (String s:Livelinks) {
			YoutubeMedia media=new YoutubeMedia();
			media.setUrl(s);
			media.setIsMuxed(true);
			muxedMedia.add(media);
			//LogUtils.log(s);
		}

	}
	
	
	
	
	
	
	
	




	public interface ExtractorListner {
		void onExtractionGoesWrong(ExtractorException e);
		void onExtractionDone(List<YoutubeMedia> adativeStream, List<YoutubeMedia> muxedStream, YoutubeMeta meta);

	}

}
