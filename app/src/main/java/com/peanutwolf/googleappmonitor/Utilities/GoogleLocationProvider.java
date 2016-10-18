package com.peanutwolf.googleappmonitor.Utilities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.peanutwolf.googleappmonitor.Services.Interfaces.LocationServiceDataSource;
import com.peanutwolf.googleappmonitor.Services.LocationGoogleService;

import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;

/**
 * Created by vigursky on 08.08.2016.
 */
public class GoogleLocationProvider implements IMyLocationProvider {
    public static final String TAG = GoogleLocationProvider.class.getSimpleName();
    private final Context mContext;
    private LocationServiceDataSource mLocationServiceDataSource;
    private MyLocationListenerBridge mLocationListenerBridge;
    private boolean mRegistered = false;

    class MyLocationListenerBridge implements LocationListener{
        IMyLocationConsumer mMyLocationConsumer;

        MyLocationListenerBridge(IMyLocationConsumer myLocationConsumer){
            this.mMyLocationConsumer = myLocationConsumer;
        }

        @Override
        public void onLocationChanged(Location location) {
            this.mMyLocationConsumer.onLocationChanged(location, GoogleLocationProvider.this);
        }
    }

    public GoogleLocationProvider(Context context){
        this.mContext = context;
    }
    
    @Override
    public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
        Log.d(TAG, "[startLocationProvider] Google location provider is starting");
        mLocationListenerBridge = new MyLocationListenerBridge(myLocationConsumer);
        return mContext.bindService(new Intent(mContext, LocationGoogleService.class), mLocationConnector, Context.BIND_AUTO_CREATE);
    }


    ServiceConnection mLocationConnector = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLocationServiceDataSource = ((LocationGoogleService.LocationServiceBinder)service).getService();
            mLocationServiceDataSource.setLocationListener(mLocationListenerBridge);
            mRegistered = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocationServiceDataSource.setLocationListener(null);
            mRegistered = false;
        }
    };

    @Override
    public void stopLocationProvider() {
        Log.d(TAG, "[stopLocationProvider] Google location provider is stopping");
        if (mLocationConnector != null && mRegistered){
            mContext.unbindService(mLocationConnector);
            mRegistered = false;
        }

    }

    @Override
    public Location getLastKnownLocation() {
        if(mLocationServiceDataSource != null)
            return mLocationServiceDataSource.getLastKnownLocation();
        return null;
    }
}
