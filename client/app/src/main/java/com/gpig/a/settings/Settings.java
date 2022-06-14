package com.gpig.a.settings;

import android.app.Activity;

import com.gpig.a.utils.FileUtils;

import org.json.JSONException;
import org.json.JSONObject;

public final class Settings {
    //TODO save/load to/from file
    public static String ServerIP = "tg0.uk";
    public static int ServerPort = 49300;
    public static String SessionKey = "";
    public static String LastEmail = "";
    private final static String FILENAME = "ServerSettings.json";
    public static String userID = "1";

    public static void readFromFile(Activity activity){
        if(FileUtils.doesFileExist(activity, FILENAME)) {
            try {
                JSONObject jObject = new JSONObject(FileUtils.readFromInternalStorage(activity, FILENAME));
                ServerIP = jObject.getString("ServerIP");
                ServerPort = jObject.getInt("ServerPort");
                SessionKey = jObject.getString("SessionKey");
                LastEmail = jObject.getString("LastEmail");
                userID = jObject.getString("userID");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeToFile(Activity activity){
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("ServerIP", ServerIP);
            jObject.put("ServerPort", ServerPort);
            jObject.put("SessionKey", SessionKey);
            jObject.put("LastEmail", LastEmail);
            jObject.put("userID", userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FileUtils.writeToInternalStorage(activity, FILENAME, jObject.toString());
    }

    public static String getUrlFromSettings(Activity activity){
        readFromFile(activity);
        return "https://" + ServerIP + ":" + ServerPort + "/controller/route";
    }
}
