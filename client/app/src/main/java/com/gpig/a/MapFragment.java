
package com.gpig.a;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.gpig.a.settings.Settings;
import com.gpig.a.utils.FileUtils;
import com.gpig.a.utils.MapUtils;
import com.gpig.a.utils.RouteUtils;
import com.gpig.a.utils.StatusUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class MapFragment extends Fragment {

    private MapView mapView = null;
    private MapLocationListener locationListener = null;
    private LocationManager locationManager = null;
    private SensorEventListener sensorEventListener = null;
    private SensorManager sensorManager = null;
    private GeoPoint currentLocation = null;
    private GeoPoint sourceLocation = null;
    private GeoPoint destinationLocation = null;
    private Marker m = null;

    private float[] gravity = null;
    private float[] geomagnetic = null;

    private String json = null;
    private RouteCourierTask routeCourierTask = null;
    private RouteFromUrlTask routeFromUrlTask = null;
    private String TAG = "MapFragment";

    RecyclerView recyclerView;
    RecyclerView.Adapter recyclerViewAdapter;
    RecyclerView.LayoutManager recylerViewLayoutManager;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Configuration.getInstance().setUserAgentValue("com.gpig.a");
        Configuration.getInstance().load(getContext(),
                PreferenceManager.getDefaultSharedPreferences(getContext()));

        mapView = getView().findViewById(R.id.mapView);
        mapView.setMinZoomLevel(7.0);
        mapView.setMaxZoomLevel(18.0);
        mapView.setVerticalMapRepetitionEnabled(false);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setUseDataConnection(true);
        mapView.setMultiTouchControls(true);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        requestAllPermissions();

        FloatingActionButton myFab = getView().findViewById(R.id.loc);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onCenterCall(v);
            }
        });

        FloatingActionButton zoomIn = getView().findViewById(R.id.zoomIn);
        zoomIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mapView.getZoomLevelDouble() < mapView.getMaxZoomLevel()){
                    mapView.getController().zoomIn();
                }
            }
        });

        FloatingActionButton zoomOut = getView().findViewById(R.id.zoomOut);
        zoomOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mapView.getZoomLevelDouble() > mapView.getMinZoomLevel()){
                    mapView.getController().zoomOut();
                }

            }
        });
    }

    private void setCurrentLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationListener = new MapLocationListener();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
            }
            sensorEventListener = new MapSensorListener();
            sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
            if (currentLocation != null) {
                mapView.getController().setCenter(currentLocation);
                m = MapUtils.createMarker(mapView, getContext(), "current", currentLocation);
                mapView.getOverlays().add(m);
            }
        }
    }

    private void routeCourier() {

        routeCourierTask = new RouteCourierTask((new RouteCourierTask.AsyncResponse() {

            @Override
            public void processFinish(Object[] output) {
                Road road = (Road) output[0];
                if(road == null){
                    return;
                }
                recyclerView = (RecyclerView) getActivity().findViewById(R.id.rv);
                recylerViewLayoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(recylerViewLayoutManager);
                recyclerViewAdapter = new RecyclerViewAdapter(getContext(), road.mNodes);
                DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
                recyclerView.addItemDecoration(dividerItemDecoration);
                recyclerView.swapAdapter(recyclerViewAdapter, true);

                sourceLocation = (GeoPoint) output[1];
                destinationLocation = (GeoPoint) output[2];
                try {
                    mapView.zoomToBoundingBox(road.mBoundingBox, true, 175);
                }catch (IllegalArgumentException e){
                    e.printStackTrace();
                    Log.i(TAG, "processFinish: " + road.mBoundingBox);
                }
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                mapView.getOverlays().add(roadOverlay);

                mapView.getOverlays().add(MapUtils.createMarker(mapView, getContext(), "src", sourceLocation));
                mapView.getOverlays().add(MapUtils.createMarker(mapView, getContext(), "des", destinationLocation));
                mapView.invalidate();
            }
        }));

//        if (StatusUtils.isNetworkAvailable(getActivity())) {
//
//            routeFromUrlTask = new RouteFromUrlTask(new RouteFromUrlTask.AsyncResponse() {
//
//                @Override
//                public void processFinish(String output) {
//                    json = output;
//
//                    if (json == null || json.isEmpty()){
//                        if (FileUtils.doesFileExist(getActivity(), RouteUtils.routeFilename)){
//                            json = FileUtils.readFromInternalStorage(getActivity(), RouteUtils.routeFilename);
//                            routeCourierTask.execute(json);
//                        }
//                        else {
//                            Log.e(TAG, "Failed to connect to server and no route downloaded");
//                            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
//                            alert.setTitle("Failed to connect to server");
//                            alert.setMessage("Please try to connect to the server to download a route");
//                            alert.setPositiveButton("OK", null);
//                            alert.show();
//                        }
//                    }
//                    else {
//                        // if the route is different from the one stored then update it
//                        if (RouteUtils.hasRouteChanged(getActivity(), RouteUtils.routeFilename, json)) {
//                            FileUtils.writeToInternalStorage(getActivity(), RouteUtils.routeFilename, json);
//                        }
//                        routeCourierTask.execute(json);
//                    }
//                }
//            });
//
//            routeFromUrlTask.execute(Settings.getUrlFromSettings(getActivity()));
//        }
//        else {
            if (FileUtils.doesFileExist(getActivity(), RouteUtils.routeFilename)) {
                json = FileUtils.readFromInternalStorage(getActivity(), RouteUtils.routeFilename);
                routeCourierTask.execute(json);
            } else {
                // display a popup saying connect to the internet?
                Log.e(TAG, "No route downloaded");
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                alert.setTitle("No route downloaded");
                alert.setMessage("Please connect to the internet and check in to download a route");
                alert.setPositiveButton("OK", null);
                alert.show();
            }
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // if the permissions have changed then get the location
        if (requestCode == 1) {
            setCurrentLocation();
            routeCourier();
        } else {
            Log.e(TAG, "Error");
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    private void requestAllPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_WIFI_STATE
        };

        if (!hasPermissions(getContext(), PERMISSIONS)) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        } else {
            setCurrentLocation();
            routeCourier();
        }
    }

    public class MapLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            if (location != null && currentLocation != null) {
                if (!(location.getLatitude() == currentLocation.getLatitude() && location.getLongitude() == currentLocation.getLongitude()) || currentLocation == null) {
                    currentLocation = new GeoPoint(location);
                    if (m != null) {
                        mapView.getOverlays().remove(m);
                    }
                    if(getContext() != null) {
                        m = MapUtils.createMarker(mapView, getContext(), "current", currentLocation);
                        mapView.getOverlays().add(m);
                        mapView.invalidate();
                    }
                }
            }
        }

        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    public class MapSensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {gravity = event.values;}
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {geomagnetic = event.values;}

            if (gravity != null && geomagnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    float azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                    if (m!= null){
                        float angle = 360-(float) Math.toDegrees(azimut);
                        if (angle > m.getRotation() + 10 || angle < m.getRotation() - 10 ) {
                            m.setRotation(angle);
                            mapView.invalidate();
                        }
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor s, int accuracy) {}
    }
    public void onCenterCall(View v){
        mapView.getController().animateTo(currentLocation);
    }

    @Override
    public void onStop(){
        super.onStop();
        // remove the listeners or the app crashes
        locationManager.removeUpdates(locationListener);
        sensorManager.unregisterListener(sensorEventListener);
        if (routeCourierTask != null) {
            routeCourierTask.cancel(true);
        }
        if (routeFromUrlTask != null){
            routeFromUrlTask.cancel(true);
        }

    }

    static class RouteCourierTask extends AsyncTask<String, Integer, Object[]> {

        public interface AsyncResponse {
            void processFinish(Object[] output);
        }

        private AsyncResponse delegate = null;

        private RouteCourierTask(AsyncResponse delegate){
            this.delegate = delegate;
        }

        @Override
        protected Object[] doInBackground(String... params) {
            MyGraphHopperRoadManager mgh = new MyGraphHopperRoadManager();
            try {
                Road road = mgh.getRoads(params[0])[0];
                return new Object[]{road, mgh.source, mgh.destination};
            }catch (ArrayIndexOutOfBoundsException e){
                return new Object[]{null, null, null};
            }
        }

        @Override
        protected void onPostExecute(Object[] result) {
            delegate.processFinish(result);
        }
    }

    static class RouteFromUrlTask extends AsyncTask<String, Integer, String> {

        public interface AsyncResponse {
            void processFinish(String output);
        }

        private AsyncResponse delegate = null;

        private RouteFromUrlTask(AsyncResponse delegate){
            this.delegate = delegate;
        }

        @Override
        protected String doInBackground(String... params) {
            return RouteUtils.getRouteFromUrl(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }
    }
}
