package com.peanutwolf.googleappmonitor.Processors;

import com.peanutwolf.googleappmonitor.Models.ShakePointPOJO;

/**
 * Created by vigursky on 27.09.2016.
 */
public class ShakePointProcessor extends PointTemplate<ShakePointPOJO>{

    private enum Direction{
        EQUALS,
        GROWING,
        FALLING
    }

    Direction mFunctionSign = Direction.EQUALS;

    @Override
    protected int calculateDelta() {
        int delta = 1;
        int last = this.mTimelinePoints.size() - 1;

        if(this.mTimelinePoints.size() <= 1)
            return 1;

        if(this.mTimelinePoints.get(last-1).getAccelerationValue() < this.mTimelinePoints.get(last).getAccelerationValue()){
            if(mFunctionSign == Direction.GROWING)
                delta = 0;
            mFunctionSign = Direction.GROWING;
        }else if(this.mTimelinePoints.get(last-1).getAccelerationValue() > this.mTimelinePoints.get(last).getAccelerationValue()){
            if(mFunctionSign == Direction.FALLING)
                delta = 0;
            mFunctionSign = Direction.FALLING;
        }else if(this.mTimelinePoints.get(last-1).getAccelerationValue() == this.mTimelinePoints.get(last).getAccelerationValue()){
            mFunctionSign = Direction.EQUALS;
            delta = 0;
        }

        return delta;
    }
}
