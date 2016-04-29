package com.peanutwolf.googleappmonitor.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.peanutwolf.googleappmonitor.Database.ShakeDatabase;
import com.peanutwolf.googleappmonitor.Models.ShakePointModel;
import com.peanutwolf.googleappmonitor.Utilities.LocationSensorManager;
import com.peanutwolf.googleappmonitor.Utilities.RangedLinkedList;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ShakeSensorService extends Service implements SensorEventListener, ShakeServiceDataSource {
    public static final String TAG = ShakeSensorService.class.getName();
    public static final String BROADCAST_SHAKE_SENSOR = "com.peanutwolf.shakesensorservice";
    private static final  int DOMAIN_WIDTH = 100;
    private SensorManager mSensorMgr;
    private LocationSensorManager mLocationManager;
    private Intent intentBroadcast;
    private static final int TIME_THRESHOLD = 1;
    private static final int TIME_THRESHOLD_DB = 5000;
    private long mLastTime;
    private long mLastTime_db;
    private HandlerThread thread;
    private final Binder mBinder = new ShakeSensorBinder();
    private List<Number> mSensorData;
    private boolean mShakeStarted = false;
    private float mLastAxisXValue = 0.0F;
    private Thread mUpdaterThread;

    @Override
    public IBinder onBind(Intent intent) {
        mShakeStarted = startShakeSensorListening();
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mShakeStarted = startShakeSensorListening();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorData = new RangedLinkedList<>(DOMAIN_WIDTH);
        mLocationManager = new LocationSensorManager(getApplicationContext());

        mUpdaterThread = new Thread(new SupportSensorUpdater());
        mUpdaterThread.start();

        thread = new HandlerThread("SensorLoop");
        thread.start();
        intentBroadcast = new Intent(BROADCAST_SHAKE_SENSOR);

        startLocationManger();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        thread.quit();
        mUpdaterThread.interrupt();
        if(mShakeStarted)
            mSensorMgr.unregisterListener(this);
        mLocationManager.stopLocationManager();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long now = System.currentTimeMillis();
        mLastAxisXValue = event.values[0];

        if ((now - mLastTime) > TIME_THRESHOLD) {
            synchronized (mSensorData) {
                mSensorData.add(event.values[0]);
            }
            mLastTime = now;
        }
        if ((now - mLastTime_db) > TIME_THRESHOLD_DB) {
            intentBroadcast.putExtra(ShakeDatabase.COLUMN_AXISX, event.values[0]);
            intentBroadcast.putExtra(ShakeDatabase.COLUMN_LATITUDE, mLocationManager.getCurrentLatitude());
            intentBroadcast.putExtra(ShakeDatabase.COLUMN_LONGITUDE, mLocationManager.getCurrentLongitude());
            intentBroadcast.putExtra(ShakeDatabase.COLUMN_SPEED, mLocationManager.getCurrentSpeed());
            sendBroadcast(intentBroadcast);
            mLastTime_db = now;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public List<Number> getAxisXData() {
        synchronized (mSensorData) {
            return new LinkedList<>(mSensorData);
        }
    }


    public class ShakeSensorBinder extends Binder {
        public ShakeSensorService getService() {
            return ShakeSensorService.this;
        }
    }


    private boolean startLocationManger() {
        mLocationManager.startLocationManager();

        return true;
    }


    private boolean startShakeSensorListening(){
        boolean supported = false;

        if(mShakeStarted)
            return true;
        mSensorMgr = (SensorManager)getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

        if (mSensorMgr == null){
            throw new UnsupportedOperationException("Sensors not supported");
        }
        try{
            Handler handler = new Handler(thread.getLooper());
            supported = mSensorMgr.registerListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST, handler);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        if ((!supported)&&(mSensorMgr != null)) mSensorMgr.unregisterListener(this);

        return supported;
    }

    class SupportSensorUpdater implements Runnable{

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (mSensorData) {
                    long now = System.currentTimeMillis();

                    if ((now - mLastTime) > TIME_THRESHOLD) {
                        if(!mSensorData.isEmpty())
                            mSensorData.add(mLastAxisXValue);
                        mLastTime = now;
                    }
                }
                try {
                    Thread.sleep(TIME_THRESHOLD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
