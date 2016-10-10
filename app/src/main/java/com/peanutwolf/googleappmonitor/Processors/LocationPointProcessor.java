package com.peanutwolf.googleappmonitor.Processors;

import android.util.Log;

import com.peanutwolf.googleappmonitor.Models.ShakePointPOJO;

/**
 * Created by vigursky on 27.09.2016.
 */

public class LocationPointProcessor extends PointTemplate<ShakePointPOJO>{
    private static final String TAG = LocationPointProcessor.class.getSimpleName();

    @Override
    protected int calculateDelta() {
        double latitude_old, longitude_old;
        double latitude_new, longitude_new;
        int delta = 0;

        do {
            if(this.mTimelinePoints.size() <= 1) {
                delta = 1;
                break;
            }

            latitude_old  = mTimelinePoints.get(mTimelinePoints.size()-2).getCurrentLatitude();
            longitude_old = mTimelinePoints.get(mTimelinePoints.size()-2).getCurrentLongitude();

            latitude_new  = mTimelinePoints.get(mTimelinePoints.size()-1).getCurrentLatitude();
            longitude_new = mTimelinePoints.get(mTimelinePoints.size()-1).getCurrentLongitude();

            if(latitude_old != latitude_new || longitude_old != longitude_new){
                delta = 1;
                Log.d(TAG, "[calculateDelta] calculated delta = " + delta);
                break;
            }
        }while (false);

        return delta;
    }
}