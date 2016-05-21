package com.peanutwolf.googleappmonitor.Utilities;

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
        }

        return result;
    }

    public int getAverage(){
        return mAverage;
    }

}