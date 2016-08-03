package com.peanutwolf.googleappmonitor.Utilities;

import android.util.Log;

import com.peanutwolf.googleappmonitor.Models.ShakePointModel;

import java.util.LinkedList;

/**
 * Created by vigursky on 30.03.2016.
 */
public class RangedLinkedList<E> extends LinkedList<E> {

    private int mRange;
    private int mAverage;
    private int mSum;

    public RangedLinkedList(int range){
        super();
        mRange = range;
    }

    @Override
    public synchronized boolean add(E object) {
        boolean result;
        E firstElement = null;
        if(this.size() >= mRange){
            firstElement = this.removeFirst();
        }
        result = super.add(object);
        if(Number.class.isInstance(object)){
            mSum += ((Number)object).intValue();
            if(firstElement != null){
                mSum -= ((Number)firstElement).intValue();
            }
            mAverage = mSum / this.size();
        }else if(ShakePointModel.class.isInstance(object)){

            int val = ((int) ((ShakePointModel) object).getAccelerationValue());
            mSum += val;
            if(firstElement != null){
                val = (int) ((ShakePointModel)firstElement).getAccelerationValue();
                mSum -= val;
            }
            mAverage = mSum / this.size();
        }

        return result;
    }

    public int getAverage(){
        return mAverage;
    }

}