package com.peanutwolf.googleappmonitor.Fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.peanutwolf.googleappmonitor.BuildConfig;
import com.peanutwolf.googleappmonitor.Database.AndroidDatabaseManager;
import com.peanutwolf.googleappmonitor.MainActivity;
import com.peanutwolf.googleappmonitor.Models.DynamicXYPlotModel;
import com.peanutwolf.googleappmonitor.R;
import com.peanutwolf.googleappmonitor.Utilities.DynamicDataSourceLoop;
import com.peanutwolf.googleappmonitor.Utilities.SimpleDynamicSeries;

/**
 * Created by vigursky on 11.05.2016.
 */
public class DynamicPlotFragment extends Fragment implements DynamicDataSourceLoop.iCallback, View.OnClickListener {

    private final static int DOMAIN_WIDTH = 100;
    public static final String TAG = DynamicPlotFragment.class.getName();
    private static boolean mDBSaverStarted = false;
    private XYPlot dynamicPlot;
    private Button startTrekButton;
    private SimpleDynamicSeries sine1Series;
    private Handler mUiUpdater;
    private HandlerThread plotUpdater;
    private Handler plotHandler;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dynamic_plot_xy, container, false);

        startTrekButton = (Button) view.findViewById(R.id.btn_record_trek);
        startTrekButton.setOnClickListener(this);
        dynamicPlot = (XYPlot) view.findViewById(R.id.dynamicXYPlot);
        this.customizeDynamicPlotView(dynamicPlot);
        LineAndPointFormatter formatter1 = new LineAndPointFormatter(
                Color.rgb(0, 0, 0), null, null, null);
        formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter1.getLinePaint().setStrokeWidth(5);

        if(BuildConfig.DEBUG) {
            dynamicPlot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity().getApplicationContext(), AndroidDatabaseManager.class));
                }
            });
        }

        sine1Series = new SimpleDynamicSeries();
        sine1Series.setDataSource(((MainActivity)getActivity()));
        ((DynamicXYPlotModel)dynamicPlot).addSeries(sine1Series, formatter1);
        dynamicPlot.setRangeBoundaries(-5, 5, BoundaryMode.FIXED);

        dynamicPlot.setDomainBoundaries(0, DOMAIN_WIDTH - 1, BoundaryMode.FIXED);

        plotUpdater = new HandlerThread("PlotUpdater");
        plotUpdater.start();
        mUiUpdater = new Handler();

        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        DynamicDataSourceLoop dataSourceLoop = new DynamicDataSourceLoop(mUiUpdater, this);
        plotHandler = new Handler(plotUpdater.getLooper(), dataSourceLoop);
        plotHandler.obtainMessage().sendToTarget();
    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        plotUpdater.interrupt();
        plotUpdater.quit();
    }



    @Override
    public void onUpdate() {
        dynamicPlot.redraw();
    }


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

    @Override
    public void onClick(View v) {
        if(mDBSaverStarted == false){
            mDBSaverStarted = true;
            startTrekButton.setText("Stop recording");
        }else{
            mDBSaverStarted = false;
            startTrekButton.setText("Start recording");
        }

    }
}
