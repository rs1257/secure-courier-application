package com.gpig.a.utils;

import android.app.Activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class FileUtils {

    private FileUtils(){}

    public static String readJsonAsset(Activity act, String location){
        String json = null;
        try {
            InputStream is = act.getAssets().open(location);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

    public static boolean doesFileExist(Activity act, String filename){
        File file = act.getFileStreamPath(filename);
        return file.exists();
    }

    public static boolean removeInternalFile(Activity act, String filename){
        File dir = act.getFilesDir();
        File file = new File(dir, filename);
        return file.delete();
    }

    public static void writeToInternalStorage(Activity act, String filename, String content){
        try {
            FileOutputStream os = act.openFileOutput(filename, 0);
            os.write(content.getBytes());
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public static String readFromInternalStorage(Activity act, String filename){
        String data = null;
        try {
            FileInputStream is = act.openFileInput(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            data = new String(buffer, StandardCharsets.UTF_8);
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
        return data;
    }
}
