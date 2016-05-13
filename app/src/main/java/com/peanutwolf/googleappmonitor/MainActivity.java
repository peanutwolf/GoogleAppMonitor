package com.peanutwolf.googleappmonitor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.peanutwolf.googleappmonitor.Fragments.MainFragment;
import com.peanutwolf.googleappmonitor.Services.DataSaverService;
import com.peanutwolf.googleappmonitor.Services.ShakeSensorService;
import com.peanutwolf.googleappmonitor.Services.ShakeServiceDataSource;
import com.peanutwolf.googleappmonitor.Utilities.DynamicDataSourceLoop;
import com.peanutwolf.googleappmonitor.Utilities.RangedLinkedList;

import java.util.List;


public class MainActivity extends FragmentActivity  implements OnMapReadyCallback, ShakeServiceDataSource {
    public static final String TAG = MainActivity.class.getName();
    private GoogleMap mMap;
    private Intent mShakeServiceIntent;
    private Intent mDataSaverServiceIntent;
    private ShakeSensorService mShakeSensorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mDataSaverServiceIntent = new Intent(getApplicationContext(), DataSaverService.class);
        mShakeServiceIntent = new Intent(getApplicationContext(), ShakeSensorService.class);

        return;
    }


    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        bindService(mShakeServiceIntent, mShakeConnector, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        unbindService(mShakeConnector);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public List<Number> getAccelerationData() {
        if(mShakeSensorService != null)
            return mShakeSensorService.getAccelerationData();
        else
            return null;
    }
}
