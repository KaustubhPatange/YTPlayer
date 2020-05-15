package com.naveed.ytextractor.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;

import com.naveed.ytextractor.model.YTMedia;
import java.util.ArrayList;
import java.util.List;

public class Utils {
	
	public static String loginCookie="SID=sQebaLqpbk6gtBslCoKf2QMlnsSGyHZ10Ape5KC4w6A2xNcwmoB-l1xew71X6vvjm4D7Yw.;__Secure-3PSID=sQebaLqpbk6gtBslCoKf2QMlnsSGyHZ10Ape5KC4w6A2xNcwYZWWACA_8IrlbEWauUE_Wg.;HSID=AZIcZ8BttTa33E_1m;SSID=AfAdb5xkm4o5xYY0Y;APISID=mhQXsz03mKxKRJ2a/AFVQUDIGbgnQtpUWj;SAPISID=DyN8WKX8Y8mKkr3w/A9hFkEgGGnKgh5R-Y;__Secure-HSID=AZIcZ8BttTa33E_1m;__Secure-SSID=AfAdb5xkm4o5xYY0Y;__Secure-APISID=mhQXsz03mKxKRJ2a/AFVQUDIGbgnQtpUWj;__Secure-3PAPISID=DyN8WKX8Y8mKkr3w/A9hFkEgGGnKgh5R-Y;CONSENT=YES+en+202001;VISITOR_INFO1_LIVE=4Hp4-x4BFMQ;YSC=bNQ8TA61Fu8;GPS=1;LOGIN_INFO=AFmmF2swRgIhAL6ujtG8K3M0a4xThywxj4q3WGcotlfzfQlB2te4wgwIAiEAihKRgpgxwjvwSoiinpkywMoA6bkm-gWp7pycMJSAwgU:QUQ3MjNmekZ5SGU3YUZhT1NlN3lQcV9hQnY2ZjZuRjRUblpjT09QdEozZ3hQY0ZyN0lkU0tKeURhc2R4b3luR3BUQTQzOVpLNHpjTkxSa1JmVEh5Qll6RVhfT1JEOU9VYTZkV0RobDN1cTJhZEtHV2FYZWt1dUs3TEJUenpGSnlkeFUzdVNIREk0cjVGeGYtU0RDVDR1OXhCTUh6UXlzX3JyN0FRVDhhREM5ZW82M09qWHNXeVFr;SIDCC=AN0-TYs22Wg_H5D4kzTjQgEFunRGQljZGxdNtosqFUrGVHU-qRIHF6PFHovUG9S9pE333EZd";
	
	
	
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
			if (statement != null && statement.toLowerCase().contains(str)) {
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
