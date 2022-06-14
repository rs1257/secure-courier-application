package com.gpig.a.utils;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.gpig.a.MainActivity;
import com.gpig.a.PollServer;
import com.gpig.a.settings.Settings;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public final class ServerUtils {

    private static final String TAG = "ServerUtils";
    public static PollServer pollServer;

    public static void postToServer(String path, String data, POSTAPI.AsyncResponse response){
        new POSTAPI(response).execute("https://" + Settings.ServerIP + ":" + Settings.ServerPort + "/" + path, data);
    }

    public static AsyncTask<String, String, String> getFromServer(String path) {
        String serverUrl = "https://" + Settings.ServerIP + ":" + Settings.ServerPort + "/" + path;
        return new GETAPI().execute(serverUrl);
    }

    public static void checkIn(String oneTimeKey, MainActivity activity) throws ExecutionException, InterruptedException {
        if(StatusUtils.canCheckIn(activity) || StatusUtils.hasNewRoute(activity)) {
            String data = "one_time_key=" + oneTimeKey;
            Log.i(TAG, "sendVerifyCompleteToClient: " + data);
            Location location = StatusUtils.getLastKnownLocation(activity, true);
            assert location != null;
            AsyncTask<String, String, String> updateTask = ServerUtils.getFromServer("controller/update/" + location.getLatitude() + "/" + location.getLongitude() + "/" + Settings.userID + "/");
            updateTask.get();//make sure the server has current location
            data += "&check_in=" + location.getLatitude() + "," + location.getLongitude();
            ServerUtils.postToServer("controller/checkin/" + Settings.userID + "/", data, (json)->{
                boolean result;
                if(json.length() < 5){
                    result = false;
                }
                else if(json.equals("no route")){
                    //TODO final destination screen
                    Toast.makeText(activity.getApplicationContext(), "Route Completed!", Toast.LENGTH_LONG).show();
                    FileUtils.removeInternalFile(activity, RouteUtils.routeFilename);
                    activity.switchToMap();
                    result = true;
                }else if(json.equals("authentication failed")){
                    result = false;
                }else if(json.equals("key no longer valid")){
                    result = false;
                }else if(json.equals("key doesnt match")){
                    result = false;
                }else {
                    PollServer.areUpdatesAvailable = false;
                    if (RouteUtils.hasRouteChanged(activity, RouteUtils.routeFilename, json)) {
                        FileUtils.writeToInternalStorage(activity, RouteUtils.routeFilename, json);
                    }
                    activity.switchToMap();
                    result = true;
                }
                if(!result) {
                    Toast.makeText(activity.getApplicationContext(), "Check in Failed!", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public static void checkUpdate(Context context){
        if(Settings.SessionKey.equals("") || Integer.parseInt(Settings.SessionKey.split(",")[1]) < System.currentTimeMillis()/1000){
            // if there is no session key or it has expired then cancel the alarm
            NotificationUtils.notify(context, "Not Receiving Updates", "Please verify your credentials with the server to check for new updates");
            if(pollServer != null) {
                pollServer.cancelAlarm(context);
            }
            Log.d(TAG, "onReceive: no session key or it has expired: " + Settings.SessionKey);
            Log.d(TAG, "onReceive: Current time: " + System.currentTimeMillis()/1000);
            return;
        }
        //TODO check for network
//        if(!StatusUtils.isNetworkAvailable()){
//            // no network so cant poll
//            return;
//        }
        Location location = StatusUtils.getLastKnownLocation(context, false);
        String data = "session_key=" + Settings.SessionKey;
        if (location != null) {
            String path = "controller/update/" + location.getLatitude() + "/" + location.getLongitude() + "/" + Settings.userID + "/";
            postToServer(path, data, (updates)->{
                Log.d(TAG, "onReceive: " + updates);
                if (updates.contains("True")) {
                    PollServer.areUpdatesAvailable = true;
                    NotificationUtils.notify(context, "Updates Available", "New updates are available, sign into the app for more details");
                }else if(updates.equals("key doesnt match")){
                    NotificationUtils.notify(context, "Not Receiving Updates", "Please verify your credentials with the server to check for new updates");
                    if(pollServer != null) {
                        pollServer.cancelAlarm(context);
                    }
                }
            });
        }
    }

    static class POSTAPI extends AsyncTask<String, String, String> {
        public interface AsyncResponse {
            void processFinish(String output);
        }

        private AsyncResponse delegate = null;

        private POSTAPI(AsyncResponse delegate){
            this.delegate = delegate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            String data = params[1];
            OutputStream out;
            StringBuilder result = new StringBuilder();
            Log.d(TAG, "POST: " + urlString);
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                out = new BufferedOutputStream(urlConnection.getOutputStream());

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
                writer.write(data);
                writer.flush();
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
            } catch (Exception e) {
                Log.i(TAG, "POST ERROR: " + e.getMessage());
                e.printStackTrace();
            }
            Log.d(TAG, "POST Done: " + result.toString());
            return result.toString();
        }
    }

    static class GETAPI extends AsyncTask<String, String, String> {

        GETAPI() {
            //set context variables if required
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urlString);
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
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                Log.e(TAG, ex.toString());
            } catch (IOException ex) {
                ex.printStackTrace();
                Log.e(TAG, ex.toString());
            }

            return result.toString();
        }
    }

}
