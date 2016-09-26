package com.peanutwolf.googleappmonitor.Models;

import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;

import com.google.android.gms.maps.model.LatLng;
import com.peanutwolf.googleappmonitor.Database.ShakeDatabase;

/**
 * Created by vigursky on 10.04.2016.
 */
public class ShakePointPOJO {
    private int  mRouteId = 0;
    private double mAxisAccelerationX = 0;
    private double mAxisAccelerationY = 0;
    private double mAxisAccelerationZ = 0;
    private double mAxisRotationX = 0;
    private double mAxisRotationY = 0;
    private double mAxisRotationZ = 0;
    private double mCurrentLatitude = 0;
    private double mCurrentLongitude = 0;
    private float mCurrentSpeed = 0.0F;
    private long mCurrentTimestamp  = 0L;

    public ShakePointPOJO(){

    }

    public ShakePointPOJO(final Cursor cursor){
        this.dataToModel(cursor);
    }

    public double getAxisAccelerationX() {
        return mAxisAccelerationX;
    }

    public void setAxisX(double axisX) {
        this.mAxisAccelerationX = axisX;
    }

    public double getAxisAccelerationY() {
        return mAxisAccelerationY;
    }

    public void setAxisY(double axisY) {
        this.mAxisAccelerationY = axisY;
    }

    public double getAxisAccelerationZ() {
        return mAxisAccelerationZ;
    }

    public void setAxisZ(double axisZ) {
        this.mAxisAccelerationZ = axisZ;
    }

    public double getCurrentLatitude() {
        return mCurrentLatitude;
    }

    public void setCurrentLatLng(LatLng latLng){
        this.mCurrentLatitude = latLng.latitude;
        this.mCurrentLongitude = latLng.longitude;
    }

    public void setCurrentLatitude(double currentLatitude) {
        this.mCurrentLatitude = currentLatitude;
    }

    public double getCurrentLongitude() {
        return mCurrentLongitude;
    }

    public void setCurrentLongitude(double currentLongitude) {
        this.mCurrentLongitude = currentLongitude;
    }

    public float getCurrentSpeed() {
        return mCurrentSpeed;
    }

    public void setCurrentSpeed(float currentSpeed) {
        this.mCurrentSpeed = currentSpeed;
    }

    public void setAxisAll(double axisX, double axisY, double axisZ){
        this.mAxisAccelerationX = axisX;
        this.mAxisAccelerationY = axisY;
        this.mAxisAccelerationZ = axisZ;
    }

    public void setGeoPointAll(double latitude, double longitude, float speed){
        this.mCurrentLatitude = latitude;
        this.mCurrentLongitude = longitude;
        this.mCurrentSpeed = speed;
    }

    public long getCurrentTimestamp() {
        return mCurrentTimestamp;
    }

    public void setCurrentTimestamp(long currentTimestamp) {
        this.mCurrentTimestamp = currentTimestamp;
    }

    public double getAxisRotationX() {
        return mAxisRotationX;
    }

    public void setAxisRotationX(double axisRotationX) {
        this.mAxisRotationX = axisRotationX;
    }

    public double getAxisRotationY() {
        return mAxisRotationY;
    }

    public void setAxisRotationY(double axisRotationY) {
        this.mAxisRotationY = axisRotationY;
    }

    public double getAxisRotationZ() {
        return mAxisRotationZ;
    }

    public void setAxisRotationZ(double axisRotationZ) {
        this.mAxisRotationZ = axisRotationZ;
    }

    public double getAccelerationValue(){
        return (mAxisAccelerationX + mAxisAccelerationY + mAxisAccelerationZ)/3;
    }

    public void dataToModel(SensorEvent event){
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mAxisAccelerationX = event.values[0];
            mAxisAccelerationY = event.values[1];
            mAxisAccelerationZ = event.values[2];
            setCurrentTimestamp(getEventTimestampInMills(event));
        }else if(event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR){
            mAxisRotationX = event.values[0];
            mAxisRotationY = event.values[1];
            mAxisRotationZ = event.values[2];
        }
    }

    public void dataToModel(final Cursor cursor){
        final String [] columnsToGet = {ShakeDatabase.COLUMN_TREKID,
                ShakeDatabase.COLUMN_AXISACCELX,
                ShakeDatabase.COLUMN_AXISACCELY,
                ShakeDatabase.COLUMN_AXISACCELZ,
                ShakeDatabase.COLUMN_AXISROTATX,
                ShakeDatabase.COLUMN_AXISROTATY,
                ShakeDatabase.COLUMN_AXISROTATZ,
                ShakeDatabase.COLUMN_LATITUDE,
                ShakeDatabase.COLUMN_LONGITUDE,
                ShakeDatabase.COLUMN_SPEED,
                ShakeDatabase.COLUMN_TIMESTAMP};

        for(String column : columnsToGet){
            int columnIndex = cursor.getColumnIndex(column);
            if(columnIndex == -1)
                continue;
            String columnValue = cursor.getString(columnIndex);
            switch (column){
                case ShakeDatabase.COLUMN_TREKID:
                    mRouteId = Integer.valueOf(columnValue);
                    break;
                case ShakeDatabase.COLUMN_AXISACCELX:
                    this.mAxisAccelerationX = Double.valueOf(columnValue);
                    break;
                case ShakeDatabase.COLUMN_AXISACCELY:
                    this.mAxisAccelerationY = Double.valueOf(columnValue);
                    break;
                case ShakeDatabase.COLUMN_AXISACCELZ:
                    this.mAxisAccelerationZ = Double.valueOf(columnValue);
                    break;
                case ShakeDatabase.COLUMN_AXISROTATX:
                    this.mAxisRotationX = Double.valueOf(columnValue);
                    break;
                case ShakeDatabase.COLUMN_AXISROTATY:
                    this.mAxisRotationY = Double.valueOf(columnValue);
                    break;
                case ShakeDatabase.COLUMN_AXISROTATZ:
                    this.mAxisRotationZ = Double.valueOf(columnValue);
                    break;
                case ShakeDatabase.COLUMN_LATITUDE:
                    this.mCurrentLatitude = Double.valueOf(columnValue);
                    break;
                case ShakeDatabase.COLUMN_LONGITUDE:
                    this.mCurrentLongitude = Double.valueOf(columnValue);
                    break;
                case ShakeDatabase.COLUMN_SPEED:
                    this.mCurrentSpeed = Float.valueOf(columnValue);
                    break;
                case ShakeDatabase.COLUMN_TIMESTAMP:
                    mCurrentTimestamp = Long.valueOf(columnValue);
                    break;
                default:
                    break;
            }
        }
    }

    protected long getEventTimestampInMills(SensorEvent event) {
        long timestamp = event.timestamp / 1000 / 1000;

        if (System.currentTimeMillis() - timestamp > 86400000 * 2) {

            timestamp = System.currentTimeMillis()
                    + (event.timestamp - System.nanoTime()) / 1000000L;
        }

        return timestamp;
    }

    public int getRouteId() {
        return mRouteId;
    }

    public void setRouteId(int routeId) {
        this.mRouteId = routeId;
    }
}
