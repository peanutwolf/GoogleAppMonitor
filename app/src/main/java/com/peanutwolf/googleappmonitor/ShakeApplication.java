package com.peanutwolf.googleappmonitor;

import android.app.Application;
import android.content.Intent;
import android.support.multidex.MultiDexApplication;

import com.peanutwolf.googleappmonitor.Services.ShakeSensorService;
import com.peanutwolf.googleappmonitor.Utilities.ServiceTools;

/**
 * Created by vigursky on 02.09.2016.
 */
public class ShakeApplication extends MultiDexApplication {

    private Intent mShakeServiceIntent;


    @Override
    public void onCreate() {
        super.onCreate();
        if(!ServiceTools.isServiceRunning(this, ShakeSensorService.class)){
            mShakeServiceIntent = new Intent(this, ShakeSensorService.class);
            startService(mShakeServiceIntent);
        }
    }


}
