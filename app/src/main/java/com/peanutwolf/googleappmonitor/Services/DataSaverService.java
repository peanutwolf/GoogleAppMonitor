package com.peanutwolf.googleappmonitor.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.peanutwolf.googleappmonitor.Database.ShakeDBContentProvider;
import com.peanutwolf.googleappmonitor.Database.ShakeDatabase;
import com.peanutwolf.googleappmonitor.Models.ShakePointModel;


/**
 * Created by vigursky on 30.03.2016.
 */
public class DataSaverService extends Service {

    private static final String TAG = DataSaverService.class.getName();
    private ContentResolver mContentResolver;
    private final Binder mBinder = new DataSaverBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        mContentResolver = getContentResolver();
    }

    @Override
    public void onDestroy() {

    }

    public void saveShakePoint(ShakePointModel mModel){
        ContentValues values = new ContentValues();
        values.put(ShakeDatabase.COLUMN_ROUTEID, "1");
        values.put(ShakeDatabase.COLUMN_AXISACCELX, mModel.getAxisAccelerationX() + "");
        values.put(ShakeDatabase.COLUMN_AXISACCELY, mModel.getAxisAccelerationY() + "");
        values.put(ShakeDatabase.COLUMN_AXISACCELZ, mModel.getAxisAccelerationZ() + "");
        values.put(ShakeDatabase.COLUMN_AXISROTATX, mModel.getAxisRotationX() + "");
        values.put(ShakeDatabase.COLUMN_AXISROTATY, mModel.getAxisRotationY() + "");
        values.put(ShakeDatabase.COLUMN_AXISROTATZ, mModel.getAxisRotationZ() + "");
        values.put(ShakeDatabase.COLUMN_LONGITUDE, mModel.getCurrentLongitude() + "");
        values.put(ShakeDatabase.COLUMN_LATITUDE, mModel.getCurrentLatitude() + "");
        values.put(ShakeDatabase.COLUMN_SPEED, mModel.getCurrentSpeed() + "");
        values.put(ShakeDatabase.COLUMN_TIMESTAMP, mModel.getCurrentTimestamp() + "");
        mContentResolver.insert(ShakeDBContentProvider.CONTENT_URI, values);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class DataSaverBinder extends Binder {
        public DataSaverService getService() {
            return DataSaverService.this;
        }
    }
}
