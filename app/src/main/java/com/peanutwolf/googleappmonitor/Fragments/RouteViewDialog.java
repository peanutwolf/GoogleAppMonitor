package com.peanutwolf.googleappmonitor.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.peanutwolf.googleappmonitor.Models.ShakePointModel;
import com.peanutwolf.googleappmonitor.R;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vigursky on 23.08.2016.
 */
public class RouteViewDialog extends DialogFragment implements OpenStreetMapConstants{

    public static final String TAG = RouteViewDialog.class.getSimpleName();
    private Button dismissWindow;
    private MapView mMapView;
    private List<ShakePointModel> mRoutePointsList;
    private SharedPreferences mPrefs;
    private Polyline mTrackPolyline;

    public RouteViewDialog(){

    }

    public static RouteViewDialog newInstance(){
        RouteViewDialog dialog = new RouteViewDialog();
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_route_view, container);

        mMapView = (MapView) view.findViewById(R.id.mapview_dialog);

        dismissWindow = (Button)view.findViewById(R.id.btn_cancel_route_dialog);
        dismissWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPrefs = this.getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public void onStart(){
        super.onStart();

        mTrackPolyline = this.prepareTrack(this.mRoutePointsList);
        mTrackPolyline.setVisible(false);
        mTrackPolyline.setColor(Color.GREEN);
        mMapView.getOverlays().add(mTrackPolyline);

        mMapView.getController().setZoom(mPrefs.getInt(PREFS_ZOOM_LEVEL, 1));
        mMapView.scrollTo(mPrefs.getInt(PREFS_SCROLL_X, 0), mPrefs.getInt(PREFS_SCROLL_Y, 0));

        mMapView.addOnFirstLayoutListener(new MapView.OnFirstLayoutListener() {
            @Override
            public void onFirstLayout(View v, int left, int top, int right, int bottom) {
                BoundingBoxE6 trackBoundingBox = RouteViewDialog.this.calcTrackBoundingBox(RouteViewDialog.this.mRoutePointsList);
                mMapView.zoomToBoundingBox(trackBoundingBox, false);
                mTrackPolyline.setVisible(true);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    public void setRoutePoints(List<ShakePointModel> routePointsList){
        this.mRoutePointsList = routePointsList;
    }

    @NonNull
    private Polyline prepareTrack(@NonNull List<ShakePointModel> routePointsList){
        Polyline track = new Polyline(this.getActivity());
        List<GeoPoint> geoPoints = new ArrayList<>();

        for(ShakePointModel shakePointModel : routePointsList){
            GeoPoint geoPoint = new GeoPoint(shakePointModel.getCurrentLatitude(), shakePointModel.getCurrentLongitude());
            geoPoints.add(geoPoint);
        }

        track.setPoints(geoPoints);
        return track;
    }

    private BoundingBoxE6 calcTrackBoundingBox(@NonNull List<ShakePointModel> routePointsList){
        double north, east, south, west;
        BoundingBoxE6 trackBox;

        if(routePointsList.size() == 0){
            return null;
        }
        north = south = routePointsList.get(0).getCurrentLatitude();
        east = west = routePointsList.get(0).getCurrentLongitude();

        for(ShakePointModel shakePointModel : routePointsList){
            if(north < shakePointModel.getCurrentLatitude()){
                north = shakePointModel.getCurrentLatitude();
            }
            if(south > shakePointModel.getCurrentLatitude()){
                south = shakePointModel.getCurrentLatitude();
            }
            if(west > shakePointModel.getCurrentLongitude()){
                west = shakePointModel.getCurrentLongitude();
            }
            if(east < shakePointModel.getCurrentLongitude()){
                east = shakePointModel.getCurrentLongitude();
            }
        }

        trackBox = new BoundingBoxE6(north, east, south, west);

        return trackBox;
    }
}
