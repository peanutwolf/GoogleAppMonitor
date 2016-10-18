package com.peanutwolf.googleappmonitor.Services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.peanutwolf.googleappmonitor.Models.ShakePointPOJO;
import com.peanutwolf.googleappmonitor.Models.TrekModel;
import com.peanutwolf.googleappmonitor.Models.TrekModelDAO;
import com.peanutwolf.googleappmonitor.Processors.ShakePointProcessor;
import com.peanutwolf.googleappmonitor.Services.Interfaces.LocationServiceDataSource;
import com.peanutwolf.googleappmonitor.Services.Interfaces.ShakeServiceDataSource;
import com.peanutwolf.googleappmonitor.Utilities.RangedLinkedList;
import com.peanutwolf.googleappmonitor.Processors.LocationPointProcessor;

import java.util.LinkedList;
import java.util.List;

public class ShakeSensorService extends Service implements SensorEventListener, ShakeServiceDataSource<ShakePointPOJO> {
    public static final String TAG = ShakeSensorService.class.getName()+"Service";
    private static final  int DOMAIN_WIDTH = 100;
    private SensorManager mSensorMgr;
    private static final int TIME_THRESHOLD = 10;
    private static final int TIME_THRESHOLD_DB = 1;
    private long mLastTime;
    private long mLastTime_db;
    private HandlerThread thread;
    private final Binder mBinder = new ShakeSensorBinder();
    private RangedLinkedList<ShakePointPOJO> mSensorViewData = new RangedLinkedList<>(DOMAIN_WIDTH);;
    private boolean mShakeStarted = false;
    private Thread mUpdaterThread;
    private LocationServiceDataSource mLocationServiceDataSource;
    private DataSaverService mDataSaverSource;
    private Intent mLocationGoogleServiceIntent;
    private Intent mDataSaverServiceIntent;
    private boolean mAllowSaving = false;
    private TrekModel mCurrentTrek;
    private LocationPointProcessor mLocationPointProcessor;
    private ShakePointProcessor mShakePointProcessor;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mLocationPointProcessor = new LocationPointProcessor();
        mShakePointProcessor = new ShakePointProcessor(20);

        mUpdaterThread = new Thread(new SupportSensorUpdater());
        mUpdaterThread.start();

        thread = new HandlerThread("SensorLoop");
        thread.start();

        mShakeStarted = startShakeSensorListening();

        mLocationGoogleServiceIntent = new Intent(getApplicationContext(), LocationGoogleService.class);
        mDataSaverServiceIntent = new Intent(getApplicationContext(), DataSaverService.class);

        bindService(mLocationGoogleServiceIntent, mLocationConnector, Context.BIND_AUTO_CREATE);
        bindService(mDataSaverServiceIntent, mDataSaverConnector, Context.BIND_AUTO_CREATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "OnDestroy");
        super.onDestroy();
        thread.quit();
        mUpdaterThread.interrupt();
        if(mShakeStarted)
            mSensorMgr.unregisterListener(this);
        unbindService(mLocationConnector);
        unbindService(mDataSaverConnector);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long now = System.currentTimeMillis();
        ShakePointPOJO shakePoint = new ShakePointPOJO();
        shakePoint.dataToModel(event);
        if(mLocationServiceDataSource != null){
            shakePoint.setCurrentLatLng(mLocationServiceDataSource.getLastKnownLatLng());
            shakePoint.setCurrentSpeed(mLocationServiceDataSource.getSpeed());
        }

        mLocationPointProcessor.addPoint(shakePoint);
        mShakePointProcessor.addPoint(shakePoint);

        if((now - mLastTime) > TIME_THRESHOLD){
            synchronized(mSensorViewData){
                mSensorViewData.add(shakePoint);
            }
            mLastTime = now;
        }
        if((now - mLastTime_db) > TIME_THRESHOLD_DB
                && (mShakePointProcessor.needToWrite() || mLocationPointProcessor.needToWrite())){
            writeToDB(shakePoint);
            mLastTime_db = now;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private synchronized void writeToDB(ShakePointPOJO shakePoint){
        if(mAllowSaving == true)
            mDataSaverSource.saveShakePoint(mCurrentTrek.getId(), shakePoint);
    }

    public synchronized void startTrekking(){
        mCurrentTrek = new TrekModel();
        mCurrentTrek.setTimestamp(System.currentTimeMillis());
        mCurrentTrek.setDistance(0);
        int trekId = mDataSaverSource.saveTrekModel(mCurrentTrek);
        mCurrentTrek.setId(trekId);
        mAllowSaving = true;
    }

    public synchronized void stopTrekking(){
        mAllowSaving = false;
    }


    public synchronized boolean isDataSavingAllowed(){
        return mAllowSaving;
    }

    @Override
    public LinkedList<ShakePointPOJO> getAccelerationData() {
        synchronized (mSensorViewData) {
            return new LinkedList<>(mSensorViewData);
        }
    }

    @Override
    public int getAverageAccelerationData() {
        return (int)mSensorViewData.getAverageType().getAccelerationValue();
        //return ((RangedLinkedList)mSensorViewData).getAverage();
    }


    public class ShakeSensorBinder extends Binder {
        public ShakeSensorService getService() {
            return ShakeSensorService.this;
        }
    }

    ServiceConnection mLocationConnector = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLocationServiceDataSource = ((LocationGoogleService.LocationServiceBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    ServiceConnection mDataSaverConnector = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDataSaverSource = ((DataSaverService.DataSaverBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

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
                synchronized (mSensorViewData) {
                    long now = System.currentTimeMillis();

                    if ((now - mLastTime) > TIME_THRESHOLD) {
                        if(!mSensorViewData.isEmpty())
                            mSensorViewData.add(mSensorViewData.get(mSensorViewData.size()-1));
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
