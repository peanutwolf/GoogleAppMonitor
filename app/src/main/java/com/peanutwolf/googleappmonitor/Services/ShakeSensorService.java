package com.peanutwolf.googleappmonitor.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import com.peanutwolf.googleappmonitor.Database.ShakeDatabase;
import com.peanutwolf.googleappmonitor.Models.ShakePointModel;
import com.peanutwolf.googleappmonitor.Utilities.LocationSensorManager;
import com.peanutwolf.googleappmonitor.Utilities.RangedLinkedList;

import java.util.LinkedList;
import java.util.List;

public class ShakeSensorService extends Service implements SensorEventListener, ShakeServiceDataSource {
    public static final String TAG = ShakeSensorService.class.getName();
    public static final String BROADCAST_SHAKE_SENSOR = "com.peanutwolf.shakesensorservice";
    private static final  int DOMAIN_WIDTH = 100;
    private SensorManager mSensorMgr;
    private LocationSensorManager mLocationManager;
    private Intent intentBroadcast;
    private static final int TIME_THRESHOLD = 10;
    private static final int TIME_THRESHOLD_DB = 1;
    private long mLastTime;
    private long mLastTime_db;
    private HandlerThread thread;
    private final Binder mBinder = new ShakeSensorBinder();
    private List<Number> mSensorData;
    private boolean mShakeStarted = false;
    private Thread mUpdaterThread;
    private ShakePointModel mShakePointModel;

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

        mShakePointModel = new ShakePointModel();

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

        mShakePointModel.fillModelFromEvent(event);

        if ((now - mLastTime) > TIME_THRESHOLD) {
            synchronized (mSensorData) {
                mSensorData.add(mShakePointModel.getAccelerationValue());
            }
            mLastTime = now;
        }
        if ((now - mLastTime_db) > TIME_THRESHOLD_DB) {
            writeToDB();
            mLastTime_db = now;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    void writeToDB(){
        intentBroadcast.putExtra(ShakeDatabase.COLUMN_AXISACCELX, mShakePointModel.getAxisAccelerationX());
        intentBroadcast.putExtra(ShakeDatabase.COLUMN_AXISACCELY, mShakePointModel.getAxisAccelerationY());
        intentBroadcast.putExtra(ShakeDatabase.COLUMN_AXISACCELZ, mShakePointModel.getAxisAccelerationZ());
        intentBroadcast.putExtra(ShakeDatabase.COLUMN_AXISROTATX, mShakePointModel.getAxisRotationX());
        intentBroadcast.putExtra(ShakeDatabase.COLUMN_AXISROTATY, mShakePointModel.getAxisRotationY());
        intentBroadcast.putExtra(ShakeDatabase.COLUMN_AXISROTATZ, mShakePointModel.getAxisRotationZ());
        intentBroadcast.putExtra(ShakeDatabase.COLUMN_LATITUDE, mLocationManager.getCurrentLatitude());
        intentBroadcast.putExtra(ShakeDatabase.COLUMN_LONGITUDE, mLocationManager.getCurrentLongitude());
        intentBroadcast.putExtra(ShakeDatabase.COLUMN_SPEED, mLocationManager.getCurrentSpeed());
        intentBroadcast.putExtra(ShakeDatabase.COLUMN_TIMESTAMP, mShakePointModel.getCurrentTimestamp());
        sendBroadcast(intentBroadcast);
    }

    @Override
    public List<Number> getAccelerationData() {
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
            supported = mSensorMgr.registerListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI, handler);
            supported = mSensorMgr.registerListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI, handler);
        }
        catch (Exception e){
            e.printStackTrace();
        }
       // if ((!supported)&&(mSensorMgr != null)) mSensorMgr.unregisterListener(this);

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
                            mSensorData.add(mShakePointModel.getAccelerationValue());
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
