package com.peanutwolf.googleappmonitor.Models;

import android.content.Context;
import android.util.AttributeSet;

import com.androidplot.Plot;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYSeriesFormatter;
import com.peanutwolf.googleappmonitor.Utilities.SimpleDynamicSeries;

/**
 * Created by vigursky on 15.04.2016.
 */
public class DynamicXYPlotModel extends XYPlot {

    SimpleDynamicSeries mSeries;

    public DynamicXYPlotModel(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    public DynamicXYPlotModel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public synchronized boolean addSeries(SimpleDynamicSeries series, XYSeriesFormatter formatter) {
        mSeries = series;
        return super.addSeries(series, formatter);
    }

    @Override
    public void redraw() {
        if(mSeries != null)
            mSeries.update();
        super.redraw();
    }
}
