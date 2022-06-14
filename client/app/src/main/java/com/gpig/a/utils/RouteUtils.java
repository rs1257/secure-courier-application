package com.gpig.a.utils;

import android.app.Activity;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public final class RouteUtils {

    private static final String TAG = "RouteUtils";
    public static final String routeFilename = "Route.json";

    private RouteUtils(){}

    public static boolean hasRouteChanged(Activity act, String storedFile, String serverResponse){
        if (FileUtils.doesFileExist(act, storedFile)) {
            String data = FileUtils.readFromInternalStorage(act, storedFile);
            return !data.equals(serverResponse);
        }
        return true;
    }

    public static String getRouteFromUrl(String serverUrl){
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(serverUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(3000);
            try {
                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                in.close();
                reader.close();
            } finally {
                urlConnection.disconnect();
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
            Log.e(TAG, ex.toString());
        }

        return result.toString();
    }
}
