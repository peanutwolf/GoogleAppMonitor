package com.peanutwolf.googleappmonitor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.peanutwolf.googleappmonitor.Services.DataSaverService;
import com.peanutwolf.googleappmonitor.Services.Interfaces.LocationServiceDataSource;
import com.peanutwolf.googleappmonitor.Services.LocationGoogleService;
import com.peanutwolf.googleappmonitor.Services.ShakeSensorService;
import com.peanutwolf.googleappmonitor.Services.Interfaces.ShakeServiceDataSource;

import java.util.List;


public class MainActivity extends FragmentActivity  implements OnMapReadyCallback, ShakeServiceDataSource {
    public static final String TAG = MainActivity.class.getName();
    private GoogleMap mMap;
    private Intent mShakeServiceIntent;
    private Intent mDataSaverServiceIntent;
    private ShakeSensorService mShakeSensorService;
    private LocationServiceDataSource mLocationServiceDataSource;
    private Intent mLocationGoogleServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDataSaverServiceIntent = new Intent(getApplicationContext(), DataSaverService.class);
        mShakeServiceIntent = new Intent(getApplicationContext(), ShakeSensorService.class);
        mLocationGoogleServiceIntent = new Intent(getApplicationContext(), LocationGoogleService.class);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_main);
        mapFragment.getMapAsync(this);

        return;
    }


    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        bindService(mShakeServiceIntent, mShakeConnector, Context.BIND_AUTO_CREATE);
        bindService(mLocationGoogleServiceIntent, mLocationConnector, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        unbindService(mShakeConnector);
        unbindService(mLocationConnector);
        stopService(mDataSaverServiceIntent);
    }


    ServiceConnection mShakeConnector = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mShakeSensorService = ((ShakeSensorService.ShakeSensorBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    ServiceConnection mLocationConnector = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLocationServiceDataSource = ((LocationGoogleService.LocationServiceBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mLocationServiceDataSource.getLastKnownLatLng()));
    }

    @Override
    public List<Number> getAccelerationData() {
        if(mShakeSensorService != null)
            return mShakeSensorService.getAccelerationData();
        else
            return null;
    }
}
