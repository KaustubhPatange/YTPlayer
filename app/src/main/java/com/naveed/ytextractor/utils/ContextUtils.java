package com.naveed.ytextractor.utils;

import android.content.ClipData;
import android.content.Context;

import android.widget.Toast;
import android.content.ClipboardManager;

public class ContextUtils 
{
	public static Context context;
	public static void init(Context c){
		context=c;
	}
	public static  void CopytoClip(String x){

        ((ClipboardManager)context.getSystemService(context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("clipboard", x));    

		Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show(); 





	}
	
}
