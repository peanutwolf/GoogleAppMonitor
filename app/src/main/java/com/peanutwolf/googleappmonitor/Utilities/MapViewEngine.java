package com.peanutwolf.googleappmonitor.Utilities;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.peanutwolf.googleappmonitor.Services.Interfaces.LocationServiceDataSource;

/**
 * Created by vigursky on 03.08.2016.
 */
public class MapViewEngine implements DynamicDataSourceLoop.iCallback, GoogleMap.OnMapLoadedCallback, GoogleMap.OnCameraChangeListener {
    private static final String TAG = MapViewEngine.class.getSimpleName();
    private GoogleMap mMap;
    private LocationServiceDataSource mLocationSource;
    private HandlerThread mapsUpdater;

    public MapViewEngine(GoogleMap googleMap, LocationServiceDataSource locationSource){
        mMap = googleMap;
        mLocationSource = locationSource;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMapLoadedCallback(this);
        mMap.setOnCameraChangeListener(this);
    }

    public void setLocationServiceDataSource(LocationServiceDataSource locationSource){
        mLocationSource = locationSource;
    }

    public void beginUpdate(){
        mapsUpdater = new HandlerThread("MapsUpdater");
        mapsUpdater.start();
        DynamicDataSourceLoop dataSourceLoop = new DynamicDataSourceLoop(new Handler(), this);
        Handler mapsHandler = new Handler(mapsUpdater.getLooper(), dataSourceLoop);
        mapsHandler.obtainMessage().sendToTarget();
    }

    public void stopUpdate(){
        mapsUpdater.interrupt();
        mapsUpdater.quit();
    }


    @Override
    public void onUpdate() {

    }

    @Override
    public void onMapLoaded() {
        CameraPosition position = mMap.getCameraPosition();
        Log.d(TAG, "onMapLoaded:" + position.target.toString());
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        Log.d(TAG, "onCameraChange" + cameraPosition.target.toString());
    }
}
