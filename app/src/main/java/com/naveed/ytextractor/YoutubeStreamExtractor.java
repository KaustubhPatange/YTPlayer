package com.naveed.ytextractor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.naveed.ytextractor.CipherManager;
import com.naveed.ytextractor.model.PlayerResponse;
import com.naveed.ytextractor.model.Response;
import com.naveed.ytextractor.model.StreamingData;
import com.naveed.ytextractor.model.YTMedia;
import com.naveed.ytextractor.model.YoutubeMeta;
import com.naveed.ytextractor.utils.HTTPUtility;
import com.naveed.ytextractor.utils.LogUtils;
import com.naveed.ytextractor.utils.RegexUtils;
import com.naveed.ytextractor.utils.Utils;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YoutubeStreamExtractor extends AsyncTask<String,Void,Void> {


	Map<String,String> Headers=new HashMap<>();
	List<YTMedia> adaptiveMedia=new ArrayList<>();
	List<YTMedia> muxedMedia=new ArrayList<>();
	String regexUrl=("(?<=url=).*");
	String regexYtshortLink="(http|https)://(www\\.|)youtu.be/.*";
	String regexPageLink = ("(http|https)://(www\\.|m.|)youtube\\.com/watch\\?v=(.+?)( |\\z|&)");
	String regexFindReason="(?<=(class=\"message\">)).*?(?=<)";
	String regexPlayerJson="(?<=ytplayer.config\\s=).*?((\\}(\n|)\\}(\n|))|(\\}))(?=;)";
	ExtractorListner listener;
	private ExtractorException Ex;
	List<String> reasonUnavialable=Arrays.asList(new String[]{"This video is unavailable on this device.","Content Warning","who has blocked it on copyright grounds."});
	Handler han=new Handler(Looper.getMainLooper());
	private Response response;
	private YoutubeMeta ytmeta;





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
		} else {
			listener.onExtractionDone(adaptiveMedia, muxedMedia, ytmeta);
			}
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
	protected Void doInBackground(String[] ids) {

		String Videoid=Utils.extractVideoID(ids[0]);
        String jsonBody = null;
        try {
			String body = HTTPUtility.downloadPageSource("https://www.youtube.com/watch?v=" + Videoid + "&has_verified=1&bpctr=9999999999", Headers);
			jsonBody = parsePlayerConfig(body);
			//Utils.copyToBoard(jsonBody);
			PlayerResponse playerResponse=parseJson(jsonBody);
			ytmeta = playerResponse.getVideoDetails();
			if (playerResponse.getVideoDetails().getisLive()) {
				parseLiveUrls(playerResponse.getStreamingData());
			} else {
				adaptiveMedia =	parseUrls(playerResponse.getStreamingData().getAdaptiveFormats());
				muxedMedia =	parseUrls(playerResponse.getStreamingData().getFormats());
			}
		} catch (Exception e) {
			LogUtils.log(Arrays.toString(e.getStackTrace()));// e.toString());
			Ex = new ExtractorException("Error While getting Youtube Data:" + e.getMessage());
			this.cancel(true);
		}
		return null;
	}

	/*this function creates Json models using Gson*/
	private PlayerResponse parseJson(String body) throws Exception {
		JsonParser parser=new JsonParser();
		response = new GsonBuilder().serializeNulls().create().fromJson(parser.parse(body), Response.class);
		return new GsonBuilder().serializeNulls().create().fromJson(response.getArgs().getPlayerResponse(), PlayerResponse.class);
	}

	/*This function is used to check if webpage contain steam data and then gets the Json part of from the page using regex*/
	private String parsePlayerConfig(String body) throws ExtractorException {

		if (Utils.isListContain(reasonUnavialable, RegexUtils.matchGroup(regexFindReason, body))) {
			throw new ExtractorException(RegexUtils.matchGroup(regexFindReason, body));
		}
		if (body.contains("ytplayer.config")) {
			return RegexUtils.matchGroup(regexPlayerJson, body);
		} else {
			throw new ExtractorException("This Video is unavialable");
		}
	}




	/*independent function Used to parse urls for adaptive & muxed stream with cipher protection*/

	private List<YTMedia> parseUrls(YTMedia[] rawMedia) {
		List<YTMedia> links=new ArrayList<>();
		try {
			for (int x=0;x < rawMedia.length;x++) {
				YTMedia media=rawMedia[x];
				if (media.useCipher()) {
					String tempUrl=URLDecoder.decode(RegexUtils.matchGroup(regexUrl, media.getCipher()));
					for (String url_part:tempUrl.split("&")) {
						if (url_part.startsWith("s=")) {
							String decodedSig=CipherManager.dechiperSig(URLDecoder.decode(url_part.replace("s=", "")), response.getAssets().getJs());
							String FinalUrl;
							if (tempUrl.contains("&lsig=")) {
								FinalUrl = tempUrl + "&sig=" + decodedSig;
							} else {
								FinalUrl = tempUrl + "&signature=" + decodedSig;
							}
							media.setUrl(FinalUrl);
							links.add(media);
							LogUtils.log(FinalUrl);
						}
					}
				}else{
					links.add(media);
				}
			}

		} catch (Exception e) {
			Ex = new ExtractorException(e.getMessage());
			this.cancel(true);
		}
		return links;
	}



	/*This funtion parse live youtube videos links from streaming data  */

	private void parseLiveUrls(StreamingData streamData) throws Exception {
		if (streamData.getHlsManifestUrl() == null) {
			throw new ExtractorException("No link for hls video");
		}
		String hlsPageSource=HTTPUtility.downloadPageSource(streamData.getHlsManifestUrl());
		String regexhlsLinks="(#EXT-X-STREAM-INF).*?(index.m3u8)";
		List<String> rawData= RegexUtils.getAllMatches(regexhlsLinks, hlsPageSource);
		for (String data:rawData) {
			YTMedia media=new YTMedia();
			String[] info_list=RegexUtils.matchGroup("(#).*?(?=https)", data).split(",");
			String live_url=RegexUtils.matchGroup("(https:).*?(index.m3u8)", data);
			media.setUrl(live_url);
			for (String info:info_list) {
				if (info.startsWith("BANDWIDTH")) {
					media.setBitrate(Integer.valueOf(info.replace("BANDWIDTH=", "")));
				}
				if (info.startsWith("CODECS")) {
					media.setMimeType((info.replace("CODECS=", "").replace("\"", "")));
				}
				if (info.startsWith("FRAME-RATE")) {
					media.setFps(Integer.valueOf((info.replace("FRAME-RATE=", ""))));
				}
				if (info.startsWith("RESOLUTION")) {
					String[] RESOLUTION= info.replace("RESOLUTION=", "").split("x");
					media.setWidth(Integer.valueOf(RESOLUTION[0]));
					media.setHeight(Integer.valueOf(RESOLUTION[1]));
					media.setQualityLabel(RESOLUTION[1] + "p");
				}
			}
			LogUtils.log(media.getUrl());
			muxedMedia.add(media);
		}


	}

	public interface ExtractorListner {
		void onExtractionGoesWrong(ExtractorException e);
		void onExtractionDone(List<YTMedia> adativeStream, List<YTMedia> muxedStream, YoutubeMeta meta);

	}

}     
