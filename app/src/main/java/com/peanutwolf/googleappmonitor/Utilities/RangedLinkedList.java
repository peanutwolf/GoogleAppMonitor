package com.peanutwolf.googleappmonitor.Utilities;

import java.util.LinkedList;

/**
 * Created by vigursky on 30.03.2016.
 */
public class RangedLinkedList<E> extends LinkedList<E> {

    private int mRange;
    private boolean mUpdated;

    public RangedLinkedList(int range){
        super();
        mRange = range;
        mUpdated = false;
    }

    @Override
    public synchronized boolean add(E object) {
        if(this.size() >= mRange){
            this.removeFirst();
        }
        return mUpdated = super.add(object);
    }

    public boolean isUpdated(){
        return mUpdated;
    }

    public void setUpdated(boolean updated){
        mUpdated = updated;
    }

}