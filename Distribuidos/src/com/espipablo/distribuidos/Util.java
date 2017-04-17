package com.espipablo.distribuidos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;

public class Util {
	public static String request(String urlS) {
        URL url = null;
        try {
            url = new URL(urlS);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            conn.setRequestMethod("GET");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        try {
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            String result = "";
            while ((output = br.readLine()) != null) {
                result += output;
            }

            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }

        conn.disconnect();
        return "";
    }
	
	public static String readFileToString(String path) throws IOException {
		JSONArray jsonArr = new JSONArray();
        try {
            Files.lines(Paths.get(path)).forEach(string -> {
            	jsonArr.put(string);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
		return jsonArr.toString();
	}

}
