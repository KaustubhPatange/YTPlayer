package com.naveed.ytextractor.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class HTTPUtility 
{
	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36";

	
	public static String downloadPageSource(String stringURL,Map<String,String> headers) throws IOException {

        URL url;
        HttpURLConnection conn;
        StringBuilder source = new StringBuilder();


        try {

            url = new URL(stringURL);
            conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("User-Agent",USER_AGENT);
			
        } catch (IOException ex) {

            throw ex;

        }

		
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			conn.setRequestProperty(entry.getKey(),entry.getValue());
		}

        try {

            String line;

            conn.setRequestMethod("GET");
			  BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = in.readLine()) != null)
                source.append(line);

        } catch (IOException ex) {

            throw ex;

        } finally {

            conn.disconnect();

        }


        return source.toString();

    }
	
	
	public static String downloadPageSource(String stringURL) throws IOException {

        URL url;

        HttpURLConnection conn;

        StringBuilder source = new StringBuilder();


        try {

            url = new URL(stringURL);

            conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("User-Agent",USER_AGENT);
        } catch (IOException ex) {

            throw ex;

        }


        try {

            String line;

            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line = in.readLine()) != null)

                source.append(line);

        } catch (IOException ex) {

            throw ex;

        } finally {

            conn.disconnect();

        }


        return source.toString();

    }
	
}
