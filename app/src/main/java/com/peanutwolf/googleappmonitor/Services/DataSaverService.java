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


/**
 * Created by vigursky on 30.03.2016.
 */
public class DataSaverService extends Service {

    public static final String TAG = DataSaverService.class.getName();
    private int mRouteID;
    private ContentResolver mContentResolver;
    private final Binder mBinder = new DataSaverBinder();
    private HandlerThread mSaverThread;
    private Handler mSaverHandler;


    @Override
    public void onCreate() {
        super.onCreate();
        mSaverThread = new HandlerThread("DataSaverThread");
        mSaverThread.start();
        mSaverHandler = new Handler(mSaverThread.getLooper());
        mContentResolver = getContentResolver();
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ContentValues values = new ContentValues();
            values.put(ShakeDatabase.COLUMN_ROUTEID, "1");
            values.put(ShakeDatabase.COLUMN_AXISACCELX, intent.getDoubleExtra(ShakeDatabase.COLUMN_AXISACCELX, 0.0) + "");
            values.put(ShakeDatabase.COLUMN_AXISACCELY, intent.getDoubleExtra(ShakeDatabase.COLUMN_AXISACCELY, 0.0) + "");
            values.put(ShakeDatabase.COLUMN_AXISACCELZ, intent.getDoubleExtra(ShakeDatabase.COLUMN_AXISACCELZ, 0.0) + "");
            values.put(ShakeDatabase.COLUMN_AXISROTATX, intent.getDoubleExtra(ShakeDatabase.COLUMN_AXISROTATX, 0.0) + "");
            values.put(ShakeDatabase.COLUMN_AXISROTATY, intent.getDoubleExtra(ShakeDatabase.COLUMN_AXISROTATY, 0.0) + "");
            values.put(ShakeDatabase.COLUMN_AXISROTATZ, intent.getDoubleExtra(ShakeDatabase.COLUMN_AXISROTATZ, 0.0) + "");
            values.put(ShakeDatabase.COLUMN_LATITUDE, intent.getDoubleExtra(ShakeDatabase.COLUMN_LATITUDE, 0) + "");
            values.put(ShakeDatabase.COLUMN_LONGITUDE, intent.getDoubleExtra(ShakeDatabase.COLUMN_LONGITUDE, 0) + "");
            values.put(ShakeDatabase.COLUMN_SPEED, intent.getFloatExtra(ShakeDatabase.COLUMN_SPEED, 0.0F) + "");
            values.put(ShakeDatabase.COLUMN_TIMESTAMP, intent.getLongExtra(ShakeDatabase.COLUMN_TIMESTAMP, 0L) + "");
            mContentResolver.insert(ShakeDBContentProvider.CONTENT_URI, values);
        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        mSaverThread.quit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContentResolver.delete(ShakeDBContentProvider.CONTENT_URI, null,null);
        registerReceiver(receiver, new IntentFilter(ShakeSensorService.BROADCAST_SHAKE_SENSOR), null, mSaverHandler);
        return START_STICKY;
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
