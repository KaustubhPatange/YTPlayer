package com.kpstv.youtube.ytextractor.utils;

import android.util.Log;

import com.kpstv.youtube.BuildConfig;

public class LogUtils
{
	public static void log(String x){
		if(BuildConfig.DEBUG)
		Log.d("Naveed",x);
	}
	public static void log(int x){
		if(BuildConfig.DEBUG)
			Log.d("Naveed", String.valueOf(x));
	}
	
}
