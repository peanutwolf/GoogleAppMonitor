package com.peanutwolf.googleappmonitor.Utilities;

import com.androidplot.xy.XYSeries;
import com.peanutwolf.googleappmonitor.Services.ShakeServiceDataSource;

import java.util.List;

/**
 * Created by vigursky on 15.04.2016.
 */
public class SimpleDynamicSeries implements XYSeries {
    private List<Number> mAxisX;
    private ShakeServiceDataSource mDataSource;

    public void setDataSource(ShakeServiceDataSource source){
        mDataSource = source;
    }

    public void update(){
        if(mDataSource != null)
            mAxisX = mDataSource.getAccelerationData();
    }

    @Override
    public int size() {
        if (mAxisX != null)
            return mAxisX.size();
        else
            return 0;
    }

    @Override
    public Number getX(int index) {
        return index;
    }

    @Override
    public Number getY(int index) {
        Number number = 0;
        try {
            number = mAxisX.get(index);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.print("Index is = " + index);
        }
        return number;

    }

    @Override
    public String getTitle() {
        return "Accelerometer";
    }
}
