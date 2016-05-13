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
    private DynamicDataSourceLoop.iCallback mPlotCallback;

    public interface iCallback{
        void onUpdate();
    }

    public DynamicDataSourceLoop(Handler responseHandler, DynamicDataSourceLoop.iCallback callback){
        mResponseHandler = responseHandler;
        mPlotCallback = callback;
    }

    @Override
    public boolean handleMessage(Message msg) {
        long sleep, before;
        while (true) {
            before = System.currentTimeMillis();
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    mPlotCallback.onUpdate();
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
