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

    public ShakePointProcessor(int bandwidth){
        super(bandwidth);
    }

    Direction mFunctionSign = Direction.EQUALS;

    @Override
    protected int calculateDelta() {
        int delta = 1;
        int last = this.mTimelinePoints.size() - 1;
        double prevAccelValue;
        double currAccelValue;

        if(this.mTimelinePoints.size() <= 1)
            return 1;

        ShakePointPOJO averagePoint = mTimelinePoints.getAverageType();

        double max = averagePoint.getAxisAccelerationX();
        prevAccelValue = this.mTimelinePoints.get(last-1).getAxisAccelerationX();
        currAccelValue = this.mTimelinePoints.get(last).getAxisAccelerationX();
        if (max < averagePoint.getAxisAccelerationY()){
            max = averagePoint.getAxisAccelerationY();
            prevAccelValue = this.mTimelinePoints.get(last-1).getAxisAccelerationY();
            currAccelValue = this.mTimelinePoints.get(last).getAxisAccelerationY();
        }
        if (max < averagePoint.getAxisAccelerationZ()){
            //max = averagePoint.getAxisAccelerationZ();
            prevAccelValue = this.mTimelinePoints.get(last-1).getAxisAccelerationZ();
            currAccelValue = this.mTimelinePoints.get(last).getAxisAccelerationZ();
        }

        if(prevAccelValue < currAccelValue){
            if(mFunctionSign == Direction.GROWING)
                delta = 0;
            mFunctionSign = Direction.GROWING;
        }else if(prevAccelValue > currAccelValue){
            if(mFunctionSign == Direction.FALLING)
                delta = 0;
            mFunctionSign = Direction.FALLING;
        }else if(prevAccelValue == currAccelValue){
            mFunctionSign = Direction.EQUALS;
            delta = 0;
        }

        return delta;
    }
}
