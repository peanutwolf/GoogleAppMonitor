package com.peanutwolf.googleappmonitor.Utilities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;

import com.peanutwolf.googleappmonitor.Models.ShakePointPOJO;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by vigursky on 05.10.2016.
 */
public class GradientPolyline extends Polyline {
    public static final String TAG = GradientPolyline.class.getSimpleName()+"Class";
    private ArrayList<GradientLine> mLines = new ArrayList<>();

    public GradientPolyline(Context ctx) {
        super(ctx);
    }

    @Override
    public void setPoints(List<GeoPoint> points) {
        Point point0 = new Point(points.get(0).getLatitudeE6(), points.get(0).getLongitudeE6());

        for(int i = 0; i < points.size(); i++){
            Point point1 = new Point(points.get(i).getLatitudeE6(), points.get(i).getLongitudeE6());
            mLines.add(new GradientLine(point0, point1));

            point0 = point1;
        }

        super.setPoints(points);
    }

    public void setShakePoints(List<ShakePointPOJO> points){
        for(GradientLine line : mLines){
            double acceleration = 0;
            int n = 0;

            for(int i = 0; i < points.size(); i++){
                if(points.get(i).getLatitudeE6() == line.mPoint0.x && points.get(i).getLongitudeE6() == line.mPoint0.y){
                    acceleration += points.get(i).getAccelerationValue();
                    n++;
                }else if(points.get(i).getLatitudeE6() == line.mPoint1.x && points.get(i).getLongitudeE6() == line.mPoint1.y){
                    acceleration += points.get(i).getAccelerationValue();
                    n++;
                }
            }
            Log.d(TAG, "[setShakePoints] Calculated acceleration="+acceleration/n+" for line=" + line);
        }

    }

    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);
    }

    private class GradientLine{
        private Point mPoint0;
        private Point mPoint1;
        private Color mColor0;
        private Color mColor1;

        GradientLine(Point point0, Point point1){
            this.mPoint0 = point0;
            this.mPoint1 = point1;
        }

        @Override
        public String toString() {
            return mPoint0.toString() + "/" + mPoint1.toString();
        }
    }
}
