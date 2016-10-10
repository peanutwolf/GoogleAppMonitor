package com.peanutwolf.googleappmonitor.Utilities;

import com.peanutwolf.googleappmonitor.Models.ShakePointPOJO;

import java.util.List;

/**
 * Created by vigursky on 26.09.2016.
 */

public class DeltaProcessor<T> {
    private static final int DELTA_BANDWIDTH = 100;
    private List<T> mTimelinePoints;

    public DeltaProcessor(){
        mTimelinePoints = new RangedLinkedList<>(100);
    }

    public void addPoint(T t){
        mTimelinePoints.add(t);
    }

    public boolean needToWrite(T t){
        return true;
    }
}
