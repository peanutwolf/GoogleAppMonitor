package com.peanutwolf.googleappmonitor.Processors;

import com.peanutwolf.googleappmonitor.Processors.Intefaces.SaveStrategy;
import com.peanutwolf.googleappmonitor.Utilities.RangedLinkedList;

import java.util.List;

/**
 * Created by vigursky on 27.09.2016.
 */
public abstract class PointTemplate<T extends AverageProcessor<T>> implements SaveStrategy {
    private static final int DELTA_BANDWIDTH_DEFAULT = 3;
    protected RangedLinkedList<T> mTimelinePoints;

    protected PointTemplate(int bandwidth){
        mTimelinePoints = new RangedLinkedList<>(bandwidth);
    }

    protected PointTemplate(){
        this(DELTA_BANDWIDTH_DEFAULT);
    }

    public boolean needToWrite(){
        if(this.calculateDelta() > 0){
            return true;
        }
        return false;
    }

    public void addPoint(T t){
        mTimelinePoints.add(t);
    }

    protected abstract int calculateDelta();
}