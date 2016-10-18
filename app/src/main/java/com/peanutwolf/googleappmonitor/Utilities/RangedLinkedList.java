package com.peanutwolf.googleappmonitor.Utilities;


import com.peanutwolf.googleappmonitor.Models.ShakePointDAO;
import com.peanutwolf.googleappmonitor.Models.ShakePointPOJO;
import com.peanutwolf.googleappmonitor.Processors.AverageProcessor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;


/**
 * Created by vigursky on 30.03.2016.
 */
public class RangedLinkedList<E extends AverageProcessor<E>> extends LinkedList<E> {

    private int mRange;
    private E   mSumType;
    private E   mAverageType;

    public RangedLinkedList(int range){
        super();
        mRange = range;

        //TODO: Check type;
        mSumType     = (E) new ShakePointPOJO();
        mAverageType = (E) new ShakePointPOJO();
    }

    @Override
    public synchronized boolean add(E object) {
        boolean result;
        E firstElement = null;

        if(this.size() >= mRange){
            firstElement = this.removeFirst();
        }
        result = super.add(object);

        mSumType.sumValue(object);
        if(firstElement != null)
            mSumType.subValue(object);

        mAverageType.divValue(this.size());

        return result;
    }

    public E getAverageType(){

        return mAverageType;
    }

}
