package com.kpstv.youtube.utils;


import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * DownloadUtils
 * @author       DOBAO
 * @Email        dotrungbao@gmail.com
 * @Skype        baopfiev_k50
 * @Date         Aug 12, 2013
 * @Packagename  com.dobao.utils.net
 */

public class DownloadUtils {

    private static final Pattern URI_FILENAME_PATTERN = Pattern.compile("[^/]+$");
    private static final String TAG = DownloadUtils.class.getSimpleName();

    private static final int BUFFER_SIZE = 8192;
    private static final int READ_TIME_OUT = 10000;
    private static final int CONNECT_TIME_OUT = 15000;

    public static String getFileName(String mUrl) {
        Matcher mMatcher = URI_FILENAME_PATTERN.matcher(mUrl);
        if (!mMatcher.find()) {
            throw new IllegalArgumentException("uri");
        }
        return mMatcher.group();
    }

    public static String downloadString(String url){
        InputStream is = null;
        try {
            URL mUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
            conn.setReadTimeout(READ_TIME_OUT);
            conn.setConnectTimeout(CONNECT_TIME_OUT);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            if(response== HttpURLConnection.HTTP_OK){
                is=conn.getInputStream();
                return convertStreamToString(is);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream download(String url){
        InputStream is = null;
        try {
            URL mUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
            conn.setReadTimeout(READ_TIME_OUT);
            conn.setConnectTimeout(CONNECT_TIME_OUT);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.connect();

            int response = conn.getResponseCode();
            Log.d(TAG, "The response is: " + response);
            if (response == HttpURLConnection.HTTP_OK) {
                is=conn.getInputStream();
                return is;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String convertStreamToString(InputStream is){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            is.close();
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean downloadImg(String mRootDir, String nameFile, String url) {
        File mFile = new File(mRootDir, nameFile);
        if (mFile.exists() && mFile.isFile()) {
            mFile.delete();
        }
        InputStream is = null;
        URL u = null;
        try {
            u = new URL(url);
            is = u.openStream();
            HttpURLConnection huc = (HttpURLConnection) u.openConnection();
            huc.setReadTimeout(READ_TIME_OUT);
            huc.setConnectTimeout(CONNECT_TIME_OUT);
            huc.setDoInput(true);
            huc.setUseCaches(false);
            huc.connect();

            int status = huc.getResponseCode();
            if (huc != null && is != null && status == HttpURLConnection.HTTP_OK) {
                int size = huc.getContentLength();
                FileOutputStream fos = new FileOutputStream(mFile);
                byte[] buffer = new byte[BUFFER_SIZE];
                long total = 0;
                int len1 = 0;
                while ((len1 = is.read(buffer)) > 0) {
                    total += len1;
                    fos.write(buffer, 0, len1);
                }
                if (fos != null) {
                    fos.close();
                }
                if (total >= size) {
                    return true;
                }
            }
        }
        catch (MalformedURLException mue) {
            mue.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            try {
                if (is != null) {
                    is.close();
                }
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return false;
    }

}
