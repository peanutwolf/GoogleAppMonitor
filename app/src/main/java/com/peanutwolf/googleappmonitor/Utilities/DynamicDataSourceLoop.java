package com.peanutwolf.googleappmonitor.Utilities;

import android.os.Handler;
import android.os.Message;

import java.util.List;

/**
 * Created by vigursky on 15.04.2016.
 */
public class DynamicDataSourceLoop implements Handler.Callback{
    public static final String TAG = DynamicDataSourceLoop.class.getName();
    private Handler mResponseHandler;
    private RangedLinkedList<Float > mSensorData;
    private DynamicDataSourceLoop.iCallback mPlotCallback;

    public interface iCallback{
        public void onUpdate(List<Float> data);
    }

    public DynamicDataSourceLoop(Handler responceHandler, DynamicDataSourceLoop.iCallback callback){
        mResponseHandler = responceHandler;
        mPlotCallback = callback;
    }

    public void setSensorDataPipe(RangedLinkedList<Float> list){
        mSensorData = list;
    }

    @Override
    public boolean handleMessage(Message msg) {
        long sleep, before;
        while (true) {
            before = System.currentTimeMillis();
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPlotCallback.onUpdate(mSensorData);
                }
            });
            try {
                sleep = 100 - (System.currentTimeMillis() - before);
                Thread.sleep(sleep > 0 ? sleep : 0);
            } catch (InterruptedException e) {
                break;
            }

        }
        return true;
    }
}
