package com.peanutwolf.googleappmonitor.Services;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.peanutwolf.googleappmonitor.Database.ShakeDBContentProvider;
import com.peanutwolf.googleappmonitor.Database.ShakeDatabase;
import com.peanutwolf.googleappmonitor.Models.ShakePointModel;
import com.peanutwolf.googleappmonitor.Models.TrekModel;
import com.peanutwolf.googleappmonitor.Utilities.DynamicDataSourceLoop;


/**
 * Created by vigursky on 30.03.2016.
 */
public class DataSaverService extends Service {

    private static final String TAG = DataSaverService.class.getName();
    private ContentResolver mContentResolver;
    private final Binder mBinder = new DataSaverBinder();
    private int mRouteId = 1;
    private Looper mServiceLooper;
    private DataSaverHandler mServiceHandler;

    private class DataSaverHandler extends Handler{
        public DataSaverHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            synchronized (mContentResolver){

            }
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread dataSaverServiceThread = new HandlerThread("DataSaverService");
        dataSaverServiceThread.start();

        mServiceLooper = dataSaverServiceThread.getLooper();
        mServiceHandler = new DataSaverHandler(mServiceLooper);

        mContentResolver = getContentResolver();
    }

    @Override
    public void onDestroy() {
        mServiceLooper.quit();
    }


    public void saveShakePoint(int trekID, ShakePointModel shakeModel){
        ContentValues values = new ContentValues();
        values.put(ShakeDatabase.COLUMN_AXISACCELX, shakeModel.getAxisAccelerationX() + "");
        values.put(ShakeDatabase.COLUMN_AXISACCELY, shakeModel.getAxisAccelerationY() + "");
        values.put(ShakeDatabase.COLUMN_AXISACCELZ, shakeModel.getAxisAccelerationZ() + "");
        values.put(ShakeDatabase.COLUMN_AXISROTATX, shakeModel.getAxisRotationX() + "");
        values.put(ShakeDatabase.COLUMN_AXISROTATY, shakeModel.getAxisRotationY() + "");
        values.put(ShakeDatabase.COLUMN_AXISROTATZ, shakeModel.getAxisRotationZ() + "");
        values.put(ShakeDatabase.COLUMN_LONGITUDE,  shakeModel.getCurrentLongitude() + "");
        values.put(ShakeDatabase.COLUMN_LATITUDE,   shakeModel.getCurrentLatitude() + "");
        values.put(ShakeDatabase.COLUMN_SPEED,      shakeModel.getCurrentSpeed() + "");
        values.put(ShakeDatabase.COLUMN_TIMESTAMP,  shakeModel.getCurrentTimestamp() + "");
        values.put(ShakeDatabase.COLUMN_TREKID,     trekID + "");
        mContentResolver.insert(ShakeDBContentProvider.CONTENT_URI, values);
    }

    public int saveTrekModel(TrekModel trek){
        ContentValues values = new ContentValues();

        values.put(ShakeDatabase.COLUMN_TIMESTAMP, trek.getTimestamp() + "");
        values.put(ShakeDatabase.COLUMN_DISTANCE, trek.getDistance() + "");

        Uri savedTrekId = mContentResolver.insert(ShakeDBContentProvider.CONTENT_TREK_URI, values);

        return new Integer(savedTrekId.getLastPathSegment());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public int calculateNextRouteId() {

        int currentRouteId = 0;
        String [] mProjection = {ShakeDatabase.COLUMN_TREKID};
        Cursor routeIDs = mContentResolver.query(ShakeDBContentProvider.CONTENT_URI, mProjection, null, null, null);
        routeIDs.moveToFirst();
        if(routeIDs.getCount() > 0){
            do{
                assert routeIDs != null;
                int columnIndex = routeIDs.getColumnIndex(ShakeDatabase.COLUMN_TREKID);
                String columnValue = routeIDs.getString(columnIndex);
                int routeid_tmp = Integer.valueOf(columnValue);
                currentRouteId = currentRouteId < routeid_tmp ? routeid_tmp : currentRouteId;
            }while(routeIDs.moveToNext());
        }

        routeIDs.close();
        return ++currentRouteId;

    }

    public class DataSaverBinder extends Binder {
        public DataSaverService getService() {
            return DataSaverService.this;
        }
    }
}
