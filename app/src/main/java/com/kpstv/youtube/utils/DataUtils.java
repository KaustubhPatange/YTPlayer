package com.kpstv.youtube.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.kpstv.youtube.models.YTConfig;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DataUtils {

    // yyyyMMddHHmmss|videoId|url|text,url,ext,isAudio>text,url,ext,isAudio


    // 2020 01 11 12 45 32
    // 2020 01 11 12 56 00
    private static final String TAG = "DataUtils";

    public static class DataModel {
        String audioLink;
        ArrayList<YTConfig> configs;
        public DataModel(String audioLink, ArrayList<YTConfig> configs) {
            this.audioLink = audioLink;
            this.configs = configs;
        }
        public String getAudioLink() {
            return audioLink;
        }
        public ArrayList<YTConfig> getConfigs() {
            return configs;
        }
    }

    public static DataModel getSavedUrl(Context context,String videoId,String title,String channelTitle) {
        long start = System.currentTimeMillis();
        String data = YTutils.readContent(context,"urlList.csv");

        if (data!=null && !data.isEmpty()) {
            if (data.contains("|"+videoId)) {
                Date date = Calendar.getInstance().getTime();
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                c.add(Calendar.HOUR, -5);

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                Long timeBefore = Long.parseLong(df.format(c.getTime()));

                String[] items = data.split("\n|\r");
                for (int i=0;i<items.length;i++) {
                    if (items[i].contains("|"+videoId)) {
                        String[] childs = items[i].split("\\|");

                        Long timeToday = Long.parseLong(childs[0]);

                        if (timeBefore<timeToday) {

                            Log.e(TAG, "getSavedUrl: Getting saved data..." );

                            ArrayList<YTConfig> configs = new ArrayList<>();
                            String[] confs = childs[3].split(">");
                            for (String conf : confs) {
                                if (conf.isEmpty()) continue;
                                if (!conf.contains(",")) continue;
                                String[] confItems = conf.split(",");
                                boolean isAudio=false;
                                if (confItems[3].equals("true")) {
                                    isAudio=true;
                                }
                                configs.add(new YTConfig(confItems[0],confItems[1],confItems[2],title,channelTitle,isAudio,
                                        YTutils.getImageUrlID(videoId)));
                            }
                            long end = System.currentTimeMillis();
                            Log.e(TAG, "Got Url: "+videoId+" in "+(end-start)+"ms" );
                            return new DataModel(childs[2],configs);
                        }else return null;
                    }
                }
            }
        }
        return null;
    }

    public static void saveUrl(Context context, String videoId, String audioUrl, ArrayList<YTConfig> ytconfigs) {

        long start = System.currentTimeMillis();

        String data = YTutils.readContent(context,"urlList.csv");

        /** Create data string... */

        Date c = Calendar.getInstance().getTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        Long timeToday = Long.parseLong(df.format(c));

        StringBuilder builder = new StringBuilder();
        StringBuilder finalBuilder = new StringBuilder();
        builder.append(timeToday).append("|").append(videoId).append("|").append(audioUrl).append("|");
        for (YTConfig config : ytconfigs) {
            builder.append(config.getText()).append(",").append(config.getUrl()).append(",").append(config.getExt())
                    .append(",").append(config.isAudio()).append(">");
        }
        if (data!=null && !data.isEmpty()) {
            if (data.contains("|"+videoId)) {
                String[] items = data.split("\n|\r");
                for (int i=0;i<items.length;i++) {
                    String item = items[i];
                    if (items[i].contains("|"+videoId)) {
                        item = builder.toString();
                    }
                    finalBuilder.append(item).append("\n");
                }
                YTutils.writeContent(context,"urlList.csv",finalBuilder.toString().trim());
            }else YTutils.writeContent(context,"urlList.csv",data.trim()+"\n"+builder.toString());
        }else YTutils.writeContent(context,"urlList.csv",builder.toString());
        long end = System.currentTimeMillis();
        Log.e(TAG, "saveUrl: "+videoId+" in "+(end-start)+"ms" );
    }
}
