package com.peanutwolf.googleappmonitor.Models;

import android.content.ContentResolver;
import android.database.Cursor;

import com.peanutwolf.googleappmonitor.Database.ShakeDBContentProvider;

/**
 * Created by vigursky on 07.09.2016.
 */
public class TrekModel {

    private int id;
    private long mTimestamp;
    private long mDistance;

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }

    public long getDistance() {
        return mDistance;
    }

    public void setDistance(long distance) {
        this.mDistance = distance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
