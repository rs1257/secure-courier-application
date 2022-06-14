package com.gpig.a.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.gpig.a.MapFragment;
import com.gpig.a.MyGraphHopperRoadManager;
import com.gpig.a.PollServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.util.GeoPoint;

import java.util.Objects;

public final class StatusUtils {

    private static final String TAG = "StatusUtils";

    public enum LocationStatus{
        FOUND,
        DISABLED,
        SEARCHING,
        NO_PERMISSION
    }

    private StatusUtils(){}

    public static boolean isNetworkAvailable(Activity act) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) act.getSystemService(Context.CONNECTIVITY_SERVICE);
        return isNetworkAvailable(connectivityManager);
    }

    public static boolean isNetworkAvailable(ConnectivityManager connectivityManager) {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static Location getLastKnownLocation(Context context, boolean timeSensitive){
        timeSensitive = false; // TODO for demo set it always to false for smoother experience
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if(!locationManager.isLocationEnabled()){
                    return null;
                }
            }
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(loc.getTime() < System.currentTimeMillis() - 30000 && timeSensitive){ // GPS must be within the last 30 seconds
                Log.i(TAG, "isLocationAvailable: GPS Stale");
            }else{
                return loc;
            }
//            loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            if(loc.getTime() < System.currentTimeMillis() - 30000){ // GPS must be within the last 30 seconds
//                Log.i(TAG, "isLocationAvailable: Network Stale");
//                return null;
//            }else{
//                return loc;
//            }
        }
        return null;
    }

    public static LocationStatus isLocationAvailable(Activity act) {
        if(ContextCompat.checkSelfPermission(act.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationManager locationManager = (LocationManager) act.getSystemService(Context.LOCATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if(!locationManager.isLocationEnabled()){
                    return LocationStatus.DISABLED;
                }
            }
            Location loc = getLastKnownLocation(act, true);
            if(loc == null){
                return LocationStatus.SEARCHING;
            }
            if(loc.getTime() < System.currentTimeMillis() - 30000){ // GPS must be within the last 30 seconds
                Log.i(TAG, "isLocationAvailable: Location Stale");
                return LocationStatus.SEARCHING;
            }else{
                return LocationStatus.FOUND;
            }
        }
        return LocationStatus.NO_PERMISSION;
    }

    public static boolean isLocationCorrect(Activity activity) {
        if(isLocationAvailable(activity) != LocationStatus.FOUND){
            return false;
        }
        if (FileUtils.doesFileExist(activity, RouteUtils.routeFilename)) {
            String json = FileUtils.readFromInternalStorage(activity, RouteUtils.routeFilename);
            if(json.length() < 10){
                return false;
            }
            MyGraphHopperRoadManager mgh = new MyGraphHopperRoadManager();
            mgh.getRoads(json);
            GeoPoint targetLocation = mgh.destination;
            assert targetLocation != null;
            Location location = getLastKnownLocation(activity, true);
            assert location != null;
            GeoPoint currentLocation = new GeoPoint(location);
            return targetLocation.distanceToAsDouble(currentLocation) < 1000;
        }
        return false;
    }

    public static boolean hasNewRoute(Activity activity){
        return PollServer.areUpdatesAvailable && StatusUtils.isLocationAvailable(activity) == StatusUtils.LocationStatus.FOUND;// && !FileUtils.doesFileExist(activity, RouteUtils.routeFilename)
    }

    public static boolean canCheckIn(Activity act) {
        return isNetworkAvailable(act) && isLocationAvailable(act) == LocationStatus.FOUND && isLocationCorrect(act);
    }
}
