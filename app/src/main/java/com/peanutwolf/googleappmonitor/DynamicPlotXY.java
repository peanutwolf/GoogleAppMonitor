package com.peanutwolf.googleappmonitor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.peanutwolf.googleappmonitor.Database.AndroidDatabaseManager;
import com.peanutwolf.googleappmonitor.Models.DynamicXYPlotModel;
import com.peanutwolf.googleappmonitor.Services.DataSaverService;
import com.peanutwolf.googleappmonitor.Services.ShakeSensorService;
import com.peanutwolf.googleappmonitor.Utilities.DynamicDataSourceLoop;
import com.peanutwolf.googleappmonitor.Utilities.SimpleDynamicSeries;

@Deprecated
public class DynamicPlotXY extends AppCompatActivity implements DynamicDataSourceLoop.iCallback{

    private final static int DOMAIN_WIDTH = 100;
    public static final String TAG = DynamicPlotXY.class.getName();
    private static boolean mDBSaverStarted = false;
    private XYPlot dynamicPlot;
    private SimpleDynamicSeries sine1Series;
    private Handler mUiUpdater;
    private Handler plotHandler;
    private HandlerThread plotUpdater;
    private Intent mShakeServiceIntent;
    private Intent mDataSaverServiceIntent;
    private ShakeSensorService mShakeSensorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_plot_xy);

        dynamicPlot = (XYPlot) findViewById(R.id.dynamicXYPlot);
        this.customizeDynamicPlotView(dynamicPlot);
        LineAndPointFormatter formatter1 = new LineAndPointFormatter(
                Color.rgb(0, 0, 0), null, null, null);
        formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter1.getLinePaint().setStrokeWidth(5);

        dynamicPlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AndroidDatabaseManager.class));
            }
        });

        sine1Series = new SimpleDynamicSeries();
        ((DynamicXYPlotModel)dynamicPlot).addSeries(sine1Series, formatter1);
        dynamicPlot.setRangeBoundaries(-5, 5, BoundaryMode.FIXED);
        dynamicPlot.setDomainBoundaries(0, DOMAIN_WIDTH - 1, BoundaryMode.FIXED);

        plotUpdater = new HandlerThread("PlotUpdater");
        plotUpdater.start();
        mUiUpdater = new Handler();

        mDataSaverServiceIntent = new Intent(getApplicationContext(), DataSaverService.class);
        mShakeServiceIntent = new Intent(getApplicationContext(), ShakeSensorService.class);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        DynamicDataSourceLoop dataSourceLoop = new DynamicDataSourceLoop(mUiUpdater, this);
        plotHandler = new Handler(plotUpdater.getLooper(), dataSourceLoop);
        plotHandler.obtainMessage().sendToTarget();
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
        stopService(mDataSaverServiceIntent);
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
        plotUpdater.interrupt();
        plotUpdater.quit();
    }

    @Override
    public void onUpdate() {
        dynamicPlot.redraw();
    }

    ServiceConnection  mShakeConnector = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mShakeSensorService = ((ShakeSensorService.ShakeSensorBinder)service).getService();
            sine1Series.setDataSource(mShakeSensorService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void customizeDynamicPlotView(XYPlot dynamicPlot){
        dynamicPlot.setPlotMargins(0, 0, 0, 0);
        dynamicPlot.setPlotPadding(0, 0, 0, 0);
        dynamicPlot.setDomainLabelWidget(null);
        dynamicPlot.setRangeLabelWidget(null);

        dynamicPlot.getGraphWidget().getBackgroundPaint().setColor(Color.DKGRAY);
        dynamicPlot.getBackgroundPaint().setColor(Color.DKGRAY);
        dynamicPlot.getGraphWidget().setGridBackgroundPaint(null);
        dynamicPlot.setBorderPaint(null);

        dynamicPlot.getGraphWidget().setDomainTickLabelWidth(0.0f);
        dynamicPlot.getGraphWidget().setRangeTickLabelWidth(20.0f);

        dynamicPlot.getGraphWidget().setDomainTickLabelPaint(null);
        dynamicPlot.getGraphWidget().setDomainOriginTickLabelPaint(null);

        dynamicPlot.getGraphWidget().getRangeTickLabelPaint().setColor(Color.GRAY);
        dynamicPlot.getGraphWidget().setRangeOriginTickLabelPaint(null);

        dynamicPlot.getGraphWidget().setDomainOriginLinePaint(null);
//        dynamicPlot.getGraphWidget().setRangeOriginLinePaint(null);

        dynamicPlot.getLayoutManager().remove(dynamicPlot.getTitleWidget());

        dynamicPlot.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().getRangeGridLinePaint().setColor(Color.LTGRAY);
        dynamicPlot.getGraphWidget().getRangeSubGridLinePaint().setColor(Color.LTGRAY);

        dynamicPlot.getLayoutManager().remove(dynamicPlot.getLegendWidget());
        dynamicPlot.getLayoutManager().remove(dynamicPlot.getDomainLabelWidget());
        dynamicPlot.getLayoutManager().remove(dynamicPlot.getRangeLabelWidget());
    }

}

