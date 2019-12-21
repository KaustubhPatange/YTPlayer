package com.naveed.ytextractor.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import com.naveed.ytextractor.model.YTMedia;
import java.util.ArrayList;
import java.util.List;

public class Utils {
	public static List<YTMedia> filterInvalidLinks(List<YTMedia> urls) {
		List<YTMedia> workingLinks=new ArrayList<>();
		for (YTMedia media:urls) {
			if (!media.getUrl().contains("&dur=0.0")) {
				workingLinks.add(media);
			}
		}

		return workingLinks;
	}

	public static String extractVideoID(String Url) {
		String r="(?<=(be/|v=))(.*?)(?=(&|\n| |\\z))";
		if (RegexUtils.hasMatch(r, Url)) {
			return RegexUtils.matchGroup(r, Url);
		}
		return Url;
	}

	public static boolean isListContain(List<String> arraylist, String statement) {
		for (String str : arraylist) {
			if (statement.toLowerCase().contains(str)) {
				return true;
			}
		}
		return false;
	}

	public static void copyToBoard(String x){
		ClipboardManager clipboard = (ClipboardManager)ContextUtils.context. getSystemService(Context.CLIPBOARD_SERVICE); 
		ClipData clip = ClipData.newPlainText("text", x);
		clipboard.setPrimaryClip(clip);
	}
}
