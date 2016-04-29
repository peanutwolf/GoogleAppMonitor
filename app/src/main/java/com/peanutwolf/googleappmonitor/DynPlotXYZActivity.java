package com.peanutwolf.googleappmonitor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.androidplot.xy.XYPlot;
import com.peanutwolf.googleappmonitor.Services.ShakeSensorService;
import com.peanutwolf.googleappmonitor.Utilities.RangedLinkedList;

/**
 * Created by vigursky on 14.04.2016.
 */
public class DynPlotXYZActivity extends AppCompatActivity {

    public static final String TAG = DynPlotXYZActivity.class.getName();
    private Intent mShakeServiceIntent;
    private ShakeSensorService mShakeSensorService;
    private XYPlot dynamicPlotX;
    private XYPlot dynamicPlotY;
    private XYPlot dynamicPlotZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dynamicPlotX = (XYPlot) findViewById(R.id.dynamicXPlot);
        dynamicPlotY = (XYPlot) findViewById(R.id.dynamicYPlot);
        dynamicPlotZ = (XYPlot) findViewById(R.id.dynamicZPlot);

        mShakeServiceIntent = new Intent(getApplicationContext(), ShakeSensorService.class);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }


    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        bindService(mShakeServiceIntent, mShakeConnector, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        unbindService(mShakeConnector);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
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
}
