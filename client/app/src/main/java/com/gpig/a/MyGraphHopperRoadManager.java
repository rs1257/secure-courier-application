package com.gpig.a;

import android.util.Log;
import android.util.SparseIntArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.bonuspack.utils.PolylineEncoder;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class MyGraphHopperRoadManager {

    private final String TAG = "MyGHRM";
    public GeoPoint source = null;
    public GeoPoint destination = null;

    private static final SparseIntArray MANEUVERS;
    static {
        MANEUVERS = new SparseIntArray();
        MANEUVERS.put(0, 1); //Continue
        MANEUVERS.put(1, 6); //Slight right
        MANEUVERS.put(2, 7); //Right
        MANEUVERS.put(3, 8); //Sharp right
        MANEUVERS.put(-3, 5); //Sharp left
        MANEUVERS.put(-2, 4); //Left
        MANEUVERS.put(-1, 3); //Slight left
        MANEUVERS.put(4, 24); //Arrived
        MANEUVERS.put(5, 24); //Arrived at waypoint
    }

    public MyGraphHopperRoadManager() {
        super();
    }

    private Road[] defaultRoad(ArrayList<GeoPoint> waypoints) {
        Road[] roads = new Road[1];
        roads[0] = new Road(waypoints);
        return roads;
    }

    public Road[] getRoads(String jString) {
        try {
            JSONObject jRoot = new JSONObject(jString);
            JSONArray jPaths = jRoot.optJSONArray("paths");
            Road[] roads = new Road[jPaths.length()];
            for (int r = 0; r < jPaths.length(); r++) {
                JSONObject jPath = jPaths.getJSONObject(r);
                // This gets the points from the JSON and adds them to an array
                JSONObject route_geometry = jPath.getJSONObject("points");
                JSONArray points = route_geometry.getJSONArray("coordinates");
                int no = points.length();
                ArrayList<GeoPoint> finalPoints = new ArrayList<>();
                for (int i = 0; i < no; i++) {
                    JSONArray lnglat = points.getJSONArray(i);
                    finalPoints.add(new GeoPoint(lnglat.getDouble(1), lnglat.getDouble(0)));
                }
                Road road = new Road();
                roads[r] = road;
                road.mRouteHigh = finalPoints;
                source = finalPoints.get(0);
                destination = finalPoints.get(finalPoints.size() - 1);
                JSONArray jInstructions = jPath.getJSONArray("instructions");
                int n = jInstructions.length();
                for (int i = 0; i < n; i++) {
                    JSONObject jInstruction = jInstructions.getJSONObject(i);
                    RoadNode node = new RoadNode();
                    JSONArray jInterval = jInstruction.getJSONArray("interval");
                    int positionIndex = jInterval.getInt(0);
                    node.mLocation = road.mRouteHigh.get(positionIndex);
                    node.mLength = jInstruction.getDouble("distance") / 1000.0;
                    node.mDuration = jInstruction.getInt("time") / 1000.0; //Segment duration in seconds.
                    int direction = jInstruction.getInt("sign");
                    node.mManeuverType = getManeuverCode(direction);
                    node.mInstructions = jInstruction.getString("text");
                    road.mNodes.add(node);
                }
                road.mLength = jPath.getDouble("distance") / 1000.0;
                road.mDuration = jPath.getInt("time") / 1000.0;
                JSONArray jBBox = jPath.getJSONArray("bbox");
                road.mBoundingBox = new BoundingBox(jBBox.getDouble(3),
                        jBBox.getDouble(2), jBBox.getDouble(1),
                        jBBox.getDouble(0));
                road.mStatus = Road.STATUS_OK;
                Log.d(TAG, "MyGraphHopper.getRoads - finished");
            }
            return roads;
        } catch (JSONException e) {
            e.printStackTrace();
            return new Road[]{};
        }
    }

    private int getManeuverCode(int direction){
        Integer code = MANEUVERS.get(direction);
        if (code != null)
            return code;
        else
            return 0;
    }

}