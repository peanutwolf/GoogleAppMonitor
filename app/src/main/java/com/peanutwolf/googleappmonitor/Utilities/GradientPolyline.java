package com.peanutwolf.googleappmonitor.Utilities;

import android.content.Context;
import android.graphics.Canvas;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

/**
 * Created by vigursky on 05.10.2016.
 */
public class GradientPolyline extends Polyline {
    public GradientPolyline(Context ctx) {
        super(ctx);
    }

    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);
    }
}
