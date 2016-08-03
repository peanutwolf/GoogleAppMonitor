package com.peanutwolf.googleappmonitor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.peanutwolf.googleappmonitor.Database.ShakeDBContentProvider;
import com.peanutwolf.googleappmonitor.Models.ShakePointModel;
import com.peanutwolf.googleappmonitor.Services.DataSaverService;
import com.peanutwolf.googleappmonitor.Services.Interfaces.LocationServiceDataSource;
import com.peanutwolf.googleappmonitor.Services.Interfaces.ShakeServiceDataSource;
import com.peanutwolf.googleappmonitor.Services.LocationGoogleService;
import com.peanutwolf.googleappmonitor.Services.ShakeSensorService;
import com.peanutwolf.googleappmonitor.Utilities.DynamicDataSourceLoop;
import com.peanutwolf.googleappmonitor.Utilities.MapViewEngine;

import java.util.LinkedList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ShakeServiceDataSource<ShakePointModel>, View.OnClickListener {
    public static final String TAG = MainActivity.class.getName();
    private MapViewEngine mMapViewEngine;
    private Intent mShakeServiceIntent;
    private ShakeSensorService mShakeSensorService;
    private LocationServiceDataSource mLocationServiceDataSource;
    private Intent mLocationGoogleServiceIntent;
    private Button mRideItButton;
    private TextView mCurrentAxisValuesDbg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mRideItButton = (Button) findViewById(R.id.btn_ride_main);
        if(BuildConfig.DEBUG){
            mCurrentAxisValuesDbg = (TextView) findViewById(R.id.txt_axis_values_dbg);
        }

        assert mRideItButton != null;
        mRideItButton.setOnClickListener(this);

        mShakeServiceIntent = new Intent(getApplicationContext(), ShakeSensorService.class);
        mLocationGoogleServiceIntent = new Intent(getApplicationContext(), LocationGoogleService.class);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_main);
        mapFragment.getMapAsync(this);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(myToolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        bindService(mLocationGoogleServiceIntent, mLocationConnector, Context.BIND_AUTO_CREATE);
        bindService(mShakeServiceIntent, mShakeConnector, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        unbindService(mShakeConnector);
        unbindService(mLocationConnector);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapViewEngine.stopUpdate();
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
            if(mMapViewEngine != null)
                mMapViewEngine.setLocationServiceDataSource(mLocationServiceDataSource);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMapViewEngine = new MapViewEngine(googleMap, mLocationServiceDataSource);
        mMapViewEngine.beginUpdate();
    }

    @Override
    public LinkedList<ShakePointModel> getAccelerationData() {
        if(mShakeSensorService != null){
            LinkedList<ShakePointModel> accelerationData = mShakeSensorService.getAccelerationData();
            if(!accelerationData.isEmpty() && BuildConfig.DEBUG){
                ShakePointModel lastPoint = accelerationData.getLast();
                mCurrentAxisValuesDbg.setText("x:" + lastPoint.getAxisAccelerationX() + "\ny:" + lastPoint.getAxisAccelerationY() + "\nz:" + lastPoint.getAxisAccelerationZ());
            }
            return accelerationData;
        }
        else {
            return null;
        }
    }

    @Override
    public int getAverageAccelerationData() {
        if(mShakeSensorService != null)
            return mShakeSensorService.getAverageAccelerationData();
        else
            return 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_export_data:
                startActivity(new Intent(this.getApplicationContext(), ExportDataTestActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        if(mShakeSensorService.isDataSavingAllowed()){
            mShakeSensorService.setAllowDataSaving(false);
            mRideItButton.setText(getResources().getString(R.string.str_ride_it));
        }else{
            getContentResolver().delete(ShakeDBContentProvider.CONTENT_URI, null,null);
            mShakeSensorService.setAllowDataSaving(true);
            mRideItButton.setText(getResources().getString(R.string.str_stop_it));
        }
    }
}
