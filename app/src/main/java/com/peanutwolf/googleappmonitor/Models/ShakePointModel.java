package com.peanutwolf.googleappmonitor.Models;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

/**
 * Created by vigursky on 10.04.2016.
 */
public class ShakePointModel {
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

    public void fillModelFromEvent(SensorEvent event){
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

    protected long getEventTimestampInMills(SensorEvent event) {
        long timestamp = event.timestamp / 1000 / 1000;

        if (System.currentTimeMillis() - timestamp > 86400000 * 2) {

            timestamp = System.currentTimeMillis()
                    + (event.timestamp - System.nanoTime()) / 1000000L;
        }

        return timestamp;
    }
}
