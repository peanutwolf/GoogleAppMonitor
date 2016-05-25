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
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.peanutwolf.googleappmonitor.Database.ShakeDBContentProvider;
import com.peanutwolf.googleappmonitor.Services.DataSaverService;
import com.peanutwolf.googleappmonitor.Services.Interfaces.LocationServiceDataSource;
import com.peanutwolf.googleappmonitor.Services.Interfaces.ShakeServiceDataSource;
import com.peanutwolf.googleappmonitor.Services.LocationGoogleService;
import com.peanutwolf.googleappmonitor.Services.ShakeSensorService;
import com.peanutwolf.googleappmonitor.Utilities.DynamicDataSourceLoop;

import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ShakeServiceDataSource, DynamicDataSourceLoop.iCallback {
    public static final String TAG = MainActivity.class.getName();
    private GoogleMap mMap;
    private Intent mShakeServiceIntent;
    private Intent mDataSaverServiceIntent;
    private ShakeSensorService mShakeSensorService;
    private LocationServiceDataSource mLocationServiceDataSource;
    private Intent mLocationGoogleServiceIntent;
    private HandlerThread mapsUpdater;
    private Handler mUiUpdater;
    private Handler mapsHandler;

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


        mapsUpdater = new HandlerThread("PlotUpdater");
        mapsUpdater.start();
        mUiUpdater = new Handler();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(myToolbar);

        return;
    }

    @Override
    protected void onStart() {
        super.onStart();
        DynamicDataSourceLoop dataSourceLoop = new DynamicDataSourceLoop(mUiUpdater, this);
        mapsHandler = new Handler(mapsUpdater.getLooper(), dataSourceLoop);
        mapsHandler.obtainMessage().sendToTarget();
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
        stopService(mDataSaverServiceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapsUpdater.interrupt();
        mapsUpdater.quit();
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
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    @Override
    public List<Number> getAccelerationData() {
        if(mShakeSensorService != null)
            return mShakeSensorService.getAccelerationData();
        else
            return null;
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
            case R.id.action_write_trek:
                getContentResolver().delete(ShakeDBContentProvider.CONTENT_URI, null,null);
                mShakeSensorService.setAllowDataSaving(true);
                return true;
            case R.id.action_export_data:
                startActivity(new Intent(this.getApplicationContext(), ExportDataTestActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onUpdate() {
        if(mLocationServiceDataSource != null) {
//            CameraPosition cameraPosition = new CameraPosition.Builder().target(mLocationServiceDataSource.getLastKnownLatLng()).zoom(12).build();
//            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

    }
}
