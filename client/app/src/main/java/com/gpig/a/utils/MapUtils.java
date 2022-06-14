package com.gpig.a.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import com.gpig.a.R;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public final class MapUtils {

    private MapUtils(){}

    private static Drawable getMapIcon(Context c, String type){
        Drawable drawable = null;
        switch (type) {
            case "current":
                drawable = ContextCompat.getDrawable(c, R.drawable.current_location);
                drawable.setColorFilter(Color.argb(150, 0, 191, 255), PorterDuff.Mode.SRC_IN);
                break;
            case "src":
                drawable = ContextCompat.getDrawable(c, R.drawable.location).mutate();
                drawable.setColorFilter(Color.argb(150, 0, 255, 0), PorterDuff.Mode.SRC_IN);
                break;
            case "des":
                drawable = ContextCompat.getDrawable(c, R.drawable.location).mutate();
                drawable.setColorFilter(Color.argb(150,255,0,0), PorterDuff.Mode.SRC_IN);
                break;
        }
        return drawable;
    }

    public static Marker createMarker(MapView mv, Context c, String type, GeoPoint loc){
        Marker marker = new Marker(mv);
        marker.setIcon(MapUtils.getMapIcon(c, type));
        marker.setPosition(loc);
        return marker;
    }
}
