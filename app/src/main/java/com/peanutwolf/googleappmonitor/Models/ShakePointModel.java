package com.peanutwolf.googleappmonitor.Models;

/**
 * Created by vigursky on 10.04.2016.
 */
public class ShakePointModel {
    private double mAxisX = 0;
    private double mAxisY = 0;
    private double mAxisZ = 0;
    private double mCurrentLatitude = 0;
    private double mCurrentLongitude = 0;
    private float mCurrentSpeed = 0.0F;

    public double getAxisX() {
        return mAxisX;
    }

    public void setAxisX(double axisX) {
        this.mAxisX = axisX;
    }

    public double getAxisY() {
        return mAxisY;
    }

    public void setAxisY(double axisY) {
        this.mAxisY = axisY;
    }

    public double getAxisZ() {
        return mAxisZ;
    }

    public void setAxisZ(double axisZ) {
        this.mAxisZ = axisZ;
    }

    public double getCurrentLatitude() {
        return mCurrentLatitude;
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
        this.mAxisX = axisX;
        this.mAxisY = axisY;
        this.mAxisZ = axisZ;
    }

    public void setGeoPointAll(double latitude, double longitude, float speed){
        this.mCurrentLatitude = latitude;
        this.mCurrentLongitude = longitude;
        this.mCurrentSpeed = speed;
    }
}
